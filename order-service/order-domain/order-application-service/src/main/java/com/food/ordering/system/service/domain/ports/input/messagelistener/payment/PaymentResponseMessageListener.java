package com.food.ordering.system.service.domain.ports.input.messagelistener.payment;

import com.food.ordering.system.service.domain.dto.message.PaymentResponse;

public interface PaymentResponseMessageListener {

    void paymentCompleted(PaymentResponse paymentResponse);

    void paymentCancelled(PaymentResponse paymentResponse);
}
