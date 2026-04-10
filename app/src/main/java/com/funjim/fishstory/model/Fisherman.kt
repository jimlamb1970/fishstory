package com.funjim.fishstory.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "fisherman_table",
    indices = [Index(value = ["firstName", "lastName", "nickname"], unique = true)]
)
data class Fisherman(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = ""
) {
    @get:Ignore
    val fullName: String
        get() = if (nickname.isNotBlank()) {
            "$firstName \"$nickname\" $lastName".trim()
        } else {
            "$firstName $lastName".trim()
        }
}

data class TackleBoxWithLures(
    @Embedded val tackleBox: TackleBox,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TackleBoxLureCrossRef::class,
            parentColumn = "tackleBoxId",
            entityColumn = "lureId"
        )
    )
    val lures: List<Lure>
)

data class FishermanWithTackleBox(
    @Embedded val fisherman: Fisherman,
    @Relation(
        parentColumn = "id",
        entityColumn = "fishermanId"
    )
    val tackleBox: TackleBox?
)

data class FishermanWithTrips(
    @Embedded val fisherman: Fisherman,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripFishermanCrossRef::class,
            parentColumn = "fishermanId",
            entityColumn = "tripId"
        )
    )
    val trips: List<Trip>
)

data class FishermanWithDetails(
    @Embedded val fisherman: Fisherman,
    @Relation(
        entity = TackleBox::class,
        parentColumn = "id",
        entityColumn = "fishermanId"
    )
    val tackleBoxWithLures: TackleBoxWithLures?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripFishermanCrossRef::class,
            parentColumn = "fishermanId",
            entityColumn = "tripId"
        )
    )
    val trips: List<Trip>
)

data class FishermanSummary(
    @Embedded val fisherman: Fisherman,
    val totalCatches: Int,
    val totalReleased: Int,
    val totalTrips: Int
)

data class FishermanTripSummary(
    @Embedded val trip: Trip,
    val totalCaught: Int,
    val totalKept: Int,
)
data class FishermanFullStatistics(
    @Embedded val fisherman: Fisherman,

    @Relation(
        entity = TackleBox::class,
        parentColumn = "id",
        entityColumn = "fishermanId"
    )
    val tackleBoxesWithLures: List<TackleBoxWithLures?>,

    // Fish Extremes
    val largestFishLength: Double?,
    val largestFishTimestamp: Long?,
    val smallestFishLength: Double?,
    val smallestFishTimestamp: Long?,

    // Best Trip
    val mostTripCatches: Int,
    val bestTripName: String?,
    val bestTripTime: Long?,

    // Best Segment & Its Parent Trip
    val mostSegmentCatches: Int,
    val bestSegmentName: String?,
    val bestSegmentTripName: String?,
    val bestSegmentTime: Long?,

    // Worst Trip
    val fewestTripCatches: Int,
    val worstTripName: String?,
    val worstTripTime: Long?,

    // Worst Segment
    val fewestSegmentCatches: Int,
    val worstSegmentName: String?,
    val worstSegmentTripName: String?,
    val worstSegmentTime: Long?
)