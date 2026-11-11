package com.example.ui.backup

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AppLanguage
import com.example.core.model.StringsHelper
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSurface
import com.example.ui.theme.PrimaryNavy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    onExportBackup: suspend () -> Pair<Boolean, String>,
    onImportBackup: suspend (String) -> Pair<Boolean, String>
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

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
                        text = StringsHelper.get("ব্যাকআপ ও পুনরুদ্ধার (Backup & Restore)", "Backup & Restore"),
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
            // Backup Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = StringsHelper.get("সম্পূর্ণ ব্যাকআপ তৈরি করুন (Export Project Archive)", "Export Full Backup Archive"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = StringsHelper.get(
                            "আপনার সকল বই, অধ্যায়, ফরম্যাটিং, রেফারেন্স বক্স ও ডিজাইন সেটিংস একটি একক পোর্টেবল JSON প্যাকেজে এনকোড করে সংরক্ষণ করা হবে। এটি যেকোনো ডিভাইসে পুনরায় রিস্টোর করা যাবে।",
                            "Encodes all books, chapters, blocks, references, and styling into a portable project package."
                        ),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            isExporting = true
                            scope.launch {
                                val (success, message) = onExportBackup()
                                isExporting = false
                                snackbarHostState.showSnackbar(message)
                            }
                        },
                        enabled = !isExporting,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DarkNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkNavy, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(StringsHelper.get("ব্যাকআপ প্রস্তুত হচ্ছে...", "Creating backup..."), fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(StringsHelper.get("ব্যাকআপ ফাইল এক্সপোর্ট ও শেয়ার করুন", "Export & Share Backup"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Restore Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = StringsHelper.get("ব্যাকআপ পুনরুদ্ধার করুন (Restore Backup)", "Restore Project Backup"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = StringsHelper.get(
                            "পূর্বে সেভ করা BoiLekho ব্যাকআপ JSON ডেটা ইনপুট দিয়ে আপনার বইসমূহ এবং সকল অধ্যায় অবিকল ফিরিয়ে আনুন। ট্রানজ্যাকশনাল পদ্ধতিতে ডেটাবেস আপডেট হবে।",
                            "Paste or import BoiLekho backup data to safely restore your books and chapters."
                        ),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy, contentColor = LightSurface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(StringsHelper.get("ব্যাকআপ ডেটা ইমপোর্ট করুন", "Import Backup Data"), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Security note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = StringsHelper.get(
                            "লোকাল ডেটা নিশ্চয়তা: ব্যাকআপ ও রিস্টোর সম্পূর্ণ অফলাইনে আপনার ডিভাইসে সম্পন্ন হয়। কোনো দূরবর্তী সার্ভারে ডেটা আদান-প্রদান করা হয় না।",
                            "Offline guarantee: All backup & restore operations run completely on-device without remote servers."
                        ),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text(
                    text = StringsHelper.get("ব্যাকআপ JSON ডেটা পেস্ট করুন", "Paste Backup JSON"),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = StringsHelper.get("ব্যাকআপ ফাইলের সম্পূর্ণ JSON টেক্সট নিচে দিন:", "Enter raw backup JSON content:"),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("{\"app\": \"BoiLekho\", \"books\": [...]}") },
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val textToImport = importJsonText.trim()
                        if (textToImport.isNotEmpty()) {
                            showImportDialog = false
                            isImporting = true
                            scope.launch {
                                val (success, message) = onImportBackup(textToImport)
                                isImporting = false
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    },
                    enabled = importJsonText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DarkNavy)
                ) {
                    Text(StringsHelper.get("রিস্টোর করুন", "Restore"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(StringsHelper.get("বাতিল", "Cancel"))
                }
            }
        )
    }
}
