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

object TextExporter {

    suspend fun exportText(
        context: Context,
        book: BookEntity,
        chaptersWithBlocks: List<Pair<ChapterEntity, List<ChapterBlock>>>
    ): File = withContext(Dispatchers.IO) {
        val exportDir = FileUtils.getExportDirectory(context)
        val sanitizedName = book.title.replace("[^a-zA-Z0-9\\u0980-\\u09FF]".toRegex(), "_").take(30)
        val txtFile = File(exportDir, "BoiLekho_${sanitizedName}_${System.currentTimeMillis()}.txt")

        val content = buildString {
            append("====================================================\n")
            append("              ${book.title.uppercase()}\n")
            if (book.subtitle.isNotEmpty()) {
                append("          ${book.subtitle}\n")
            }
            append("====================================================\n")
            append("লেখক (Author): ${book.author.ifEmpty { "Muhammad Omor" }}\n")
            append("প্রকাশক: BoiLekho (বইলেখো) Publishing Studio\n")
            append("====================================================\n\n")

            append("সূচিপত্র (TABLE OF CONTENTS)\n")
            append("----------------------------------------------------\n")
            chaptersWithBlocks.forEachIndexed { index, (ch, _) ->
                append("  ${index + 1}. ${ch.title}\n")
            }
            append("----------------------------------------------------\n\n\n")

            chaptersWithBlocks.forEachIndexed { index, (chapter, blocks) ->
                append("****************************************************\n")
                append("  অধ্যায় ${index + 1}: ${chapter.title}\n")
                append("****************************************************\n\n")
                blocks.forEach { block ->
                    when (block.type) {
                        BlockType.PARAGRAPH -> append("${block.text}\n\n")
                        BlockType.HEADING_1 -> append(">>> ${block.text} <<<\n\n")
                        BlockType.HEADING_2 -> append(">> ${block.text} <<\n\n")
                        BlockType.HEADING_3 -> append("> ${block.text}\n\n")
                        BlockType.QUOTE -> append("  \"${block.text}\"\n\n")
                        BlockType.REFERENCE_BOX -> {
                            val title = block.caption.ifEmpty { "রেফারেন্স" }
                            append("[REFERENCE: $title]\n${block.text}\n[/REFERENCE]\n\n")
                        }
                        BlockType.LEARNING_BOX -> {
                            val title = block.caption.ifEmpty { "শিক্ষণীয় সারসংক্ষেপ" }
                            append("[LEARNING BOX: $title]\n${block.text}\n[/LEARNING BOX]\n\n")
                        }
                        BlockType.DIVIDER -> append("~ ~ ~ ~ ~ ~ ~ ~ ~ ~\n\n")
                        else -> append("${block.text}\n\n")
                    }
                }
                append("\n\n")
            }
        }

        val outStream = FileOutputStream(txtFile)
        outStream.write(content.toByteArray(StandardCharsets.UTF_8))
        outStream.close()

        txtFile
    }
}
