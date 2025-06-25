package com.dpv.data.model

data class UserModel(
    val id: Long,
    val username: String,
    val avatarUrl: String,
    val githubId: Long,
    val githubUrl: String,
    val createdAt: String,
    val updatedAt: String
)