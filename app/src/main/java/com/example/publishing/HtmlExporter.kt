package com.example.publishing

import android.content.Context
import com.example.core.model.BlockType
import com.example.core.model.ChapterBlock
import com.example.core.util.FileUtils
import com.example.data.entity.BookDesignEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object HtmlExporter {

    suspend fun exportHtml(
        context: Context,
        book: BookEntity,
        design: BookDesignEntity,
        chaptersWithBlocks: List<Pair<ChapterEntity, List<ChapterBlock>>>
    ): File = withContext(Dispatchers.IO) {
        val exportDir = FileUtils.getExportDirectory(context)
        val sanitizedName = book.title.replace("[^a-zA-Z0-9\\u0980-\\u09FF]".toRegex(), "_").take(30)
        val htmlFile = File(exportDir, "BoiLekho_${sanitizedName}_${System.currentTimeMillis()}.html")

        val htmlContent = buildString {
            append("""<!DOCTYPE html>
<html lang="${book.language}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${escapeHtml(book.title)}</title>
    <style>
        :root {
            --primary-navy: #0B2345;
            --gold-accent: #F2A900;
            --bg-color: #F8F5ED;
            --surface-color: #FFFFFF;
            --text-color: #17243A;
            --secondary-text: #5D6675;
            --border-color: #E2E8F0;
        }
        body {
            font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, 'SolaimanLipi', 'Noto Serif Bengali', serif;
            background-color: var(--bg-color);
            color: var(--text-color);
            line-height: 1.8;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 780px;
            margin: 0 auto;
            background-color: var(--surface-color);
            padding: 48px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.06);
            border-radius: 8px;
        }
        .book-cover {
            text-align: center;
            padding: 60px 20px;
            border-bottom: 2px solid var(--gold-accent);
            margin-bottom: 40px;
        }
        h1.title {
            color: var(--primary-navy);
            font-size: 2.4em;
            margin-bottom: 8px;
        }
        p.subtitle {
            font-style: italic;
            color: var(--secondary-text);
            font-size: 1.2em;
        }
        p.author {
            color: var(--gold-accent);
            font-size: 1.3em;
            font-weight: bold;
            margin-top: 24px;
        }
        .toc {
            background-color: #F8FAFC;
            border: 1px solid var(--border-color);
            padding: 24px;
            border-radius: 8px;
            margin-bottom: 48px;
        }
        .toc h3 {
            margin-top: 0;
            color: var(--primary-navy);
        }
        .toc ol {
            padding-left: 20px;
        }
        .toc li a {
            color: var(--primary-navy);
            text-decoration: none;
            font-weight: 500;
        }
        .toc li a:hover {
            color: var(--gold-accent);
            text-decoration: underline;
        }
        .chapter {
            margin-bottom: 60px;
        }
        h2.chapter-title {
            color: var(--primary-navy);
            border-bottom: 2px solid var(--gold-accent);
            padding-bottom: 8px;
            margin-top: 40px;
        }
        p {
            text-align: justify;
            font-size: 1.05em;
            margin-bottom: 1.2em;
        }
        blockquote.quote {
            border-left: 4px solid var(--gold-accent);
            background-color: #F8FAFC;
            padding: 12px 20px;
            margin: 20px 0;
            font-style: italic;
            color: #334155;
            border-radius: 0 6px 6px 0;
        }
        .reference-box {
            background-color: #F1F5F9;
            border: 1.5px solid var(--primary-navy);
            border-radius: 8px;
            padding: 16px 20px;
            margin: 24px 0;
        }
        .reference-box h4 {
            margin: 0 0 8px 0;
            color: var(--primary-navy);
            font-size: 1.05em;
        }
        .learning-box {
            background-color: #FFFDF0;
            border: 1.5px solid var(--gold-accent);
            border-radius: 8px;
            padding: 16px 20px;
            margin: 24px 0;
        }
        .learning-box h4 {
            margin: 0 0 8px 0;
            color: #B45309;
            font-size: 1.05em;
        }
        .divider {
            text-align: center;
            color: var(--gold-accent);
            font-size: 1.5em;
            margin: 36px 0;
        }
        footer {
            text-align: center;
            margin-top: 60px;
            padding-top: 24px;
            border-top: 1px solid var(--border-color);
            color: var(--secondary-text);
            font-size: 0.9em;
        }
        @media print {
            body { background: #FFF; }
            .container { box-shadow: none; padding: 0; }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="book-cover">
            <h1 class="title">${escapeHtml(book.title)}</h1>
            ${if (book.subtitle.isNotEmpty()) "<p class=\"subtitle\">${escapeHtml(book.subtitle)}</p>" else ""}
            <p class="author">${escapeHtml(book.author.ifEmpty { "Muhammad Omor" })}</p>
            <p style="color: #718096; font-size: 0.9em;">Published via BoiLekho Publishing Studio</p>
        </div>

        <div class="toc">
            <h3>${if (book.language == "bn") "সূচিপত্র" else "Table of Contents"}</h3>
            <ol>
""")
            chaptersWithBlocks.forEachIndexed { index, (ch, _) ->
                append("                <li><a href=\"#ch_${index + 1}\">${escapeHtml(ch.title)}</a></li>\n")
            }
            append("""            </ol>
        </div>
""")

            chaptersWithBlocks.forEachIndexed { index, (chapter, blocks) ->
                append("        <div class=\"chapter\" id=\"ch_${index + 1}\">\n")
                append("            <h2 class=\"chapter-title\">${escapeHtml(chapter.title)}</h2>\n")
                blocks.forEach { block ->
                    when (block.type) {
                        BlockType.PARAGRAPH -> append("            <p>${escapeHtml(block.text)}</p>\n")
                        BlockType.HEADING_1 -> append("            <h3>${escapeHtml(block.text)}</h3>\n")
                        BlockType.HEADING_2 -> append("            <h4>${escapeHtml(block.text)}</h4>\n")
                        BlockType.HEADING_3 -> append("            <h5>${escapeHtml(block.text)}</h5>\n")
                        BlockType.QUOTE -> append("            <blockquote class=\"quote\">${escapeHtml(block.text)}</blockquote>\n")
                        BlockType.REFERENCE_BOX -> {
                            val title = block.caption.ifEmpty { "রেফারেন্স ও উৎসসূত্র" }
                            append("            <div class=\"reference-box\"><h4>📚 ${escapeHtml(title)}</h4><p>${escapeHtml(block.text)}</p></div>\n")
                        }
                        BlockType.LEARNING_BOX -> {
                            val title = block.caption.ifEmpty { "শিক্ষণীয় সারসংক্ষেপ" }
                            append("            <div class=\"learning-box\"><h4>💡 ${escapeHtml(title)}</h4><p>${escapeHtml(block.text)}</p></div>\n")
                        }
                        BlockType.DIVIDER -> append("            <div class=\"divider\">❦   ❦   ❦</div>\n")
                        else -> append("            <p>${escapeHtml(block.text)}</p>\n")
                    }
                }
                append("        </div>\n")
            }

            append("""        <footer>
            <p>রচিত ও প্রকাশিত: <strong>BoiLekho (বইলেখো)</strong> | Studio Edition 1.0.0</p>
            <p>Developer: Muhammad Omor (muhammedomor.studio@gmail.com)</p>
        </footer>
    </div>
</body>
</html>""")
        }

        val outStream = FileOutputStream(htmlFile)
        outStream.write(htmlContent.toByteArray(StandardCharsets.UTF_8))
        outStream.close()

        htmlFile
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
