package com.example.ui.designer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AppLanguage
import com.example.core.model.StringsHelper
import com.example.data.entity.BookDesignEntity
import com.example.data.entity.BookEntity
import com.example.ui.components.BookCoverView
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NotoSerifBengaliFontFamily
import com.example.ui.theme.PrimaryNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDesignerScreen(
    book: BookEntity,
    initialDesign: BookDesignEntity,
    language: AppLanguage,
    onBack: () -> Unit,
    onSaveDesign: (BookDesignEntity) -> Unit
) {
    var coverTemplate by remember { mutableStateOf(initialDesign.coverTemplate) }
    var coverBgColor by remember { mutableStateOf(initialDesign.coverBgColor) }
    var coverTitleColor by remember { mutableStateOf(initialDesign.coverTitleColor) }
    var coverAuthorColor by remember { mutableStateOf(initialDesign.coverAuthorColor) }
    var pageSize by remember { mutableStateOf(initialDesign.pageSize) }
    var pageMargins by remember { mutableStateOf(initialDesign.pageMargins) }
    var fontChoice by remember { mutableStateOf(initialDesign.fontChoice) }
    var lineSpacingMultiplier by remember { mutableFloatStateOf(initialDesign.lineSpacingMultiplier) }
    var includeToc by remember { mutableStateOf(initialDesign.includeToc) }
    var pageNumberFormat by remember { mutableStateOf(initialDesign.pageNumberFormat) }

    val currentDesign = initialDesign.copy(
        coverTemplate = coverTemplate,
        coverBgColor = coverBgColor,
        coverTitleColor = coverTitleColor,
        coverAuthorColor = coverAuthorColor,
        pageSize = pageSize,
        pageMargins = pageMargins,
        fontChoice = fontChoice,
        lineSpacingMultiplier = lineSpacingMultiplier,
        includeToc = includeToc,
        pageNumberFormat = pageNumberFormat
    )

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
                    Text(
                        text = StringsHelper.get("বইয়ের কভার ও লেআউট ডিজাইনার", "Cover & Layout Studio"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                actions = {
                    Button(
                        onClick = { onSaveDesign(currentDesign) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DarkNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(StringsHelper.get("সেভ করুন", "Save"), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Interactive Front Cover Preview
            Text(
                text = StringsHelper.get("কভারের লাইভ প্রিভিউ (Live Preview)", "Live Cover Preview"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = PrimaryNavy
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.width(170.dp)) {
                    BookCoverView(
                        title = book.title,
                        subtitle = book.subtitle,
                        author = book.author,
                        design = currentDesign,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Cover Template Presets
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = StringsHelper.get("কভার টেমপ্লেট নির্বাচন করুন:", "Select Cover Style:"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val templates = listOf(
                        Triple("CLASSIC_NAVY", "#0B2345", "#F2A900" to "ক্লাসিক নেভি"),
                        Triple("GOLD_ROYAL", "#1A1405", "#F2A900" to "রয়্যাল গোল্ড"),
                        Triple("MINIMAL_CREAM", "#F8F5ED", "#0B2345" to "মিনিমাল ক্রিম"),
                        Triple("LITERARY_DARK", "#0B0F16", "#E2E8F0" to "সাহিত্য ডার্ক"),
                        Triple("MODERN_EMERALD", "#064E3B", "#F2A900" to "পান্না সবুজ")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        templates.forEach { (tplName, bgHex, titlePair) ->
                            val isSelected = coverTemplate == tplName
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        coverTemplate = tplName
                                        coverBgColor = bgHex
                                        coverTitleColor = titlePair.first
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(bgHex)))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) GoldAccent else Color.Gray,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = titlePair.second,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryNavy else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Interior Page Setup (A5, A4, margins, typography)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = StringsHelper.get("বইয়ের পৃষ্ঠা বিন্যাস ও সাইজ (Page & Margins):", "Page Layout & Sizing:"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryNavy
                    )

                    // Page size selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "A5" to "A5 (মানসম্মত বই)",
                            "A4" to "A4 (বড় পাতা)",
                            "LETTER" to "Letter",
                            "POCKET" to "Pocket (পকেট বুক)"
                        ).forEach { (code, label) ->
                            FilterChip(
                                selected = pageSize == code,
                                onClick = { pageSize = code },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent.copy(alpha = 0.3f),
                                    selectedLabelColor = PrimaryNavy
                                )
                            )
                        }
                    }

                    // Margins
                    Text(
                        text = StringsHelper.get("পৃষ্ঠার মার্জিন (Margins):", "Page Margins:"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("COMPACT" to "সংকীর্ণ (Compact)", "STANDARD" to "স্বাভাবিক (Standard)", "GENEROUS" to "প্রশস্ত (Generous)").forEach { (code, label) ->
                            FilterChip(
                                selected = pageMargins == code,
                                onClick = { pageMargins = code },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent.copy(alpha = 0.3f),
                                    selectedLabelColor = PrimaryNavy
                                )
                            )
                        }
                    }

                    // Line Spacing
                    Text(
                        text = StringsHelper.get("লাইনের ব্যবধান (Line Spacing):", "Line Spacing:"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1.2f to "1.2x", 1.5f to "1.5x (আদর্শ)", 1.8f to "1.8x", 2.0f to "2.0x").forEach { (mult, label) ->
                            FilterChip(
                                selected = lineSpacingMultiplier == mult,
                                onClick = { lineSpacingMultiplier = mult },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent.copy(alpha = 0.3f),
                                    selectedLabelColor = PrimaryNavy
                                )
                            )
                        }
                    }

                    // TOC & Page numbers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StringsHelper.get("স্বয়ংক্রিয় সূচিপত্র যোগ করুন", "Include Table of Contents"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = StringsHelper.get("বইয়ের শুরুতে অধ্যায়সমূহের তালিকা যুক্ত হবে", "Auto-generate TOC at the beginning"),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = includeToc,
                            onCheckedChange = { includeToc = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = PrimaryNavy)
                        )
                    }

                    // Page number format
                    Text(
                        text = StringsHelper.get("পৃষ্ঠা নম্বর ফরম্যাট:", "Page Number Format:"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "BANGLA_DASH" to "— ১ — (বাংলা ড্যাশ)",
                            "ARABIC" to "1, 2, 3",
                            "ENGLISH_PAGE" to "Page 1"
                        ).forEach { (code, label) ->
                            FilterChip(
                                selected = pageNumberFormat == code,
                                onClick = { pageNumberFormat = code },
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

            Button(
                onClick = { onSaveDesign(currentDesign) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DarkNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(StringsHelper.get("ডিজাইন সেটিংস সংরক্ষণ করুন", "Apply & Save Design"), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
