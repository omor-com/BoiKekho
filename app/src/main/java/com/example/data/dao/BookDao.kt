package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE isDeleted = 0 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllActiveBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isDeleted = 0 AND isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getTrashBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookById(id: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookByIdDirect(id: String): BookEntity?

    @Query("SELECT * FROM books WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun hardDeleteBook(id: String)

    @Query("DELETE FROM books WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("SELECT COUNT(*) FROM books WHERE isDeleted = 0")
    suspend fun getActiveBooksCount(): Int

    @Query("SELECT * FROM books")
    suspend fun getAllBooksForBackup(): List<BookEntity>
}
