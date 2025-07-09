package com.dpv.repository

import com.dpv.data.dto.github.BranchDto
import com.dpv.data.entity.BranchEntity
import com.dpv.data.entity.Branches
import com.dpv.data.model.BranchModel
import com.dpv.mapper.toModel
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Singleton
import java.time.LocalDateTime

@Singleton
class BranchRepository {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun findAllByRepoId(repoId: Long): List<BranchModel> {
        return newSuspendedTransaction {
            logger.info { "[BranchRepository:findAll]" }
            BranchEntity.find { Branches.repoId eq repoId }.map { it.toModel() }
        }
    }

    suspend fun create(branchDto: BranchDto, repoId: Long): Int {
        return newSuspendedTransaction {
            logger.info { "[BranchRepository:create] with repoId: $repoId" }
            Branches.insert {
                it[name] = branchDto.name
                it[this.repoId] = repoId
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }[Branches.id].value
        }
    }

    suspend fun bulkCreate(branchDtos: List<BranchDto>, repoId: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[BranchRepository:bulkCreate] with repoId: $repoId" }
            Branches.batchInsert(branchDtos) { branchDto ->
                this[Branches.name] = branchDto.name
                this[Branches.repoId] = repoId
                this[Branches.createdAt] = LocalDateTime.now()
                this[Branches.updatedAt] = LocalDateTime.now()
            }.isNotEmpty()
        }
    }

    suspend fun update(branch: BranchDto, repoId: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[BranchRepository:update] with id: $id" }
            Branches.update({ Branches.name eq branch.name and (Branches.repoId eq repoId) }) {
                it[name] = branch.name
                it[updatedAt] = LocalDateTime.now()
            } > 0
        }
    }

    suspend fun findById(id: Int): BranchModel? {
        return newSuspendedTransaction {
            logger.info { "[BranchRepository:findById] with id: $id" }
            BranchEntity.findById(id)?.toModel()
        }
    }

    suspend fun delete(id: Int): Boolean {
        return newSuspendedTransaction {
            logger.info { "[BranchRepository:delete] with id: $id" }
            Branches.deleteWhere { Branches.id eq id } > 0
        }
    }

    suspend fun validateExistence(name: String, repoId: Long): Boolean {
        return newSuspendedTransaction {
            logger.info { "[BranchRepository:validateExistence]" }
            BranchEntity.find { Branches.name eq name and (Branches.repoId eq repoId) }.firstOrNull() != null
        }
    }

    suspend fun findByName(name: String, repoId: Long): BranchModel? {
        return newSuspendedTransaction {
            logger.info { "[BranchRepository:findByName] with name: $name and repoId: $repoId" }
            BranchEntity.find { Branches.name eq name and (Branches.repoId eq repoId) }.singleOrNull()?.toModel()
        }
    }
}