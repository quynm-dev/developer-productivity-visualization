package com.dpv.helper

import io.ktor.client.statement.*
import io.ktor.http.*

suspend inline fun <reified T> HttpResponse.deserialize(): T? {
    return JsonHelper.deserialize(this.bodyAsText())
}

suspend inline fun <reified T> HttpResponse.deserializeIgnoreKeys(): T? {
    return JsonHelper.deserializeIgnoreKeys(this.bodyAsText())
}

suspend inline fun <reified T> HttpResponse.deserializeWhen(
    expectStatusCode: HttpStatusCode = HttpStatusCode.OK,
    shortCircuit: HttpResponse.(HttpStatusCode) -> Unit
): T? {
    if (status == expectStatusCode) {
        return deserialize()
    }
    shortCircuit(this, status)
    throw IllegalStateException("Illegal state")
}

suspend inline fun <reified T> HttpResponse.deserializeIgnoreKeysWhen(
    expectStatusCode: HttpStatusCode = HttpStatusCode.OK,
    shortCircuit: HttpResponse.(HttpStatusCode) -> Unit
): T? {
    if (status == expectStatusCode) {
        return deserializeIgnoreKeys()
    }
    shortCircuit(this, status)
    throw IllegalStateException("Illegal state")
}