package com.dpv.service

import com.dpv.data.dto.github.MilestoneDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.err
import com.dpv.helper.ok
import com.dpv.repository.MilestoneRepository
import mu.KotlinLogging
import org.koin.core.annotation.Singleton

@Singleton
class MilestoneService(
    private val milestoneRepository: MilestoneRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun create(milestoneDto: MilestoneDto, repoId: Long): UniResult<Long> {
        logger.info { "[MilestoneService:create] with repoId: $repoId" }
        return milestoneRepository.create(milestoneDto, repoId).ok()
    }

    suspend fun validateExistence(id: Long): UniResult<Boolean> {
        val exist = milestoneRepository.validateExistence(id)
        if (!exist) {
            logger.warn("[MilestoneService:validateExistence] Milestone with id: $id does not exist")
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND, "Milestone with id: $id does not exist").err()
        }

        return exist.ok()
    }

    suspend fun update(id: Long, milestoneDto: MilestoneDto): UniResult<Boolean> {
        return milestoneRepository.update(id, milestoneDto).ok()
    }

    suspend fun bulkCreate(milestoneDtos: List<MilestoneDto>, repoId: Long): UniResult<Boolean> {
        logger.info { "[MilestoneService:bulkCreate] with repoId: $repoId" }
        return milestoneRepository.bulkCreate(milestoneDtos, repoId).ok()
    }
}