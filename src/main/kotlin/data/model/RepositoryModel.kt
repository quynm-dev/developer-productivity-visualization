package com.dpv.data.model

import java.time.LocalDateTime

data class RepositoryModel(
    val id: Long,
    val name: String,
    val githubUrl: String,
    val userId: Long,
    val language: String,
    val pullsUrl: String,
    val commitsUrl: String,
    val pat: String,
    val lastSyncAt: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)