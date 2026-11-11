package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.AppLanguage
import com.example.core.model.StringsHelper
import com.example.core.util.BengaliNumberUtil
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NotoSerifBengaliFontFamily
import com.example.ui.theme.PrimaryNavy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    currentThemeMode: String, // "SYSTEM", "LIGHT", "DARK"
    stats: Map<String, Any>,
    onBack: () -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onThemeChanged: (String) -> Unit,
    onClearCache: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

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
                        text = StringsHelper.get("স্টুডিও সেটিংস ও পরিচিতি", "Studio Settings"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
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
            // App & Developer Branding Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_boilekho_logo),
                        contentDescription = "BoiLekho Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "BoiLekho (বইলেখো)",
                        fontFamily = NotoSerifBengaliFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PrimaryNavy
                    )

                    Text(
                        text = "Write. Design. Publish.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldAccent
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Advanced Book Writing, Designing & Publishing Studio",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Version 1.0.0 | Offline-First, Local-First Architecture",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            // Language Selection
            SettingsSectionCard(title = StringsHelper.get("অ্যাপের ভাষা (Language)", "App Language"), icon = Icons.Default.Language) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        AppLanguage.BANGLA to "বাংলা (Bengali)",
                        AppLanguage.ENGLISH to "English"
                    ).forEach { (lang, label) ->
                        FilterChip(
                            selected = currentLanguage == lang,
                            onClick = { onLanguageChanged(lang) },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent.copy(alpha = 0.35f),
                                selectedLabelColor = PrimaryNavy
                            )
                        )
                    }
                }
            }

            // Theme Selection
            SettingsSectionCard(title = StringsHelper.get("থিম ও ভিজ্যুয়াল লুক", "Theme & Appearance"), icon = Icons.Default.ColorLens) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "SYSTEM" to StringsHelper.get("সিস্টেম ডিফল্ট", "System Default"),
                        "LIGHT" to StringsHelper.get("লাইট মোড", "Light"),
                        "DARK" to StringsHelper.get("ডার্ক মোড", "Dark")
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = currentThemeMode == mode,
                            onClick = { onThemeChanged(mode) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent.copy(alpha = 0.35f),
                                selectedLabelColor = PrimaryNavy
                            )
                        )
                    }
                }
            }

            // Storage & Database Diagnostics
            SettingsSectionCard(title = StringsHelper.get("লোকাল স্টোরেজ ও নিরাপত্তা", "Storage & Local Database"), icon = Icons.Default.Storage) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val booksCount = (stats["booksCount"] as? Int) ?: 0
                    val chaptersCount = (stats["chaptersCount"] as? Int) ?: 0
                    val wordsCount = (stats["wordsCount"] as? Int) ?: 0

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(StringsHelper.get("সক্রিয় গ্রন্থ:", "Active Books:"), fontSize = 13.sp, color = Color.Gray)
                        Text(BengaliNumberUtil.formatNumber(booksCount, currentLanguage), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(StringsHelper.get("মোট অধ্যায়:", "Total Chapters:"), fontSize = 13.sp, color = Color.Gray)
                        Text(BengaliNumberUtil.formatNumber(chaptersCount, currentLanguage), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(StringsHelper.get("মোট রচিত শব্দ:", "Total Authored Words:"), fontSize = 13.sp, color = Color.Gray)
                        Text(BengaliNumberUtil.formatNumber(wordsCount, currentLanguage), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(StringsHelper.get("ডাটাবেস সুরক্ষা:", "Database Sandbox:"), fontSize = 13.sp, color = Color.Gray)
                        Text("SQLite App-Private (Offline)", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            onClearCache()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    StringsHelper.get("ক্যাশ পরিষ্কার করা হয়েছে (আপনার রচিত বই অক্ষত আছে)", "Cache cleared successfully (user books safe)")
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = PrimaryNavy),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsHelper.get("এক্সপোর্ট ক্যাশ পরিষ্কার করুন", "Clear Export Cache"), fontSize = 12.sp)
                    }
                }
            }

            // Developer & Official Shop Links (Section 14 & Blueprint specifications)
            SettingsSectionCard(title = StringsHelper.get("ডেভেলপার ও অফিসিয়াল শপ", "Developer & Official Shop"), icon = Icons.Default.Info) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${StringsHelper.get("ডেভেলপার:", "Developer:")} Muhammad Omor",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )

                    // Email Developer
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:muhammedomor.studio@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "BoiLekho App Inquiry")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(StringsHelper.get("ডেভেলপারের সাথে যোগাযোগ", "Contact Developer"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("muhammedomor.studio@gmail.com", fontSize = 11.sp, color = Color.Gray)
                            }
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Official Shop
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://muhammedomor.supportkori.shop"))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                        colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StringsHelper.get("অফিসিয়াল ডেভেলপার শপ", "Official Developer Shop"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                                Text("muhammedomor.supportkori.shop", fontSize = 11.sp, color = Color(0xFF92400E))
                            }
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Legal & Privacy Policy
            SettingsSectionCard(title = StringsHelper.get("আইনি নীতিমালা ও শর্তাবলী", "Legal & Privacy"), icon = Icons.Default.Security) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(StringsHelper.get("প্রাইভেসি পলিসি", "Privacy Policy"), fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showTermsDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(StringsHelper.get("ব্যবহারের শর্তাবলী", "Terms & Conditions"), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = StringsHelper.get("অফলাইন প্রাইভেসি পলিসি", "Offline Privacy Policy"),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = StringsHelper.get(
                            "১. স্থানীয় ডেটা সংরক্ষণ (Local-First):\nBoiLekho আপনার কোনো গ্রন্থ, অধ্যায় বা ব্যক্তিগত লেখা কোনো দূরবর্তী রিমোট সার্ভারে পাঠায় না। সব তথ্য আপনার ফোনের নিরাপদ SQLite অ্যাপ স্যান্ডবক্সে সংরক্ষিত থাকে।\n\n২. ব্যবহারকারীর পূর্ণ মালিকানা:\nআপনার রচিত সমস্ত বই, কবিতা, উপন্যাস ও গবেষণাকর্মের শতভাগ কপিরাইট ও মালিকানা সম্পূর্ণভাবে আপনার।\n\n৩. এক্সপোর্ট ও শেয়ারিং:\nআপনি স্বেচ্ছায় যখন কোনো PDF, EPUB বা ব্যাকআপ ফাইল এক্সপোর্ট করেন, কেবল তখনই আপনার অনুমোদিত ফাইলটি শেয়ার হয়।",
                            "1. Local-First Data Storage:\nBoiLekho never sends your manuscripts, chapters, or personal writings to remote servers. All data stays strictly in your phone's private SQLite sandbox.\n\n2. 100% User Ownership:\nYou retain full copyright and ownership of all books authored using BoiLekho.\n\n3. Export & Sharing:\nFiles are only shared when you explicitly trigger Export (PDF/EPUB/Backup)."
                        ),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy, contentColor = LightSurface)
                ) {
                    Text(StringsHelper.get("বুঝেছি", "Understood"))
                }
            }
        )
    }

    // Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = StringsHelper.get("ব্যবহারের শর্তাবলী (Terms & Conditions)", "Terms & Conditions"),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = StringsHelper.get(
                            "১. কপিরাইট দায়িত্ব:\nলেখক হিসেবে আপনার লেখার মৌলিকতা ও নৈতিক দায়িত্ব আপনার নিজের। BoiLekho একটি নিরপেক্ষ লেখার ও প্রকাশনার প্রযুক্তিগত স্টুডিও।\n\n২. ব্যাকআপ পরামর্শ:\nফোনের মেমরি বা দুর্ঘটনাজনিত ক্ষতি থেকে বাঁচতে নিয়মিত BoiLekho ব্যাকআপ ফাইল তৈরি করে নিরাপদ স্টোরেজে রাখুন।\n\nডেভেলপার: Muhammad Omor (muhammedomor.studio@gmail.com)\nঅফিসিয়াল শপ: https://muhammedomor.supportkori.shop",
                            "1. Copyright & Authorship:\nYou are the sole creator responsible for the originality of your content. BoiLekho is an authoring tool.\n\n2. Regular Backups:\nUsers are encouraged to export project backups periodically to guard against hardware loss.\n\nDeveloper: Muhammad Omor (muhammedomor.studio@gmail.com)\nOfficial Shop: https://muhammedomor.supportkori.shop"
                        ),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy, contentColor = LightSurface)
                ) {
                    Text(StringsHelper.get("সম্মত", "Accept"))
                }
            }
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PrimaryNavy
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
