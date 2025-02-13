package com.example.facex.data.local.ml.liteRT

import com.example.facex.data.local.ml.liteRT.modelhandling.LiteRTModelConfig
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import com.example.facex.domain.logger.logWarning
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.gpu.GpuDelegateFactory
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.nio.MappedByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class TFLiteInterpreterWrapper<in T, out R>(
    private val config: LiteRTModelConfig,
    private val modelMappedByteBuffer: MappedByteBuffer,
    private val inputProcessor: InputProcessor<T>,
    private val outputProcessor: OutputProcessor<R>,
    private val logger: Logger,
) : AutoCloseable {
    private val mutex = Mutex()
    private val isReleased = AtomicBoolean(false)

    private var interpreter: Interpreter? = null
    private var nnapiDelegate: NnApiDelegate? = null
    private var gpuDelegate: GpuDelegate? = null


    init {
        initializeInterpreter()
    }

    private fun initializeInterpreter() {
        interpreter = Interpreter(modelMappedByteBuffer, createInterpreterOptions()).also {
            validateModelIO(it)
        }
    }

    private fun createInterpreterOptions(): Interpreter.Options {
        return Interpreter.Options().apply {
            when (config.delegate) {
                DelegateType.NNAPI -> runCatching { setupNNAPIDelegate() }.onFailure {
                    fallbackToCPU("NNAPI delegate setup failed")
                }

                DelegateType.GPU -> runCatching { setupGPUDelegate() }.onFailure {
                    fallbackToCPU("GPU delegate setup failed")
                }

                DelegateType.CPU -> setupCPUDelegate()
            }
        }
    }

    private fun Interpreter.Options.fallbackToCPU(reason: String) {
        "$reason. Falling back to CPU.".logWarning(logger)
        setupCPUDelegate()
    }

    private fun Interpreter.Options.setupNNAPIDelegate() {
        useNNAPI = true
        nnapiDelegate = NnApiDelegate(NnApiDelegate.Options().apply {
            executionPreference = NnApiDelegate.Options.EXECUTION_PREFERENCE_SUSTAINED_SPEED
        }).also { addDelegate(it) }
    }

    private fun Interpreter.Options.setupGPUDelegate() {
        if (CompatibilityList().isDelegateSupportedOnThisDevice) {
            gpuDelegate = GpuDelegate(GpuDelegateFactory.Options().apply {
                inferencePreference =
                    GpuDelegateFactory.Options.INFERENCE_PREFERENCE_SUSTAINED_SPEED
                isPrecisionLossAllowed = false
            }).also { addDelegate(it) }

        } else {
            throw UnsupportedOperationException("GPU delegate not supported on this device")
        }
    }

    private fun Interpreter.Options.setupCPUDelegate() {
        numThreads = config.numThreads
        useXNNPACK = true
    }

    private fun validateModelIO(interpreter: Interpreter) {
        val inputShape = interpreter.getInputTensor(0).shape()
        val outputShape = interpreter.getOutputTensor(0).shape()

        require(inputShape.size == 4) { "Invalid input shape: expected 4D tensor" }
        require(outputShape.size == 2) { "Invalid output shape: expected 2D tensor" }


        "Model initialized with input shape: ${inputShape.contentToString()}, " +
                "output shape: ${outputShape.contentToString()}".logDebug(logger)
    }

    fun predict(input: T): Result<R> {
        return runCatching {
            interpreter?.let { interpreter ->
                val processedInput = inputProcessor.process(input)
                val output = outputProcessor.createOutput()
                interpreter.run(processedInput, output)
                val result = outputProcessor.process(output)

                result
            } ?: throw IllegalStateException("Interpreter is null")
        }.onFailure { logger.logError("Prediction failed", it) }
    }

    override fun close() {
        if (isReleased.compareAndSet(false, true)) {
            runBlocking {
                mutex.withLock {
                    interpreter?.close()
                    nnapiDelegate?.close()
                    gpuDelegate?.close()
                    interpreter = null
                    nnapiDelegate = null
                    gpuDelegate = null
                    "Interpreter resources released".logDebug(logger)
                }
            }
        }
    }
}

