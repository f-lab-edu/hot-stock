package com.bjcareer.payment.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.bjcareer.payment.application.domain.CheckoutResult;
import com.bjcareer.payment.application.domain.Product;
import com.bjcareer.payment.application.domain.entity.coupon.PaymentCoupon;
import com.bjcareer.payment.application.port.in.CheckoutCommand;
import com.bjcareer.payment.application.port.in.CheckoutUsecase;
import com.bjcareer.payment.application.port.out.LoadCouponPort;
import com.bjcareer.payment.application.port.out.SavePaymentPort;
import com.bjcareer.payment.application.service.exceptions.CheckoutFailedException;
import com.bjcareer.payment.application.service.exceptions.DuplicatedCheckout;
import com.bjcareer.payment.application.domain.entity.event.PaymentEvent;
import com.bjcareer.payment.application.domain.entity.order.PaymentOrder;
import com.bjcareer.payment.application.port.out.LoadProductPort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CheckoutService implements CheckoutUsecase {
	public static final String MOCK_ORDER_NAME = "1년치 구독 결제";
	private final LoadProductPort loadProductPort;
	private final LoadCouponPort loadCouponPort;
	private final SavePaymentPort savePaymentPort;

	@Override
	public Mono<CheckoutResult> checkout(CheckoutCommand checkoutCommand) {
		List<Product> products = loadProducts(checkoutCommand);
		List<PaymentCoupon> coupons = loadCoupons(checkoutCommand);

		PaymentEvent paymentEvent = createPaymentEvent(checkoutCommand, products, coupons);
		return savePaymentPort.save(paymentEvent).flatMap(this::toCheckoutResult).onErrorMap(e ->
		{
			if(isUniqueConstraintViolation(e)){
				return new DuplicatedCheckout("진행중인 결제건이 있습니다.");
			}
			System.out.println("e = " + e);
			return new CheckoutFailedException("Checkout failed due to a database error.", e);
		});
	}

	private List<Product> loadProducts(CheckoutCommand checkoutCommand) {
		return loadProductPort.getProducts(checkoutCommand.getProductIds());
	}

	private List<PaymentCoupon> loadCoupons(CheckoutCommand checkoutCommand) {
		return loadCouponPort.getCoupons(checkoutCommand.getCouponIds());
	}

	private PaymentEvent createPaymentEvent(CheckoutCommand checkoutCommand, List<Product> products, List<PaymentCoupon> coupons) {
		List<PaymentOrder> paymentOrders = products.stream()
			.map(product -> toPaymentOrder(product))
			.collect(Collectors.toList());

		return new PaymentEvent(checkoutCommand.getBuyerId(), checkoutCommand.getIdempotenceKey(), paymentOrders, coupons);
	}

	private PaymentOrder toPaymentOrder(Product product) {
		return new PaymentOrder(product.getId(), product.getPrice());
	}

	private Mono<CheckoutResult> toCheckoutResult(PaymentEvent paymentEvent) {
		System.out.println("paymentEvent.getTotalAmount() = " + paymentEvent.getTotalAmount());
		return Mono.just(new CheckoutResult(paymentEvent.getTotalAmount(), paymentEvent.getOrderId(), paymentEvent.getBuyerId(),
			MOCK_ORDER_NAME));
	}

	private boolean isUniqueConstraintViolation(Throwable e) {
		return e instanceof DataIntegrityViolationException;
	}
}
