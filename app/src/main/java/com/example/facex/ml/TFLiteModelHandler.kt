package com.example.facex.ml

import android.content.Context
import com.example.facex.ml.delegation.DelegateType
import com.example.facex.ml.delegation.TFLiteDelegateHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import javax.inject.Inject

class TFLiteModelHandler @Inject constructor(@ApplicationContext private val context: Context) {
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
        interpreter = tfliteModel?.let { Interpreter(it, options) }
    }

    fun changeDelegate(delegateType: DelegateType) {
        if (interpreter != null) {
            interpreter!!.close()
        }
        delegateHelper.closeDelegates()
        val options = delegateHelper.createOptions(delegateType)
        interpreter = Interpreter(tfliteModel!!, options)
    }

    fun runModel(inputByteBuffer: ByteBuffer, outputByteBuffer: ByteBuffer) {
        interpreter?.run(
            inputByteBuffer,
            outputByteBuffer
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