package com.example.facex.domain.helpers

// class LSHFaceRecognizer {
//    companion object {
//        private const val ORIGINAL_DIMENSION = 512
//        private const val REDUCED_DIMENSION = 64
//        private const val NUM_HASH_TABLES = 10
//        private const val HASH_SIZE = 16
//        private const val CHUNK_SIZE = 1000
//        private const val RECOGNITION_CONFIDENCE_THRESHOLD = 0.6f
//    }
//
//    private val projectionMatrix = generateRandomProjectionMatrix()
//    private val randomHyperplanes = generateRandomHyperplanes()
//    private val hashTables = Array(NUM_HASH_TABLES) { HashMap<String, MutableList<Person>>() }
//
//    private fun generateRandomProjectionMatrix() = Array(REDUCED_DIMENSION) {
//        FloatArray(ORIGINAL_DIMENSION) { Random.nextFloat() * 2 - 1 }
//    }
//
//    private fun generateRandomHyperplanes() = Array(NUM_HASH_TABLES) {
//        Array(HASH_SIZE) { FloatArray(REDUCED_DIMENSION) { Random.nextFloat() * 2 - 1 } }
//    }
//
//    fun addPerson(person: Person) {
//        val projectedEmbedding = projectVector(person.embedding)
//        for (tableIndex in 0 until NUM_HASH_TABLES) {
//            val hashCode = hashVector(projectedEmbedding, randomHyperplanes[tableIndex])
//            hashTables[tableIndex].getOrPut(hashCode) { ArrayList() }.add(person)
//        }
//    }
//
//    fun removePerson(person: Person) {
//        val projectedEmbedding = projectVector(person.embedding)
//        for (tableIndex in 0 until NUM_HASH_TABLES) {
//            val hashCode = hashVector(projectedEmbedding, randomHyperplanes[tableIndex])
//            hashTables[tableIndex][hashCode]?.remove(person)
//        }
//    }
//
//    fun updatePerson(oldPerson: Person, newPerson: Person) {
//        removePerson(oldPerson)
//        addPerson(newPerson)
//    }
//
//    private fun projectVector(vector: FloatArray): FloatArray {
//        val result = FloatArray(REDUCED_DIMENSION)
//        for (i in 0 until REDUCED_DIMENSION) {
//            var sum = 0f
//            for (j in vector.indices) {
//                sum += vector[j] * projectionMatrix[i][j]
//            }
//            result[i] = sum
//        }
//        return result
//    }
//
//    private fun hashVector(vector: FloatArray, randomHyperplane: Array<FloatArray>): String {
//        return buildString(HASH_SIZE) {
//            for (i in 0 until HASH_SIZE) {
//                var dotProduct = 0f
//                for (j in vector.indices) {
//                    dotProduct += vector[j] * randomHyperplane[i][j]
//                }
//                append(if (dotProduct >= 0) '1' else '0')
//            }
//        }
//    }
//
//    private fun getCandidateMatches(projectedQuery: FloatArray): Set<Person> {
//        val candidates = HashSet<Person>()
//        for (tableIndex in 0 until NUM_HASH_TABLES) {
//            val hashCode = hashVector(projectedQuery, randomHyperplanes[tableIndex])
//            hashTables[tableIndex][hashCode]?.let { candidates.addAll(it) }
//        }
//        return candidates
//    }
//
//    suspend fun findRecognizedPerson(
//        detectedFace: DetectedFace,
//        embedding: FloatArray
//    ): RecognizedPerson? = withContext(Dispatchers.Default) {
//        val projectedQuery = projectVector(embedding)
//        val normA = embedding.norm()
//        val candidates = getCandidateMatches(projectedQuery)
//
//        candidates.asSequence()
//            .map { person ->
//                async {
//                    val similarity =
//                        cosineSimilarity(embedding, person.embedding, normA, person.norm.toFloat())
//                    if (similarity >= RECOGNITION_CONFIDENCE_THRESHOLD) {
//                        RecognizedPerson(person, similarity, detectedFace)
//                    } else null
//                }
//            }
//            .toList()
//            .awaitAll()
//            .filterNotNull()
//            .maxByOrNull { it.confidence }
//    }
//
//    private fun cosineSimilarity(
//        vectorA: FloatArray,
//        vectorB: FloatArray,
//        normA: Float,
//        normB: Float
//    ): Float {
//        var dotProduct = 0f
//        var i = 0
//        while (i < vectorA.size) {
//            val chunkEnd = (i + CHUNK_SIZE).coerceAtMost(vectorA.size)
//            var chunkDotProduct = 0f
//            for (j in i until chunkEnd) {
//                chunkDotProduct += vectorA[j] * vectorB[j]
//            }
//            dotProduct += chunkDotProduct
//
//            val remainingIndices = vectorA.size - chunkEnd
//            val maxRemaining =
//                remainingIndices * (vectorA.last().absoluteValue * vectorB.last().absoluteValue)
//            if (dotProduct + maxRemaining < RECOGNITION_CONFIDENCE_THRESHOLD * normA * normB) {
//                return 0f
//            }
//            i = chunkEnd
//        }
//        return dotProduct / (normA * normB)
//    }
//
//    private fun FloatArray.norm(): Float = sqrt(sumOf { it * it.toDouble() }).toFloat()
// }

