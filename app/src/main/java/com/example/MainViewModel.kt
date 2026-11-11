package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.AppLanguage
import com.example.core.model.ChapterBlock
import com.example.core.model.StringsHelper
import com.example.core.util.FileUtils
import com.example.data.db.BoiLekhoDatabase
import com.example.data.entity.BookDesignEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.SectionEntity
import com.example.data.repository.BookRepository
import com.example.publishing.EpubExporter
import com.example.publishing.HtmlExporter
import com.example.publishing.MarkdownExporter
import com.example.publishing.PdfExporter
import com.example.publishing.TextExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

sealed interface Screen {
    object Library : Screen
    data class BookDetail(val bookId: String) : Screen
    data class ChapterEditor(val chapterId: String) : Screen
    data class BookDesigner(val bookId: String) : Screen
    data class ExportHub(val bookId: String) : Screen
    object Settings : Screen
    object Backup : Screen
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BoiLekhoDatabase.getInstance(application)
    val repository = BookRepository(database)

    // Navigation state stack for BackHandler
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Library))
    val currentScreen: StateFlow<Screen> = MutableStateFlow(Screen.Library)
    private val _currentScreen = _screenStack.value.lastOrNull() ?: Screen.Library

    private val _screen = MutableStateFlow<Screen>(Screen.Library)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    // Preferences & Locale
    private val _language = MutableStateFlow(AppLanguage.BANGLA)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _stats = MutableStateFlow<Map<String, Any>>(emptyMap())
    val stats: StateFlow<Map<String, Any>> = _stats.asStateFlow()

    // Reactive Data Sources
    val activeBooks: StateFlow<List<BookEntity>> = repository.allActiveBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteBooks: StateFlow<List<BookEntity>> = repository.favoriteBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val archivedBooks: StateFlow<List<BookEntity>> = repository.archivedBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val trashBooks: StateFlow<List<BookEntity>> = repository.trashBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Load saved preferences
        viewModelScope.launch {
            repository.getSetting("app_language").collect { saved ->
                if (saved == "en") {
                    _language.value = AppLanguage.ENGLISH
                    StringsHelper.currentLanguage = AppLanguage.ENGLISH
                } else {
                    _language.value = AppLanguage.BANGLA
                    StringsHelper.currentLanguage = AppLanguage.BANGLA
                }
            }
        }
        viewModelScope.launch {
            repository.getSetting("theme_mode").collect { saved ->
                _themeMode.value = saved ?: "SYSTEM"
            }
        }
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            _stats.value = repository.getDatabaseStatistics()
        }
    }

    // Navigation Stack Handlers
    fun navigateTo(newScreen: Screen) {
        val currentStack = _screenStack.value.toMutableList()
        currentStack.add(newScreen)
        _screenStack.value = currentStack
        _screen.value = newScreen
    }

    fun navigateBack(): Boolean {
        val currentStack = _screenStack.value.toMutableList()
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.size - 1)
            _screenStack.value = currentStack
            _screen.value = currentStack.last()
            refreshStats()
            return true
        }
        return false
    }

    // Language & Theme
    fun setLanguage(newLanguage: AppLanguage) {
        _language.value = newLanguage
        StringsHelper.currentLanguage = newLanguage
        viewModelScope.launch {
            repository.saveSetting("app_language", newLanguage.code)
        }
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        viewModelScope.launch {
            repository.saveSetting("theme_mode", mode)
        }
    }

    // Book Actions
    fun createBook(title: String, subtitle: String, author: String, genre: String, lang: String) {
        viewModelScope.launch {
            val bookId = repository.createBook(title, subtitle, author, "", lang, genre)
            navigateTo(Screen.BookDetail(bookId))
            refreshStats()
        }
    }

    fun toggleFavorite(book: BookEntity) {
        viewModelScope.launch { repository.toggleFavorite(book) }
    }

    fun toggleArchive(book: BookEntity) {
        viewModelScope.launch { repository.toggleArchive(book) }
    }

    fun moveToTrash(book: BookEntity) {
        viewModelScope.launch { repository.moveToTrash(book) }
    }

    fun restoreFromTrash(book: BookEntity) {
        viewModelScope.launch { repository.restoreFromTrash(book) }
    }

    fun hardDeleteBook(id: String) {
        viewModelScope.launch {
            repository.hardDeleteBook(id)
            refreshStats()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            refreshStats()
        }
    }

    // Chapter Actions
    fun createChapter(bookId: String, title: String, count: Int) {
        viewModelScope.launch {
            val chapterId = repository.createChapter(bookId, title, null, count)
            navigateTo(Screen.ChapterEditor(chapterId))
            refreshStats()
        }
    }

    fun duplicateChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.duplicateChapter(chapter)
            refreshStats()
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
            refreshStats()
        }
    }

    // Save-on-Exit
    fun saveChapterOnExit(
        chapterId: String,
        newTitle: String,
        blocks: List<ChapterBlock>,
        status: String
    ) {
        viewModelScope.launch {
            repository.saveChapterOnExit(chapterId, newTitle, blocks, status)
            navigateBack()
            refreshStats()
        }
    }

    fun checkpointRecoveryJournal(chapterId: String, draftTitle: String, blocks: List<ChapterBlock>) {
        viewModelScope.launch {
            repository.checkpointRecoveryJournal(chapterId, draftTitle, blocks)
        }
    }

    fun discardRecoveryDraft(chapterId: String) {
        viewModelScope.launch {
            repository.discardRecoveryDraft(chapterId)
        }
    }

    fun updateBookDesign(design: BookDesignEntity) {
        viewModelScope.launch {
            repository.updateDesign(design)
            navigateBack()
        }
    }

    // Exporter Runner
    suspend fun exportBook(bookId: String, format: String): Pair<Boolean, String> {
        val book = repository.getBookDirect(bookId) ?: return Pair(false, "Book not found")
        val design = repository.getDesignDirect(bookId)
        val chapters = repository.getChaptersForBookDirect(bookId)
        val chaptersWithBlocks = chapters.map { ch ->
            Pair(ch, repository.getBlocksForChapterDirect(ch.id))
        }

        return try {
            val context = getApplication<Application>()
            val exportedFile: File = when (format) {
                "PDF" -> PdfExporter.exportPdf(context, book, design, chaptersWithBlocks)
                "EPUB" -> EpubExporter.exportEpub(context, book, design, chaptersWithBlocks)
                "HTML" -> HtmlExporter.exportHtml(context, book, design, chaptersWithBlocks)
                "MD" -> MarkdownExporter.exportMarkdown(context, book, chaptersWithBlocks)
                "TXT" -> TextExporter.exportText(context, book, chaptersWithBlocks)
                else -> return Pair(false, "Unknown format")
            }

            val mimeType = when (format) {
                "PDF" -> "application/pdf"
                "EPUB" -> "application/epub+zip"
                "HTML" -> "text/html"
                "MD" -> "text/markdown"
                else -> "text/plain"
            }

            // Trigger system share chooser
            FileUtils.shareFile(context, exportedFile, mimeType, "Share ${book.title}")
            Pair(true, StringsHelper.get("সফলভাবে এক্সপোর্ট করা হয়েছে!", "Successfully exported!"))
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Export failed: ${e.message}")
        }
    }

    // Full Project Backup & Restore
    suspend fun exportFullBackup(): Pair<Boolean, String> {
        return try {
            val context = getApplication<Application>()
            val backupJson = repository.exportFullBackupJson()
            val backupDir = FileUtils.getBackupDirectory(context)
            val backupFile = File(backupDir, "BoiLekho_Backup_${System.currentTimeMillis()}.json")
            val outStream = FileOutputStream(backupFile)
            outStream.write(backupJson.toByteArray(StandardCharsets.UTF_8))
            outStream.close()

            FileUtils.shareFile(context, backupFile, "application/json", "BoiLekho Project Backup")
            Pair(true, StringsHelper.get("ব্যাকআপ ফাইল সফলভাবে প্রস্তুত হয়েছে!", "Backup created and shared successfully!"))
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Backup failed: ${e.message}")
        }
    }

    suspend fun importFullBackup(jsonText: String): Pair<Boolean, String> {
        val success = repository.importFullBackupJson(jsonText)
        return if (success) {
            refreshStats()
            Pair(true, StringsHelper.get("ব্যাকআপ সফলভাবে পুনরুদ্ধার করা হয়েছে!", "Backup restored successfully!"))
        } else {
            Pair(false, StringsHelper.get("ব্যাকআপ ডেটা সঠিক নয় বা ত্রুটিযুক্ত!", "Invalid backup JSON format!"))
        }
    }

    fun clearCache() {
        val context = getApplication<Application>()
        try {
            val exportDir = FileUtils.getExportDirectory(context)
            exportDir.listFiles()?.forEach { it.delete() }
        } catch (_: Exception) {}
    }
}
