package com.bananagtn.sleeplock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bananagtn.sleeplock.billing.BillingPolicy
import com.bananagtn.sleeplock.billing.UnlockLedger
import com.bananagtn.sleeplock.data.AppState
import com.bananagtn.sleeplock.payment.FakePaymentGateway
import com.bananagtn.sleeplock.payment.PaymentChannel
import com.bananagtn.sleeplock.payment.PaymentStatus
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun SleepLockApp() {
    val billingPolicy = remember { BillingPolicy() }
    val paymentGateway = remember { FakePaymentGateway() }
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf(AppState()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("BananaGtn 睡眠锁机", style = MaterialTheme.typography.headlineSmall)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("睡觉时间 (HH:mm)")
                TextField(
                    value = state.sleepTime,
                    onValueChange = { state = state.copy(sleepTime = it) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(onClick = {
                    state = state.copy(audioSource = "已录制语音（<=60秒）")
                }) {
                    Text("录制语音（示例）")
                }
                OutlinedButton(onClick = {
                    state = state.copy(audioSource = "已上传语音（<=60秒）")
                }) {
                    Text("上传语音（示例）")
                }
                Text("当前语音：${state.audioSource}")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("锁机控制")
                Button(onClick = {
                    state = state.copy(lockActive = true, statusMessage = "已到${state.sleepTime}，手机进入锁机状态。")
                }) { Text("立即模拟锁机") }

                if (state.lockActive) {
                    Text("当前状态：LOCKED")
                    Button(onClick = {
                        scope.launch {
                            val decision = billingPolicy.decide(Instant.now(), state.ledger)
                            if (decision.isFree) {
                                state = state.copy(
                                    ledger = state.ledger.copy(
                                        totalUnlockCount = decision.newTotalUnlockCount,
                                        lastUnlockAt = Instant.now()
                                    ),
                                    lockActive = false,
                                    statusMessage = decision.message
                                )
                            } else {
                                val order = paymentGateway.createOrder(decision.amountFen, PaymentChannel.ALIPAY)
                                val paid = paymentGateway.pay(order)
                                if (paid.status == PaymentStatus.PAID) {
                                    state = state.copy(
                                        ledger = UnlockLedger(
                                            totalUnlockCount = decision.newTotalUnlockCount,
                                            lastUnlockAt = Instant.now()
                                        ),
                                        lockActive = false,
                                        statusMessage = "${decision.message} 支付成功（订单${paid.orderId}）。已解锁10分钟。"
                                    )
                                } else {
                                    state = state.copy(statusMessage = "支付失败，保持锁定。")
                                }
                            }
                        }
                    }) { Text("付费解锁（示例支付）") }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("提示：正式版需接入 Device Owner/Kiosk + 真正支付宝/微信服务端验签。")
        Text("累计解锁次数：${state.ledger.totalUnlockCount}")
        Text("状态：${state.statusMessage}")
    }
}
