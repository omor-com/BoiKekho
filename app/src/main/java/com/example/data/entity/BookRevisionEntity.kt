package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "book_revisions",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chapterId", "createdAt"])
    ]
)
data class BookRevisionEntity(
    @PrimaryKey val id: String,
    val chapterId: String,
    val revisionNumber: Int,
    val snapshotBlocksJson: String,
    val wordCount: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = ""
)
