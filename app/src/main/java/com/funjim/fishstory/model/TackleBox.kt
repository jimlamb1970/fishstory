package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tackle_box_table",
    foreignKeys = [
        ForeignKey(
            entity = Fisherman::class,
            parentColumns = ["id"],
            childColumns = ["fishermanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["fishermanId"])]
)
data class TackleBox(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fishermanId: Int,
    val name: String = "My Tackle Box"
)

@Entity(
    tableName = "tackle_box_lure_cross_ref",
    primaryKeys = ["tackleBoxId", "lureId"],
    foreignKeys = [
        ForeignKey(
            entity = TackleBox::class,
            parentColumns = ["id"],
            childColumns = ["tackleBoxId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Lure::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lureId"])]
)
data class TackleBoxLureCrossRef(
    val tackleBoxId: Int,
    val lureId: Int
)
