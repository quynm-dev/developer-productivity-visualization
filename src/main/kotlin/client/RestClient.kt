package com.dpv.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import mu.KotlinLogging
import org.koin.core.annotation.Factory

class RestConfig<T>(var url: String, var body: T? = null) {
    lateinit var authorization: String
    var contentType: ContentType = ContentType.Application.Json
    var headers: HeadersBuilder = HeadersBuilder()

    fun configureHeaders(builder: HeadersBuilder.() -> Unit) {
        headers.builder()
    }

    fun path(path: String) = url { appendPathSegments(path) }

    fun url(builder: URLBuilder.() -> Unit) {
        val urlBuilder = URLBuilder(url)
        builder(urlBuilder)
        url = urlBuilder.buildString()
    }
}

@Factory([HttpClient::class, RestClient::class])
class RestClient : IRestClient {
    private val logger = KotlinLogging.logger {}
    private val engine = HttpClient(CIO) {
        engine {
            this.endpoint.connectAttempts = 3
        }
        install(ContentNegotiation) {
            json()
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
        install(Logging) { level = LogLevel.INFO }
        install(HttpTimeout)
    }

    override suspend fun <T> exec(
        url: String,
        method: HttpMethod,
        body: T?,
        config: RestConfig<T>.() -> Unit
    ): HttpResponse {
        val restConfig = RestConfig(url, body)
        config(restConfig)
        try {
            return engine.request(restConfig.url) {
                this.method = method
                restConfig.body?.let { setBody(it as Any) }
                headers.appendAll(restConfig.headers.build())
                headers.append(HttpHeaders.ContentType, restConfig.contentType)
                headers.append(HttpHeaders.Authorization, restConfig.authorization)
                timeout { requestTimeoutMillis = 10_000L }
            }
        } catch (ex: ClientRequestException) {
            logger.error { ex.message }
            logger.error { ex.stackTraceToString() }
            throw ex
        } catch (ex: ServerResponseException) {
            logger.error { ex.message }
            logger.error { ex.stackTraceToString() }
            throw ex
        }
    }
}
