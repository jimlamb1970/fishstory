package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "lure_color_table")
data class LureColor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val primaryColorId: Int?,
    val secondaryColorId: Int?,
    val hasSingleHook: Boolean,
    val glows: Boolean,
    val glowColorId: Int?
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
