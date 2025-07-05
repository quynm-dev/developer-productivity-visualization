package com.dpv.service

import com.dpv.data.dto.github.ReleaseDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.err
import com.dpv.helper.ok
import com.dpv.repository.ReleaseRepository
import mu.KotlinLogging
import org.koin.core.annotation.Singleton

@Singleton
class ReleaseService(
    private val releaseRepository: ReleaseRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun create(releaseDto: ReleaseDto, userId: Long): UniResult<Long> {
        return releaseRepository.create(releaseDto, userId).ok()
    }

    suspend fun validateExistence(id: Long): UniResult<Boolean> {
        val exist = releaseRepository.validateExistence(id)
        if (!exist) {
            logger.warn("[ReleaseService:validateExistence] Release with id: $id does not exist")
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND, "Release with id: $id does not exist").err()
        }

        return exist.ok()
    }

    suspend fun update(id: Long, releaseDto: ReleaseDto): UniResult<Boolean> {
        return releaseRepository.update(id, releaseDto).ok()
    }

    suspend fun bulkCreate(releaseDtos: List<ReleaseDto>, repoId: Long): UniResult<Boolean> {
        logger.info { "[ReleaseService:bulkCreate] with repoId: $repoId" }
        return releaseRepository.bulkCreate(releaseDtos, repoId).ok()
    }
}