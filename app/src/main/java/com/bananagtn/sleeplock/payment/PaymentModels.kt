package com.bananagtn.sleeplock.payment

enum class PaymentChannel {
    ALIPAY,
    WECHAT
}

enum class PaymentStatus {
    CREATED,
    PAID,
    FAILED
}

data class PaymentOrder(
    val orderId: String,
    val amountFen: Int,
    val channel: PaymentChannel,
    val status: PaymentStatus = PaymentStatus.CREATED
)
