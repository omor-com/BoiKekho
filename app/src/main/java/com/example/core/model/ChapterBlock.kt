package com.example.core.model

import java.util.UUID

data class ChapterBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.PARAGRAPH,
    val text: String = "",
    val caption: String = "", // Used for citation/source or learning box title
    val orderIndex: Int = 0
)
