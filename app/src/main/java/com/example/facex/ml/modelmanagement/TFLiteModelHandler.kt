package com.example.facex.ml.modelmanagement

import android.content.Context
import com.example.facex.ml.delegation.DelegateType
import com.example.facex.ml.delegation.TFLiteDelegateHelper
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.IOException
import java.nio.MappedByteBuffer

class TFLiteModelHandler(private val context: Context) {
    private val delegateHelper = TFLiteDelegateHelper()
    private var interpreter: Interpreter? = null
    private var tfliteModel: MappedByteBuffer? = null

    @Throws(IOException::class)
    fun loadModel(modelPath: String) {
        tfliteModel = loadModelFile(modelPath)
        initializeInterpreter()
    }

    private fun initializeInterpreter() {
        val bestDelegate = delegateHelper.selectBestDelegate()
        val options = delegateHelper.createOptions(bestDelegate)
        interpreter = Interpreter(tfliteModel!!, options)
    }

    fun changeDelegate(delegateType: DelegateType?) {
        if (interpreter != null) {
            interpreter!!.close()
        }
        delegateHelper.closeDelegates()
        val options = delegateHelper.createOptions(delegateType)
        interpreter = Interpreter(tfliteModel!!, options)
    }

    fun runModel(inputHandler: TFLiteInputHandler, outputHandler: TFLiteOutputHandler) {
        interpreter?.run(
            inputHandler.inputBuffer,
            outputHandler.outputBuffer
        )
    }

    @Throws(IOException::class)
    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        return FileUtil.loadMappedFile(context, modelFileName)
    }

    fun close() {
        if (interpreter != null) {
            interpreter?.close()
        }
        delegateHelper.closeDelegates()
    }
}