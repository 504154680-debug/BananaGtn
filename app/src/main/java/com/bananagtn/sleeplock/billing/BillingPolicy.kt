package com.bananagtn.sleeplock.billing

import java.time.Duration
import java.time.Instant

data class UnlockLedger(
    val totalUnlockCount: Int = 0,
    val lastUnlockAt: Instant? = null
)

data class ChargeDecision(
    val amountFen: Int,
    val message: String,
    val newTotalUnlockCount: Int,
    val isFree: Boolean
)

class BillingPolicy(
    private val resetDays: Long = 33
) {
    fun decide(now: Instant, ledger: UnlockLedger): ChargeDecision {
        val reset = shouldReset(now, ledger.lastUnlockAt)
        val effectiveCount = if (reset) 0 else ledger.totalUnlockCount
        val nextIndex = effectiveCount + 1

        val amountFen = when (nextIndex) {
            1 -> 0
            2, 3, 4 -> 100
            else -> 500
        }

        val message = when (nextIndex) {
            1 -> "首次解锁免费，后续将按规则收费。"
            in 2..4 -> "第${nextIndex}次解锁，收费1元。"
            else -> "第${nextIndex}次解锁，收费5元。"
        }

        return ChargeDecision(
            amountFen = amountFen,
            message = if (reset) "已连续33天未解锁，计费阶梯已重置。$message" else message,
            newTotalUnlockCount = nextIndex,
            isFree = amountFen == 0
        )
    }

    private fun shouldReset(now: Instant, lastUnlockAt: Instant?): Boolean {
        if (lastUnlockAt == null) return false
        val days = Duration.between(lastUnlockAt, now).toDays()
        return days >= resetDays
    }
}
