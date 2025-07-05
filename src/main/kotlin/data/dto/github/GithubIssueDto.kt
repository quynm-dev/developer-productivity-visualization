package com.dpv.data.dto.github

import com.dpv.data.serialization.ISO8601LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class IssueDto(
    val id: Long,
    val url: String,
    val state: String,
    val title: String,
    val body: String? = null,
    val milestone: IssueMilestone? = null,
    val assignees: List<IssueAssigneeDto> = emptyList(),
    @SerialName("closed_at")
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val closedAt: LocalDateTime? = null,
    @SerialName("created_at")
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @SerialName("updated_at")
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)

@Serializable
data class IssueMilestone(
    val id: Long
)

@Serializable
data class IssueAssigneeDto(
    val id: Long,
    @SerialName("login")
    val username: String
)