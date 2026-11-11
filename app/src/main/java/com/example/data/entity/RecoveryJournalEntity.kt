package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recovery_journal")
data class RecoveryJournalEntity(
    @PrimaryKey val chapterId: String,
    val draftTitle: String,
    val draftBlocksJson: String,
    val lastCheckpointMillis: Long = System.currentTimeMillis(),
    val hasUncommittedChanges: Boolean = true
)
