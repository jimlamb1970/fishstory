package com.funjim.fishstory.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.funjim.fishstory.database.LureEntity
import com.funjim.fishstory.database.LureEntityWithColors
import com.funjim.fishstory.model.Fisherman
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
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
data class TackleBoxEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val fishermanId: String,
    val name: String = "My Tackle Box"
)

@Serializable
@Entity(
    tableName = "tackle_box_lure_cross_ref",
    primaryKeys = ["tackleBoxId", "lureId"],
    foreignKeys = [
        ForeignKey(
            entity = TackleBoxEntity::class,
            parentColumns = ["id"],
            childColumns = ["tackleBoxId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LureEntity::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lureId"])]
)
data class TackleBoxLureEntity(
    val tackleBoxId: String,
    val lureId: String
)

data class TackleBoxEntityWithLures(
    @Embedded val tackleBox: TackleBoxEntity,
    @Relation(
        entity = LureEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TackleBoxLureEntity::class,
            parentColumn = "tackleBoxId",
            entityColumn = "lureId"
        )
    )
    val lures: List<LureEntityWithColors>
)
