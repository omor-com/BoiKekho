package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "book_designs",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId"], unique = true)
    ]
)
data class BookDesignEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val coverTemplate: String = "CLASSIC_NAVY", // CLASSIC_NAVY, GOLD_ROYAL, MINIMAL_CREAM, LITERARY_DARK, MODERN_EMERALD
    val coverBgColor: String = "#0B2345",
    val coverTitleColor: String = "#F2A900",
    val coverSubtitleColor: String = "#FFFFFF",
    val coverAuthorColor: String = "#B7C0CE",
    val coverAccentColor: String = "#F2A900",
    val coverShowEmblem: Boolean = true,
    val coverShowBorder: Boolean = true,
    val pageSize: String = "A5", // A5, A4, LETTER, POCKET
    val pageMargins: String = "STANDARD", // COMPACT, STANDARD, GENEROUS
    val fontChoice: String = "SERIF", // SERIF, SANS, MONO
    val baseFontSizeSp: Float = 15f,
    val lineSpacingMultiplier: Float = 1.5f,
    val showHeader: Boolean = true,
    val showFooter: Boolean = true,
    val showPageNumbers: Boolean = true,
    val pageNumberFormat: String = "BANGLA_DASH", // BANGLA_DASH (— ১ —), ARABIC (1), ENGLISH_PAGE (Page 1)
    val includeToc: Boolean = true,
    val schemaVersion: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)
