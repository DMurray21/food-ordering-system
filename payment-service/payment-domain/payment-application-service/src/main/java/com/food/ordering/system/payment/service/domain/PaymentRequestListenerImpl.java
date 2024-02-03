package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentRequestListenerImpl implements PaymentRequestMessageListener {

    private final PaymentRequestHelper paymentRequestHelper;


    public PaymentRequestListenerImpl(PaymentRequestHelper paymentRequestHelper) {
        this.paymentRequestHelper = paymentRequestHelper;
    }

    @Override
    public void completePayment(PaymentRequest paymentRequest) {
        paymentRequestHelper.persistPayment(paymentRequest);
    }


    @Override
    public void cancelPayment(PaymentRequest paymentRequest) {
        paymentRequestHelper.persistCancelledPayment(paymentRequest);
    }
}
