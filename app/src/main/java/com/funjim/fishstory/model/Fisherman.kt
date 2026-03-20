package com.funjim.fishstory.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "fisherman_table")
data class Fisherman(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
