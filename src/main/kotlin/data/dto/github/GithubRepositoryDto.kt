package com.dpv.data.dto.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepositoryDto(
    val id: Long,
    @SerialName("full_name")
    val name: String,
    val url: String,
    val language: String,
    @SerialName("pulls_url")
    val pullsUrl: String,
    @SerialName("commits_url")
    val commitsUrl: String,
    @SerialName("issues_url")
    val issuesUrl: String,
    @SerialName("milestones_url")
    val milestonesUrl: String,
    @SerialName("releases_url")
    val releasesUrl: String,
    @SerialName("branches_url")
    val branchesUrl: String,
    @SerialName("comments_url")
    val commentsUrl: String,
    val owner: UserDto
)