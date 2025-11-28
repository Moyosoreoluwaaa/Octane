package com.octane.browser.data.mappers

import com.octane.browser.data.local.db.entities.ConnectionEntity
import com.octane.browser.domain.models.DAppConnection

fun ConnectionEntity.toDomain(): DAppConnection = DAppConnection(
    id = id,
    domain = domain,
    chainId = chainId,
    connectedAddress = connectedAddress,
    connectedAt = connectedAt,
    lastUsedAt = lastUsedAt,
    permissions = permissions.split(",").filter { it.isNotBlank() }
)

fun DAppConnection.toEntity(): ConnectionEntity = ConnectionEntity(
    id = id,
    domain = domain,
    chainId = chainId,
    connectedAddress = connectedAddress,
    connectedAt = connectedAt,
    lastUsedAt = lastUsedAt,
    permissions = permissions.joinToString(",")
)

