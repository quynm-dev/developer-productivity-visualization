package com.dpv.repository

import com.dpv.data.entity.JobEntity
import com.dpv.data.entity.Jobs
import com.dpv.data.enum.JobStatus
import com.dpv.data.model.JobModel
import com.dpv.mapper.toModel
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime

@Singleton
class JobRepository {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun findAll(): List<JobModel> {
        return newSuspendedTransaction {
            logger.info { "[JobRepository:findAll]" }
            Jobs.selectAll().map { JobEntity.wrapRow(it).toModel() }
        }
    }

    suspend fun create(repoName: String): Int {
        return newSuspendedTransaction {
            logger.info { "[JobRepository:create] with repoName: $repoName" }
            Jobs.insert {
                it[repositoryName] = repoName
                it[status] = JobStatus.IN_PROGRESS
                it[description] = null
                it[lastRunAt] = LocalDateTime.now()
                it[failedCount] = 0
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }[Jobs.id].value
        }
    }

    suspend fun update(
        id: Int,
        description: String? = null,
        lastRunAt: LocalDateTime,
        failedCount: Int
    ) {
        return newSuspendedTransaction {
            logger.info { "[JobRepository:update] with id: $id" }
            Jobs.update({ Jobs.id eq id }) {
                it[this.description] = description
                it[this.lastRunAt] = lastRunAt
                it[this.failedCount] = failedCount
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }

    suspend fun delete(id: Int) {
        return newSuspendedTransaction {
            logger.info { "[JobRepository:delete] with id: $id" }
            Jobs.deleteWhere { Jobs.id eq id }
        }
    }
}