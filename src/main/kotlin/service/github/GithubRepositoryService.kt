package com.dpv.service.github

import com.dpv.client.GithubClient
import com.dpv.data.dto.github.RepositoryDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.deserializeIgnoreKeysWhen
import com.dpv.helper.err
import com.dpv.helper.ok
import io.ktor.server.application.*
import org.koin.core.annotation.Singleton

@Singleton
class GithubRepositoryService(
    environment: ApplicationEnvironment,
    private val githubClient: GithubClient
) : GithubConfiguration(environment) {
    suspend fun getRepo(pat: String, name: String): UniResult<RepositoryDto> {
        val response = githubClient.get(BASE_URL + REPOS_PREFIX + name) {
            authorization = "Bearer $pat"
            configureHeaders {
                appendAll(xGithubApiVersionHeader)
            }
        }

        val data = response.deserializeIgnoreKeysWhen<RepositoryDto> {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.INTERNAL_SERVER_ERROR, "Failed to get repository").err()
        }
        if (data == null) {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND, "Repository not found").err()
        }

        return data.ok()
    }
}