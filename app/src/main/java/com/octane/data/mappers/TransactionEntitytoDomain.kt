package com.octane.data.mappers

import com.octane.data.local.database.entities.TransactionEntity
import com.octane.domain.models.Transaction

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    walletId = walletId,
    chainId = chainId,
    txHash = txHash,
    type = type.toDomain(),
    status = status.toDomain(),
    fromAddress = fromAddress,
    toAddress = toAddress,
    amount = amount,
    tokenSymbol = tokenSymbol,
    tokenMint = tokenMint,
    fee = fee,
    feePriority = feePriority,
    blockNumber = blockNumber,
    confirmationCount = confirmationCount,
    errorMessage = errorMessage,
    memo = memo,
    timestamp = timestamp,
    simulated = simulated,
    simulationSuccess = simulationSuccess
)
