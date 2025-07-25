package com.dpv.data.model

data class UserModel(
    val id: Long,
    val username: String,
    val avatarUrl: String,
    val githubUrl: String,
    val createdAt: String,
    val updatedAt: String
)