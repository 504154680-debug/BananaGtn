package com.bananagtn.sleeplock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.bananagtn.sleeplock.ui.SleepLockApp
import com.bananagtn.sleeplock.ui.theme.SleepLockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepLockTheme {
                Surface {
                    SleepLockApp()
                }
            }
        }
    }
}
