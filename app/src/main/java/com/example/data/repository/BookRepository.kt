package com.example.data.repository

import com.example.core.model.BlockType
import com.example.core.model.ChapterBlock
import com.example.data.db.BoiLekhoDatabase
import com.example.data.entity.AppSettingsEntity
import com.example.data.entity.BookDesignEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterBlockEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.RecoveryJournalEntity
import com.example.data.entity.SectionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class BookRepository(private val database: BoiLekhoDatabase) {
    private val bookDao = database.bookDao()
    private val sectionDao = database.sectionDao()
    private val chapterDao = database.chapterDao()
    private val blockDao = database.chapterBlockDao()
    private val designDao = database.bookDesignDao()
    private val recoveryDao = database.recoveryJournalDao()
    private val settingsDao = database.appSettingsDao()

    // Books
    val allActiveBooks: Flow<List<BookEntity>> = bookDao.getAllActiveBooks()
    val favoriteBooks: Flow<List<BookEntity>> = bookDao.getFavoriteBooks()
    val archivedBooks: Flow<List<BookEntity>> = bookDao.getArchivedBooks()
    val trashBooks: Flow<List<BookEntity>> = bookDao.getTrashBooks()

    fun getBook(id: String): Flow<BookEntity?> = bookDao.getBookById(id)
    suspend fun getBookDirect(id: String): BookEntity? = withContext(Dispatchers.IO) {
        bookDao.getBookByIdDirect(id)
    }

    fun searchBooks(query: String): Flow<List<BookEntity>> = bookDao.searchBooks(query)

    suspend fun createBook(
        title: String,
        subtitle: String = "",
        author: String = "Muhammad Omor",
        description: String = "",
        language: String = "bn",
        genre: String = "Literature"
    ): String = withContext(Dispatchers.IO) {
        val bookId = UUID.randomUUID().toString()
        val book = BookEntity(
            id = bookId,
            title = title.trim(),
            subtitle = subtitle.trim(),
            author = author.trim(),
            description = description.trim(),
            language = language,
            genre = genre,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        bookDao.insertBook(book)

        // Create default design
        val design = BookDesignEntity(
            id = UUID.randomUUID().toString(),
            bookId = bookId
        )
        designDao.insertOrUpdateDesign(design)

        // Create first chapter
        val chapterId = UUID.randomUUID().toString()
        val firstChapter = ChapterEntity(
            id = chapterId,
            bookId = bookId,
            title = if (language == "bn") "অধ্যায় ১: সূচনা" else "Chapter 1: Introduction",
            orderIndex = 0
        )
        chapterDao.insertChapter(firstChapter)

        val firstBlock = ChapterBlockEntity(
            id = UUID.randomUUID().toString(),
            chapterId = chapterId,
            blockType = "PARAGRAPH",
            orderIndex = 0,
            contentText = if (language == "bn") "এখানে আপনার নতুন অধ্যায়ের লেখা শুরু করুন..." else "Begin writing your new chapter here..."
        )
        blockDao.insertBlocks(listOf(firstBlock))

        bookId
    }

    suspend fun updateBook(book: BookEntity) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavorite(book: BookEntity) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book.copy(isFavorite = !book.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleArchive(book: BookEntity) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book.copy(isArchived = !book.isArchived, updatedAt = System.currentTimeMillis()))
    }

    suspend fun moveToTrash(book: BookEntity) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book.copy(isDeleted = true, updatedAt = System.currentTimeMillis()))
    }

    suspend fun restoreFromTrash(book: BookEntity) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book.copy(isDeleted = false, updatedAt = System.currentTimeMillis()))
    }

    suspend fun hardDeleteBook(id: String) = withContext(Dispatchers.IO) {
        bookDao.hardDeleteBook(id)
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        bookDao.emptyTrash()
    }

    // Sections & Chapters
    fun getSectionsForBook(bookId: String): Flow<List<SectionEntity>> = sectionDao.getSectionsForBook(bookId)
    suspend fun getSectionsForBookDirect(bookId: String): List<SectionEntity> = withContext(Dispatchers.IO) {
        sectionDao.getSectionsForBookDirect(bookId)
    }

    suspend fun createSection(bookId: String, title: String, orderIndex: Int) = withContext(Dispatchers.IO) {
        val section = SectionEntity(
            id = UUID.randomUUID().toString(),
            bookId = bookId,
            title = title,
            orderIndex = orderIndex
        )
        sectionDao.insertSection(section)
    }

    suspend fun deleteSection(section: SectionEntity) = withContext(Dispatchers.IO) {
        sectionDao.deleteSection(section)
    }

    fun getChaptersForBook(bookId: String): Flow<List<ChapterEntity>> = chapterDao.getChaptersForBook(bookId)
    suspend fun getChaptersForBookDirect(bookId: String): List<ChapterEntity> = withContext(Dispatchers.IO) {
        chapterDao.getChaptersForBookDirect(bookId)
    }

    fun getChapter(chapterId: String): Flow<ChapterEntity?> = chapterDao.getChapterById(chapterId)
    suspend fun getChapterDirect(chapterId: String): ChapterEntity? = withContext(Dispatchers.IO) {
        chapterDao.getChapterByIdDirect(chapterId)
    }

    suspend fun createChapter(bookId: String, title: String, sectionId: String? = null, orderIndex: Int): String = withContext(Dispatchers.IO) {
        val chapterId = UUID.randomUUID().toString()
        val chapter = ChapterEntity(
            id = chapterId,
            bookId = bookId,
            sectionId = sectionId,
            title = title,
            orderIndex = orderIndex,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chapterDao.insertChapter(chapter)

        val initialBlock = ChapterBlockEntity(
            id = UUID.randomUUID().toString(),
            chapterId = chapterId,
            blockType = "PARAGRAPH",
            orderIndex = 0,
            contentText = ""
        )
        blockDao.insertBlocks(listOf(initialBlock))
        chapterId
    }

    suspend fun duplicateChapter(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        val newChapterId = UUID.randomUUID().toString()
        val currentBlocks = blockDao.getBlocksForChapterDirect(chapter.id)
        val duplicatedChapter = chapter.copy(
            id = newChapterId,
            title = "${chapter.title} (Copy)",
            orderIndex = chapter.orderIndex + 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chapterDao.insertChapter(duplicatedChapter)

        val newBlocks = currentBlocks.map {
            it.copy(id = UUID.randomUUID().toString(), chapterId = newChapterId)
        }
        blockDao.insertBlocks(newBlocks)
    }

    suspend fun deleteChapter(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        chapterDao.deleteChapter(chapter)
        recoveryDao.clearRecoveryForChapter(chapter.id)
    }

    // Blocks & Save-on-Exit
    fun getBlocksForChapter(chapterId: String): Flow<List<ChapterBlock>> {
        return blockDao.getBlocksForChapter(chapterId).map { entities ->
            entities.map { entity ->
                ChapterBlock(
                    id = entity.id,
                    type = try { BlockType.valueOf(entity.blockType) } catch (_: Exception) { BlockType.PARAGRAPH },
                    text = entity.contentText,
                    caption = entity.captionOrNote,
                    orderIndex = entity.orderIndex
                )
            }
        }
    }

    suspend fun getBlocksForChapterDirect(chapterId: String): List<ChapterBlock> = withContext(Dispatchers.IO) {
        blockDao.getBlocksForChapterDirect(chapterId).map { entity ->
            ChapterBlock(
                id = entity.id,
                type = try { BlockType.valueOf(entity.blockType) } catch (_: Exception) { BlockType.PARAGRAPH },
                text = entity.contentText,
                caption = entity.captionOrNote,
                orderIndex = entity.orderIndex
            )
        }
    }

    /**
     * SAVE-ON-EXIT SYSTEM (Section 7.2 of Blueprint)
     * Persists editor content atomically on exit without continuous keystroke DB thrashing.
     */
    suspend fun saveChapterOnExit(
        chapterId: String,
        newTitle: String,
        blocks: List<ChapterBlock>,
        newStatus: String = "DRAFT"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val chapter = chapterDao.getChapterByIdDirect(chapterId) ?: return@withContext false
            val entities = blocks.mapIndexed { index, block ->
                ChapterBlockEntity(
                    id = block.id,
                    chapterId = chapterId,
                    blockType = block.type.name,
                    orderIndex = index,
                    contentText = block.text,
                    captionOrNote = block.caption,
                    updatedAt = System.currentTimeMillis()
                )
            }
            // 1. Atomically replace blocks
            blockDao.replaceChapterBlocks(chapterId, entities)

            // 2. Update chapter header metadata & revision
            val updatedChapter = chapter.copy(
                title = newTitle.trim(),
                status = newStatus,
                contentRevision = chapter.contentRevision + 1,
                updatedAt = System.currentTimeMillis()
            )
            chapterDao.updateChapter(updatedChapter)

            // 3. Touch parent book updatedAt
            val book = bookDao.getBookByIdDirect(chapter.bookId)
            book?.let {
                bookDao.updateBook(it.copy(updatedAt = System.currentTimeMillis()))
            }

            // 4. Safe clear of recovery journal
            recoveryDao.clearRecoveryForChapter(chapterId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Recovery Journal (Section 7.4 of Blueprint)
    suspend fun checkpointRecoveryJournal(
        chapterId: String,
        draftTitle: String,
        blocks: List<ChapterBlock>
    ) = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            blocks.forEach { block ->
                val obj = JSONObject()
                obj.put("id", block.id)
                obj.put("type", block.type.name)
                obj.put("text", block.text)
                obj.put("caption", block.caption)
                obj.put("orderIndex", block.orderIndex)
                jsonArray.put(obj)
            }
            val recovery = RecoveryJournalEntity(
                chapterId = chapterId,
                draftTitle = draftTitle,
                draftBlocksJson = jsonArray.toString(),
                lastCheckpointMillis = System.currentTimeMillis(),
                hasUncommittedChanges = true
            )
            recoveryDao.saveRecoveryDraft(recovery)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun checkRecoveryJournal(chapterId: String): Pair<String, List<ChapterBlock>>? = withContext(Dispatchers.IO) {
        val entry = recoveryDao.getRecoveryForChapter(chapterId) ?: return@withContext null
        if (!entry.hasUncommittedChanges) return@withContext null
        try {
            val jsonArray = JSONArray(entry.draftBlocksJson)
            val blocks = mutableListOf<ChapterBlock>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeStr = obj.optString("type", BlockType.PARAGRAPH.name)
                val type = try { BlockType.valueOf(typeStr) } catch (_: Exception) { BlockType.PARAGRAPH }
                blocks.add(
                    ChapterBlock(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        type = type,
                        text = obj.optString("text", ""),
                        caption = obj.optString("caption", ""),
                        orderIndex = obj.optInt("orderIndex", i)
                    )
                )
            }
            Pair(entry.draftTitle, blocks)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun discardRecoveryDraft(chapterId: String) = withContext(Dispatchers.IO) {
        recoveryDao.clearRecoveryForChapter(chapterId)
    }

    // Design
    fun getDesign(bookId: String): Flow<BookDesignEntity?> = designDao.getDesignForBook(bookId)
    suspend fun getDesignDirect(bookId: String): BookDesignEntity = withContext(Dispatchers.IO) {
        designDao.getDesignForBookDirect(bookId) ?: BookDesignEntity(
            id = UUID.randomUUID().toString(),
            bookId = bookId
        ).also { designDao.insertOrUpdateDesign(it) }
    }

    suspend fun updateDesign(design: BookDesignEntity) = withContext(Dispatchers.IO) {
        designDao.insertOrUpdateDesign(design.copy(updatedAt = System.currentTimeMillis()))
    }

    // App Settings
    fun getSetting(key: String): Flow<String?> = settingsDao.getSetting(key).map { it?.value }
    suspend fun saveSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        settingsDao.saveSetting(AppSettingsEntity(key, value))
    }

    suspend fun getDatabaseStatistics(): Map<String, Any> = withContext(Dispatchers.IO) {
        val booksCount = bookDao.getActiveBooksCount()
        val allBooks = bookDao.getAllBooksForBackup()
        var totalWords = 0
        var totalChapters = 0
        allBooks.forEach { book ->
            val chapters = chapterDao.getChaptersForBookDirect(book.id)
            totalChapters += chapters.size
            chapters.forEach { chapter ->
                val blocks = blockDao.getBlocksForChapterDirect(chapter.id)
                blocks.forEach { b ->
                    totalWords += b.contentText.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
                }
            }
        }
        mapOf(
            "booksCount" to booksCount,
            "chaptersCount" to totalChapters,
            "wordsCount" to totalWords,
            "pendingRecoveries" to recoveryDao.getPendingRecoveriesCount()
        )
    }

    // Backup & Restore (Section 13)
    suspend fun exportFullBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "BoiLekho")
        root.put("version", "1.0.0")
        root.put("timestamp", System.currentTimeMillis())

        val booksArray = JSONArray()
        val allBooks = bookDao.getAllBooksForBackup()
        for (book in allBooks) {
            val bObj = JSONObject()
            bObj.put("id", book.id)
            bObj.put("title", book.title)
            bObj.put("subtitle", book.subtitle)
            bObj.put("author", book.author)
            bObj.put("description", book.description)
            bObj.put("language", book.language)
            bObj.put("genre", book.genre)
            bObj.put("isFavorite", book.isFavorite)
            bObj.put("isArchived", book.isArchived)
            bObj.put("createdAt", book.createdAt)
            bObj.put("updatedAt", book.updatedAt)

            // Design
            val design = designDao.getDesignForBookDirect(book.id)
            design?.let { d ->
                val dObj = JSONObject()
                dObj.put("coverTemplate", d.coverTemplate)
                dObj.put("coverBgColor", d.coverBgColor)
                dObj.put("coverTitleColor", d.coverTitleColor)
                dObj.put("pageSize", d.pageSize)
                dObj.put("fontChoice", d.fontChoice)
                bObj.put("design", dObj)
            }

            // Chapters & Blocks
            val chapters = chapterDao.getChaptersForBookDirect(book.id)
            val cArray = JSONArray()
            for (ch in chapters) {
                val cObj = JSONObject()
                cObj.put("id", ch.id)
                cObj.put("title", ch.title)
                cObj.put("orderIndex", ch.orderIndex)
                cObj.put("status", ch.status)

                val blocks = blockDao.getBlocksForChapterDirect(ch.id)
                val blkArray = JSONArray()
                for (b in blocks) {
                    val blkObj = JSONObject()
                    blkObj.put("id", b.id)
                    blkObj.put("blockType", b.blockType)
                    blkObj.put("contentText", b.contentText)
                    blkObj.put("captionOrNote", b.captionOrNote)
                    blkObj.put("orderIndex", b.orderIndex)
                    blkArray.put(blkObj)
                }
                cObj.put("blocks", blkArray)
                cArray.put(cObj)
            }
            bObj.put("chapters", cArray)
            booksArray.put(bObj)
        }
        root.put("books", booksArray)
        root.toString(2)
    }

    suspend fun importFullBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("books")) return@withContext false
            val booksArray = root.getJSONArray("books")

            for (i in 0 until booksArray.length()) {
                val bObj = booksArray.getJSONObject(i)
                val bookId = bObj.optString("id", UUID.randomUUID().toString())
                val book = BookEntity(
                    id = bookId,
                    title = bObj.getString("title"),
                    subtitle = bObj.optString("subtitle", ""),
                    author = bObj.optString("author", "Muhammad Omor"),
                    description = bObj.optString("description", ""),
                    language = bObj.optString("language", "bn"),
                    genre = bObj.optString("genre", "Literature"),
                    isFavorite = bObj.optBoolean("isFavorite", false),
                    isArchived = bObj.optBoolean("isArchived", false),
                    createdAt = bObj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = System.currentTimeMillis()
                )
                bookDao.insertBook(book)

                if (bObj.has("design")) {
                    val dObj = bObj.getJSONObject("design")
                    val design = BookDesignEntity(
                        id = UUID.randomUUID().toString(),
                        bookId = bookId,
                        coverTemplate = dObj.optString("coverTemplate", "CLASSIC_NAVY"),
                        coverBgColor = dObj.optString("coverBgColor", "#0B2345"),
                        coverTitleColor = dObj.optString("coverTitleColor", "#F2A900"),
                        pageSize = dObj.optString("pageSize", "A5"),
                        fontChoice = dObj.optString("fontChoice", "SERIF")
                    )
                    designDao.insertOrUpdateDesign(design)
                }

                if (bObj.has("chapters")) {
                    val cArray = bObj.getJSONArray("chapters")
                    for (c in 0 until cArray.length()) {
                        val cObj = cArray.getJSONObject(c)
                        val chapterId = cObj.optString("id", UUID.randomUUID().toString())
                        val chapter = ChapterEntity(
                            id = chapterId,
                            bookId = bookId,
                            title = cObj.getString("title"),
                            orderIndex = cObj.optInt("orderIndex", c),
                            status = cObj.optString("status", "DRAFT")
                        )
                        chapterDao.insertChapter(chapter)

                        if (cObj.has("blocks")) {
                            val blkArray = cObj.getJSONArray("blocks")
                            val blockEntities = mutableListOf<ChapterBlockEntity>()
                            for (k in 0 until blkArray.length()) {
                                val blkObj = blkArray.getJSONObject(k)
                                blockEntities.add(
                                    ChapterBlockEntity(
                                        id = blkObj.optString("id", UUID.randomUUID().toString()),
                                        chapterId = chapterId,
                                        blockType = blkObj.optString("blockType", "PARAGRAPH"),
                                        orderIndex = blkObj.optInt("orderIndex", k),
                                        contentText = blkObj.optString("contentText", ""),
                                        captionOrNote = blkObj.optString("captionOrNote", "")
                                    )
                                )
                            }
                            blockDao.insertBlocks(blockEntities)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
