package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.RecoveryJournalEntity

@Dao
interface RecoveryJournalDao {
    @Query("SELECT * FROM recovery_journal WHERE chapterId = :chapterId LIMIT 1")
    suspend fun getRecoveryForChapter(chapterId: String): RecoveryJournalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecoveryDraft(recovery: RecoveryJournalEntity)

    @Query("DELETE FROM recovery_journal WHERE chapterId = :chapterId")
    suspend fun clearRecoveryForChapter(chapterId: String)

    @Query("SELECT COUNT(*) FROM recovery_journal WHERE hasUncommittedChanges = 1")
    suspend fun getPendingRecoveriesCount(): Int

    @Query("DELETE FROM recovery_journal")
    suspend fun clearAllRecoveries()
}
