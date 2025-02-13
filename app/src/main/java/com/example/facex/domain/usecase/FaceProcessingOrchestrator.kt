package com.example.facex.domain.usecase

import com.example.facex.di.DefaultDispatcher
import com.example.facex.domain.entities.DetectedFace
import com.example.facex.domain.entities.FaceRecognitionResult
import com.example.facex.domain.entities.Frame
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.RecognitionStatus
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.logger.logDebug
import com.example.facex.domain.logger.logInfo
import com.example.facex.domain.repository.PersonRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class FaceProcessingOrchestrator @Inject constructor(
    private val detectFacesUseCase: DetectFacesUseCase,
    private val getFaceEmbeddings: GetFaceEmbeddingUseCase,
    private val findBestMatchUseCase: FindBestMatchUseCase,
    private val faceTrackingManager: FaceTrackingManager,
    private val logger: Logger,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    personRepository: PersonRepository,
) {
    init {
        logger.tag = this::class.simpleName.toString()
    }

    private val knownPersonsFlow = personRepository.getAllPersons().distinctUntilChanged().stateIn(
        scope = CoroutineScope(defaultDispatcher + SupervisorJob()),
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private suspend fun detectFaces(
        frameData: Frame,
    ): List<DetectedFace>? = detectFacesUseCase(frameData).onFailure { logger.logError("Face detection failed", it) }.getOrNull()

    suspend operator fun invoke(
        frameData: Frame,
    ): Result<List<FaceRecognitionResult>> = withContext(defaultDispatcher) {
        try {
            processFaces(frameData)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.logError("Face processing failed", e)
            Result.failure(e)
        }
    }
    private suspend fun processFaces(

        frameData: Frame,
    ): Result<List<FaceRecognitionResult>> = coroutineScope {
        val detectedFaces =
            detectFaces(frameData) ?: return@coroutineScope Result.success(
                emptyList<FaceRecognitionResult>()
            ).logDebug(logger) { "No faces detected" }

        "Detected ${detectedFaces.size} faces".logDebug(logger)

//        val stableFaces = detectedFaces.filter { face ->
//            faceTrackingManager.isStableFace(
//                face.trackingId,
//                face.boundingBox,
//                frameWidth = frameData.width,
//                frameHeight = frameData.height
//            )
//        }
//
//        if (stableFaces.isEmpty()) {
//            return@coroutineScope Result.success(emptyList<FaceRecognitionResult>())
//                .logDebug(logger) { "No stable faces" }
//        }
//
//        "Processing ${stableFaces.size} stable faces".logDebug(logger)
//

        val knownPersons = knownPersonsFlow.value
        if (knownPersons.isEmpty()) {
            return@coroutineScope handleEmptyKnownPersons(detectedFaces)
        }

        val recognitionResults = detectedFaces.map { face ->
            processRecognitionParallel(
                face = face, knownPersons = knownPersons
            )
        }

        Result.success(recognitionResults)
            .logInfo(logger) { "Processing complete: ${recognitionResults.size} results" }
    }

    private fun handleEmptyKnownPersons(
        detectedFaces: List<DetectedFace>
    ): Result<List<FaceRecognitionResult>> {
        "No known persons available".logDebug(logger)
        return Result.success(detectedFaces.map { face ->
            FaceRecognitionResult(
                faceDetection = face,
                recognitionStatus = RecognitionStatus.Unknown,
            )
        })
    }

    private suspend fun processRecognitionParallel(
        face: DetectedFace,
        knownPersons: List<Person>,
    ): FaceRecognitionResult = coroutineScope {
        val embeddingDeferred = async {
            getFaceEmbeddings(face.faceImage).getOrNull()
        }

        val embedding = embeddingDeferred.await() ?: return@coroutineScope createUnknownResult(face)

        findBestMatchUseCase(embedding, knownPersons)?.let { (person, confidence) ->
            FaceRecognitionResult(face, RecognitionStatus.Known(person, confidence))
        } ?: createUnknownResult(face)
    }


    private fun createUnknownResult(face: DetectedFace) =
        FaceRecognitionResult(face, RecognitionStatus.Unknown)
}