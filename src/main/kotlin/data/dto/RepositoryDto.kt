package com.dpv.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RepositoriesOnboardingDto(
    val repoNames: List<String>? = null
)

@Serializable
data class RepositorySyncDto(
    val repoName: String? = null
)