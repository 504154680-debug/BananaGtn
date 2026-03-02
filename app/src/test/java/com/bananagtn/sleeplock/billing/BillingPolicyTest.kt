package com.bananagtn.sleeplock.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class BillingPolicyTest {
    private val policy = BillingPolicy()

    @Test
    fun firstUnlockIsFree() {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val decision = policy.decide(now, UnlockLedger())

        assertEquals(0, decision.amountFen)
        assertTrue(decision.isFree)
        assertEquals(1, decision.newTotalUnlockCount)
    }

    @Test
    fun secondToFourthUnlockChargeOneYuan() {
        val now = Instant.parse("2026-01-05T00:00:00Z")

        val second = policy.decide(now, UnlockLedger(totalUnlockCount = 1, lastUnlockAt = now.minus(1, ChronoUnit.DAYS)))
        val fourth = policy.decide(now, UnlockLedger(totalUnlockCount = 3, lastUnlockAt = now.minus(1, ChronoUnit.DAYS)))

        assertEquals(100, second.amountFen)
        assertEquals(100, fourth.amountFen)
    }

    @Test
    fun fifthUnlockAndAfterChargeFiveYuan() {
        val now = Instant.parse("2026-01-05T00:00:00Z")
        val decision = policy.decide(now, UnlockLedger(totalUnlockCount = 4, lastUnlockAt = now.minus(1, ChronoUnit.DAYS)))

        assertEquals(500, decision.amountFen)
        assertEquals(5, decision.newTotalUnlockCount)
    }

    @Test
    fun resetAfter33DaysNoUnlock() {
        val now = Instant.parse("2026-02-10T00:00:00Z")
        val old = now.minus(33, ChronoUnit.DAYS)

        val decision = policy.decide(now, UnlockLedger(totalUnlockCount = 7, lastUnlockAt = old))

        assertEquals(0, decision.amountFen)
        assertTrue(decision.message.contains("重置"))
        assertEquals(1, decision.newTotalUnlockCount)
    }
}
