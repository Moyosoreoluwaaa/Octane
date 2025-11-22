package com.octane.data.mappers



import com.octane.data.local.database.entities.ApprovalEntity
import com.octane.domain.models.Approval

fun ApprovalEntity.toDomain(): Approval = Approval(
    id = id,
    walletId = walletId,
    chainId = chainId,
    tokenMint = tokenMint,
    tokenSymbol = tokenSymbol,
    spenderAddress = spenderAddress,
    spenderName = spenderName,
    allowance = allowance,
    isRevoked = isRevoked,
    approvedAt = approvedAt,
    revokedAt = revokedAt
)

fun Approval.toEntity(): ApprovalEntity = ApprovalEntity(
    id = id,
    walletId = walletId,
    chainId = chainId,
    tokenMint = tokenMint,
    tokenSymbol = tokenSymbol,
    spenderAddress = spenderAddress,
    spenderName = spenderName,
    allowance = allowance,
    isRevoked = isRevoked,
    approvedAt = approvedAt,
    revokedAt = revokedAt
)