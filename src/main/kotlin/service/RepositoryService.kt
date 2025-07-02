package com.dpv.service

import com.dpv.data.dto.github.RepositoryDto
import com.dpv.data.model.RepositoryModel
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.err
import com.dpv.helper.ok
import com.dpv.repository.RepositoryRepository
import mu.KotlinLogging
import org.koin.core.annotation.Singleton

@Singleton
class RepositoryService(
    private val repoRepository: RepositoryRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun findAll(): UniResult<List<RepositoryModel>> {
        logger.info { "[RepositoryService:findAll]" }
        return repoRepository.findAll().ok()
    }

    suspend fun findById(id: Long): UniResult<RepositoryModel> {
        val repo = repoRepository.findById(id)
        if (repo == null) {
            logger.warn("[RepositoryService:findById] with id: $id not found")
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND, "Repository with id $id not found").err()
        }

        return repo.ok()
    }

    suspend fun findByName(name: String): UniResult<RepositoryModel> {
        logger.info { "[RepositoryService:findByName] with name: $name" }
        val repo = repoRepository.findByName(name)
        if (repo == null) {
            logger.warn("[RepositoryService:findByName] Repo with name: $name not found")
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND, "Repository with name $name not found").err()
        }

        return repo.ok()
    }

    suspend fun create(repoDto: RepositoryDto): UniResult<Long> {
        return repoRepository.create(repoDto).ok()
    }

    suspend fun update(repoModel: RepositoryModel): UniResult<Boolean> {
        return repoRepository.update(repoModel).ok()
    }
}