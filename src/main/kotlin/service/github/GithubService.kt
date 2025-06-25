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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    }

    suspend fun sync(repoName: String): UniResult<Unit> {
        logger.info { "[GithubService:sync] Start" }
        val repo = getOrCreateRepoByName(repoName).getOrElse { getOrCreateRepoErr ->
            return getOrCreateRepoErr.err()
        }

        logger.info { "Sync commits" }
        val (since, until) = if(repo.lastSyncAt != null) getSyncTimeFrame() else Pair(null, null)
        syncCommits(since, until, repo.commitsUrl).getOrElse { syncCommitsErr ->
            return syncCommitsErr.err()
        }

        logger.info { "Sync pulls" }
        syncPulls(repo.pullsUrl).getOrElse { syncPullsErr ->
            return syncPullsErr.err()
        }

        logger.info { "[GithubService:sync] End" }
        return Unit.ok()
    }

    suspend fun syncCommits(since: LocalDateTime? = null, until: LocalDateTime? = null, commitsUrl: String): UniResult<Unit> {
        var page = 1
        val perPage = 30
        while(true) {
            val commits = githubCommitService.getCommits(since, until, commitsUrl, perPage, page).getOrElse { getCommitsErr ->
                return getCommitsErr.err()
            }

            if(commits.isEmpty()) {
                logger.info { "[GithubService:syncCommits] Finished" }
                return Unit.ok()
            }

            CoroutineScope(Dispatchers.IO).launch {
                syncCommitsPerPage(commits).getOrElse { syncCommitsWithDBErr ->
                    logger.error { "[GithubService:syncCommits] Error syncing commits: ${syncCommitsWithDBErr.message}" }
                    // handle coroutine error
                }
            }

            page += 1
        }
    }

    suspend fun syncCommitsPerPage(commits: List<CommitDto>): UniResult<Unit> {
        val existUserIds = mutableListOf<Long>()
        val newCommits = mutableListOf<CommitDto>()
        commits.forEach { commit ->
            if(!existUserIds.contains(commit.author.id)) {
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

    suspend fun syncPulls(pullsUrl: String): UniResult<Unit> {
        var page = 1
        val perPage = 30
        while(true) {
            val pulls = githubPullService.getPulls(pullsUrl, null, perPage, page).getOrElse {
                return AppError.new(GITHUB_ERROR_CODE_FACTORY.INTERNAL_SERVER_ERROR, "Failed to get pulls").err()
            }

            if(pulls.isEmpty()) {
                logger.info { "[GithubService:syncPulls] Finished" }
                return Unit.ok()
            }

            CoroutineScope(Dispatchers.IO).launch {
                syncPullsPerPage(pulls).getOrElse { syncPullsWithDBErr ->
                    logger.error { "[GithubService:syncPulls] Error syncing pulls: ${syncPullsWithDBErr.message}" }
                    // handle coroutine error
                }
            }

            page += 1
        }
    }

    suspend fun syncPullsPerPage(pulls: List<PullDto>): UniResult<Unit> {
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