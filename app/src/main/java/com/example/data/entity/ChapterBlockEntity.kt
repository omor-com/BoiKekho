package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapter_blocks",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chapterId", "orderIndex"])
    ]
)
data class ChapterBlockEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val blockType: String,
    val orderIndex: Int,
    val contentText: String,
    val captionOrNote: String = "",
    val schemaVersion: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)
