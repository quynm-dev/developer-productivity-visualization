package com.dpv.data.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class UserEntity(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(Users)

    var username by Users.username
    var avatarUrl by Users.avatarUrl
    var githubUrl by Users.githubUrl
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
}

object Users: LongIdTable("users") {
    val username = varchar("username", 255).uniqueIndex()
    val avatarUrl = text("avatar_url")
    val githubUrl = text("github_url")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}