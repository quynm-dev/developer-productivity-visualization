package com.dpv.service.github

import com.dpv.client.GithubClient
import com.dpv.data.dto.github.UserDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.deserializeIgnoreKeysWhen
import com.dpv.helper.err
import com.dpv.helper.ok
import io.ktor.server.application.*
import org.koin.core.annotation.Singleton

@Singleton
class GithubUserService(
    environment: ApplicationEnvironment,
    private val githubClient: GithubClient,
) : GithubConfiguration(environment) {
    suspend fun getUser(username: String): UniResult<UserDto> {
        val response = githubClient.get(BASE_URL + USERS_PREFIX + username) {
            authorization = AUTHORIZATION
            configureHeaders {
                appendAll(xGithubApiVersionHeader)
            }
        }

        return response.deserializeIgnoreKeysWhen<UserDto> {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.INTERNAL_SERVER_ERROR, "Failed to get user").err()
        }.ok()
    }
}