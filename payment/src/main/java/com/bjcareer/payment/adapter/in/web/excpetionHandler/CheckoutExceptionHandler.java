package com.bjcareer.payment.adapter.in.web.excpetionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.bjcareer.payment.adapter.in.response.ApiResponse;
import com.bjcareer.payment.adapter.out.persistent.exceptions.DataNotFoundException;
import com.bjcareer.payment.application.service.exceptions.CheckoutFailedException;
import com.bjcareer.payment.application.service.exceptions.DuplicatedCheckout;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class CheckoutExceptionHandler {

	@ExceptionHandler(CheckoutFailedException.class)
	public ResponseEntity<ApiResponse> handleCheckoutException(CheckoutFailedException e) {
		log.error("Checkout failed: {}", e.getMessage());
		return buildResponseEntity(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"서버가 혼잡합니다. 잠시 후 이용해주세요"
		);
	}

	@ExceptionHandler(DuplicatedCheckout.class)
	public ResponseEntity<ApiResponse> handleDuplicatedCheckoutException(DuplicatedCheckout e) {
		log.error("Duplicated checkout attempt: {}", e.getMessage());
		return buildResponseEntity(
			HttpStatus.TOO_MANY_REQUESTS,
			"진행중인 결제건이 있습니다."
		);
	}

	@ExceptionHandler(DataNotFoundException.class)
	public ResponseEntity<ApiResponse> handleDataNotFoundExceptionException(DataNotFoundException e) {
		log.error("DataNotFoundException: {}", e.getMessage());
		return buildResponseEntity(
			HttpStatus.BAD_REQUEST,
			"요청한 데이터를 찾을 수 없습니다"
		);
	}

	private ResponseEntity<ApiResponse> buildResponseEntity(HttpStatus status, String message) {
		ApiResponse response = new ApiResponse(status, message, null);
		return ResponseEntity.status(status).body(response);
	}
}
