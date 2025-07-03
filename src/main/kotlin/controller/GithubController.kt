package com.dpv.controller

import com.dpv.data.dto.RepositoriesOnboardingDto
import com.dpv.data.dto.RepositorySyncDto
import com.dpv.error.AppError
import com.dpv.error.GENERIC_ERROR_CODE_FACTORY
import com.dpv.helper.respondError
import com.dpv.service.github.GithubService
import com.github.michaelbull.result.mapBoth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.githubController() {
    val githubService by inject<GithubService>()

    route("/repositories") {
        post("/sync") {
            val syncDto = call.receive<RepositorySyncDto>()
            if (syncDto.repoName.isNullOrEmpty()) {
                return@post call.respondError(AppError.new(GENERIC_ERROR_CODE_FACTORY.BAD_REQUEST, "repoName is invalid"))
            }

            githubService.sync(syncDto.repoName).mapBoth(
                success = { call.respond(HttpStatusCode.OK) },
                failure = { call.respondError(it) }
            )
        }

        post("/onboarding") {
            val onboardingDto = call.receive<RepositoriesOnboardingDto>()
            if (onboardingDto.repoNames.isNullOrEmpty() || onboardingDto.pat.isNullOrEmpty()) {
                return@post call.respondError(AppError.new(GENERIC_ERROR_CODE_FACTORY.BAD_REQUEST, "repoName or pat is invalid"))
            }

            githubService.onboarding(onboardingDto.pat, onboardingDto.repoNames).mapBoth(
                success = { call.respond(HttpStatusCode.Created) },
                failure = { call.respondError(it) }
            )
        }
    }
}