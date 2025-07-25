package com.dpv.mapper

import com.dpv.data.entity.RepositoryEntity
import com.dpv.data.model.RepositoryModel

fun RepositoryEntity.toModel(): RepositoryModel {
    return RepositoryModel(
        id = this.id.value,
        name = this.name,
        githubUrl = this.githubUrl,
        userId = this.userId.value,
        language = this.language,
        pullsUrl = this.pullsUrl,
        commitsUrl = this.commitsUrl,
        issuesUrl = this.issuesUrl,
        milestonesUrl = this.milestonesUrl,
        releasesUrl = this.releasesUrl,
        branchesUrl = this.branchesUrl,
        commentsUrl = this.commentsUrl,
        pat = this.pat,
        lastSyncAt = this.lastSyncAt,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}