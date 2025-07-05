package com.dpv.data.dto.github

import kotlinx.serialization.Serializable

@Serializable
data class RateLimitDto(
    val resources: RateLimitResources
)

@Serializable
data class RateLimitResources(
    val core: RateLimitResourcesCore
)

@Serializable
data class RateLimitResourcesCore(
    val limit: Int,
    val used: Int,
    val remaining: Int,
    val reset: Long
)

@Serializable
data class GithubResourcesDto(
    val pulls: List<PullDto>,
    val commitDetails: List<CommitDetailDto>,
    val issues: List<IssueDto>,
    val releases: List<ReleaseDto>,
    val milestones: List<MilestoneDto>
)