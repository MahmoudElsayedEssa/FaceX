package com.example.facex.di

import com.example.facex.data.local.db.ModelConfigDao
import com.example.facex.data.local.ml.FaceProcessorFacadeImpl
import com.example.facex.data.local.ml.MLRepository
import com.example.facex.data.local.ml.ModelStorageManager
import com.example.facex.data.local.ml.liteRT.ModelValidatorFactory
import com.example.facex.data.repository.MLRepositoryImpl
import com.example.facex.domain.entities.DetectionConfig
import com.example.facex.domain.entities.FaceDetectorFactory
import com.example.facex.domain.entities.FaceRecognizerFactory
import com.example.facex.domain.entities.RecognitionConfig
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.ml.FaceProcessorFacade
import com.example.facex.domain.ml.config.FaceDetectionService
import com.example.facex.domain.ml.config.FaceRecognitionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FaceDetectorModel

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EmbeddingModel

@Module
@InstallIn(SingletonComponent::class)
object FaceProcessorModule {

    @Provides
    @Singleton
    fun provideMLRepository(
        modelConfigDao: ModelConfigDao,
        modelStorageManager: ModelStorageManager,
        modelValidatorFactory: ModelValidatorFactory,
        logger: Logger,
    ): MLRepository {
     return   MLRepositoryImpl(
         modelConfigDao = modelConfigDao,
         modelStorageManager = modelStorageManager,
         modelValidatorFactory = modelValidatorFactory,
         logger = logger
     )
    }


    @Provides
    @Singleton
    fun provideFaceProcessorFacade(
        faceRecognizerFactory: FaceRecognizerFactory,
        faceDetectorFactory: FaceDetectorFactory,
        mlRepository: MLRepository,
        logger: Logger,
    ): FaceProcessorFacade {
        val faceDetector = faceDetectorFactory.create(DetectionConfig.MLKit()).getOrThrow()

        val faceRecognizer = faceRecognizerFactory.create(RecognitionConfig.LiteRT()).getOrThrow()

        return FaceProcessorFacadeImpl.Builder(
            logger, mlRepository,
            detectorType = FaceDetectionService.ML_KIT,
            recognitionType = FaceRecognitionService.LITE_RT,
        ).setFaceDetector(faceDetector)
            .setEmbeddingGenerator(faceRecognizer)
            .build()
            .logInfo(logger) { "Created FaceProcessorFacade instance" }

    }


}
