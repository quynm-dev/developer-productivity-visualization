package com.dpv.mapper

import com.dpv.data.entity.BranchEntity
import com.dpv.data.model.BranchModel

fun BranchEntity.toModel(): BranchModel {
    return BranchModel(
        id = this.id.value,
        name = this.name,
        repoId = this.repoId.value,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}