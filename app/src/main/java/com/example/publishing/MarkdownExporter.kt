package com.example.publishing

import android.content.Context
import com.example.core.model.BlockType
import com.example.core.model.ChapterBlock
import com.example.core.util.FileUtils
import com.example.data.entity.BookEntity
import com.example.data.entity.ChapterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object MarkdownExporter {

    suspend fun exportMarkdown(
        context: Context,
        book: BookEntity,
        chaptersWithBlocks: List<Pair<ChapterEntity, List<ChapterBlock>>>
    ): File = withContext(Dispatchers.IO) {
        val exportDir = FileUtils.getExportDirectory(context)
        val sanitizedName = book.title.replace("[^a-zA-Z0-9\\u0980-\\u09FF]".toRegex(), "_").take(30)
        val mdFile = File(exportDir, "BoiLekho_${sanitizedName}_${System.currentTimeMillis()}.md")

        val content = buildString {
            append("# ${book.title}\n\n")
            if (book.subtitle.isNotEmpty()) {
                append("*${book.subtitle}*\n\n")
            }
            append("**Author:** ${book.author.ifEmpty { "Muhammad Omor" }}  \n")
            append("**Published via:** BoiLekho Studio  \n\n")
            append("---\n\n")

            // Table of Contents
            append("## সূচিপত্র (Table of Contents)\n\n")
            chaptersWithBlocks.forEachIndexed { index, (ch, _) ->
                append("${index + 1}. [${ch.title}](#chapter-${index + 1})\n")
            }
            append("\n---\n\n")

            // Chapters
            chaptersWithBlocks.forEachIndexed { index, (chapter, blocks) ->
                append("## <a name=\"chapter-${index + 1}\"></a>${chapter.title}\n\n")
                blocks.forEach { block ->
                    when (block.type) {
                        BlockType.PARAGRAPH -> append("${block.text}\n\n")
                        BlockType.HEADING_1 -> append("### ${block.text}\n\n")
                        BlockType.HEADING_2 -> append("#### ${block.text}\n\n")
                        BlockType.HEADING_3 -> append("##### ${block.text}\n\n")
                        BlockType.QUOTE -> append("> ${block.text}\n\n")
                        BlockType.REFERENCE_BOX -> {
                            val title = block.caption.ifEmpty { "রেফারেন্স ও উৎসসূত্র" }
                            append("```[REFERENCE]\n$title:\n${block.text}\n```\n\n")
                        }
                        BlockType.LEARNING_BOX -> {
                            val title = block.caption.ifEmpty { "শিক্ষণীয় সারসংক্ষেপ" }
                            append("```[LEARNING BOX]\n💡 $title:\n${block.text}\n```\n\n")
                        }
                        BlockType.DIVIDER -> append("\n---\n\n")
                        else -> append("${block.text}\n\n")
                    }
                }
            }
        }

        val outStream = FileOutputStream(mdFile)
        outStream.write(content.toByteArray(StandardCharsets.UTF_8))
        outStream.close()

        mdFile
    }
}
