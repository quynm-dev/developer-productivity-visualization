package com.dpv.repository

import com.dpv.data.dto.github.ReleaseDto
import com.dpv.data.entity.ReleaseEntity
import com.dpv.data.entity.Releases
import com.dpv.data.entity.Releases.body
import com.dpv.data.entity.Releases.branch
import com.dpv.data.entity.Releases.draft
import com.dpv.data.entity.Releases.githubCreatedAt
import com.dpv.data.entity.Releases.githubPublishedAt
import com.dpv.data.entity.Releases.name
import com.dpv.data.entity.Releases.tagName
import mu.KotlinLogging
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime

@Singleton
class ReleaseRepository {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun create(releaseDto: ReleaseDto, repoId: Long): Long {
        return newSuspendedTransaction {
            logger.info { "[ReleaseRepository:create] with repoId: $repoId" }
            Releases.insert {
                it[githubUrl] = releaseDto.url
                it[Releases.repoId] = repoId
                it[tagName] = releaseDto.tagName
                it[branch] = releaseDto.branch
                it[name] = releaseDto.name
                it[body] = releaseDto.body
                it[draft] = releaseDto.draft
                it[githubPublishedAt] = releaseDto.publishedAt
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }[Releases.id].value
        }
    }

    suspend fun validateExistence(id: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[ReleaseRepository:validateExistence] with id: $id" }
            ReleaseEntity.findById(id) != null
        }
    }

    suspend fun update(id: Long, releaseDto: ReleaseDto): Boolean {
        return newSuspendedTransaction {
            logger.info { "[ReleaseRepository:update]" }
            Releases.update({ Releases.id eq id }) {
                it[githubUrl] = releaseDto.url
                it[tagName] = releaseDto.tagName
                it[branch] = releaseDto.branch
                it[name] = releaseDto.name
                it[body] = releaseDto.body
                it[draft] = releaseDto.draft
                it[githubPublishedAt] = releaseDto.publishedAt
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            } > 0
        }
    }

    suspend fun bulkCreate(releaseDtos: List<ReleaseDto>, repoId: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[MilestoneRepository:bulkCreate]" }
            Releases.batchInsert(releaseDtos) { releaseDto ->
                this[Releases.githubUrl] = releaseDto.url
                this[Releases.repoId] = repoId
                this[tagName] = releaseDto.tagName
                this[branch] = releaseDto.branch
                this[name] = releaseDto.name
                this[body] = releaseDto.body
                this[draft] = releaseDto.draft
                this[githubCreatedAt] = releaseDto.createdAt
                this[githubPublishedAt] = releaseDto.publishedAt
                this[Releases.createdAt] = LocalDateTime.now()
                this[Releases.updatedAt] = LocalDateTime.now()
            }.isNotEmpty()
        }
    }
}