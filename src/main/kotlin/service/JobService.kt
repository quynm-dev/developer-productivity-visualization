package com.dpv.service

import com.dpv.data.model.JobModel
import com.dpv.helper.UniResult
import com.dpv.helper.ok
import com.dpv.repository.JobRepository
import mu.KotlinLogging
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime

@Singleton
class JobService(
    private val jobRepository: JobRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun findAll(): UniResult<List<JobModel>> {
        logger.info { "[JobService:findAll]" }
        return jobRepository.findAll().ok()
    }

    suspend fun create(repoName: String): UniResult<Int> {
        logger.info { "[JobService:create] with repoName: $repoName" }
        return jobRepository.create(repoName).ok()
    }

    suspend fun update(id: Int, description: String? = null, lastRunAt: LocalDateTime,failedCount: Int): UniResult<Unit> {
        logger.info { "[JobService:update] with id: $id" }
        return jobRepository.update(id, description, lastRunAt, failedCount).ok()
    }

    suspend fun delete(id: Int): UniResult<Unit> {
        logger.info { "[JobService:delete] with id: $id" }
        return jobRepository.delete(id).ok()
    }
}