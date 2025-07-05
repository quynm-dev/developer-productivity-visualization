package com.dpv.service.github

import com.dpv.client.GithubClient
import com.dpv.data.dto.github.IssueDto
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
class GithubIssueService(
    environment: ApplicationEnvironment,
    private val githubClient: GithubClient
) : GithubConfiguration(environment) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun getIssues(pat: String, url: String, perPage: Int = 30, page: Int = 1): UniResult<List<IssueDto>> {
        val response = githubClient.get(url) {
            authorization = "Bearer $pat"
            configureHeaders {
                appendAll(xGithubApiVersionHeader)
            }
            url {
                parameters.append("per_page", perPage.toString())
                parameters.append("page", page.toString())
                parameters.append("state", "all")
                parameters.append("filter", "all") // Check whether this is correct or not
            }
        }

        val data = response.deserializeIgnoreKeysWhen<List<IssueDto>> {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.INTERNAL_SERVER_ERROR, "Failed to get issues").err()
        }
        if(data.isNullOrEmpty()) {
            return emptyList<IssueDto>().ok()
        }

        return data.ok()
    }
}