package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AppLanguage
import com.example.core.model.BlockType
import com.example.core.model.ChapterBlock
import com.example.core.model.StringsHelper
import com.example.core.util.BengaliNumberUtil
import com.example.data.entity.ChapterEntity
import com.example.ui.components.BlockItemView
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NotoSerifBengaliFontFamily
import com.example.ui.theme.PrimaryNavy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditorScreen(
    chapter: ChapterEntity,
    initialBlocks: List<ChapterBlock>,
    recoveryDraft: Pair<String, List<ChapterBlock>>?,
    language: AppLanguage,
    onSaveAndExit: (newTitle: String, blocks: List<ChapterBlock>, status: String) -> Unit,
    onRecoveryCheckpoint: (title: String, blocks: List<ChapterBlock>) -> Unit,
    onDiscardRecovery: () -> Unit,
    onDirectExit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf(chapter.title) }
    var status by remember { mutableStateOf(chapter.status) }
    val blocks = remember {
        mutableStateListOf<ChapterBlock>().apply {
            if (initialBlocks.isEmpty()) {
                add(ChapterBlock(text = ""))
            } else {
                addAll(initialBlocks)
            }
        }
    }

    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(recoveryDraft != null) }
    var showFindReplaceBar by remember { mutableStateOf(false) }

    // Find & Replace state
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }

    // Statistics derived state
    val totalWords by remember {
        derivedStateOf {
            blocks.sumOf { b ->
                b.text.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }
            }
        }
    }
    val totalChars by remember {
        derivedStateOf {
            blocks.sumOf { it.text.length }
        }
    }

    // Periodic Recovery Checkpoint (Section 7.4 of Blueprint)
    LaunchedEffect(hasUnsavedChanges, title, blocks.toList()) {
        if (hasUnsavedChanges) {
            delay(4000) // 4-second debounced checkpoint
            onRecoveryCheckpoint(title, blocks.toList())
        }
    }

    // Intercept Back Press for Save-on-Exit Safety (Section 7.3)
    BackHandler {
        if (hasUnsavedChanges) {
            showExitConfirmDialog = true
        } else {
            onDirectExit()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryNavy,
                    titleContentColor = LightSurface,
                    navigationIconContentColor = LightSurface
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            showExitConfirmDialog = true
                        } else {
                            onDirectExit()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = title,
                            fontFamily = NotoSerifBengaliFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (hasUnsavedChanges) {
                                Text(
                                    text = "• ${StringsHelper.get("পরিবর্তিত (Unsaved)", "Unsaved")}",
                                    fontSize = 10.sp,
                                    color = GoldAccent
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "${BengaliNumberUtil.formatNumber(totalWords, language)} ${StringsHelper.get("শব্দ", "words")} | ${BengaliNumberUtil.formatReadingTime(totalWords, language)}",
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                },
                actions = {
                    // Find/Replace toggle
                    IconButton(onClick = { showFindReplaceBar = !showFindReplaceBar }) {
                        Icon(Icons.Default.FindReplace, contentDescription = "Find & Replace", tint = LightSurface)
                    }

                    // SAVE & EXIT BUTTON (Section 7.2)
                    Button(
                        onClick = {
                            onSaveAndExit(title, blocks.toList(), status)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = DarkNavy
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = StringsHelper.get("Save & Exit", "Save & Exit"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Find & Replace Banner
            AnimatedVisibility(visible = showFindReplaceBar) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(StringsHelper.get("শব্দ খুঁজুন...", "Find text...")) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = replaceQuery,
                                onValueChange = { replaceQuery = it },
                                placeholder = { Text(StringsHelper.get("প্রতিস্থাপন করুন...", "Replace with...")) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    if (searchQuery.isNotEmpty()) {
                                        var count = 0
                                        for (i in blocks.indices) {
                                            if (blocks[i].text.contains(searchQuery)) {
                                                val replaced = blocks[i].text.replace(searchQuery, replaceQuery)
                                                blocks[i] = blocks[i].copy(text = replaced)
                                                count++
                                            }
                                        }
                                        hasUnsavedChanges = true
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                StringsHelper.get("$count টি জায়গায় প্রতিস্থাপন সম্পন্ন", "Replaced in $count places")
                                            )
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy, contentColor = LightSurface)
                            ) {
                                Text(StringsHelper.get("সব প্রতিস্থাপন করুন", "Replace All"), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Quick Insertion Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reference Box Button (Highlight)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryNavy)
                        .clickable {
                            blocks.add(
                                ChapterBlock(
                                    id = UUID.randomUUID().toString(),
                                    type = BlockType.REFERENCE_BOX,
                                    text = "",
                                    caption = "উৎসসূত্র (Source Note)",
                                    orderIndex = blocks.size
                                )
                            )
                            hasUnsavedChanges = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ ${StringsHelper.get("রেফারেন্স বক্স", "Reference Box")}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightSurface
                        )
                    }
                }

                // Learning Box Button (Highlight)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldAccent)
                        .clickable {
                            blocks.add(
                                ChapterBlock(
                                    id = UUID.randomUUID().toString(),
                                    type = BlockType.LEARNING_BOX,
                                    text = "",
                                    caption = "শিক্ষণীয় সারসংক্ষেপ (Learning Lesson)",
                                    orderIndex = blocks.size
                                )
                            )
                            hasUnsavedChanges = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ ${StringsHelper.get("শিক্ষণীয় বক্স", "Learning Box")}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy
                        )
                    }
                }

                // Paragraph
                InsertChip(label = "+ অনুচ্ছেদ (P)") {
                    blocks.add(ChapterBlock(id = UUID.randomUUID().toString(), type = BlockType.PARAGRAPH, orderIndex = blocks.size))
                    hasUnsavedChanges = true
                }

                // Heading 1
                InsertChip(label = "+ শিরোনাম (H1)") {
                    blocks.add(ChapterBlock(id = UUID.randomUUID().toString(), type = BlockType.HEADING_1, orderIndex = blocks.size))
                    hasUnsavedChanges = true
                }

                // Heading 2
                InsertChip(label = "+ উপ-শিরোনাম (H2)") {
                    blocks.add(ChapterBlock(id = UUID.randomUUID().toString(), type = BlockType.HEADING_2, orderIndex = blocks.size))
                    hasUnsavedChanges = true
                }

                // Quote
                InsertChip(label = "+ উদ্ধৃতি (Quote)") {
                    blocks.add(ChapterBlock(id = UUID.randomUUID().toString(), type = BlockType.QUOTE, orderIndex = blocks.size))
                    hasUnsavedChanges = true
                }

                // Divider
                InsertChip(label = "+ বিভাজক (❦)") {
                    blocks.add(ChapterBlock(id = UUID.randomUUID().toString(), type = BlockType.DIVIDER, text = "❦   ❦   ❦", orderIndex = blocks.size))
                    hasUnsavedChanges = true
                }
            }

            // Chapter Title Field
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = StringsHelper.get("অধ্যায়ের নাম:", "Chapter Title:"),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            hasUnsavedChanges = true
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = NotoSerifBengaliFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PrimaryNavy
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = StringsHelper.get("অবস্থা:", "Status:"),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        listOf("DRAFT" to "খসড়া", "IN_PROGRESS" to "চলমান", "COMPLETED" to "সম্পন্ন").forEach { (code, label) ->
                            FilterChip(
                                selected = status == code,
                                onClick = {
                                    status = code
                                    hasUnsavedChanges = true
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent.copy(alpha = 0.3f),
                                    selectedLabelColor = PrimaryNavy
                                )
                            )
                        }
                    }
                }
            }

            // Blocks List (Editable with reordering and specialized components)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
                    BlockItemView(
                        block = block,
                        canMoveUp = index > 0,
                        canMoveDown = index < blocks.size - 1,
                        onTextChanged = { newText ->
                            blocks[index] = block.copy(text = newText)
                            hasUnsavedChanges = true
                        },
                        onCaptionChanged = { newCaption ->
                            blocks[index] = block.copy(caption = newCaption)
                            hasUnsavedChanges = true
                        },
                        onMoveUp = {
                            if (index > 0) {
                                val item = blocks.removeAt(index)
                                blocks.add(index - 1, item)
                                hasUnsavedChanges = true
                            }
                        },
                        onMoveDown = {
                            if (index < blocks.size - 1) {
                                val item = blocks.removeAt(index)
                                blocks.add(index + 1, item)
                                hasUnsavedChanges = true
                            }
                        },
                        onDelete = {
                            if (blocks.size > 1) {
                                blocks.removeAt(index)
                            } else {
                                blocks[0] = ChapterBlock(id = UUID.randomUUID().toString(), text = "")
                            }
                            hasUnsavedChanges = true
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Safety Exit Confirmation Dialog (Section 7.3 Back Button policy)
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = StringsHelper.get("পরিবর্তনগুলো সেভ করবেন?", "Save Unsaved Changes?"),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            },
            text = {
                Text(
                    text = StringsHelper.get(
                        "আপনার লেখা এখনও প্রধান ডাটাবেসে সেভ করা হয়নি। আপনি কি সেভ করে বের হতে চান?",
                        "Your latest changes are not yet persisted to the primary database. Would you like to Save & Exit?"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onSaveAndExit(title, blocks.toList(), status)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DarkNavy)
                ) {
                    Text(StringsHelper.get("Save & Exit", "Save & Exit"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showExitConfirmDialog = false
                        onDirectExit()
                    }) {
                        Text(StringsHelper.get("বাতিল করে বের হন", "Discard"), color = Color(0xFFDC2626))
                    }
                    TextButton(onClick = { showExitConfirmDialog = false }) {
                        Text(StringsHelper.get("লেখা চালিয়ে যান", "Keep Editing"))
                    }
                }
            }
        )
    }

    // Crash Recovery Journal Dialog (Section 7.4 of Blueprint)
    if (showRecoveryDialog && recoveryDraft != null) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = StringsHelper.get("অপ্রত্যাশিত বন্ধ হওয়া খসড়া পাওয়া গেছে", "Recovery Draft Detected"),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                }
            },
            text = {
                Text(
                    text = StringsHelper.get(
                        "পূর্ববর্তী সেশনে অপ্রত্যাশিতভাবে অ্যাপ বন্ধ হওয়ার কারণে একটি অসম্পূর্ণ খসড়া রিকভারি জার্নালে পাওয়া গেছে। আপনি কি এটি পুনরুদ্ধার করতে চান?",
                        "An uncommitted recovery draft from a previous unexpected shutdown was found. Would you like to recover this draft?"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        title = recoveryDraft.first
                        blocks.clear()
                        blocks.addAll(recoveryDraft.second)
                        hasUnsavedChanges = true
                        showRecoveryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DarkNavy)
                ) {
                    Text(StringsHelper.get("খসড়া পুনরুদ্ধার করুন", "Recover Draft"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDiscardRecovery()
                    showRecoveryDialog = false
                }) {
                    Text(StringsHelper.get("বাতিল করুন", "Discard Draft"), color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun InsertChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
