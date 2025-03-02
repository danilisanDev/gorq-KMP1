package com.danilisan.kmp.core.log

import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock


object DebugTest {
    var startTime = 0L
    var endTime = 0L
    fun debugLog(functionName: String, start: Boolean){
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if(start){
            startTime = currentTime
            endTime = 0L
        }else{
            endTime = currentTime
        }
        val message = if(start){
            "START"
        }else if(startTime > 0L && endTime > 0L){
            "END -> Time: ${endTime - startTime}ms"
        }else{
            error(message = "Debug called by $functionName END without START!")
        }
        Napier.d("$functionName $message")
    }
}