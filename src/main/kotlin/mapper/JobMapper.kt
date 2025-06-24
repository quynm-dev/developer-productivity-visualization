package com.dpv.mapper

import com.dpv.data.entity.JobEntity
import com.dpv.data.model.JobModel

fun JobEntity.toModel(): JobModel {
    return JobModel(
        id = this.id.value,
        status = this.status,
        repoName = this.repositoryName,
        description = this.description,
        lastRunAt = this.lastRunAt,
        failedCount = this.failedCount
    )
}