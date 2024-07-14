package com.example.facex.data.local.ml.delegation

import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate

class TFLiteDelegateHelper {
    private var gpuDelegate: GpuDelegate? = null
    private var nnApiDelegate: NnApiDelegate? = null

    fun createOptions(delegateType: DelegateType): Interpreter.Options {
        val options = Interpreter.Options()
        when (delegateType) {
            DelegateType.GPU -> {
                if (gpuDelegate == null) {
                    gpuDelegate = GpuDelegate()
                }
                closeNnapiDelegates()
                options.addDelegate(gpuDelegate)
            }

            DelegateType.NNAPI -> {
                if (nnApiDelegate == null) {
                    nnApiDelegate = NnApiDelegate()
                }
                closeGpuDelegates()
                options.addDelegate(nnApiDelegate)
            }

            DelegateType.CPU -> {
                //default
            }

            else -> {}
        }
        return options
    }

    fun closeDelegates() {
        closeGpuDelegates()
        closeNnapiDelegates()
    }

    private fun closeNnapiDelegates() {
        if (nnApiDelegate != null) {
            nnApiDelegate!!.close()
            nnApiDelegate = null
        }
    }

    private fun closeGpuDelegates() {
        if (gpuDelegate != null) {
            gpuDelegate!!.close()
            gpuDelegate = null
        }
    }

    fun selectBestDelegate(): DelegateType {
        return if (isGpuDelegateSupported()) {
            DelegateType.GPU
        } else if (isNnApiDelegateSupported()) {
            DelegateType.NNAPI
        } else {
            DelegateType.CPU
        }

    }


    private fun isGpuDelegateSupported(): Boolean {
        val options = GpuDelegate.Options()
        try {
            GpuDelegate(options).use {
                return true
            }
        } catch (e: UnsupportedOperationException) {
            return false
        }
    }


    private fun isNnApiDelegateSupported(): Boolean {
        try {
            NnApiDelegate().use {
                return true
            }
        } catch (e: java.lang.UnsupportedOperationException) {
            return false
        }
    }


}

