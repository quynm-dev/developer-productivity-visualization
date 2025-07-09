package com.dpv.service.github

import com.dpv.data.dto.github.*
import com.dpv.data.model.RepositoryModel
import com.dpv.error.AppError
import com.dpv.error.GITHUB_ERROR_CODE_FACTORY
import com.dpv.helper.*
import com.dpv.service.*
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
    private val releaseService: ReleaseService,
    private val issueService: IssueService,
    private val milestoneService: MilestoneService,
    private val branchService: BranchService,
    private val githubRepoService: GithubRepositoryService,
    private val githubCommitService: GithubCommitService,
    private val githubUserService: GithubUserService,
    private val githubPullService: GithubPullService,
    private val githubReleaseService: GithubReleaseService,
    private val githubMilestoneService: GithubMilestoneService,
    private val githubIssueService: GithubIssueService,
    private val githubBranchService: GithubBranchService
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

        val (since, until) = if(repo.lastSyncAt != null) getSyncTimeFrame() else Pair(null, null)
        val githubResources = syncGithubResources(repo, since, until).getOrElse { syncGithubResourcesErr ->
            return syncGithubResourcesErr.err()
        }

        syncResourcesWithDB(repo, githubResources).getOrElse { syncResourcesWithDBErr ->
            return syncResourcesWithDBErr.err()
        }

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
            syncRepoResourcesWithDB(pat, repoName).getOrElse { syncAndCreateRepoErr ->
                return syncAndCreateRepoErr.err()
            }
        }

        return Unit.ok()
    }

    suspend fun syncRepoResourcesWithDB(pat: String, name: String): UniResult<Unit> {
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

        repoService.create(pat, repo).getOrElse { createRepoErr ->
            return createRepoErr.err()
        }

        sync(name).getOrElse { syncErr ->
            return syncErr.err()
        }

        return Unit.ok()
    }

    suspend fun syncGithubResources(repo: RepositoryModel, since: LocalDateTime? = null, until: LocalDateTime? = null): UniResult<GithubResourcesDto> {
        logger.info { "Sync branches" }
        val branches = syncGithubDatasWithPagination(
            apiCall = { page ->
                githubBranchService.getBranches(repo.pat, repo.branchesUrl, page = page)
            }
        ).getOrElse { syncBranchesErr ->
            return syncBranchesErr.err()
        }

        val commitDetails = mutableListOf<CommitDetailDto>()
        val mapBranchNamesCommitHashes = mutableMapOf<String, MutableList<String>>()
        val existedCommitHashes = mutableSetOf<String>()

        logger.info { "Sync commit details" }
        branches.forEach { branch ->
            val branchCommits = syncGithubDatasWithPagination(
                apiCall = { page ->
                    githubCommitService.getCommits(repo.pat, since, until, repo.commitsUrl, branch = branch.name, page = page)
                }
            ).getOrElse { syncCommitsErr ->
                return syncCommitsErr.err()
            }

            val branchCommitDetails = syncGithubCommitDetailsInBatch(repo.pat, branchCommits).getOrElse { syncGithubCommitDetailsInBatchErr ->
                return syncGithubCommitDetailsInBatchErr.err()
            }

            branchCommitDetails.forEach { commit ->
                if(!existedCommitHashes.contains(commit.sha)) {
                    commitDetails.add(commit)
                    existedCommitHashes.add(commit.sha)
                }
            }

            if (!mapBranchNamesCommitHashes.containsKey(branch.name)) {
                mapBranchNamesCommitHashes[branch.name] = mutableListOf()
            }
            mapBranchNamesCommitHashes[branch.name]?.addAll(branchCommitDetails.map { it.sha })
        }

        logger.info { "Sync pulls" }
        val pulls = syncGithubDatasWithPagination(
            apiCall = { page ->
                githubPullService.getPulls(repo.pat, repo.pullsUrl, null, page = page)
            }
        ).getOrElse { syncPullsErr ->
            return syncPullsErr.err()
        }

        logger.info { "Sync releases" }
        val releases = syncGithubDatasWithPagination(
            apiCall = { page ->
                githubReleaseService.getReleases(repo.pat, repo.releasesUrl, page = page)
            }
        ).getOrElse { syncReleasesErr ->
            return syncReleasesErr.err()
        }

        logger.info { "Sync milestones" }
        val milestones = syncGithubDatasWithPagination(
            apiCall = { page ->
                githubMilestoneService.getMilestones(repo.pat, repo.milestonesUrl, page = page)
            }
        ).getOrElse { syncMilestonesErr ->
            return syncMilestonesErr.err()
        }

        logger.info { "Sync issues" }
        val issues = syncGithubDatasWithPagination(
            apiCall = { page ->
                githubIssueService.getIssues(repo.pat, repo.issuesUrl, page = page)
            }
        ).getOrElse { syncIssuesErr ->
            return syncIssuesErr.err()
        }

        return GithubResourcesDto(
            pulls = pulls,
            commitDetails = commitDetails,
            issues = issues,
            releases = releases,
            milestones = milestones,
            branches = branches,
            mapBranchNamesCommitHashes = mapBranchNamesCommitHashes
        ).ok()
    }

    suspend fun syncResourcesWithDB(repo: RepositoryModel, githubResources: GithubResourcesDto): UniResult<Unit> {
        syncBranchesResourcesWithDB(githubResources.branches, repo.id).getOrElse { syncBranchesResourcesErr ->
            return syncBranchesResourcesErr.err()
        }
        syncCommitsResourcesWithDB(repo.pat, githubResources.commitDetails, repo.id, githubResources.mapBranchNamesCommitHashes).getOrElse { syncCommitsResourcesErr ->
            return syncCommitsResourcesErr.err()
        }
        syncPullsResourcesWithDB(repo.pat, githubResources.pulls, repo.id).getOrElse { syncPullsResourcesErr ->
            return syncPullsResourcesErr.err()
        }
        syncReleasesResourcesWithDB(githubResources.releases, repo.id).getOrElse { syncReleasesResourcesErr ->
            return syncReleasesResourcesErr.err()
        }
        syncMilestonesResourcesWithDB(githubResources.milestones, repo.id).getOrElse { syncMilestonesResourcesErr ->
            return syncMilestonesResourcesErr.err()
        }
        syncIssuesResourcesWithDB(githubResources.issues, repo.id).getOrElse { syncIssuesResourcesErr ->
            return syncIssuesResourcesErr.err()
        }

        return Unit.ok()
    }

    suspend fun syncBranchesResourcesWithDB(branches: List<BranchDto>, repoId: Long): UniResult<Unit> {
        val newBranches = mutableListOf<BranchDto>()
        branches.forEach { branch ->
            val exist = branchService.validateExistence(branch.name, repoId).getOrElse { validateExistenceErr ->
                if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                    return validateExistenceErr.err()
                }

                false
            }

            if(exist) {
                branchService.update(branch, repoId).getOrElse { updateErr ->
                    return updateErr.err()
                }
            } else {
                newBranches.add(branch)
            }
        }

        branchService.bulkCreate(newBranches, repoId).getOrElse { bulkCreateErr ->
            return bulkCreateErr.err()
        }

        return Unit.ok()
    }

    suspend fun syncCommitsResourcesWithDB(
        pat: String, commits: List<CommitDetailDto>, repoId: Long, mapBranchNamesCommitHashes: Map<String, List<String>>
    ): UniResult<Unit> {
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

                false
            }
            if(exist) {
                commitService.update(commit.sha, commit).getOrElse { createErr ->
                    return createErr.err()
                }
            } else {
                newCommits.add(commit)
            }
        }

        commitService.bulkCreate(newCommits, repoId, mapBranchNamesCommitHashes).getOrElse { bulkCreateErr ->
            return bulkCreateErr.err()
        }

        return Unit.ok()
    }

    suspend fun syncPullsResourcesWithDB(pat:String, pulls: List<PullDto>, repoId: Long): UniResult<Unit> {
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

                false
            }
            if(exist) {
                pullService.update(pull.id, pull).getOrElse { updateErr ->
                    return updateErr.err()
                }
            } else {
                newPulls.add(pull)
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

    suspend fun syncReleasesResourcesWithDB(releases: List<ReleaseDto>, repoId: Long): UniResult<Unit> {
        val newReleases = mutableListOf<ReleaseDto>()
        releases.forEach { release ->
            val exist = releaseService.validateExistence(release.id).getOrElse { validateExistenceErr ->
                if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                    return validateExistenceErr.err()
                }

                false
            }
            if(exist) {
                releaseService.update(release.id, release).getOrElse { updateErr ->
                    return updateErr.err()
                }
            } else {
                newReleases.add(release)
            }
        }

        releaseService.bulkCreate(newReleases, repoId).getOrElse { bulkCreateErr ->
            return bulkCreateErr.err()
        }

        return Unit.ok()
    }

    suspend fun syncIssuesResourcesWithDB(issues: List<IssueDto>, repoId: Long): UniResult<Unit> {
        val newIssues = mutableListOf<IssueDto>()
        issues.forEach { issue ->
            val exist = issueService.validateExistence(issue.id).getOrElse { validateExistenceErr ->
                if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                    return validateExistenceErr.err()
                }

                false
            }
            if(exist) {
                issueService.update(issue.id, issue).getOrElse { updateErr ->
                    return updateErr.err()
                }
            } else {
                newIssues.add(issue)
            }
        }

        issueService.bulkCreate(newIssues, repoId).getOrElse { bulkCreateErr ->
            return bulkCreateErr.err()
        }

        return Unit.ok()
    }

    suspend fun syncMilestonesResourcesWithDB(milestones: List<MilestoneDto>, repoId: Long): UniResult<Unit> {
        val newMilestones = mutableListOf<MilestoneDto>()
        milestones.forEach { milestone ->
            val exist = issueService.validateExistence(milestone.id).getOrElse { validateExistenceErr ->
                if (!validateExistenceErr.hasCode(GITHUB_ERROR_CODE_FACTORY.NOT_FOUND)) {
                    return validateExistenceErr.err()
                }

                false
            }
            if(exist) {
                milestoneService.update(milestone.id, milestone).getOrElse { updateErr ->
                    return updateErr.err()
                }
            } else {
                newMilestones.add(milestone)
            }
        }

        milestoneService.bulkCreate(newMilestones, repoId).getOrElse { bulkCreateErr ->
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
}