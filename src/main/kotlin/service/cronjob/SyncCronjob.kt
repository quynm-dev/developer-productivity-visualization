package com.dpv.service.cronjob

import com.cronutils.model.Cron
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import com.dpv.config.ApplicationConfigurer
import com.dpv.data.enum.JobStatus
import com.dpv.helper.UniResult
import com.dpv.helper.err
import com.dpv.helper.getProperty
import com.dpv.helper.ok
import com.dpv.service.JobService
import com.dpv.service.RepositoryService
import com.dpv.service.github.GithubService
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapBoth
import io.ktor.server.application.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Singleton
class SyncCronjob(
    environment: ApplicationEnvironment,
    private val repoService: RepositoryService,
    private val githubService: GithubService,
    private val jobService: JobService
) : ApplicationConfigurer {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val expression = environment.getProperty("sync-cronjob.expression")

    override fun configure() {
        logger.info { "[SyncCronjob] Started" }
        CoroutineScope(Dispatchers.IO).launch {
            val cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
            val parser = CronParser(cronDefinition)
            val cron: Cron = parser.parse(expression)
            val executionTime = ExecutionTime.forCron(cron)

            while (true) {
                val now = ZonedDateTime.now()
                val nextExecution = executionTime.nextExecution(now).orElseThrow()
                val delayMillis = ChronoUnit.MILLIS.between(now, nextExecution)

                delay(delayMillis)

                retryFailedJobs()
                sync()
            }
        }
    }

    private suspend fun sync(): UniResult<Unit> {
        logger.info { "[SyncCronjob] Syncing" }
        val repos = repoService.findAll().getOrElse { findAllErr ->
            return findAllErr.err()
        }

        repos.forEach { repo ->
            githubService.sync(repo.name).mapBoth(
                success = {
                    logger.info { "[SyncCronjob] Synced repository: ${repo.name}" }
                },
                failure = {
                    jobService.create(repo.name).getOrElse { createJobErr ->
                        return createJobErr.err()
                    }
                }
            )
        }

        return Unit.ok()
    }

    private suspend fun retryFailedJobs(): UniResult<Unit> {
        logger.info { "[SyncCronjob] Retrying failed jobs" }
        val failedJobs = jobService.findFailedJobs().getOrElse { findFailedJobsErr ->
            return findFailedJobsErr.err()
        }

        failedJobs.forEach { job ->
            jobService.update(job.id, job.description, JobStatus.IN_PROGRESS, job.lastRunAt, job.failedCount).getOrElse { updateErr ->
                return updateErr.err()
            }

            val lastRunAt = LocalDateTime.now()
            githubService.sync(job.repoName).mapBoth(
                success = {
                    jobService.delete(job.id).getOrElse { deleteErr ->
                        return deleteErr.err()
                    }
                },
                failure = { syncErr ->
                    jobService.update(job.id, description = syncErr.message, JobStatus.FAILED, lastRunAt = lastRunAt, failedCount = job.failedCount + 1).getOrElse { updateErr ->
                        return updateErr.err()
                    }
                }
            )
        }

        return Unit.ok()
    }
}