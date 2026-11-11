package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BookDesignEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDesignDao {
    @Query("SELECT * FROM book_designs WHERE bookId = :bookId LIMIT 1")
    fun getDesignForBook(bookId: String): Flow<BookDesignEntity?>

    @Query("SELECT * FROM book_designs WHERE bookId = :bookId LIMIT 1")
    suspend fun getDesignForBookDirect(bookId: String): BookDesignEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDesign(design: BookDesignEntity)

    @Query("SELECT * FROM book_designs")
    suspend fun getAllDesignsForBackup(): List<BookDesignEntity>
}
