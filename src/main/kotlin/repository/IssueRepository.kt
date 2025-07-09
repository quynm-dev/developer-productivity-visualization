package com.dpv.repository

import com.dpv.data.dto.github.IssueDto
import com.dpv.data.entity.IssueEntity
import com.dpv.data.entity.IssueUsers
import com.dpv.data.entity.IssueUsers.issueId
import com.dpv.data.entity.IssueUsers.userId
import com.dpv.data.entity.Issues
import com.dpv.data.entity.Issues.body
import com.dpv.data.entity.Issues.closedAt
import com.dpv.data.entity.Issues.createdAt
import com.dpv.data.entity.Issues.githubUrl
import com.dpv.data.entity.Issues.milestoneId
import com.dpv.data.entity.Issues.state
import com.dpv.data.entity.Issues.title
import com.dpv.data.entity.Issues.updatedAt
import com.dpv.data.enum.GithubState
import mu.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime

@Singleton
class IssueRepository {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun create(issueDto: IssueDto, repoId: Long): Long {
        return newSuspendedTransaction {
            logger.info { "[IssueRepository:create] with repoId: $repoId" }
            Issues.insert {
                it[githubUrl] = issueDto.url
                it[Issues.repoId] = repoId
                it[milestoneId] = issueDto.milestone?.id
                it[state] = GithubState.fromString(issueDto.state)
                it[title] = issueDto.title
                it[body] = issueDto.body
                it[closedAt] = issueDto.closedAt
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }[Issues.id].value
        }
    }

    suspend fun validateExistence(id: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[IssueRepository:validateExistence] with id: $id" }
            IssueEntity.findById(id) != null
        }
    }

    suspend fun update(id: Long, issueDto: IssueDto): Boolean {
        return newSuspendedTransaction {
            logger.info { "[IssueRepository:update]" }
            Issues.update({ Issues.id eq id }) {
                it[githubUrl] = issueDto.url
                it[milestoneId] = issueDto.milestone?.id
                it[state] = GithubState.fromString(issueDto.state)
                it[title] = issueDto.title
                it[body] = issueDto.body
                it[closedAt] = issueDto.closedAt
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            } > 0
        }
    }

    suspend fun bulkCreate(issueDtos: List<IssueDto>, repoId: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[IssueRepository:bulkCreate]" }
            Issues.batchInsert(issueDtos) { issueDto ->
                this[githubUrl] = issueDto.url
                this[Issues.repoId] = repoId
                this[milestoneId] = issueDto.milestone?.id
                this[state] = GithubState.fromString(issueDto.state)
                this[title] = issueDto.title
                this[body] = issueDto.body
                this[closedAt] = issueDto.closedAt
                this[createdAt] = LocalDateTime.now()
                this[updatedAt] = LocalDateTime.now()
            }.isNotEmpty()

            // TODO: Verify issue user creation
            issueDtos.forEach { issueDto ->
                IssueUsers.batchInsert(issueDto.assignees) { user ->
                    this[issueId] = issueDto.id
                    this[userId] = user.id
                    this[createdAt] = LocalDateTime.now()
                    this[updatedAt] = LocalDateTime.now()
                }
            }

            true
        }
    }
}