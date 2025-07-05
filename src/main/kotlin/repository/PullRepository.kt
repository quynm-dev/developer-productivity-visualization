package com.dpv.repository

import com.dpv.data.dto.github.PullDto
import com.dpv.data.entity.PullEntity
import com.dpv.data.entity.Pulls
import com.dpv.data.entity.Pulls.closedAt
import com.dpv.data.entity.Pulls.createdAt
import com.dpv.data.entity.Pulls.githubCreatedAt
import com.dpv.data.entity.Pulls.githubUpdatedAt
import com.dpv.data.entity.Pulls.githubUrl
import com.dpv.data.entity.Pulls.mergedAt
import com.dpv.data.entity.Pulls.state
import com.dpv.data.entity.Pulls.title
import com.dpv.data.entity.Pulls.updatedAt
import com.dpv.data.entity.Pulls.userId
import com.dpv.data.enum.GithubState
import mu.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime

@Singleton
class PullRepository {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    suspend fun create(pullDto: PullDto): Long {
        return newSuspendedTransaction {
            logger.info { "[PullRepository:create]" }
            Pulls.insert {
                it[id] = pullDto.id
                it[githubUrl] = pullDto.url
                it[state] = GithubState.fromString(pullDto.state)
                it[title] = pullDto.title
                it[userId] = pullDto.user.id
                it[closedAt] = pullDto.closedAt
                it[mergedAt] = pullDto.mergedAt
                it[githubCreatedAt] = pullDto.createdAt
                it[githubUpdatedAt] = pullDto.updatedAt
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }[Pulls.id].value
        }
    }

    suspend fun bulkCreate(pullDtos: List<PullDto>, repoId: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[PullRepository:bulkCreate]" }
            Pulls.batchInsert(pullDtos) { pullDto ->
                this[Pulls.id] = pullDto.id
                this[githubUrl] = pullDto.url
                this[state] = GithubState.fromString(pullDto.state)
                this[title] = pullDto.title
                this[userId] = pullDto.user.id
                this[Pulls.repoId] = repoId
                this[closedAt] = pullDto.closedAt
                this[mergedAt] = pullDto.mergedAt
                this[githubCreatedAt] = pullDto.createdAt
                this[githubUpdatedAt] = pullDto.updatedAt
                this[createdAt] = LocalDateTime.now()
                this[updatedAt] = LocalDateTime.now()
            }.isNotEmpty()
        }
    }

    suspend fun validateExistence(id: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[PullRepository:validateExistence] with id: $id" }
            PullEntity.findById(id) != null
        }
    }

    suspend fun update(id: Long, pullDto: PullDto): Boolean {
        return newSuspendedTransaction {
            logger.info { "[PullRepository:update] with id: $id" }
            Pulls.update({ Pulls.id eq id }) {
                it[githubUrl] = pullDto.url
                it[state] = GithubState.fromString(pullDto.state)
                it[title] = pullDto.title
                it[userId] = pullDto.user.id
                it[closedAt] = pullDto.closedAt
                it[mergedAt] = pullDto.mergedAt
                it[githubCreatedAt] = pullDto.createdAt
                it[githubUpdatedAt] = pullDto.updatedAt
                it[updatedAt] = LocalDateTime.now()
            } > 0
        }
    }
}