package com.dpv.repository

import com.dpv.data.dto.github.MilestoneDto
import com.dpv.data.entity.MilestoneEntity
import com.dpv.data.entity.Milestones
import com.dpv.data.entity.Milestones.closedAt
import com.dpv.data.entity.Milestones.closedIssuesCount
import com.dpv.data.entity.Milestones.createdAt
import com.dpv.data.entity.Milestones.description
import com.dpv.data.entity.Milestones.dueOn
import com.dpv.data.entity.Milestones.githubCreatedAt
import com.dpv.data.entity.Milestones.githubUpdatedAt
import com.dpv.data.entity.Milestones.githubUrl
import com.dpv.data.entity.Milestones.openIssuesCount
import com.dpv.data.entity.Milestones.state
import com.dpv.data.entity.Milestones.title
import com.dpv.data.entity.Milestones.updatedAt
import com.dpv.data.entity.Milestones.userId
import com.dpv.data.enum.GithubState
import mu.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime

@Singleton
class MilestoneRepository {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun create(milestoneDto: MilestoneDto, repoId: Long): Long {
        return newSuspendedTransaction {
            logger.info { "[MilestoneRepository:create] with repoId: $repoId" }
            Milestones.insert {
                it[githubUrl] = milestoneDto.url
                it[state] = GithubState.fromString(milestoneDto.state)
                it[title] = milestoneDto.title
                it[description] = milestoneDto.description
                it[Milestones.repoId] = repoId
                it[openIssuesCount] = milestoneDto.openIssues
                it[closedIssuesCount] = milestoneDto.closedIssues
                it[closedAt] = milestoneDto.closedAt
                it[dueOn] = milestoneDto.dueOn
                it[githubCreatedAt] = milestoneDto.createdAt
                it[githubUpdatedAt] = milestoneDto.updatedAt
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }[Milestones.id].value
        }
    }

    suspend fun validateExistence(id: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[MilestoneRepository:validateExistence] with id: $id" }
            MilestoneEntity.findById(id) != null
        }
    }

    suspend fun update(id: Long, milestoneDto: MilestoneDto): Boolean {
        return newSuspendedTransaction {
            logger.info { "[MilestoneRepository:update]" }
            Milestones.update({ Milestones.id eq id }) {
                it[githubUrl] = milestoneDto.url
                it[state] = GithubState.fromString(milestoneDto.state)
                it[title] = milestoneDto.title
                it[description] = milestoneDto.description
                it[openIssuesCount] = milestoneDto.openIssues
                it[closedIssuesCount] = milestoneDto.closedIssues
                it[closedAt] = milestoneDto.closedAt
                it[dueOn] = milestoneDto.dueOn
                it[githubCreatedAt] = milestoneDto.createdAt
                it[githubUpdatedAt] = milestoneDto.updatedAt
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            } > 0
        }
    }

    suspend fun bulkCreate(milestoneDtos: List<MilestoneDto>, repoId: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[MilestoneRepository:bulkCreate]" }
            Milestones.batchInsert(milestoneDtos) { milestoneDto ->
                this[githubUrl] = milestoneDto.url
                this[state] = GithubState.fromString(milestoneDto.state)
                this[title] = milestoneDto.title
                this[description] = milestoneDto.description
                this[Milestones.repoId] = repoId
                this[userId] = milestoneDto.creator.id
                this[openIssuesCount] = milestoneDto.openIssues
                this[closedIssuesCount] = milestoneDto.closedIssues
                this[closedAt] = milestoneDto.closedAt
                this[dueOn] = milestoneDto.dueOn
                this[githubCreatedAt] = milestoneDto.createdAt
                this[githubUpdatedAt] = milestoneDto.updatedAt
                this[createdAt] = LocalDateTime.now()
                this[updatedAt] = LocalDateTime.now()
            }.isNotEmpty()
        }
    }
}