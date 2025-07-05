package com.dpv.data.dto.github

import com.dpv.data.serialization.ISO8601LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ReleaseDto(
    val id: Long,
    val url: String,
    val author: ReleaseAuthor,
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("target_commitish")
    val branch: String,
    val name: String,
    val body: String,
    val draft: Boolean,
    @SerialName("created_at")
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @SerialName("published_at")
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val publishedAt: LocalDateTime
)

@Serializable
data class ReleaseAuthor(
    val id: Long
)