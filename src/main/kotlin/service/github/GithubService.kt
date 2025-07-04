package com.dpv.service.github

import com.dpv.data.dto.github.*
import com.dpv.data.model.RepositoryModel
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.*
import com.dpv.service.CommitService
import com.dpv.service.PullService
import com.dpv.service.RepositoryService
import com.dpv.service.UserService
import com.github.michaelbull.result.getOrElse
import io.ktor.server.application.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime

@Singleton
class GithubService(
    environment: ApplicationEnvironment,
    private val repoService: RepositoryService,
    private val commitService: CommitService,
    private val userService: UserService,
    private val pullService: PullService,
    private val githubRepoService: GithubRepositoryService,
    private val githubCommitService: GithubCommitService,
    private val githubUserService: GithubUserService,
    private val githubPullService: GithubPullService
) : GithubConfiguration(environment) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val BATCH_SIZE = 10
    }

    suspend fun sync(repoName: String): UniResult<Unit> {
        logger.info { "[GithubService:sync] Start" }
        val repo = repoService.findByName(repoName).getOrElse { findByNameErr ->
            if (!findByNameErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                return findByNameErr.err()
            }

            return@getOrElse null
        }
        if(repo == null) {
            return AppError.new(
                GITHUB_ERROR_CODE_FACTORY.NOT_FOUND,
                "Repository with name $repoName not found"
            ).err()
        }

        logger.info { "Sync commits" }
        val (since, until) = if(repo.lastSyncAt != null) getSyncTimeFrame() else Pair(null, null)
        val commits = syncGithubDatasWithPagination(
            apiCall = { page ->
                githubCommitService.getCommits(repo.pat, since, until, repo.commitsUrl, page = page)
            }
        ).getOrElse { syncCommitsErr ->
            return syncCommitsErr.err()
        }
        val commitDetails = syncGithubCommitDetailsInBatch(repo.pat, commits).getOrElse { syncGithubCommitDetailsInBatchErr ->
            return syncGithubCommitDetailsInBatchErr.err()
        }

        syncCommitsResources(repo.pat, commitDetails, repo.id).getOrElse { syncCommitsResourcesErr ->
            return syncCommitsResourcesErr.err()
        }

        logger.info { "Sync pulls" }
        val pulls = syncGithubDatasWithPagination(
            apiCall = { page ->
                githubPullService.getPulls(repo.pat, repo.pullsUrl, null, page = page)
            }
        ).getOrElse { syncPullsErr ->
            return syncPullsErr.err()
        }
        syncPullsResources(repo.pat, pulls, repo.id).getOrElse { syncPullsResourcesErr ->
            return syncPullsResourcesErr.err()
        }

        // sync release, issue and comment

        repoService.update(repo.copy(lastSyncAt = LocalDateTime.now())).getOrElse { updateErr ->
            return updateErr.err()
        }

        logger.info { "[GithubService:sync] End" }
        return Unit.ok()
    }

    suspend fun onboarding(pat: String, repoNames: List<String>): UniResult<Unit> {
        logger.info { "[GithubService:onboarding]" }
        repoNames.forEach { repoName ->
            val repo = repoService.findByName(repoName).getOrElse { findByNameErr ->
                if (!findByNameErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                    return findByNameErr.err()
                }

                return@getOrElse null
            }
            if(repo != null) {
                return AppError.new(
                    GITHUB_ERROR_CODE_FACTORY.ALREADY_EXIST,
                    "Repository with name $repoName already exists"
                ).err()
            }
        }

        repoNames.forEach { repoName ->
            syncRepoResources(pat, repoName).getOrElse { syncAndCreateRepoErr ->
                return syncAndCreateRepoErr.err()
            }
        }

        return Unit.ok()
    }

    suspend fun syncCommitsResources(pat: String, commits: List<CommitDetailDto>, repoId: Long): UniResult<Unit> {
        val existUserIds = mutableListOf<Long>()
        val newCommits = mutableListOf<CommitDetailDto>()
        commits.forEach { commit ->
            if(commit.author?.id != null && !existUserIds.contains(commit.author.id)) {
                userService.validateExistence(commit.author.id).getOrElse { validateExistenceErr ->
                    if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                        return validateExistenceErr.err()
                    }

                    val user = githubUserService.getUser(pat, commit.author.username).getOrElse { getUserErr ->
                        return getUserErr.err()
                    }

                    userService.create(user).getOrElse { createErr ->
                        return createErr.err()
                    }
                }

                existUserIds.add(commit.author.id)
            }

            val exist = commitService.validateExistence(commit.sha).getOrElse { validateExistenceErr ->
                if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                    return validateExistenceErr.err()
                }

                newCommits.add(commit)
            }
            if(exist) {
                commitService.update(commit.sha, commit).getOrElse { createErr ->
                    return createErr.err()
                }
            }
        }

        commitService.bulkCreate(newCommits, repoId).getOrElse { bulkCreateErr ->
            return bulkCreateErr.err()
        }

        return Unit.ok()
    }

    suspend fun syncPullsResources(pat:String, pulls: List<PullDto>, repoId: Long): UniResult<Unit> {
        val newUsers = mutableListOf<UserDto>()
        val newPulls = mutableListOf<PullDto>()
        val existUserIds = mutableListOf<Long>()
        pulls.forEach { pull ->
            if(!existUserIds.contains(pull.user.id)) {
                userService.validateExistence(pull.user.id).getOrElse { validateExistenceErr ->
                    if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                        return validateExistenceErr.err()
                    }

                    val user = githubUserService.getUser(pat, pull.user.username).getOrElse { getUserErr ->
                        return getUserErr.err()
                    }

                    newUsers.add(user)
                    existUserIds.add(user.id)
                }
            }

            val exist = pullService.validateExistence(pull.id).getOrElse { validateExistenceErr ->
                if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                    return validateExistenceErr.err()
                }

                newPulls.add(pull)
            }
            if(exist) {
                pullService.update(pull.id, pull).getOrElse { updateErr ->
                    return updateErr.err()
                }
            }
        }

        userService.bulkCreate(newUsers).getOrElse { bulkCreateErr ->
            return bulkCreateErr.err()
        }

        pullService.bulkCreate(newPulls, repoId).getOrElse { bulkCreateErr ->
            return bulkCreateErr.err()
        }

        return Unit.ok()
    }

    suspend fun <T> syncGithubDatasWithPagination(
        apiCall: suspend (page: Int) -> UniResult<List<T>>
    ): UniResult<List<T>> {
        val datas = mutableListOf<T>()

        coroutineScope {
            var iteration = 0
            var error: AppError? = null
            var hitLastPage = false

            while(!hitLastPage && error == null) {
                val batchResults = (1..BATCH_SIZE).map { batchIndex ->
                    async {
                        apiCall(batchIndex + iteration * BATCH_SIZE)
                    }
                }.awaitAll()
                iteration += 1

                batchResults.forEach { result ->
                    val data = result.getOrElse { err ->
                        error = err
                        return@coroutineScope
                    }
                    hitLastPage = hitLastPage || data.isEmpty()
                    datas.addAll(data)
                }
            }
        }

        return datas.ok()
    }

    suspend fun syncGithubCommitDetailsInBatch(pat: String, commits: List<CommitDto>): UniResult<List<CommitDetailDto>> {
        val commitDetails = mutableListOf<CommitDetailDto>()

        coroutineScope {
            var iteration = 0
            var total = commits.size
            var error: AppError? = null

            while(error == null && total > 0) {
                val coroutineCount = if (total < BATCH_SIZE) total else BATCH_SIZE
                val batchResults = (1..coroutineCount).map { batchIndex ->
                    async {
                        githubCommitService.getCommit(pat, commits[batchIndex + iteration * BATCH_SIZE - 1].url)
                    }
                }.awaitAll()
                iteration += 1
                total -= coroutineCount

                batchResults.forEach { result ->
                    val commitDetail = result.getOrElse { err ->
                        error = err
                        return@coroutineScope
                    }
                    commitDetails.add(commitDetail)
                }
            }
        }

        return commitDetails.ok()
    }

    suspend fun syncRepoResources(pat: String, name: String): UniResult<RepositoryModel> {
        val repo = githubRepoService.getRepo(pat, name).getOrElse { getRepoErr ->
            return getRepoErr.err()
        }

        userService.validateExistence(repo.owner.id).getOrElse { validateExistErr ->
            if (!validateExistErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                return validateExistErr.err()
            }

            userService.create(repo.owner).getOrElse { createUserErr ->
                return createUserErr.err()
            }
        }

        val repoId = repoService.create(pat, repo).getOrElse { createRepoErr ->
            return createRepoErr.err()
        }

        sync(name).getOrElse { syncErr ->
            return syncErr.err()
        }

        return repoService.findById(repoId).getOrElse { findByIdErr ->
            return findByIdErr.err()
        }.ok()
    }
}