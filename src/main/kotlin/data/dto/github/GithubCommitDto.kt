package com.dpv.data.dto.github

import com.dpv.data.serialization.ISO8601LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class CommitDto(
    val sha: String,
    val commit: CommitCommit,
    val author: CommitAuthor? = null,
    val url: String
)

@Serializable
data class CommitCommit(
    val message: String,
    val author: CommitCommitAuthor
)

@Serializable
data class CommitCommitAuthor(
    @Serializable(with = ISO8601LocalDateTimeSerializer::class)
    val date: LocalDateTime
)

@Serializable
data class CommitAuthor(
    val id: Long,
    @SerialName("login")
    val username: String
)

@Serializable
data class CommitDetailDto(
    val sha: String,
    val commit: CommitCommit,
    val author: CommitAuthor? = null,
    val stats: CommitDetailStats,
    val url: String
)

@Serializable
data class CommitDetailStats(
    val total: Int,
    val additions: Int,
    val deletions: Int
)