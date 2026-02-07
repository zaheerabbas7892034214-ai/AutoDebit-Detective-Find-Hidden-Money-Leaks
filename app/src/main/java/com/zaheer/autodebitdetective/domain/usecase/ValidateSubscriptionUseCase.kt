package com.zaheer.autodebitdetective.domain.usecase

import com.zaheer.autodebitdetective.data.local.dao.EntitlementDao
import com.zaheer.autodebitdetective.data.local.entity.EntitlementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ValidateSubscriptionUseCase(
    private val entitlementDao: EntitlementDao
) {

    suspend operator fun invoke(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val entitlement = entitlementDao.getEntitlementSync()
            
            if (entitlement == null) {
                val newEntitlement = EntitlementEntity(
                    id = 1,
                    isProCached = false,
                    lastCheckEpoch = System.currentTimeMillis(),
                    lastState = "free"
                )
                entitlementDao.insertEntitlement(newEntitlement)
                return@withContext Result.success(false)
            }

            val currentTime = System.currentTimeMillis()
            val hoursSinceLastCheck = (currentTime - entitlement.lastCheckEpoch) / (1000 * 60 * 60)

            if (hoursSinceLastCheck > 24) {
                val updatedEntitlement = entitlement.copy(
                    lastCheckEpoch = currentTime
                )
                entitlementDao.updateEntitlement(updatedEntitlement)
            }

            Result.success(entitlement.isProCached)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProStatus(isPro: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entitlement = entitlementDao.getEntitlementSync()
            
            if (entitlement != null) {
                val updated = entitlement.copy(
                    isProCached = isPro,
                    lastCheckEpoch = System.currentTimeMillis(),
                    lastState = if (isPro) "pro" else "free"
                )
                entitlementDao.updateEntitlement(updated)
            } else {
                val newEntitlement = EntitlementEntity(
                    id = 1,
                    isProCached = isPro,
                    lastCheckEpoch = System.currentTimeMillis(),
                    lastState = if (isPro) "pro" else "free"
                )
                entitlementDao.insertEntitlement(newEntitlement)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
