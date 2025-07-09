package com.dpv.service.github

import com.dpv.client.GithubClient
import com.dpv.data.dto.github.BranchDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.deserializeIgnoreKeysWhen
import com.dpv.helper.err
import com.dpv.helper.ok
import io.ktor.server.application.*
import org.koin.core.annotation.Singleton

@Singleton
class GithubBranchService(
    environment: ApplicationEnvironment,
    private val githubClient: GithubClient
) : GithubConfiguration(environment) {
    suspend fun getBranches(
        pat: String, url: String, perPage: Int = 30, page: Int = 1
    ): UniResult<List<BranchDto>> {
        val response = githubClient.get(url) {
            authorization = "Bearer $pat"
            configureHeaders {
                appendAll(xGithubApiVersionHeader)
            }
            url {
                parameters.append("per_page", perPage.toString())
                parameters.append("page", page.toString())
            }
        }

        val data = response.deserializeIgnoreKeysWhen<List<BranchDto>> {
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.INTERNAL_SERVER_ERROR, "Failed to get branches").err()
        }
        if(data.isNullOrEmpty()) {
            return emptyList<BranchDto>().ok()
        }

        return data.ok()
    }
}