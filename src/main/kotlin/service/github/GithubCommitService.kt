package com.dpv.service.github

import com.dpv.client.GithubClient
import com.dpv.data.dto.github.CommitDetailDto
import com.dpv.data.dto.github.CommitDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.deserializeIgnoreKeysWhen
import com.dpv.helper.err
import com.dpv.helper.ok
import io.ktor.server.application.*
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Singleton
class GithubCommitService(
    environment: ApplicationEnvironment,
    private val githubClient: GithubClient,
) : GithubConfiguration(environment) {
    suspend fun getCommits(
        pat: String, since: LocalDateTime? = null, until: LocalDateTime? = null, url: String,
        perPage: Int = 30, page: Int = 1
    ): UniResult<List<CommitDto>> {
        val response = githubClient.get(url) {
            authorization = "Bearer $pat"
            configureHeaders {
                appendAll(xGithubApiVersionHeader)
            }
            url {
                since?.let { parameters.append("since", since.format(DateTimeFormatter.ISO_DATE_TIME)) }
                until?.let { parameters.append("until", until.format(DateTimeFormatter.ISO_DATE_TIME)) }
                parameters.append("per_page", perPage.toString())
                parameters.append("page", page.toString())
            }
        }

        return response.deserializeIgnoreKeysWhen<List<CommitDto>> {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.INTERNAL_SERVER_ERROR, "Failed to get commits").err()
        }.ok()
    }

    suspend fun getCommit(pat: String, url: String): UniResult<CommitDetailDto> {
        val response = githubClient.get(url) {
            authorization = "Bearer $pat"
            configureHeaders {
                appendAll(xGithubApiVersionHeader)
            }
        }

        return response.deserializeIgnoreKeysWhen<CommitDetailDto> {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.INTERNAL_SERVER_ERROR, "Failed to get commit").err()
        }.ok()
    }
}