package com.example

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.ChapterBlock
import com.example.data.entity.BookDesignEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.SectionEntity
import com.example.ui.backup.BackupRestoreScreen
import com.example.ui.designer.BookDesignerScreen
import com.example.ui.editor.ChapterEditorScreen
import com.example.ui.export.ExportHubScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.project.BookDetailScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.BoiLekhoTheme
import com.example.ui.theme.GoldAccent

@Composable
fun BoiLekhoApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.screen.collectAsStateWithLifecycle()
    val activeBooks by viewModel.activeBooks.collectAsStateWithLifecycle()
    val favoriteBooks by viewModel.favoriteBooks.collectAsStateWithLifecycle()
    val archivedBooks by viewModel.archivedBooks.collectAsStateWithLifecycle()
    val trashBooks by viewModel.trashBooks.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    BoiLekhoTheme(darkTheme = darkTheme) {
        when (val screen = currentScreen) {
            is Screen.Library -> {
                LibraryScreen(
                    books = activeBooks,
                    favoriteBooks = favoriteBooks,
                    archivedBooks = archivedBooks,
                    trashBooks = trashBooks,
                    language = language,
                    onSelectBook = { bookId -> viewModel.navigateTo(Screen.BookDetail(bookId)) },
                    onDesignBook = { bookId -> viewModel.navigateTo(Screen.BookDesigner(bookId)) },
                    onExportBook = { bookId -> viewModel.navigateTo(Screen.ExportHub(bookId)) },
                    onToggleFavorite = { book -> viewModel.toggleFavorite(book) },
                    onToggleArchive = { book -> viewModel.toggleArchive(book) },
                    onMoveToTrash = { book -> viewModel.moveToTrash(book) },
                    onRestoreFromTrash = { book -> viewModel.restoreFromTrash(book) },
                    onHardDeleteBook = { id -> viewModel.hardDeleteBook(id) },
                    onEmptyTrash = { viewModel.emptyTrash() },
                    onCreateBook = { title, subtitle, author, genre, lang ->
                        viewModel.createBook(title, subtitle, author, genre, lang)
                    },
                    onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) },
                    onNavigateToBackup = { viewModel.navigateTo(Screen.Backup) }
                )
            }

            is Screen.BookDetail -> {
                val book = activeBooks.find { it.id == screen.bookId }
                    ?: archivedBooks.find { it.id == screen.bookId }
                    ?: favoriteBooks.find { it.id == screen.bookId }

                if (book != null) {
                    val sections = remember(book.id) {
                        viewModel.repository.getSectionsForBook(book.id)
                    }.collectAsStateWithLifecycle(initialValue = emptyList()).value

                    val chapters = remember(book.id) {
                        viewModel.repository.getChaptersForBook(book.id)
                    }.collectAsStateWithLifecycle(initialValue = emptyList()).value

                    BookDetailScreen(
                        book = book,
                        sections = sections,
                        chapters = chapters,
                        language = language,
                        onBack = { viewModel.navigateBack() },
                        onOpenChapter = { chapterId -> viewModel.navigateTo(Screen.ChapterEditor(chapterId)) },
                        onDesignBook = { viewModel.navigateTo(Screen.BookDesigner(book.id)) },
                        onExportBook = { viewModel.navigateTo(Screen.ExportHub(book.id)) },
                        onCreateChapter = { title -> viewModel.createChapter(book.id, title, chapters.size) },
                        onDuplicateChapter = { ch -> viewModel.duplicateChapter(ch) },
                        onDeleteChapter = { ch -> viewModel.deleteChapter(ch) }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }
            }

            is Screen.ChapterEditor -> {
                val chapterFlow = remember(screen.chapterId) {
                    viewModel.repository.getChapter(screen.chapterId)
                }
                val chapter by chapterFlow.collectAsStateWithLifecycle(initialValue = null)

                var initialBlocks by remember { mutableStateOf<List<ChapterBlock>?>(null) }
                var recoveryDraft by remember { mutableStateOf<Pair<String, List<ChapterBlock>>?>(null) }
                var isLoaded by remember { mutableStateOf(false) }

                LaunchedEffect(screen.chapterId) {
                    initialBlocks = viewModel.repository.getBlocksForChapterDirect(screen.chapterId)
                    recoveryDraft = viewModel.repository.checkRecoveryJournal(screen.chapterId)
                    isLoaded = true
                }

                if (chapter != null && isLoaded && initialBlocks != null) {
                    ChapterEditorScreen(
                        chapter = chapter!!,
                        initialBlocks = initialBlocks!!,
                        recoveryDraft = recoveryDraft,
                        language = language,
                        onSaveAndExit = { newTitle, blocks, status ->
                            viewModel.saveChapterOnExit(chapter!!.id, newTitle, blocks, status)
                        },
                        onRecoveryCheckpoint = { draftTitle, blocks ->
                            viewModel.checkpointRecoveryJournal(chapter!!.id, draftTitle, blocks)
                        },
                        onDiscardRecovery = {
                            viewModel.discardRecoveryDraft(chapter!!.id)
                        },
                        onDirectExit = {
                            viewModel.navigateBack()
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }
            }

            is Screen.BookDesigner -> {
                val book = activeBooks.find { it.id == screen.bookId }
                    ?: archivedBooks.find { it.id == screen.bookId }
                    ?: favoriteBooks.find { it.id == screen.bookId }

                var design by remember { mutableStateOf<BookDesignEntity?>(null) }

                LaunchedEffect(screen.bookId) {
                    design = viewModel.repository.getDesignDirect(screen.bookId)
                }

                if (book != null && design != null) {
                    BookDesignerScreen(
                        book = book,
                        initialDesign = design!!,
                        language = language,
                        onBack = { viewModel.navigateBack() },
                        onSaveDesign = { updatedDesign ->
                            viewModel.updateBookDesign(updatedDesign)
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }
            }

            is Screen.ExportHub -> {
                val book = activeBooks.find { it.id == screen.bookId }
                    ?: archivedBooks.find { it.id == screen.bookId }
                    ?: favoriteBooks.find { it.id == screen.bookId }

                var chaptersCount by remember { mutableIntStateOf(0) }
                var wordsCount by remember { mutableIntStateOf(0) }
                var isReady by remember { mutableStateOf(false) }

                LaunchedEffect(screen.bookId) {
                    val chapters = viewModel.repository.getChaptersForBookDirect(screen.bookId)
                    chaptersCount = chapters.size
                    var totalWords = 0
                    chapters.forEach { ch ->
                        val blks = viewModel.repository.getBlocksForChapterDirect(ch.id)
                        blks.forEach { b ->
                            totalWords += b.text.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
                        }
                    }
                    wordsCount = totalWords
                    isReady = true
                }

                if (book != null && isReady) {
                    ExportHubScreen(
                        book = book,
                        chaptersCount = chaptersCount,
                        wordsCount = wordsCount,
                        language = language,
                        onBack = { viewModel.navigateBack() },
                        onExportFormat = { format ->
                            viewModel.exportBook(screen.bookId, format)
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                }
            }

            is Screen.Settings -> {
                SettingsScreen(
                    currentLanguage = language,
                    currentThemeMode = themeMode,
                    stats = stats,
                    onBack = { viewModel.navigateBack() },
                    onLanguageChanged = { newLang -> viewModel.setLanguage(newLang) },
                    onThemeChanged = { newMode -> viewModel.setThemeMode(newMode) },
                    onClearCache = { viewModel.clearCache() }
                )
            }

            is Screen.Backup -> {
                BackupRestoreScreen(
                    language = language,
                    onBack = { viewModel.navigateBack() },
                    onExportBackup = { viewModel.exportFullBackup() },
                    onImportBackup = { jsonText -> viewModel.importFullBackup(jsonText) }
                )
            }
        }
    }
}