// class FaceRecognizer(
//    private val originalDimension: Int = 512,
//    private val reducedDimension: Int = 64,
//    private val numHashTables: Int = 10,
//    private val hashSize: Int = 16,
//    private val chunkSize: Int = 1000,
//    private val recognitionConfidenceThreshold: Float = 0.6f,
//    private val useLSH: Boolean = true
// ) {
//    private val projectionMatrix = if (useLSH) generateRandomProjectionMatrix() else null
//    private val randomHyperplanes = if (useLSH) generateRandomHyperplanes() else null
//    private val hashTables =
//        if (useLSH) Array(numHashTables) { ConcurrentHashMap<String, CopyOnWriteArrayList<Person>>() } else null
//    private val allPersons = if (!useLSH) CopyOnWriteArrayList<Person>() else null
//
//    private fun generateRandomProjectionMatrix() = Array(reducedDimension) {
//        FloatArray(originalDimension) { Random.nextFloat() * 2 - 1 }
//    }
//
//    private fun generateRandomHyperplanes() = Array(numHashTables) {
//        Array(hashSize) {
//            FloatArray(reducedDimension) { Random.nextFloat() * 2 - 1 }
//        }
//    }
//
//    suspend fun addPerson(person: Person) = withContext(Dispatchers.Default) {
//        if (useLSH) {
//            val projectedEmbedding = projectVector(person.embedding)
//            (0 until numHashTables).map { tableIndex ->
//                async {
//                    val hashCode = hashVector(projectedEmbedding, randomHyperplanes!![tableIndex])
//                    hashTables!![tableIndex].getOrPut(hashCode) { CopyOnWriteArrayList() }
//                        .add(person)
//                }
//            }.awaitAll()
//        } else {
//            allPersons!!.add(person)
//        }
//    }
//
//    suspend fun removePerson(person: Person) = withContext(Dispatchers.Default) {
//        if (useLSH) {
//            val projectedEmbedding = projectVector(person.embedding)
//            (0 until numHashTables).map { tableIndex ->
//                async {
//                    val hashCode = hashVector(projectedEmbedding, randomHyperplanes!![tableIndex])
//                    hashTables!![tableIndex][hashCode]?.remove(person)
//                }
//            }.awaitAll()
//        } else {
//            allPersons!!.remove(person)
//        }
//    }
//
//    suspend fun updatePerson(oldPerson: Person, newPerson: Person) {
//        removePerson(oldPerson)
//        addPerson(newPerson)
//    }
//
//    private fun projectVector(vector: ByteBuffer): FloatArray {
//        if (!useLSH) return vector.asFloatBuffer().array()
//        val result = FloatArray(reducedDimension)
//        val floatBuffer = vector.asFloatBuffer()
//        for (i in 0 until reducedDimension) {
//            var sum = 0f
//            for (j in 0 until originalDimension) {
//                sum += floatBuffer.get(j) * projectionMatrix!![i][j]
//            }
//            result[i] = sum
//        }
//        return result
//    }
//
//    private fun hashVector(vector: FloatArray, randomHyperplane: Array<FloatArray>): String {
//        return buildString(hashSize) {
//            for (i in 0 until hashSize) {
//                val dotProduct =
//                    vector.zip(randomHyperplane[i]).sumOf { (a, b) -> a * b.toDouble() }
//                append(if (dotProduct >= 0) '1' else '0')
//            }
//        }
//    }
//
//    private suspend fun getCandidateMatches(projectedQuery: FloatArray): Set<Person> =
//        withContext(Dispatchers.Default) {
//            if (!useLSH) return@withContext allPersons!!.toSet()
//            (0 until numHashTables).map { tableIndex ->
//                async {
//                    val hashCode = hashVector(projectedQuery, randomHyperplanes!![tableIndex])
//                    hashTables!![tableIndex][hashCode]?.toSet() ?: emptySet()
//                }
//            }.awaitAll().flatten().toSet()
//        }
//
//    suspend fun findRecognizedPerson(
//        detectedFace: FaceImage, embedding: ByteBuffer
//    ): RecognizedFace? = withContext(Dispatchers.Default) {
//        val projectedQuery = projectVector(embedding)
//        val normA = Person.calculateNorm(embedding)
//        val candidates = getCandidateMatches(projectedQuery)
//
//        candidates.map { person ->
//            async {
//                val similarity = cosineSimilarity(embedding, person.embedding, normA, person.norm)
//                if (similarity >= recognitionConfidenceThreshold) {
//                    RecognizedFace(person, similarity, detectedFace)
//                } else null
//            }
//        }.awaitAll().filterNotNull().maxByOrNull { it.confidence }
//    }
//
//    private fun cosineSimilarity(
//        vectorA: ByteBuffer, vectorB: ByteBuffer, normA: Float, normB: Float
//    ): Float {
//        var dotProduct = 0f
//        val floatA = vectorA.asFloatBuffer()
//        val floatB = vectorB.asFloatBuffer()
//        var i = 0
//        while (i < originalDimension) {
//            val chunkEnd = (i + chunkSize).coerceAtMost(originalDimension)
//            var chunkDotProduct = 0f
//            for (j in i until chunkEnd) {
//                chunkDotProduct += floatA.get(j) * floatB.get(j)
//            }
//            dotProduct += chunkDotProduct
//
//            val remainingIndices = originalDimension - chunkEnd
//            val maxRemaining =
//                remainingIndices * (floatA.get(originalDimension - 1).absoluteValue * floatB.get(
//                    originalDimension - 1
//                ).absoluteValue)
//            if (dotProduct + maxRemaining < recognitionConfidenceThreshold * normA * normB) {
//                return 0f
//            }
//            i = chunkEnd
//        }
//        return dotProduct / (normA * normB)
//    }
// }
