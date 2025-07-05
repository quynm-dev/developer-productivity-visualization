package com.dpv.helper

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.slf4j.event.Level

object JsonHelper {
    val IgnoreKeysJson = Json { ignoreUnknownKeys = true }
    val logger = KotlinLogging.logger {}

    inline fun <reified T> deserialize(key: String, level: Level = Level.ERROR): T? {
        return try {
            Json.decodeFromString(key)
        } catch (ex: SerializationException) {
            logger.atLevel(level).log("Failed to deserialize data: ${ex.message}")
            return deserializeIgnoreKeys(key)
        }
    }

    inline fun <reified T> deserializeIgnoreKeys(key: String): T? {
        if (key.isBlank() || key == "[]") return null
        return IgnoreKeysJson.decodeFromString(key)
    }
}