package com.dpv.service.github

import com.dpv.client.GithubClient
import com.dpv.data.dto.github.PullDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.deserializeIgnoreKeysWhen
import com.dpv.helper.err
import com.dpv.helper.ok
import io.ktor.server.application.*
import mu.KotlinLogging
import org.koin.core.annotation.Singleton

@Singleton
class GithubPullService(
    environment: ApplicationEnvironment,
    private val githubClient: GithubClient
) : GithubConfiguration(environment) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun getPulls(pat: String, url: String, base: String? = null, perPage: Int = 30, page: Int = 1): UniResult<List<PullDto>> {
        val response = githubClient.get(url) {
            authorization = "Bearer $pat"
            configureHeaders {
                appendAll(xGithubApiVersionHeader)
            }
            url {
                base?.let { parameters.append("base", it) }
                parameters.append("per_page", perPage.toString())
                parameters.append("page", page.toString())
                parameters.append("state", "all")
            }
        }

        return response.deserializeIgnoreKeysWhen<List<PullDto>> {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.INTERNAL_SERVER_ERROR, "Failed to get pulls").err()
        }.ok()
    }
}