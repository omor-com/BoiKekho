package com.example.publishing

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.core.model.BlockType
import com.example.core.model.ChapterBlock
import com.example.core.util.BengaliNumberUtil
import com.example.core.util.FileUtils
import com.example.data.entity.BookDesignEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    suspend fun exportPdf(
        context: Context,
        book: BookEntity,
        design: BookDesignEntity,
        chaptersWithBlocks: List<Pair<ChapterEntity, List<ChapterBlock>>>
    ): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()

        // Page dimensions: A5 standard (420 x 595 pt) or A4 (595 x 842 pt)
        val (pageWidth, pageHeight) = when (design.pageSize) {
            "A4" -> Pair(595, 842)
            else -> Pair(420, 595) // A5 default
        }

        val margin = when (design.pageMargins) {
            "COMPACT" -> 32
            "GENEROUS" -> 56
            else -> 42 // Standard
        }
        val contentWidth = pageWidth - (margin * 2)

        var pageNumber = 1

        val bengaliTypeface = try {
            ResourcesCompat.getFont(context, R.font.noto_serif_bengali) ?: Typeface.SERIF
        } catch (_: Exception) {
            Typeface.SERIF
        }

        val displayTypeface = try {
            ResourcesCompat.getFont(context, R.font.playfair_display) ?: bengaliTypeface
        } catch (_: Exception) {
            bengaliTypeface
        }

        // ==================== 1. COVER PAGE ====================
        val coverPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
        val coverPage = document.startPage(coverPageInfo)
        val coverCanvas = coverPage.canvas

        // Cover Background
        val coverBgColorInt = try { Color.parseColor(design.coverBgColor) } catch (_: Exception) { Color.rgb(11, 35, 69) }
        val coverBgPaint = Paint().apply {
            color = coverBgColorInt
            style = Paint.Style.FILL
        }
        coverCanvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), coverBgPaint)

        // Ornamental Gold Border
        if (design.coverShowBorder) {
            val borderPaint = Paint().apply {
                color = Color.parseColor("#F2A900")
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val borderRect = RectF(18f, 18f, (pageWidth - 18).toFloat(), (pageHeight - 18).toFloat())
            coverCanvas.drawRect(borderRect, borderPaint)

            val innerBorderRect = RectF(22f, 22f, (pageWidth - 22).toFloat(), (pageHeight - 22).toFloat())
            coverCanvas.drawRect(innerBorderRect, borderPaint.apply { strokeWidth = 0.8f })
        }

        // Cover Title
        val titlePaint = TextPaint().apply {
            color = try { Color.parseColor(design.coverTitleColor) } catch (_: Exception) { Color.parseColor("#F2A900") }
            textSize = if (design.pageSize == "A4") 32f else 24f
            typeface = Typeface.create(displayTypeface, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val titleLayout = StaticLayout.Builder.obtain(
            book.title,
            0,
            book.title.length,
            titlePaint,
            contentWidth
        ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()

        coverCanvas.save()
        coverCanvas.translate((pageWidth / 2).toFloat(), (pageHeight * 0.32f))
        titleLayout.draw(coverCanvas)
        coverCanvas.restore()

        // Subtitle
        if (book.subtitle.isNotEmpty()) {
            val subPaint = TextPaint().apply {
                color = Color.parseColor("#E0D8C6")
                textSize = if (design.pageSize == "A4") 16f else 13f
                typeface = Typeface.create(bengaliTypeface, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            val subLayout = StaticLayout.Builder.obtain(
                book.subtitle,
                0,
                book.subtitle.length,
                subPaint,
                contentWidth
            ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()

            coverCanvas.save()
            coverCanvas.translate((pageWidth / 2).toFloat(), (pageHeight * 0.46f))
            subLayout.draw(coverCanvas)
            coverCanvas.restore()
        }

        // Emblem Ornament
        val ornamentPaint = Paint().apply {
            color = Color.parseColor("#F2A900")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        coverCanvas.drawText("❦  বইলেখো  ❦", (pageWidth / 2).toFloat(), pageHeight * 0.65f, ornamentPaint)

        // Author Name
        val authorPaint = Paint().apply {
            color = try { Color.parseColor(design.coverAuthorColor) } catch (_: Exception) { Color.WHITE }
            textSize = if (design.pageSize == "A4") 18f else 14f
            typeface = Typeface.create(displayTypeface, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        coverCanvas.drawText(book.author.ifEmpty { "Muhammad Omor" }, (pageWidth / 2).toFloat(), pageHeight * 0.82f, authorPaint)

        val publisherPaint = Paint().apply {
            color = Color.parseColor("#A0AEC0")
            textSize = 10f
            typeface = Typeface.create(bengaliTypeface, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        coverCanvas.drawText("BoiLekho Publishing Studio", (pageWidth / 2).toFloat(), pageHeight * 0.90f, publisherPaint)

        document.finishPage(coverPage)

        // ==================== 2. TABLE OF CONTENTS PAGE ====================
        if (design.includeToc) {
            val tocPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
            val tocPage = document.startPage(tocPageInfo)
            val tocCanvas = tocPage.canvas

            val tocHeaderPaint = Paint().apply {
                color = Color.parseColor("#0B2345")
                textSize = 20f
                typeface = Typeface.create(displayTypeface, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            tocCanvas.drawText(
                if (book.language == "bn") "সূচিপত্র (Table of Contents)" else "Table of Contents",
                (pageWidth / 2).toFloat(),
                (margin + 30).toFloat(),
                tocHeaderPaint
            )

            val linePaint = Paint().apply {
                color = Color.parseColor("#F2A900")
                strokeWidth = 1.5f
            }
            tocCanvas.drawLine((margin + 40).toFloat(), (margin + 45).toFloat(), (pageWidth - margin - 40).toFloat(), (margin + 45).toFloat(), linePaint)

            var tocY = (margin + 75).toFloat()
            val itemPaint = Paint().apply {
                color = Color.parseColor("#17243A")
                textSize = 13f
                typeface = Typeface.create(bengaliTypeface, Typeface.NORMAL)
            }

            chaptersWithBlocks.forEachIndexed { index, (ch, _) ->
                val num = if (book.language == "bn") BengaliNumberUtil.toBengaliDigits(index + 1) else (index + 1).toString()
                val text = "$num. ${ch.title}"
                tocCanvas.drawText(text, margin.toFloat(), tocY, itemPaint)
                tocY += 26f
            }

            drawHeaderFooter(tocCanvas, pageWidth, pageHeight, margin, book.title, "", "— ২ —", bengaliTypeface)
            document.finishPage(tocPage)
        }

        // ==================== 3. CHAPTER PAGES ====================
        var currentBookPage = if (design.includeToc) 3 else 2

        chaptersWithBlocks.forEach { (chapter, blocks) ->
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            var cursorY = (margin + 25).toFloat()

            // Chapter Title
            val chTitlePaint = TextPaint().apply {
                color = Color.parseColor("#0B2345")
                textSize = 18f
                typeface = Typeface.create(displayTypeface, Typeface.BOLD)
            }
            val chTitleLayout = StaticLayout.Builder.obtain(
                chapter.title,
                0,
                chapter.title.length,
                chTitlePaint,
                contentWidth
            ).build()

            chTitleLayout.draw(canvas)
            cursorY += chTitleLayout.height + 15f

            // Chapter accent line
            val chLinePaint = Paint().apply {
                color = Color.parseColor("#F2A900")
                strokeWidth = 1.5f
            }
            canvas.drawLine(margin.toFloat(), cursorY, (margin + 60).toFloat(), cursorY, chLinePaint)
            cursorY += 20f

            // Render blocks
            for (block in blocks) {
                val blockHeightEst = 120 // rough est before measuring

                // Check page overflow
                if (cursorY + blockHeightEst > pageHeight - margin - 35) {
                    val pageStr = if (book.language == "bn") {
                        "— ${BengaliNumberUtil.toBengaliDigits(currentBookPage)} —"
                    } else {
                        "Page $currentBookPage"
                    }
                    drawHeaderFooter(canvas, pageWidth, pageHeight, margin, book.title, chapter.title, pageStr, bengaliTypeface)
                    document.finishPage(page)
                    currentBookPage++

                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    cursorY = (margin + 25).toFloat()
                }

                when (block.type) {
                    BlockType.PARAGRAPH -> {
                        val bodyPaint = TextPaint().apply {
                            color = Color.parseColor("#17243A")
                            textSize = design.baseFontSizeSp
                            typeface = Typeface.create(bengaliTypeface, Typeface.NORMAL)
                        }
                        val layout = StaticLayout.Builder.obtain(
                            block.text,
                            0,
                            block.text.length,
                            bodyPaint,
                            contentWidth
                        ).setLineSpacing(0f, design.lineSpacingMultiplier).build()

                        canvas.save()
                        canvas.translate(margin.toFloat(), cursorY)
                        layout.draw(canvas)
                        canvas.restore()
                        cursorY += layout.height + 12f
                    }

                    BlockType.HEADING_1 -> {
                        val h1Paint = TextPaint().apply {
                            color = Color.parseColor("#0B2345")
                            textSize = 16f
                            typeface = Typeface.create(displayTypeface, Typeface.BOLD)
                        }
                        val layout = StaticLayout.Builder.obtain(block.text, 0, block.text.length, h1Paint, contentWidth).build()
                        canvas.save()
                        canvas.translate(margin.toFloat(), cursorY)
                        layout.draw(canvas)
                        canvas.restore()
                        cursorY += layout.height + 10f
                    }

                    BlockType.HEADING_2 -> {
                        val h2Paint = TextPaint().apply {
                            color = Color.parseColor("#17243A")
                            textSize = 14f
                            typeface = Typeface.create(displayTypeface, Typeface.BOLD)
                        }
                        val layout = StaticLayout.Builder.obtain(block.text, 0, block.text.length, h2Paint, contentWidth).build()
                        canvas.save()
                        canvas.translate(margin.toFloat(), cursorY)
                        layout.draw(canvas)
                        canvas.restore()
                        cursorY += layout.height + 8f
                    }

                    BlockType.QUOTE -> {
                        val quotePaint = TextPaint().apply {
                            color = Color.parseColor("#334155")
                            textSize = design.baseFontSizeSp
                            typeface = Typeface.create(bengaliTypeface, Typeface.ITALIC)
                        }
                        val layout = StaticLayout.Builder.obtain(block.text, 0, block.text.length, quotePaint, contentWidth - 25).build()

                        // Quote left bar
                        val barPaint = Paint().apply {
                            color = Color.parseColor("#F2A900")
                            strokeWidth = 3f
                        }
                        canvas.drawLine((margin + 5).toFloat(), cursorY, (margin + 5).toFloat(), cursorY + layout.height, barPaint)

                        canvas.save()
                        canvas.translate((margin + 20).toFloat(), cursorY)
                        layout.draw(canvas)
                        canvas.restore()
                        cursorY += layout.height + 14f
                    }

                    BlockType.REFERENCE_BOX -> {
                        val boxPaint = Paint().apply {
                            color = Color.parseColor("#F1F5F9")
                            style = Paint.Style.FILL
                        }
                        val borderBoxPaint = Paint().apply {
                            color = Color.parseColor("#0B2345")
                            style = Paint.Style.STROKE
                            strokeWidth = 1f
                        }

                        val refTitlePaint = TextPaint().apply {
                            color = Color.parseColor("#0B2345")
                            textSize = 12f
                            typeface = Typeface.create(displayTypeface, Typeface.BOLD)
                        }
                        val refBodyPaint = TextPaint().apply {
                            color = Color.parseColor("#1E293B")
                            textSize = 11f
                            typeface = Typeface.create(bengaliTypeface, Typeface.NORMAL)
                        }

                        val title = block.caption.ifEmpty { "রেফারেন্স ও উৎসসূত্র (Reference Note)" }
                        val titleLayout = StaticLayout.Builder.obtain(title, 0, title.length, refTitlePaint, contentWidth - 24).build()
                        val bodyLayout = StaticLayout.Builder.obtain(block.text, 0, block.text.length, refBodyPaint, contentWidth - 24).build()

                        val boxHeight = titleLayout.height + bodyLayout.height + 24f
                        val boxRect = RectF(margin.toFloat(), cursorY, (margin + contentWidth).toFloat(), cursorY + boxHeight)
                        canvas.drawRoundRect(boxRect, 6f, 6f, boxPaint)
                        canvas.drawRoundRect(boxRect, 6f, 6f, borderBoxPaint)

                        canvas.save()
                        canvas.translate((margin + 12).toFloat(), cursorY + 10f)
                        titleLayout.draw(canvas)
                        canvas.restore()

                        canvas.save()
                        canvas.translate((margin + 12).toFloat(), cursorY + 10f + titleLayout.height + 6f)
                        bodyLayout.draw(canvas)
                        canvas.restore()

                        cursorY += boxHeight + 14f
                    }

                    BlockType.LEARNING_BOX -> {
                        val boxPaint = Paint().apply {
                            color = Color.parseColor("#FFFBEB")
                            style = Paint.Style.FILL
                        }
                        val borderBoxPaint = Paint().apply {
                            color = Color.parseColor("#F2A900")
                            style = Paint.Style.STROKE
                            strokeWidth = 1.5f
                        }

                        val learnTitlePaint = TextPaint().apply {
                            color = Color.parseColor("#B45309")
                            textSize = 12f
                            typeface = Typeface.create(displayTypeface, Typeface.BOLD)
                        }
                        val learnBodyPaint = TextPaint().apply {
                            color = Color.parseColor("#451A03")
                            textSize = 11.5f
                            typeface = Typeface.create(bengaliTypeface, Typeface.NORMAL)
                        }

                        val title = block.caption.ifEmpty { "শিক্ষণীয় সারসংক্ষেপ (Key Learning Box)" }
                        val titleLayout = StaticLayout.Builder.obtain(title, 0, title.length, learnTitlePaint, contentWidth - 24).build()
                        val bodyLayout = StaticLayout.Builder.obtain(block.text, 0, block.text.length, learnBodyPaint, contentWidth - 24).build()

                        val boxHeight = titleLayout.height + bodyLayout.height + 24f
                        val boxRect = RectF(margin.toFloat(), cursorY, (margin + contentWidth).toFloat(), cursorY + boxHeight)
                        canvas.drawRoundRect(boxRect, 6f, 6f, boxPaint)
                        canvas.drawRoundRect(boxRect, 6f, 6f, borderBoxPaint)

                        canvas.save()
                        canvas.translate((margin + 12).toFloat(), cursorY + 10f)
                        titleLayout.draw(canvas)
                        canvas.restore()

                        canvas.save()
                        canvas.translate((margin + 12).toFloat(), cursorY + 10f + titleLayout.height + 6f)
                        bodyLayout.draw(canvas)
                        canvas.restore()

                        cursorY += boxHeight + 14f
                    }

                    BlockType.DIVIDER -> {
                        val divPaint = Paint().apply {
                            color = Color.parseColor("#F2A900")
                            textSize = 14f
                            textAlign = Paint.Align.CENTER
                        }
                        canvas.drawText(block.text.ifEmpty { "❦   ❦   ❦" }, (pageWidth / 2).toFloat(), cursorY + 14f, divPaint)
                        cursorY += 28f
                    }

                    else -> {
                        // Default text block
                        val bodyPaint = TextPaint().apply {
                            color = Color.parseColor("#17243A")
                            textSize = design.baseFontSizeSp
                            typeface = Typeface.create(bengaliTypeface, Typeface.NORMAL)
                        }
                        val layout = StaticLayout.Builder.obtain(
                            block.text,
                            0,
                            block.text.length,
                            bodyPaint,
                            contentWidth
                        ).build()

                        canvas.save()
                        canvas.translate(margin.toFloat(), cursorY)
                        layout.draw(canvas)
                        canvas.restore()
                        cursorY += layout.height + 12f
                    }
                }
            }

            val pageStr = if (book.language == "bn") {
                "— ${BengaliNumberUtil.toBengaliDigits(currentBookPage)} —"
            } else {
                "Page $currentBookPage"
            }
            drawHeaderFooter(canvas, pageWidth, pageHeight, margin, book.title, chapter.title, pageStr, bengaliTypeface)
            document.finishPage(page)
            currentBookPage++
        }

        // Save PDF to exports directory
        val exportDir = FileUtils.getExportDirectory(context)
        val sanitizedName = book.title.replace("[^a-zA-Z0-9\\u0980-\\u09FF]".toRegex(), "_").take(30)
        val pdfFile = File(exportDir, "BoiLekho_${sanitizedName}_${System.currentTimeMillis()}.pdf")
        val outStream = FileOutputStream(pdfFile)
        document.writeTo(outStream)
        outStream.close()
        document.close()

        pdfFile
    }

    private fun drawHeaderFooter(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        margin: Int,
        bookTitle: String,
        chapterTitle: String,
        pageNumberStr: String,
        typeface: Typeface
    ) {
        val hfPaint = Paint().apply {
            color = Color.parseColor("#718096")
            textSize = 9f
            this.typeface = typeface
        }

        // Header
        canvas.drawText(bookTitle.take(28), margin.toFloat(), (margin / 1.5f), hfPaint)
        if (chapterTitle.isNotEmpty()) {
            val chPaint = Paint(hfPaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText(chapterTitle.take(24), (pageWidth - margin).toFloat(), (margin / 1.5f), chPaint)
        }

        // Running rule under header
        val rulePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.5f
        }
        canvas.drawLine(margin.toFloat(), (margin / 1.3f) + 4f, (pageWidth - margin).toFloat(), (margin / 1.3f) + 4f, rulePaint)

        // Footer Page Number centered
        val footerPaint = Paint(hfPaint).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(pageNumberStr, (pageWidth / 2).toFloat(), (pageHeight - (margin / 2)).toFloat(), footerPaint)
    }
}
