package com.dpv.service

import com.dpv.data.dto.github.BranchDto
import com.dpv.data.model.BranchModel
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.err
import com.dpv.helper.ok
import com.dpv.repository.BranchRepository
import mu.KotlinLogging
import org.koin.core.annotation.Singleton

@Singleton
class BranchService(
    private val branchRepository: BranchRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun findAllByRepoId(repoId: Long): UniResult<List<BranchModel>> {
        logger.info { "[BranchService:findAllByRepoId] with repoId: $repoId" }
        return branchRepository.findAllByRepoId(repoId).ok()
    }

    suspend fun create(branchDto: BranchDto, repoId: Long): UniResult<Int> {
        logger.info { "[BranchService:create] with repoId: $repoId" }
        return branchRepository.create(branchDto, repoId).ok()
    }

    suspend fun bulkCreate(branchDtos: List<BranchDto>, repoId: Long): UniResult<Boolean> {
        logger.info { "[BranchService:bulkCreate] with repoId: $repoId" }
        return branchRepository.bulkCreate(branchDtos, repoId).ok()
    }

    suspend fun update(branchDto: BranchDto, repoId: Long): UniResult<Boolean> {
        logger.info { "[BranchService:update]" }
        return branchRepository.update(branchDto, repoId).ok()
    }

    suspend fun delete(id: Int): UniResult<Boolean> {
        logger.info { "[BranchService:delete] with id: $id" }
        return branchRepository.delete(id).ok()
    }

    suspend fun validateExistence(name: String, repoId: Long): UniResult<Boolean> {
        logger.info { "[BranchService:validateExistence]" }
        return branchRepository.validateExistence(name, repoId).ok()
    }

    suspend fun findByName(name: String, repoId: Long): UniResult<BranchModel> {
        logger.info { "[BranchService:findByName] with name: $name and repoId: $repoId" }
        val branch = branchRepository.findByName(name, repoId)
        if (branch == null) {
            logger.warn("[BranchService:findByName] with name: $name not found")
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND, "Branch with name $name not found").err()
        }

        return branch.ok()
    }
}