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
            childColumns = ["colorId"],
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
        Index(value = ["colorId"]),
        Index(value = ["glowColorId"])
    ]
)
data class Lure(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorId: Int?,
    val hasSingleHook: Boolean,
    val glows: Boolean,
    val glowColorId: Int?
)
