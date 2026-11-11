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
import java.util.UUID
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object EpubExporter {

    suspend fun exportEpub(
        context: Context,
        book: BookEntity,
        design: BookDesignEntity,
        chaptersWithBlocks: List<Pair<ChapterEntity, List<ChapterBlock>>>
    ): File = withContext(Dispatchers.IO) {
        val exportDir = FileUtils.getExportDirectory(context)
        val sanitizedName = book.title.replace("[^a-zA-Z0-9\\u0980-\\u09FF]".toRegex(), "_").take(30)
        val epubFile = File(exportDir, "BoiLekho_${sanitizedName}_${System.currentTimeMillis()}.epub")

        val zipOut = ZipOutputStream(FileOutputStream(epubFile))

        // 1. mimetype (Must be FIRST and STORED uncompressed per IDPF EPUB specification)
        val mimetypeBytes = "application/epub+zip".toByteArray(StandardCharsets.US_ASCII)
        val mimetypeEntry = ZipEntry("mimetype").apply {
            method = ZipEntry.STORED
            size = mimetypeBytes.size.toLong()
            compressedSize = mimetypeBytes.size.toLong()
            crc = CRC32().apply { update(mimetypeBytes) }.value
        }
        zipOut.putNextEntry(mimetypeEntry)
        zipOut.write(mimetypeBytes)
        zipOut.closeEntry()

        // 2. META-INF/container.xml
        val containerXml = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
  <rootfiles>
    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
  </rootfiles>
</container>"""
        writeZipEntry(zipOut, "META-INF/container.xml", containerXml)

        // 3. OEBPS/style.css
        val css = """
@charset "UTF-8";
body {
    font-family: 'SolaimanLipi', 'Kalpurush', 'Noto Serif Bengali', serif;
    margin: 5%;
    line-height: 1.7;
    color: #17243A;
    background-color: #FAF8F5;
}
h1.book-title {
    color: #0B2345;
    text-align: center;
    margin-top: 25%;
    font-size: 2.2em;
    font-weight: bold;
}
p.author {
    color: #D99500;
    text-align: center;
    font-size: 1.2em;
    margin-bottom: 25%;
}
h2.chapter-title {
    color: #0B2345;
    border-bottom: 2px solid #F2A900;
    padding-bottom: 8px;
    margin-top: 2em;
}
p {
    text-align: justify;
    margin-bottom: 1em;
}
blockquote.quote {
    border-left: 4px solid #F2A900;
    margin: 1.5em 1em;
    padding: 0.5em 1em;
    font-style: italic;
    background: #F1F5F9;
    color: #334155;
}
.reference-box {
    border: 1px solid #0B2345;
    background-color: #F8FAFC;
    border-radius: 8px;
    padding: 12px 16px;
    margin: 1.5em 0;
}
.reference-box h4 {
    margin: 0 0 6px 0;
    color: #0B2345;
    font-size: 0.95em;
}
.learning-box {
    border: 1.5px solid #F2A900;
    background-color: #FFFDF0;
    border-radius: 8px;
    padding: 12px 16px;
    margin: 1.5em 0;
}
.learning-box h4 {
    margin: 0 0 6px 0;
    color: #B45309;
    font-size: 0.95em;
}
.divider {
    text-align: center;
    color: #F2A900;
    font-size: 1.4em;
    margin: 2em 0;
}
"""
        writeZipEntry(zipOut, "OEBPS/style.css", css)

        // Generate chapters XHTML
        val chapterFiles = mutableListOf<String>()
        chaptersWithBlocks.forEachIndexed { index, (chapter, blocks) ->
            val filename = "chapter_${index + 1}.xhtml"
            chapterFiles.add(filename)

            val bodyHtml = buildString {
                append("<h2 class=\"chapter-title\">${escapeXml(chapter.title)}</h2>\n")
                blocks.forEach { block ->
                    when (block.type) {
                        BlockType.PARAGRAPH -> append("<p>${escapeXml(block.text)}</p>\n")
                        BlockType.HEADING_1 -> append("<h3>${escapeXml(block.text)}</h3>\n")
                        BlockType.HEADING_2 -> append("<h4>${escapeXml(block.text)}</h4>\n")
                        BlockType.HEADING_3 -> append("<h5>${escapeXml(block.text)}</h5>\n")
                        BlockType.QUOTE -> append("<blockquote class=\"quote\">${escapeXml(block.text)}</blockquote>\n")
                        BlockType.REFERENCE_BOX -> {
                            val title = block.caption.ifEmpty { "রেফারেন্স ও উৎসসূত্র" }
                            append("<div class=\"reference-box\"><h4>📚 ${escapeXml(title)}</h4><p>${escapeXml(block.text)}</p></div>\n")
                        }
                        BlockType.LEARNING_BOX -> {
                            val title = block.caption.ifEmpty { "শিক্ষণীয় সারসংক্ষেপ" }
                            append("<div class=\"learning-box\"><h4>💡 ${escapeXml(title)}</h4><p>${escapeXml(block.text)}</p></div>\n")
                        }
                        BlockType.DIVIDER -> append("<div class=\"divider\">❦   ❦   ❦</div>\n")
                        else -> append("<p>${escapeXml(block.text)}</p>\n")
                    }
                }
            }

            val chapterXml = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xml:lang="${book.language}">
<head>
    <title>${escapeXml(chapter.title)}</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
$bodyHtml
</body>
</html>"""
            writeZipEntry(zipOut, "OEBPS/$filename", chapterXml)
        }

        // 4. OEBPS/toc.xhtml (EPUB 3 Navigation Document)
        val tocNav = buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xml:lang="${book.language}">
<head>
    <title>Table of Contents</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
<nav epub:type="toc" id="toc">
    <h1>সূচিপত্র</h1>
    <ol>
""")
            chaptersWithBlocks.forEachIndexed { index, (ch, _) ->
                append("        <li><a href=\"chapter_${index + 1}.xhtml\">${escapeXml(ch.title)}</a></li>\n")
            }
            append("""    </ol>
</nav>
</body>
</html>""")
        }
        writeZipEntry(zipOut, "OEBPS/toc.xhtml", tocNav)

        // 5. OEBPS/content.opf
        val bookUuid = UUID.randomUUID().toString()
        val opf = buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="pub-id">
  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:identifier id="pub-id">urn:uuid:$bookUuid</dc:identifier>
    <dc:title>${escapeXml(book.title)}</dc:title>
    <dc:creator>${escapeXml(book.author.ifEmpty { "Muhammad Omor" })}</dc:creator>
    <dc:language>${book.language}</dc:language>
    <dc:publisher>BoiLekho Publishing Studio</dc:publisher>
    <meta property="dcterms:modified">2026-11-11T00:00:00Z</meta>
  </metadata>
  <manifest>
    <item id="style" href="style.css" media-type="text/css"/>
    <item id="toc" href="toc.xhtml" media-type="application/xhtml+xml" properties="nav"/>
""")
            chapterFiles.forEachIndexed { i, f ->
                append("    <item id=\"ch_${i + 1}\" href=\"$f\" media-type=\"application/xhtml+xml\"/>\n")
            }
            append("""  </manifest>
  <spine>
""")
            chapterFiles.forEachIndexed { i, _ ->
                append("    <itemref idref=\"ch_${i + 1}\"/>\n")
            }
            append("""  </spine>
</package>""")
        }
        writeZipEntry(zipOut, "OEBPS/content.opf", opf)

        zipOut.close()
        epubFile
    }

    private fun writeZipEntry(zipOut: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zipOut.putNextEntry(entry)
        zipOut.write(content.toByteArray(StandardCharsets.UTF_8))
        zipOut.closeEntry()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
