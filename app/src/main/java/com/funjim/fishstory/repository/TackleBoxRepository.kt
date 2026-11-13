package com.funjim.fishstory.repository

import com.funjim.fishstory.database.dao.LureDao
import com.funjim.fishstory.database.dao.TackleBoxDao
import com.funjim.fishstory.database.mapper.toDomain
import com.funjim.fishstory.database.mapper.toLureWithColorsSummaryDomainList
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class TackleBoxRepository(
    private val lureDao: LureDao,
    private val tackleBoxDao: TackleBoxDao

) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTripTackleBoxSummary(
        fishermanId: String,
        tripId: String
    ): Flow<TackleBoxSummary> {
        return tackleBoxDao.getTackleBoxForFishermanOnTrip(
            fishermanId = fishermanId,
            tripId = tripId
        ).flatMapLatest { tackleBox ->
            if (tackleBox != null) {
                lureDao.getLureSummariesForFishermanOnTrip(
                    tackleBoxId = tackleBox.id,
                    tripId = tripId,
                    fishermanId = fishermanId
                ).map { summaries ->
                    TackleBoxSummary(
                        tackleBox = tackleBox.toDomain(),
                        lures = summaries.toLureWithColorsSummaryDomainList()
                    )
                }
            } else {
                lureDao.getLureSummariesWithColors(
                    tripId = tripId,
                    fishermanId = fishermanId
                ).map { summaries ->
                    TackleBoxSummary(
                        tackleBox = null,
                        lures = summaries.toLureWithColorsSummaryDomainList()
                    )
                }
            }
        }
    }
}