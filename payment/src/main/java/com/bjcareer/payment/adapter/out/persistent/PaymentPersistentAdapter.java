package com.bjcareer.payment.adapter.out.persistent;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.bjcareer.payment.adapter.out.persistent.exceptions.DataNotFoundException;
import com.bjcareer.payment.adapter.out.persistent.repository.PaymentCouponRepository;
import com.bjcareer.payment.adapter.out.persistent.repository.PaymentOrderRepository;
import com.bjcareer.payment.application.domain.entity.coupon.PaymentCoupon;
import com.bjcareer.payment.application.domain.entity.event.PaymentEvent;
import com.bjcareer.payment.application.domain.entity.order.PaymentOrder;
import com.bjcareer.payment.application.port.out.LoadPaymentPort;
import com.bjcareer.payment.application.port.out.PaymentValidationPort;
import com.bjcareer.payment.application.port.out.SavePaymentPort;
import com.bjcareer.payment.adapter.out.persistent.repository.PaymentEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository @Slf4j
@RequiredArgsConstructor
public class PaymentPersistentAdapter implements SavePaymentPort, PaymentValidationPort, LoadPaymentPort {

	private final PaymentOrderRepository paymentOrderRepository;
	private final PaymentCouponRepository paymentCouponRepository;
	private final TransactionalOperator transactionalOperator;
	private final PaymentEventRepository paymentEventRepository;

	@Override
	public Mono<PaymentEvent> save(PaymentEvent paymentEvent) {
		return transactionalOperator.transactional(
			paymentEventRepository.save(paymentEvent)
				.flatMap(savedPayment ->
					saveOrders(savedPayment)
						.then(saveCoupons(savedPayment))
						.thenReturn(savedPayment)
				)
		);
	}

	@Override
	public Mono<PaymentEvent> findById(Long id) {
		return paymentEventRepository.findById(id)
			.flatMap(it ->
				Mono.zip(
					paymentOrderRepository.findByPaymentEventId(it.getId()).collectList(),
					paymentCouponRepository.findByPaymentEventId(it.getId()).collectList()
				).flatMap(tuple -> {
					List<PaymentOrder> t1 = tuple.getT1();
					List<PaymentCoupon> t2 = tuple.getT2();
					PaymentEvent paymentEvent = new PaymentEvent(it, t1, t2);
					return Mono.just(paymentEvent);
				})
			);
	}

	@Override
	public Mono<Void> delete(PaymentEvent paymentEvent) {
		return paymentEventRepository.delete(paymentEvent);
	}

	@Override
	public Mono<Boolean> isValid(String checkoutId, Long totalAmount) {
		Mono<PaymentEvent> paymentEventMono = paymentEventRepository.findByCheckoutId(checkoutId)
			.doOnNext(event -> log.debug("Fetched PaymentEvent: " + event));

		return paymentEventMono
			.flatMap(it -> findById(it.getId())
				.doOnNext(event -> {
					log.debug("Fetched PaymentEvent by ID: {}",  event);
					log.debug("Total amount in event: {}", event.getTotalAmount());
					log.debug("Expected total amount: {}", totalAmount);
				})
				.flatMap(event -> {
					boolean isValid = event.getTotalAmount().equals(totalAmount);
					log.debug("Is valid: " + isValid);
					return Mono.just(isValid);
				})
			);
	}

	@Override
	public Mono<PaymentEvent> getPaymentByCheckoutId(String checkoutId) {
		return paymentEventRepository.findByCheckoutId(checkoutId)
			.doOnSuccess(event -> {
				if (event == null) {
					String err = String.format("No PaymentEvent found for checkoutId: %s", checkoutId);
					throw new DataNotFoundException(err);
				}
			})
			.flatMap(it -> findById(it.getId()));
	}


	private Mono<Void> saveOrders(PaymentEvent paymentEvent) {
		return Flux.fromIterable(paymentEvent.getOrders())
			.flatMap(order -> {
				order.assignRelatedPaymentEventId(paymentEvent.getId());
				return paymentOrderRepository.save(order);
			})
			.then();
	}

	private Mono<Void> saveCoupons(PaymentEvent paymentEvent) {
		return Flux.fromIterable(paymentEvent.getCoupons())
			.flatMap(coupon -> {
				coupon.assignRelatedPaymentEventId(paymentEvent.getId());
				return paymentCouponRepository.save(coupon);
			})
			.then();
	}
}
