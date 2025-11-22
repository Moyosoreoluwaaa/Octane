// data/mappers/StakingMappers.kt

package com.octane.data.mappers

import com.octane.data.local.database.entities.StakingPositionEntity
import com.octane.domain.models.StakingPosition

fun StakingPositionEntity.toDomain(): StakingPosition = StakingPosition(
    id = id,
    walletId = walletId,
    chainId = chainId,
    validatorAddress = validatorAddress,
    validatorName = validatorName,
    amountStaked = amountStaked,
    rewardsEarned = rewardsEarned,
    apy = apy,
    isActive = isActive,
    stakedAt = stakedAt,
    unstakedAt = unstakedAt
)

fun StakingPosition.toEntity(): StakingPositionEntity = StakingPositionEntity(
    id = id,
    walletId = walletId,
    chainId = chainId,
    validatorAddress = validatorAddress,
    validatorName = validatorName,
    amountStaked = amountStaked,
    rewardsEarned = rewardsEarned,
    apy = apy,
    isActive = isActive,
    stakedAt = stakedAt,
    unstakedAt = unstakedAt
)