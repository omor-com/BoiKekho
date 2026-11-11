package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId", "orderIndex"]),
        Index(value = ["sectionId"])
    ]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val sectionId: String? = null,
    val parentChapterId: String? = null,
    val title: String,
    val orderIndex: Int = 0,
    val status: String = "DRAFT", // DRAFT, IN_PROGRESS, COMPLETED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val contentRevision: Int = 1
)
