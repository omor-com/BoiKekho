package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BookDesignEntity
import com.example.ui.theme.PlayfairDisplayFontFamily

@Composable
fun BookCoverView(
    title: String,
    subtitle: String = "",
    author: String = "Muhammad Omor",
    design: BookDesignEntity? = null,
    modifier: Modifier = Modifier
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(design?.coverBgColor ?: "#0B2345"))
    } catch (_: Exception) {
        Color(0xFF0B2345)
    }

    val titleColor = try {
        Color(android.graphics.Color.parseColor(design?.coverTitleColor ?: "#F2A900"))
    } catch (_: Exception) {
        Color(0xFFF2A900)
    }

    val authorColor = try {
        Color(android.graphics.Color.parseColor(design?.coverAuthorColor ?: "#B7C0CE"))
    } catch (_: Exception) {
        Color(0xFFB7C0CE)
    }

    Box(
        modifier = modifier
            .aspectRatio(0.68f) // Standard book aspect ratio (1 : 1.47)
            .shadow(6.dp, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        bgColor.copy(alpha = 0.88f),
                        bgColor,
                        bgColor.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFFF2A900).copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(12.dp)
    ) {
        // Inner ornamental frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 0.8.dp,
                    color = Color(0xFFF2A900).copy(alpha = 0.35f),
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.18f))

                // Emblem
                Text(
                    text = "❦",
                    color = Color(0xFFF2A900),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = title.ifEmpty { "গ্রন্থের শিরোনাম" },
                    color = titleColor,
                    fontSize = 15.sp,
                    fontFamily = PlayfairDisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = Color(0xFFE2E8F0),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.sp
                    )
                }

                Spacer(modifier = Modifier.weight(0.35f))

                // Author
                Text(
                    text = author.ifEmpty { "Muhammad Omor" },
                    color = authorColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "BoiLekho Studio",
                    color = Color(0xFFA0AEC0),
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
