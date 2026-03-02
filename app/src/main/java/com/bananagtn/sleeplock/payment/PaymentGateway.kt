package com.bananagtn.sleeplock.payment

interface PaymentGateway {
    suspend fun createOrder(amountFen: Int, channel: PaymentChannel): PaymentOrder
    suspend fun pay(order: PaymentOrder): PaymentOrder
}

class FakePaymentGateway : PaymentGateway {
    override suspend fun createOrder(amountFen: Int, channel: PaymentChannel): PaymentOrder {
        return PaymentOrder(
            orderId = "ORD-${System.currentTimeMillis()}",
            amountFen = amountFen,
            channel = channel
        )
    }

    override suspend fun pay(order: PaymentOrder): PaymentOrder {
        return order.copy(status = PaymentStatus.PAID)
    }
}
