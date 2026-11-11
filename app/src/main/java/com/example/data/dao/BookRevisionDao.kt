package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BookRevisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookRevisionDao {
    @Query("SELECT * FROM book_revisions WHERE chapterId = :chapterId ORDER BY createdAt DESC")
    fun getRevisionsForChapter(chapterId: String): Flow<List<BookRevisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: BookRevisionEntity)

    @Query("DELETE FROM book_revisions WHERE chapterId = :chapterId")
    suspend fun clearRevisionsForChapter(chapterId: String)
}
