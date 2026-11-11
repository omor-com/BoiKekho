package com.example.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Unarchive
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.AppLanguage
import com.example.core.model.StringsHelper
import com.example.core.util.BengaliNumberUtil
import com.example.data.entity.BookEntity
import com.example.ui.components.BookCoverView
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NotoSerifBengaliFontFamily
import com.example.ui.theme.PrimaryNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    books: List<BookEntity>,
    favoriteBooks: List<BookEntity>,
    archivedBooks: List<BookEntity>,
    trashBooks: List<BookEntity>,
    language: AppLanguage,
    onSelectBook: (String) -> Unit,
    onDesignBook: (String) -> Unit,
    onExportBook: (String) -> Unit,
    onToggleFavorite: (BookEntity) -> Unit,
    onToggleArchive: (BookEntity) -> Unit,
    onMoveToTrash: (BookEntity) -> Unit,
    onRestoreFromTrash: (BookEntity) -> Unit,
    onHardDeleteBook: (String) -> Unit,
    onEmptyTrash: () -> Unit,
    onCreateBook: (title: String, subtitle: String, author: String, genre: String, lang: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val currentList = when (selectedTab) {
        1 -> favoriteBooks
        2 -> archivedBooks
        3 -> trashBooks
        else -> books
    }.filter {
        if (searchQuery.isBlank()) true
        else it.title.contains(searchQuery, ignoreCase = true) ||
                it.author.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryNavy,
                    titleContentColor = LightSurface
                ),
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(StringsHelper.get("বই বা লেখক খুঁজুন...", "Search books or author..."), color = Color.LightGray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    isSearchActive = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Search", tint = Color.White)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_boilekho_logo),
                                contentDescription = "BoiLekho Logo",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "বইলেখো" else "BoiLekho",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = LightSurface
                                )
                                Text(
                                    text = "Write. Design. Publish.",
                                    fontSize = 10.sp,
                                    color = GoldAccent
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = LightSurface)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab != 3) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = GoldAccent,
                    contentColor = DarkNavy
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Book")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = StringsHelper.get("নতুন বই", "New Book"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (trashBooks.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onEmptyTrash,
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Empty Trash")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsHelper.get("ট্র্যাশ খালি করুন", "Empty Trash"))
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryNavy
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Library") },
                    label = { Text(StringsHelper.get("লাইব্রেরি", "Library")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkNavy,
                        selectedTextColor = PrimaryNavy,
                        indicatorColor = GoldAccent
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToBackup,
                    icon = { Icon(Icons.Default.FolderZip, contentDescription = "Backup") },
                    label = { Text(StringsHelper.get("ব্যাকআপ", "Backup")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkNavy,
                        indicatorColor = GoldAccent
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSettings,
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text(StringsHelper.get("সেটিংস", "Settings")) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkNavy,
                        indicatorColor = GoldAccent
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryNavy,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldAccent
                    )
                }
            ) {
                val tabTitles = listOf(
                    Pair(StringsHelper.get("সকল বই", "All Books"), books.size),
                    Pair(StringsHelper.get("পছন্দের বই", "Favorites"), favoriteBooks.size),
                    Pair(StringsHelper.get("আর্কাইভ", "Archived"), archivedBooks.size),
                    Pair(StringsHelper.get("ট্র্যাশ", "Trash"), trashBooks.size)
                )
                tabTitles.forEachIndexed { index, (label, count) ->
                    val countStr = BengaliNumberUtil.formatNumber(count, language)
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = "$label ($countStr)",
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) PrimaryNavy else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (selectedTab) {
                                1 -> Icons.Default.FavoriteBorder
                                2 -> Icons.Default.Archive
                                3 -> Icons.Default.Delete
                                else -> Icons.Default.MenuBook
                            },
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (selectedTab) {
                                1 -> StringsHelper.get("কোনো পছন্দের বই সংরক্ষিত নেই", "No favorite books yet")
                                2 -> StringsHelper.get("আর্কাইভে কোনো বই নেই", "No archived books")
                                3 -> StringsHelper.get("ট্র্যাশ খালি আছে", "Trash is empty")
                                else -> StringsHelper.get("এখনো কোনো বই তৈরি করা হয়নি", "No books in library")
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedTab == 0) {
                            Text(
                                text = StringsHelper.get("নিচের '+ নতুন বই' বোতামে চাপ দিয়ে প্রথম গ্রন্থ রচনা শুরু করুন।", "Tap '+ New Book' to begin authoring your masterpiece."),
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(currentList, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            isInTrash = selectedTab == 3,
                            onSelectBook = { onSelectBook(book.id) },
                            onDesignBook = { onDesignBook(book.id) },
                            onExportBook = { onExportBook(book.id) },
                            onToggleFavorite = { onToggleFavorite(book) },
                            onToggleArchive = { onToggleArchive(book) },
                            onMoveToTrash = { onMoveToTrash(book) },
                            onRestoreFromTrash = { onRestoreFromTrash(book) },
                            onHardDeleteBook = { onHardDeleteBook(book.id) }
                        )
                    }
                }
            }
        }
    }

    // Create New Book Dialog
    if (showCreateDialog) {
        CreateBookDialog(
            language = language,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, subtitle, author, genre, lang ->
                onCreateBook(title, subtitle, author, genre, lang)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun BookCard(
    book: BookEntity,
    isInTrash: Boolean,
    onSelectBook: () -> Unit,
    onDesignBook: () -> Unit,
    onExportBook: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleArchive: () -> Unit,
    onMoveToTrash: () -> Unit,
    onRestoreFromTrash: () -> Unit,
    onHardDeleteBook: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { if (!isInTrash) onSelectBook() }),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniature Book Cover View
            Box(
                modifier = Modifier
                    .width(76.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                BookCoverView(
                    title = book.title,
                    subtitle = book.subtitle,
                    author = book.author,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = book.title,
                        fontFamily = NotoSerifBengaliFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!isInTrash) {
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (book.isFavorite) Color(0xFFDC2626) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (!isInTrash) {
                                DropdownMenuItem(
                                    text = { Text(StringsHelper.get("গ্রন্থ স্টুডিও খুলুন", "Open Studio")) },
                                    leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, tint = PrimaryNavy) },
                                    onClick = { showMenu = false; onSelectBook() }
                                )
                                DropdownMenuItem(
                                    text = { Text(StringsHelper.get("কভার ও পৃষ্ঠা ডিজাইন", "Design Cover & Interior")) },
                                    leadingIcon = { Icon(Icons.Default.ColorLens, contentDescription = null, tint = GoldAccent) },
                                    onClick = { showMenu = false; onDesignBook() }
                                )
                                DropdownMenuItem(
                                    text = { Text(StringsHelper.get("প্রকাশ ও এক্সপোর্ট (PDF/EPUB)", "Publish & Export")) },
                                    leadingIcon = { Icon(Icons.Default.Publish, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                    onClick = { showMenu = false; onExportBook() }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (book.isArchived) StringsHelper.get("আর্কাইভ থেকে আনুন", "Unarchive") else StringsHelper.get("আর্কাইভ করুন", "Archive")) },
                                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                                    onClick = { showMenu = false; onToggleArchive() }
                                )
                                DropdownMenuItem(
                                    text = { Text(StringsHelper.get("ট্র্যাশে পাঠান", "Move to Trash"), color = Color(0xFFDC2626)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
                                    onClick = { showMenu = false; onMoveToTrash() }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(StringsHelper.get("পুনরুদ্ধার করুন", "Restore")) },
                                    leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                    onClick = { showMenu = false; onRestoreFromTrash() }
                                )
                                DropdownMenuItem(
                                    text = { Text(StringsHelper.get("চিরতরে মুছে ফেলুন", "Delete Forever"), color = Color(0xFFDC2626)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
                                    onClick = { showMenu = false; onHardDeleteBook() }
                                )
                            }
                        }
                    }
                }

                if (book.subtitle.isNotEmpty()) {
                    Text(
                        text = book.subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${StringsHelper.get("লেখক:", "Author:")} ${book.author}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action chips row
                if (!isInTrash) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onDesignBook() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🎨 ${StringsHelper.get("ডিজাইন", "Design")}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryNavy
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldAccent.copy(alpha = 0.2f))
                                .clickable { onExportBook() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🚀 ${StringsHelper.get("এক্সপোর্ট", "Export")}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateBookDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onCreate: (title: String, subtitle: String, author: String, genre: String, lang: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("Muhammad Omor") }
    var genre by remember { mutableStateOf("সাহিত্য ও গবেষণা") }
    var selectedLang by remember { mutableStateOf(if (language == AppLanguage.BANGLA) "bn" else "en") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = StringsHelper.get("নতুন গ্রন্থ রচনা শুরু করুন", "Create New Book Project"),
                fontWeight = FontWeight.Bold,
                color = PrimaryNavy
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(StringsHelper.get("গ্রন্থের নাম (Title) *", "Book Title *")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text(StringsHelper.get("উপ-শিরোনাম (Subtitle)", "Subtitle")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text(StringsHelper.get("লেখকের নাম (Author)", "Author Name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text(StringsHelper.get("বইয়ের ধরণ (Genre)", "Genre")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, subtitle, author, genre, selectedLang)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DarkNavy)
            ) {
                Text(StringsHelper.get("তৈরি করুন", "Create Book"), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(StringsHelper.get("বাতিল", "Cancel"))
            }
        }
    )
}
