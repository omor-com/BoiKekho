package com.example.ui.export

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AppLanguage
import com.example.core.model.StringsHelper
import com.example.core.util.BengaliNumberUtil
import com.example.data.entity.BookEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NotoSerifBengaliFontFamily
import com.example.ui.theme.PrimaryNavy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportHubScreen(
    book: BookEntity,
    chaptersCount: Int,
    wordsCount: Int,
    language: AppLanguage,
    onBack: () -> Unit,
    onExportFormat: suspend (format: String) -> Pair<Boolean, String>
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var exportingFormat by remember { mutableStateOf<String?>(null) }

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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = StringsHelper.get("প্রকাশ ও এক্সপোর্ট স্টুডিও", "Publish & Export Studio"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Book Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = book.title,
                            fontFamily = NotoSerifBengaliFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = GoldAccent
                        )
                        if (book.subtitle.isNotEmpty()) {
                            Text(text = book.subtitle, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = StringsHelper.get("অধ্যায় সংখ্যা", "Chapters"),
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = BengaliNumberUtil.formatNumber(chaptersCount, language),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightSurface
                                )
                            }
                            Column {
                                Text(
                                    text = StringsHelper.get("মোট শব্দ", "Total Words"),
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = BengaliNumberUtil.formatNumber(wordsCount, language),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightSurface
                                )
                            }
                            Column {
                                Text(
                                    text = StringsHelper.get("আনুমানিক পৃষ্ঠা", "Est. Pages"),
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                val estPages = maxOf(1, wordsCount / 250)
                                Text(
                                    text = BengaliNumberUtil.formatNumber(estPages, language),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightSurface
                                )
                            }
                        }
                    }
                }
            }

            // Exporter Items
            item {
                Text(
                    text = StringsHelper.get("এক্সপোর্ট ফরম্যাট নির্বাচন করুন:", "Available Export Formats:"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PrimaryNavy
                )
            }

            // 1. PDF Document
            item {
                ExportFormatCard(
                    title = "PDF Document (.pdf)",
                    description = StringsHelper.get(
                        "প্রিন্ট উপযোগী পূর্ণাঙ্গ পিডিএফ। কভার পেজ, সূচিপত্র, হেডার-ফুটার ও বাংলা অক্ষরের সঠিক টেক্সট-শেপিং সহ।",
                        "Print-ready PDF with cover, table of contents, headers, footers and accurate typography."
                    ),
                    icon = Icons.Default.PictureAsPdf,
                    accentColor = Color(0xFFDC2626),
                    badge = StringsHelper.get("প্রিন্ট ও পাঠ", "Print Ready"),
                    isExporting = exportingFormat == "PDF",
                    onExport = {
                        exportingFormat = "PDF"
                        scope.launch {
                            val (success, message) = onExportFormat("PDF")
                            exportingFormat = null
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }

            // 2. EPUB 3 Digital Book
            item {
                ExportFormatCard(
                    title = "EPUB 3 Digital Book (.epub)",
                    description = StringsHelper.get(
                        "ই-বুক স্ট্যান্ডার্ড প্যাকেজ। Google Play Books, Apple Books এবং Moon+ Reader-এ পাঠযোগ্য।",
                        "Standard digital eBook package for Play Books, Apple Books, and e-readers."
                    ),
                    icon = Icons.Default.Book,
                    accentColor = Color(0xFF2563EB),
                    badge = StringsHelper.get("ই-বুক ফরম্যাট", "eBook"),
                    isExporting = exportingFormat == "EPUB",
                    onExport = {
                        exportingFormat = "EPUB"
                        scope.launch {
                            val (success, message) = onExportFormat("EPUB")
                            exportingFormat = null
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }

            // 3. Offline Responsive HTML
            item {
                ExportFormatCard(
                    title = "Offline Web Page (.html)",
                    description = StringsHelper.get(
                        "স্বয়ংসম্পূর্ণ রেসপনসিভ ওয়েব পেজ। কোনো ইন্টারনেট ছাড়াই যেকোনো ব্রাউজারে নিখুঁতভাবে খোলা যাবে।",
                        "Self-contained responsive web document readable on any phone or PC browser offline."
                    ),
                    icon = Icons.Default.Html,
                    accentColor = Color(0xFF0D9488),
                    badge = StringsHelper.get("অফলাইন ওয়েব", "Offline Web"),
                    isExporting = exportingFormat == "HTML",
                    onExport = {
                        exportingFormat = "HTML"
                        scope.launch {
                            val (success, message) = onExportFormat("HTML")
                            exportingFormat = null
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }

            // 4. Markdown
            item {
                ExportFormatCard(
                    title = "Markdown Document (.md)",
                    description = StringsHelper.get(
                        "হেডিংস, কোট ও কোড ব্লক সহ পরিষ্কার টেক্সট মার্কডাউন ফরম্যাট।",
                        "Clean markdown format with headings, quotes, and structured text."
                    ),
                    icon = Icons.Default.Code,
                    accentColor = Color(0xFF7C3AED),
                    badge = StringsHelper.get("ডিজিটাল নোট", "Markdown"),
                    isExporting = exportingFormat == "MD",
                    onExport = {
                        exportingFormat = "MD"
                        scope.launch {
                            val (success, message) = onExportFormat("MD")
                            exportingFormat = null
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }

            // 5. Plain Text
            item {
                ExportFormatCard(
                    title = "Plain Text (.txt)",
                    description = StringsHelper.get(
                        "ক্লাসিক প্লেইন টেক্সট ফাইল, যেখানে অধ্যায়সমূহের মধ্যে আলংকারিক বিভাজক রেখা থাকবে।",
                        "Plain UTF-8 text file with ornamental chapter separators."
                    ),
                    icon = Icons.Default.Description,
                    accentColor = Color(0xFF475569),
                    badge = StringsHelper.get("সাধারণ টেক্সট", "Plain Text"),
                    isExporting = exportingFormat == "TXT",
                    onExport = {
                        exportingFormat = "TXT"
                        scope.launch {
                            val (success, message) = onExportFormat("TXT")
                            exportingFormat = null
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExportFormatCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    badge: String,
    isExporting: Boolean,
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryNavy
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                    }
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onExport,
                enabled = !isExporting,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy, contentColor = LightSurface),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        color = GoldAccent,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(StringsHelper.get("প্রস্তুত করা হচ্ছে...", "Exporting..."), fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldAccent)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(StringsHelper.get("এক্সপোর্ট ও শেয়ার করুন", "Export & Share"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
