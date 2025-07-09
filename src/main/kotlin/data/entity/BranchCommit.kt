package com.dpv.data.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class BranchCommitEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<BranchCommitEntity>(BranchCommits)

    var repoId by BranchCommits.repoId
    var branchName by BranchCommits.branchName
    var commitHash by BranchCommits.commitHash
    var createdAt by BranchCommits.createdAt
    var updatedAt by BranchCommits.updatedAt
}

object BranchCommits: IntIdTable("branch_commits") {
    val repoId = reference("repo_id", Repositories)
    val branchName = text("branch_name")
    val commitHash = reference("commit_hash", Commits.hash)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}