package com.dpv.service.github

import com.dpv.data.dto.github.CommitDto
import com.dpv.data.dto.github.PullDto
import com.dpv.data.dto.github.UserDto
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
        val repo = getOrCreateRepoByName(repoName).getOrElse { getOrCreateRepoErr ->
            return getOrCreateRepoErr.err()
        }

        logger.info { "Sync commits" }
        val (since, until) = if(repo.lastSyncAt != null) getSyncTimeFrame() else Pair(null, null)
        syncGithubDataInBatch(
            apiCall = { page ->
                githubCommitService.getCommits(since, until, repo.commitsUrl, page = page)
            },
            syncWithDB = ::syncCommitsWithDB
        ).getOrElse { syncErr ->
            return syncErr.err()
        }.ok()

        logger.info { "Sync pulls" }
        syncGithubDataInBatch(
            apiCall = { page ->
                githubPullService.getPulls(repo.pullsUrl, null, page = page)
            },
            syncWithDB = ::syncPullsWithDB
        ).getOrElse { syncPullsErr ->
            return syncPullsErr.err()
        }.ok()

        repoService.update(repo.copy(lastSyncAt = LocalDateTime.now())).getOrElse { updateErr ->
            return updateErr.err()
        }

        logger.info { "[GithubService:sync] End" }
        return Unit.ok()
    }

    suspend fun syncCommitsWithDB(commits: List<CommitDto>): UniResult<Unit> {
        val existUserIds = mutableListOf<Long>()
        val newCommits = mutableListOf<CommitDto>()
        commits.forEach { commit ->
            if(commit.author?.id != null && !existUserIds.contains(commit.author.id)) {
                userService.validateExistence(commit.author.id).getOrElse { validateExistenceErr ->
                    if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                        return validateExistenceErr.err()
                    }

                    val user = githubUserService.getUser(commit.author.username).getOrElse { getUserErr ->
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

        commitService.bulkCreate(newCommits).getOrElse { bulkCreateErr ->
            return bulkCreateErr.err()
        }

        return Unit.ok()
    }

    suspend fun syncPullsWithDB(pulls: List<PullDto>): UniResult<Unit> {
        val newUsers = mutableListOf<UserDto>()
        val newPulls = mutableListOf<PullDto>()
        val existUserIds = mutableListOf<Long>()
        pulls.forEach { pull ->
            if(!existUserIds.contains(pull.user.id)) {
                userService.validateExistence(pull.user.id).getOrElse { validateExistenceErr ->
                    if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                        return validateExistenceErr.err()
                    }

                    val user = githubUserService.getUser(pull.user.username).getOrElse { getUserErr ->
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

        pullService.bulkCreate(newPulls).getOrElse { bulkCreateErr ->
            return bulkCreateErr.err()
        }

        return Unit.ok()
    }

    suspend fun <T> syncGithubDataInBatch(
        apiCall: suspend (page: Int) -> UniResult<List<T>>,
        syncWithDB: suspend (datas: List<T>) -> UniResult<Unit>,
    ): UniResult<Unit> {
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

        return syncWithDB(datas)
    }

    suspend fun getOrCreateRepoByName(name: String): UniResult<RepositoryModel> {
        return repoService.findByName(name).getOrElse { findByNameErr ->
            if (!findByNameErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                return findByNameErr.err()
            }

            val repo = githubRepoService.getRepo(name).getOrElse { getRepoErr ->
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

            val repoId = repoService.create(repo).getOrElse { createRepoErr ->
                return createRepoErr.err()
            }

            repoService.findById(repoId).getOrElse { findByIdErr ->
                return findByIdErr.err()
            }
        }.ok()
    }
}