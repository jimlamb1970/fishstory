package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "lure_color_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class LureColor(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

@Serializable
@Entity(
    tableName = "lure_table",
    foreignKeys = [
        ForeignKey(
            entity = LureColor::class,
            parentColumns = ["id"],
            childColumns = ["primaryColorId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = LureColor::class,
            parentColumns = ["id"],
            childColumns = ["secondaryColorId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = LureColor::class,
            parentColumns = ["id"],
            childColumns = ["glowColorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["primaryColorId"]),
        Index(value = ["secondaryColorId"]),
        Index(value = ["glowColorId"])
    ]
)
data class Lure(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val primaryColorId: String?,
    val secondaryColorId: String?,
    val hasSingleHook: Boolean,
    val glows: Boolean,
    val glowColorId: String?
) {
    fun getDisplayName(primaryColorName: String?, secondaryColorName: String?, glowColorName: String?): String {
        val sb = StringBuilder(name)
        
        val colors = mutableListOf<String>()
        if (!primaryColorName.isNullOrBlank()) {
            colors.add(primaryColorName)
        }
        if (!secondaryColorName.isNullOrBlank()) {
            colors.add(secondaryColorName)
        }

        if (colors.isNotEmpty()) {
            sb.append(" : ${colors.joinToString("/")}")
        }

        if (glows) {
            sb.append(", Glow")
            if (!glowColorName.isNullOrBlank()) {
                sb.append(" : $glowColorName")
            }
        }
        return sb.toString()
    }
}

// TODO - Need a version of lure that has the full name already built