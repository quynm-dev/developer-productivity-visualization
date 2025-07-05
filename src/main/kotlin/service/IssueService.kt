package com.dpv.service

import com.dpv.data.dto.github.IssueDto
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.UniResult
import com.dpv.helper.err
import com.dpv.helper.ok
import com.dpv.repository.IssueRepository
import mu.KotlinLogging
import org.koin.core.annotation.Singleton

@Singleton
class IssueService(
    private val issueRepository: IssueRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun create(issueDto: IssueDto, repoId: Long): UniResult<Long> {
        logger.info { "[IssueService:create] with repoId: $repoId" }
        return issueRepository.create(issueDto, repoId).ok()
    }

    suspend fun validateExistence(id: Long): UniResult<Boolean> {
        val exist = issueRepository.validateExistence(id)
        if (!exist) {
            logger.warn("[IssueService:validateExistence] Issue with id: $id does not exist")
            return AppError.new(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND, "Issue with id: $id does not exist").err()
        }

        return exist.ok()
    }

    suspend fun update(id: Long, issueDto: IssueDto): UniResult<Boolean> {
        return issueRepository.update(id, issueDto).ok()
    }

    suspend fun bulkCreate(issueDtos: List<IssueDto>, repoId: Long): UniResult<Boolean> {
        logger.info { "[IssueService:bulkCreate] with repoId: $repoId" }
        return issueRepository.bulkCreate(issueDtos, repoId).ok()
    }
}