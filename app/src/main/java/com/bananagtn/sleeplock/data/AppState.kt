package com.bananagtn.sleeplock.data

import com.bananagtn.sleeplock.billing.UnlockLedger


data class AppState(
    val sleepTime: String = "23:30",
    val audioSource: String = "未设置",
    val ledger: UnlockLedger = UnlockLedger(),
    val lockActive: Boolean = false,
    val statusMessage: String = "请先设置睡觉时间和提醒语音。"
)
