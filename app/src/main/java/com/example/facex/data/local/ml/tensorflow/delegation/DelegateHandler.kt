package com.example.facex.data.local.ml.tensorflow.delegation

import org.tensorflow.lite.Interpreter

interface DelegateHandler : AutoCloseable {
    fun createInterpreterOptions(delegateType: DelegateType): Interpreter.Options
    fun selectBestDelegate(): DelegateType
}