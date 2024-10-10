package com.example.facex.data.local.ml.tensorflow.delegation

import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate


class TFLiteDelegateHandler : DelegateHandler {
    private var gpuDelegate: GpuDelegate? = null
    private var nnApiDelegate: NnApiDelegate? = null

    override fun createInterpreterOptions(delegateType: DelegateType): Interpreter.Options {
        return Interpreter.Options().apply {
            when (delegateType) {
                DelegateType.GPU -> addDelegate(getGpuDelegate())
                DelegateType.NNAPI -> addDelegate(getNnApiDelegate())
                DelegateType.CPU -> {} // No delegate needed for CPU
            }
        }
    }

    private fun getGpuDelegate(): GpuDelegate {
        if (gpuDelegate == null) {
            gpuDelegate = GpuDelegate()
        }
        return gpuDelegate!!
    }

    private fun getNnApiDelegate(): NnApiDelegate {
        if (nnApiDelegate == null) {
            nnApiDelegate = NnApiDelegate()
        }
        return nnApiDelegate!!
    }

    override fun selectBestDelegate(): DelegateType {
        return if (isGpuDelegateSupported()) {
            DelegateType.GPU
        } else if (isNnApiDelegateSupported()) {
            DelegateType.NNAPI
        } else {
            DelegateType.CPU
        }
    }

    private fun isGpuDelegateSupported(): Boolean {
        return CompatibilityList().isDelegateSupportedOnThisDevice
    }

    private fun isNnApiDelegateSupported(): Boolean {
        return try {
            NnApiDelegate().close()
            true
        } catch (e: UnsupportedOperationException) {
            false
        }
    }

    override fun close() {
        gpuDelegate?.close()
        nnApiDelegate?.close()
        gpuDelegate = null
        nnApiDelegate = null
    }
}
