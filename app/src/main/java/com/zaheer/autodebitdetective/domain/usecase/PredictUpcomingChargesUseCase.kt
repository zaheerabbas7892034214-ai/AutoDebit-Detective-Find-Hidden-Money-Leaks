package com.zaheer.autodebitdetective.domain.usecase

import com.zaheer.autodebitdetective.domain.model.RecurringItem
import com.zaheer.autodebitdetective.domain.model.UpcomingCharge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PredictUpcomingChargesUseCase {

    suspend operator fun invoke(recurringItems: List<RecurringItem>, daysAhead: Int = 30): Result<List<UpcomingCharge>> = withContext(Dispatchers.Default) {
        try {
            val currentTime = System.currentTimeMillis()
            val cutoffTime = currentTime + TimeUnit.DAYS.toMillis(daysAhead.toLong())
            val upcomingCharges = mutableListOf<UpcomingCharge>()

            for (item in recurringItems) {
                if (item.nextPredictedEpoch in currentTime..cutoffTime) {
                    val daysUntil = TimeUnit.MILLISECONDS.toDays(item.nextPredictedEpoch - currentTime).toInt()
                    
                    val upcomingCharge = UpcomingCharge(
                        merchant = item.merchant,
                        amount = item.avgAmount,
                        predictedDate = item.nextPredictedEpoch,
                        cadenceType = item.cadenceType,
                        category = item.category,
                        daysUntilCharge = daysUntil,
                        recurringItemId = item.id
                    )
                    upcomingCharges.add(upcomingCharge)
                }
            }

            val sortedCharges = upcomingCharges.sortedBy { it.predictedDate }
            Result.success(sortedCharges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun calculateTotalUpcoming(recurringItems: List<RecurringItem>, daysAhead: Int = 30): Result<Double> = withContext(Dispatchers.Default) {
        try {
            val upcomingResult = invoke(recurringItems, daysAhead)
            val upcoming = upcomingResult.getOrNull() ?: emptyList()
            val total = upcoming.sumOf { it.amount }
            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
