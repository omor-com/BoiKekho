package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections WHERE bookId = :bookId ORDER BY orderIndex ASC")
    fun getSectionsForBook(bookId: String): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections WHERE bookId = :bookId ORDER BY orderIndex ASC")
    suspend fun getSectionsForBookDirect(bookId: String): List<SectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity)

    @Update
    suspend fun updateSection(section: SectionEntity)

    @Delete
    suspend fun deleteSection(section: SectionEntity)

    @Query("SELECT * FROM sections")
    suspend fun getAllSectionsForBackup(): List<SectionEntity>
}
