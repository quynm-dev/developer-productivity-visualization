package com.dpv.repository

import com.dpv.data.dto.github.CommitDetailDto
import com.dpv.data.dto.github.CommitDto
import com.dpv.data.entity.CommitEntity
import com.dpv.data.entity.Commits
import mu.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime

@Singleton
class CommitRepository {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun create(commitDto: CommitDto, userId: Long): Long {
        return newSuspendedTransaction {
            logger.info { "[CommitRepository:create]" }
            Commits.insert {
                it[hash] = commitDto.sha
                it[this.userId] = userId
                it[githubUrl] = commitDto.url
                it[message] = commitDto.commit.message
                it[commitedAt] = commitDto.commit.author.date
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }[Commits.id].value
        }
    }

    suspend fun bulkCreate(commitDtos: List<CommitDetailDto>, repoId: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[CommitRepository:bulkCreate]" }
            Commits.batchInsert(commitDtos) { commitDto ->
                this[Commits.hash] = commitDto.sha
                this[Commits.userId] = commitDto.author?.id
                this[Commits.repoId] = repoId
                this[Commits.githubUrl] = commitDto.url
                this[Commits.message] = commitDto.commit.message
                this[Commits.commitedAt] = commitDto.commit.author.date
                this[Commits.total] = commitDto.stats.total
                this[Commits.additions] = commitDto.stats.additions
                this[Commits.deletions] = commitDto.stats.deletions
                this[Commits.createdAt] = LocalDateTime.now()
                this[Commits.updatedAt] = LocalDateTime.now()
            }.isNotEmpty()
        }
    }

    suspend fun validateExistence(hash: String): Boolean {
        return newSuspendedTransaction {
            logger.info { "[CommitRepository:validateExistence] with hash: $hash" }
            CommitEntity.find { Commits.hash eq hash }.firstOrNull() != null
        }
    }

    suspend fun update(hash: String, commitDto: CommitDetailDto): Boolean {
        return newSuspendedTransaction {
            logger.info { "[CommitRepository:update] with hash: $hash" }
            Commits.update({ Commits.hash eq hash }) {
                it[userId] = commitDto.author?.id
                it[githubUrl] = commitDto.url
                it[message] = commitDto.commit.message
                it[total] = commitDto.stats.total
                it[additions] = commitDto.stats.additions
                it[deletions] = commitDto.stats.deletions
                it[commitedAt] = commitDto.commit.author.date
                it[updatedAt] = LocalDateTime.now()
            } > 0
        }
    }
}