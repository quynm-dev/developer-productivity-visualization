package com.dpv.mapper

import com.dpv.data.entity.UserEntity
import com.dpv.data.model.UserModel

fun UserEntity.toModel(): UserModel {
    return UserModel(
        id = this.id.value,
        username = this.username,
        avatarUrl = this.avatarUrl,
        githubId = this.id.value,
        githubUrl = this.githubUrl,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString()
    )
}