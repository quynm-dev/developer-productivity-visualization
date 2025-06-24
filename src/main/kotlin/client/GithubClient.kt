package com.dpv.client

import com.dpv.service.github.GithubConfiguration
import io.ktor.http.*
import io.ktor.server.application.*
import mu.KotlinLogging
import org.koin.core.annotation.Singleton

@Singleton
class GithubClient(
    environment: ApplicationEnvironment,
    private val restClient: RestClient,
) : GithubConfiguration(environment) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun get(url: String, config: RestConfig<Unit>.() -> Unit = {}) = exec(url, HttpMethod.Get, null, config)

    suspend fun <T> post(url: String, body: T, config: RestConfig<T>.() -> Unit = {}) = exec(url, HttpMethod.Post, body, config)

    suspend fun <T> exec(url: String, method: HttpMethod, body: T?, config: RestConfig<T>.() -> Unit) = restClient.exec(url, method, body, config)
}