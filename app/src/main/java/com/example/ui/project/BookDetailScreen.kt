package com.example.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AppLanguage
import com.example.core.model.StringsHelper
import com.example.core.util.BengaliNumberUtil
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.SectionEntity
import com.example.ui.components.BookCoverView
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NotoSerifBengaliFontFamily
import com.example.ui.theme.PrimaryNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: BookEntity,
    sections: List<SectionEntity>,
    chapters: List<ChapterEntity>,
    language: AppLanguage,
    onBack: () -> Unit,
    onOpenChapter: (String) -> Unit,
    onDesignBook: () -> Unit,
    onExportBook: () -> Unit,
    onCreateChapter: (title: String) -> Unit,
    onDuplicateChapter: (ChapterEntity) -> Unit,
    onDeleteChapter: (ChapterEntity) -> Unit
) {
    var showAddChapterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryNavy,
                    titleContentColor = LightSurface,
                    navigationIconContentColor = LightSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = book.title,
                            fontFamily = NotoSerifBengaliFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${StringsHelper.get("অধ্যায় সংখ্যা:", "Chapters:")} ${BengaliNumberUtil.formatNumber(chapters.size, language)}",
                            fontSize = 11.sp,
                            color = GoldAccent
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDesignBook) {
                        Icon(Icons.Default.ColorLens, contentDescription = "Design Studio", tint = GoldAccent)
                    }
                    IconButton(onClick = onExportBook) {
                        Icon(Icons.Default.Publish, contentDescription = "Publish & Export", tint = LightSurface)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddChapterDialog = true },
                containerColor = GoldAccent,
                contentColor = DarkNavy
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Chapter")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(StringsHelper.get("নতুন অধ্যায়", "New Chapter"), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Book Studio Banner Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(85.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            BookCoverView(
                                title = book.title,
                                subtitle = book.subtitle,
                                author = book.author,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title,
                                fontFamily = NotoSerifBengaliFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = PrimaryNavy
                            )

                            if (book.subtitle.isNotEmpty()) {
                                Text(
                                    text = book.subtitle,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${StringsHelper.get("লেখক:", "Author:")} ${book.author}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onDesignBook,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy, contentColor = LightSurface),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldAccent)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(StringsHelper.get("ডিজাইন", "Design"), fontSize = 12.sp)
                                }

                                Button(
                                    onClick = onExportBook,
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DarkNavy),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(StringsHelper.get("এক্সপোর্ট", "Export"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Chapters Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = StringsHelper.get("অধ্যায়সমূহ (Chapters)", "Chapters"),
                        fontFamily = NotoSerifBengaliFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${BengaliNumberUtil.formatNumber(chapters.size, language)} ${StringsHelper.get("টি অধ্যায়", "Chapters")}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (chapters.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StringsHelper.get("কোনো অধ্যায় তৈরি করা হয়নি। নিচে '+ নতুন অধ্যায়' বোতাম চাপুন।", "No chapters yet. Tap '+ New Chapter' below."),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                itemsIndexed(chapters, key = { _, ch -> ch.id }) { index, chapter ->
                    ChapterRowItem(
                        index = index,
                        chapter = chapter,
                        language = language,
                        onOpen = { onOpenChapter(chapter.id) },
                        onDuplicate = { onDuplicateChapter(chapter) },
                        onDelete = { onDeleteChapter(chapter) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddChapterDialog) {
        var chapterTitle by remember { mutableStateOf("") }
        val defaultTitle = if (language == AppLanguage.BANGLA) {
            "অধ্যায় ${BengaliNumberUtil.toBengaliDigits(chapters.size + 1)}: নতুন অধ্যায়"
        } else {
            "Chapter ${chapters.size + 1}: New Chapter"
        }

        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            title = {
                Text(
                    text = StringsHelper.get("নতুন অধ্যায় যুক্ত করুন", "Add New Chapter"),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = StringsHelper.get("অধ্যায়ের শিরোনাম লিখুন:", "Enter chapter title:"),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = chapterTitle,
                        onValueChange = { chapterTitle = it },
                        placeholder = { Text(defaultTitle) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalTitle = chapterTitle.ifBlank { defaultTitle }
                        onCreateChapter(finalTitle)
                        showAddChapterDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DarkNavy)
                ) {
                    Text(StringsHelper.get("যোগ করুন", "Add Chapter"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }) {
                    Text(StringsHelper.get("বাতিল", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun ChapterRowItem(
    index: Int,
    chapter: ChapterEntity,
    language: AppLanguage,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chapter Number badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryNavy.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BengaliNumberUtil.formatNumber(index + 1, language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PrimaryNavy
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title,
                    fontFamily = NotoSerifBengaliFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status tag
                    val statusText = when (chapter.status) {
                        "COMPLETED" -> StringsHelper.get("সম্পন্ন", "Completed")
                        "IN_PROGRESS" -> StringsHelper.get("চলমান", "In Progress")
                        else -> StringsHelper.get("খসড়া", "Draft")
                    }
                    val statusColor = when (chapter.status) {
                        "COMPLETED" -> Color(0xFF2E7D32)
                        "IN_PROGRESS" -> Color(0xFFB45309)
                        else -> Color(0xFF64748B)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = statusText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Rev v${chapter.contentRevision}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Chapter Options", tint = Color.Gray)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(StringsHelper.get("এডিটর খুলুন", "Open Editor")) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryNavy) },
                        onClick = { showMenu = false; onOpen() }
                    )
                    DropdownMenuItem(
                        text = { Text(StringsHelper.get("অনুলিপি তৈরি করুন (Duplicate)", "Duplicate Chapter")) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = GoldAccent) },
                        onClick = { showMenu = false; onDuplicate() }
                    )
                    DropdownMenuItem(
                        text = { Text(StringsHelper.get("অধ্যায় মুছুন", "Delete Chapter"), color = Color(0xFFDC2626)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}
