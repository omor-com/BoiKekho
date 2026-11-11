package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.entity.ChapterBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterBlockDao {
    @Query("SELECT * FROM chapter_blocks WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getBlocksForChapter(chapterId: String): Flow<List<ChapterBlockEntity>>

    @Query("SELECT * FROM chapter_blocks WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    suspend fun getBlocksForChapterDirect(chapterId: String): List<ChapterBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<ChapterBlockEntity>)

    @Query("DELETE FROM chapter_blocks WHERE chapterId = :chapterId")
    suspend fun deleteBlocksForChapter(chapterId: String)

    @Transaction
    suspend fun replaceChapterBlocks(chapterId: String, newBlocks: List<ChapterBlockEntity>) {
        deleteBlocksForChapter(chapterId)
        if (newBlocks.isNotEmpty()) {
            insertBlocks(newBlocks)
        }
    }

    @Query("SELECT * FROM chapter_blocks")
    suspend fun getAllBlocksForBackup(): List<ChapterBlockEntity>
}
