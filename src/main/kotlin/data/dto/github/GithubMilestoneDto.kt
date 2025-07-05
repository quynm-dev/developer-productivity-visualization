package com.dpv.data.dto.github

import com.dpv.data.serialization.ISO8601LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class MilestoneDto(
    val id: Long,
    val url: String,
    val state: String,
    val title: String,
    val description: String,
    val creator: MilestoneCreator,
    @SerialName("open_issues")
    val openIssues: Int,
    @SerialName("closed_issues")
    val closedIssues: Int,
    @SerialName("closed_at")
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val closedAt: LocalDateTime? = null,
    @SerialName("created_at")
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @SerialName("updated_at")
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,
    @SerialName("due_on")
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val dueOn: LocalDateTime? = null,
)

@Serializable
data class MilestoneCreator(
    val id: Long,
    @SerialName("login")
    val username: String
)