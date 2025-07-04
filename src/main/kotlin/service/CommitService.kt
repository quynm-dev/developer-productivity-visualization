package com.dpv.service

import com.dpv.data.dto.github.CommitDetailDto
import com.dpv.data.dto.github.CommitDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.err
import com.dpv.helper.ok
import com.dpv.repository.CommitRepository
import mu.KotlinLogging
import org.koin.core.annotation.Singleton

@Singleton
class CommitService(
    private val commitRepository: CommitRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun create(commitDto: CommitDto, userId: Long): UniResult<Long> {
        return commitRepository.create(commitDto, userId).ok()
    }

    suspend fun bulkCreate(commitDtos: List<CommitDetailDto>, repoId: Long): UniResult<Boolean> {
        return commitRepository.bulkCreate(commitDtos, repoId).ok()
    }

    suspend fun validateExistence(hash: String): UniResult<Boolean> {
        val exist = commitRepository.validateExistence(hash)
        if (!exist) {
            logger.warn("[CommitService:validateExistence] with hash: $hash does not exist")
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND, "Commit with hash: $hash does not exist").err()
        }

        return exist.ok()
    }

    suspend fun update(hash: String, commitDto: CommitDetailDto): UniResult<Boolean> {
        return commitRepository.update(hash, commitDto).ok()
    }
}