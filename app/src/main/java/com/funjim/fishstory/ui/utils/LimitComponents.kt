package com.funjim.fishstory.ui.utils

import androidx.compose.runtime.*

@Composable
fun rememberGumballStatus(
    limitExceededCount: Int,
    limitReachedCount: Int,
    limitCount: Int
): GumballStatus {
    var showCautionState by remember { mutableStateOf(false) }

    val primaryStatus = if (limitExceededCount > 0) GumballStatus.BAD else GumballStatus.GOOD

    LaunchedEffect(limitReachedCount, limitCount) {
        if (limitReachedCount > 0) {
            if (limitCount > limitReachedCount) {
                while (true) {
                    showCautionState = !showCautionState
                    kotlinx.coroutines.delay(1000L)
                }
            } else {
                showCautionState = true
            }
        } else {
            showCautionState = false
        }
    }

    return if (showCautionState) GumballStatus.CAUTION else primaryStatus
}