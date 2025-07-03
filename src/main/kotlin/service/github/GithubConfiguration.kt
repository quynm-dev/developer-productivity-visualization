package com.dpv.service.github

import com.dpv.helper.getProperty
import io.ktor.http.*
import io.ktor.server.application.*
import java.time.LocalDateTime

abstract class GithubConfiguration(environment: ApplicationEnvironment) {
    // Base
    protected val BASE_URL = environment.getProperty("github.baseUrl")

    // Parameters
    protected val X_GITHUB_API_VERSION = "2022-11-28"

    // Path
    protected val RATE_LIMIT_PATH = "/rate_limit"
    protected val USERS_PREFIX = "/users/"
    protected val REPOS_PREFIX = "/repos/"

    // Functions
    protected fun getSyncTimeFrame(): Pair<LocalDateTime?, LocalDateTime?> {
        val startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)
        return Pair(startOfMonth, endOfMonth)
    }
    protected val xGithubApiVersionHeader: Headers
        get() = Headers.build { append("X-GitHub-Api-Version", X_GITHUB_API_VERSION) }
}