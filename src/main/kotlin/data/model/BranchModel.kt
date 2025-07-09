package com.dpv.data.model

import java.time.LocalDateTime

data class BranchModel(
    val id: Int,
    val name: String,
    val repoId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)