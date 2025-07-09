package com.dpv.data.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class BranchEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<BranchEntity>(Branches)

    var name by Branches.name
    var repoId by Branches.repoId
    var createdAt by Branches.createdAt
    var updatedAt by Branches.updatedAt
}

object Branches: IntIdTable("branches") {
    val name = text("name")
    val repoId = reference("repo_id", Repositories)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}