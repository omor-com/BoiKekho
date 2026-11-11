package com.example.core.model

enum class BlockType(
    val titleEn: String,
    val titleBn: String
) {
    PARAGRAPH("Paragraph", "অনুচ্ছেদ"),
    HEADING_1("Heading 1", "প্রধান শিরোনাম (H1)"),
    HEADING_2("Heading 2", "উপ-শিরোনাম (H2)"),
    HEADING_3("Heading 3", "অনু-শিরোনাম (H3)"),
    BULLET_LIST("Bullet List", "বুলেট তালিকা"),
    ORDERED_LIST("Numbered List", "নম্বরযুক্ত তালিকা"),
    QUOTE("Literary Quote", "উদ্ধৃতি / বচন"),
    REFERENCE_BOX("Reference Box", "রেফারেন্স বক্স"),
    LEARNING_BOX("Learning Box", "শিক্ষণীয় বক্স / সারসংক্ষেপ"),
    FOOTNOTE("Footnote", "পাদটীকা (Footnote)"),
    DIVIDER("Ornamental Divider", "আলংকারিক বিভাজক"),
    CODE_BLOCK("Code / Verse", "কোড / শ্লোক")
}
