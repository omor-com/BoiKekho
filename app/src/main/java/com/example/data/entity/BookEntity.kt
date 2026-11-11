package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["updatedAt"]),
        Index(value = ["isFavorite", "updatedAt"]),
        Index(value = ["isArchived", "updatedAt"]),
        Index(value = ["isDeleted"])
    ]
)
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String = "",
    val author: String = "Muhammad Omor",
    val description: String = "",
    val language: String = "bn",
    val genre: String = "Literature",
    val coverAssetId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val metadataJson: String = "{}",
    val schemaVersion: Int = 1
)
