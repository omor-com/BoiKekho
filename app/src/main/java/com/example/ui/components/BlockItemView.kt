package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.BlockType
import com.example.core.model.ChapterBlock
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NotoSerifBengaliFontFamily
import com.example.ui.theme.PrimaryNavy

@Composable
fun BlockItemView(
    block: ChapterBlock,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onTextChanged: (String) -> Unit,
    onCaptionChanged: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Block Header with Type Label & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Block Type Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (block.type) {
                                BlockType.LEARNING_BOX -> GoldAccent.copy(alpha = 0.2f)
                                BlockType.REFERENCE_BOX -> PrimaryNavy.copy(alpha = 0.12f)
                                BlockType.QUOTE -> Color(0xFF64748B).copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${block.type.titleBn} (${block.type.titleEn})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (block.type) {
                            BlockType.LEARNING_BOX -> Color(0xFFB45309)
                            BlockType.REFERENCE_BOX -> PrimaryNavy
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Actions: Move up, down, delete
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (canMoveUp) MaterialTheme.colorScheme.onSurfaceVariant else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (canMoveDown) MaterialTheme.colorScheme.onSurfaceVariant else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Block",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body rendering according to BlockType
            when (block.type) {
                BlockType.REFERENCE_BOX -> {
                    // Styled Reference Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.5.dp, PrimaryNavy, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = PrimaryNavy,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "রেফারেন্স ও উৎসসূত্র (Reference Note)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Reference Title / Source citation
                        BasicTextField(
                            value = block.caption,
                            onValueChange = onCaptionChanged,
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkNavy
                            ),
                            cursorBrush = SolidColor(PrimaryNavy),
                            decorationBox = { innerTextField ->
                                if (block.caption.isEmpty()) {
                                    Text("উৎস / গ্রন্থের নাম (যেমন: বাংলা একাডেমি গবেষণা পত্রিকা, খণ্ড ৪)", fontSize = 12.sp, color = Color.Gray)
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Reference Body
                        BasicTextField(
                            value = block.text,
                            onValueChange = onTextChanged,
                            textStyle = TextStyle(
                                fontFamily = NotoSerifBengaliFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(PrimaryNavy),
                            decorationBox = { innerTextField ->
                                if (block.text.isEmpty()) {
                                    Text("রেফারেন্সের বিস্তারিত বিবরণ বা উদ্ধৃতি নোট লিখুন...", fontSize = 13.sp, color = Color.Gray)
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                BlockType.LEARNING_BOX -> {
                    // Styled Learning Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFFBEB))
                            .border(1.5.dp, GoldAccent, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "শিক্ষণীয় সারসংক্ষেপ (Key Learning Box)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Learning Box Title
                        BasicTextField(
                            value = block.caption,
                            onValueChange = onCaptionChanged,
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF78350F)
                            ),
                            cursorBrush = SolidColor(Color(0xFFB45309)),
                            decorationBox = { innerTextField ->
                                if (block.caption.isEmpty()) {
                                    Text("বক্সের শিরোনাম (যেমন: অধ্যায়ের সারমর্ম / মূল পাঠ)", fontSize = 12.sp, color = Color.Gray)
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Learning Body
                        BasicTextField(
                            value = block.text,
                            onValueChange = onTextChanged,
                            textStyle = TextStyle(
                                fontFamily = NotoSerifBengaliFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF451A03),
                                lineHeight = 22.sp
                            ),
                            cursorBrush = SolidColor(Color(0xFFB45309)),
                            decorationBox = { innerTextField ->
                                if (block.text.isEmpty()) {
                                    Text("শিক্ষণীয় পয়েন্টসমূহ বা মূল ধারণা লিখুন...", fontSize = 13.sp, color = Color.Gray)
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                BlockType.QUOTE -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(44.dp)
                                .background(GoldAccent)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            BasicTextField(
                                value = block.text,
                                onValueChange = onTextChanged,
                                textStyle = TextStyle(
                                    fontFamily = NotoSerifBengaliFontFamily,
                                    fontSize = 15.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 22.sp
                                ),
                                cursorBrush = SolidColor(GoldAccent),
                                decorationBox = { inner ->
                                    if (block.text.isEmpty()) Text("উদ্ধৃতি লিখুন...", fontStyle = FontStyle.Italic, color = Color.Gray)
                                    inner()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicTextField(
                                value = block.caption,
                                onValueChange = onCaptionChanged,
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                decorationBox = { inner ->
                                    if (block.caption.isEmpty()) Text("— লেখক / বক্তার নাম", fontSize = 11.sp, color = Color.Gray)
                                    inner()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                BlockType.DIVIDER -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = block.text.ifEmpty { "❦   ❦   ❦" },
                            color = GoldAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                BlockType.HEADING_1 -> {
                    BasicTextField(
                        value = block.text,
                        onValueChange = onTextChanged,
                        textStyle = TextStyle(
                            fontFamily = NotoSerifBengaliFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy,
                            lineHeight = 26.sp
                        ),
                        cursorBrush = SolidColor(PrimaryNavy),
                        decorationBox = { inner ->
                            if (block.text.isEmpty()) Text("প্রধান শিরোনাম (H1) লিখুন...", fontSize = 18.sp, color = Color.Gray)
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                BlockType.HEADING_2 -> {
                    BasicTextField(
                        value = block.text,
                        onValueChange = onTextChanged,
                        textStyle = TextStyle(
                            fontFamily = NotoSerifBengaliFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(PrimaryNavy),
                        decorationBox = { inner ->
                            if (block.text.isEmpty()) Text("উপ-শিরোনাম (H2) লিখুন...", fontSize = 16.sp, color = Color.Gray)
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> {
                    // Paragraph & Other types
                    BasicTextField(
                        value = block.text,
                        onValueChange = onTextChanged,
                        textStyle = TextStyle(
                            fontFamily = NotoSerifBengaliFontFamily,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(PrimaryNavy),
                        decorationBox = { inner ->
                            if (block.text.isEmpty()) Text("অনুচ্ছেদের বিবরণ লিখুন...", fontSize = 14.sp, color = Color.Gray)
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
