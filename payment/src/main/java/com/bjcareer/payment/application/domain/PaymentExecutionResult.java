package com.bjcareer.payment.application.domain;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import com.bjcareer.payment.application.domain.entity.order.PaymentStatus;

import lombok.Data;

@Data
public class PaymentExecutionResult {
	String paymentKey;
	String orderId;
	String orderName;
	int totalAmount;
	PspConfirmationStatus status;
	LocalDateTime requestAt;
	LocalDateTime approvedAt;
	FailureExecution failureExecution;
	boolean isSuccess;
	boolean isFailure;
	boolean isUnknown;


	public PaymentExecutionResult(String paymentKey, String orderId, String orderName, int
		totalAmount, String status,
		String requestAt, String approvedAt, boolean isSuccess, boolean isFailure ,boolean isUnknown) {
		this.paymentKey = paymentKey;
		this.orderId = orderId;
		this.orderName = orderName;
		this.totalAmount = totalAmount;
		this.status = PspConfirmationStatus.fromString(status);
		this.requestAt = ZonedDateTime.parse(requestAt).toLocalDateTime();
		this.approvedAt = ZonedDateTime.parse(approvedAt).toLocalDateTime();
		failureExecution = null;
		this.isSuccess = isSuccess;
		this.isFailure = isFailure;
		this.isUnknown = isUnknown;
	}

	public PaymentExecutionResult(String code, String message) {
		failureExecution = new FailureExecution(code, message);
	}

	@Data
	public static class FailureExecution{
		private final String code;
		private final String message;
	}

	public PaymentStatus getStatus(){
		if (isSuccess){
			return PaymentStatus.SUCCESS;
		}

		if (isFailure){
			return PaymentStatus.FAILURE;
		}

		if (isUnknown){
			return PaymentStatus.UNKNOWN;
		}

		throw new RuntimeException("원하는 형식으로 값이 들어오지 않음 ");
	}
}
