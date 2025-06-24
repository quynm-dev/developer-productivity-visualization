package com.dpv.data.model

import com.dpv.data.enum.JobStatus
import java.time.LocalDateTime

data class JobModel(
    val id: Int,
    val status: JobStatus,
    val repoName: String,
    val description: String? = null,
    val lastRunAt: LocalDateTime? = null,
    val failedCount: Int = 0
)