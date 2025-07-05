package com.dpv.service.github

import com.dpv.client.RestClient
import com.dpv.data.dto.github.RateLimitDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.deserializeIgnoreKeysWhen
import com.dpv.helper.err
import com.dpv.helper.ok
import com.github.michaelbull.result.getOrElse
import io.ktor.server.application.*
import mu.KotlinLogging
import org.koin.core.annotation.Singleton
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Singleton
class GithubRateLimitService(
    environment: ApplicationEnvironment,
    private val restClient: RestClient
) : GithubConfiguration(environment) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun getRateLimit(pat: String): UniResult<RateLimitDto> {
        val response = restClient.get(BASE_URL) {
            authorization = "Bearer $pat"
            path(RATE_LIMIT_PATH)
            configureHeaders {
                appendAll(xGithubApiVersionHeader)
            }
        }

        val data = response.deserializeIgnoreKeysWhen<RateLimitDto> {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.INTERNAL_SERVER_ERROR, "Failed to get rate limit").err()
        }
        if (data == null) {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND, "Rate limit not found").err()
        }

        return data.ok()
    }

    suspend fun validateQuota(pat: String): UniResult<Boolean> {
        val rateLimitResult = getRateLimit(pat).getOrElse { getRateLimitErr ->
            return getRateLimitErr.err()
        }

        if(rateLimitResult.resources.core.remaining > 0) {
            return true.ok()
        } else {
            logger.warn { "Rate limit exceeded. Wait until ${LocalDateTime.ofInstant(Instant.ofEpochSecond(rateLimitResult.resources.core.reset), ZoneId.systemDefault())}" }
            return false.ok()
        }
    }
}