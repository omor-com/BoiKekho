package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppSettingsDao
import com.example.data.dao.BookDao
import com.example.data.dao.BookDesignDao
import com.example.data.dao.BookRevisionDao
import com.example.data.dao.ChapterBlockDao
import com.example.data.dao.ChapterDao
import com.example.data.dao.RecoveryJournalDao
import com.example.data.dao.SectionDao
import com.example.data.entity.AppSettingsEntity
import com.example.data.entity.BookDesignEntity
import com.example.data.entity.BookEntity
import com.example.data.entity.BookRevisionEntity
import com.example.data.entity.ChapterBlockEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.RecoveryJournalEntity
import com.example.data.entity.SectionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        BookEntity::class,
        SectionEntity::class,
        ChapterEntity::class,
        ChapterBlockEntity::class,
        BookDesignEntity::class,
        BookRevisionEntity::class,
        RecoveryJournalEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BoiLekhoDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun sectionDao(): SectionDao
    abstract fun chapterDao(): ChapterDao
    abstract fun chapterBlockDao(): ChapterBlockDao
    abstract fun bookDesignDao(): BookDesignDao
    abstract fun bookRevisionDao(): BookRevisionDao
    abstract fun recoveryJournalDao(): RecoveryJournalDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: BoiLekhoDatabase? = null

        fun getInstance(context: Context): BoiLekhoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BoiLekhoDatabase::class.java,
                    "boilekho_master.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial inspiring master book by Muhammad Omor
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: BoiLekhoDatabase) {
            val bookId = UUID.randomUUID().toString()
            val book = BookEntity(
                id = bookId,
                title = "সোনার বাংলা ও অমর সাহিত্য",
                subtitle = "বাংলা ভাষা, গ্রন্থ রচনা ও প্রকাশনা শিল্প",
                author = "Muhammad Omor",
                description = "বইলেখো (BoiLekho) স্টুডিওতে রচিত একটি প্রামাণ্য গ্রন্থ। এতে বাংলা সাহিত্য চর্চা, অধ্যায় বিন্যাস, রেফারেন্স ও শিক্ষণীয় বক্সের চমৎকার সমন্বয় প্রদর্শিত হয়েছে।",
                language = "bn",
                genre = "সাহিত্য ও গবেষণা",
                isFavorite = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            database.bookDao().insertBook(book)

            val design = BookDesignEntity(
                id = UUID.randomUUID().toString(),
                bookId = bookId,
                coverTemplate = "GOLD_ROYAL",
                coverBgColor = "#0B2345",
                coverTitleColor = "#F2A900",
                coverSubtitleColor = "#FFFFFF",
                coverAuthorColor = "#B7C0CE",
                pageSize = "A5",
                pageMargins = "STANDARD",
                fontChoice = "SERIF",
                baseFontSizeSp = 15f,
                lineSpacingMultiplier = 1.5f,
                showHeader = true,
                showFooter = true,
                showPageNumbers = true,
                pageNumberFormat = "BANGLA_DASH",
                includeToc = true
            )
            database.bookDesignDao().insertOrUpdateDesign(design)

            val section1Id = UUID.randomUUID().toString()
            val section1 = SectionEntity(
                id = section1Id,
                bookId = bookId,
                title = "প্রথম খণ্ড: সূচনা ও প্রকাশনা নীতি",
                orderIndex = 0
            )
            database.sectionDao().insertSection(section1)

            val chapter1Id = UUID.randomUUID().toString()
            val chapter1 = ChapterEntity(
                id = chapter1Id,
                bookId = bookId,
                sectionId = section1Id,
                title = "অধ্যায় ১: বইলেখো ও লেখক সত্ত্বা",
                orderIndex = 0,
                status = "COMPLETED"
            )
            database.chapterDao().insertChapter(chapter1)

            val blocksCh1 = listOf(
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter1Id,
                    blockType = "HEADING_1",
                    orderIndex = 0,
                    contentText = "লেখার শক্তি ও আত্মপ্রকাশের নান্দনিকতা"
                ),
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter1Id,
                    blockType = "PARAGRAPH",
                    orderIndex = 1,
                    contentText = "গ্রন্থ রচনা কেবল শব্দ ও বাক্যের সমাবেশ নয়; এটি এক গভীর আত্মিক সাধনা। যখন একজন লেখক তাঁর চিন্তা, আবেগ ও গবেষণাকে অক্ষরের শরীরে রূপ দেন, তখন জন্ম নেয় একটি কালজয়ী সৃষ্টি। BoiLekho স্টুডিও ঠিক এই সাধনাকে আধুনিক প্রযুক্তির সাথে যুক্ত করার অঙ্গীকার নিয়ে নির্মিত।"
                ),
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter1Id,
                    blockType = "QUOTE",
                    orderIndex = 2,
                    contentText = "পুস্তক হলো যুগের পর যুগ ধরে মানুষের আত্মিক ঐশ্বর্য বহনকারী শ্রেষ্ঠ জাহাজ।",
                    captionOrNote = "— ফ্রান্সিস বেকন"
                ),
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter1Id,
                    blockType = "LEARNING_BOX",
                    orderIndex = 3,
                    contentText = "১. প্রতিটি অধ্যায় শুরু করার পূর্বে তার মূল উদ্দেশ্য চিহ্নিত করুন।\n২. সহজ ও সাবলীল ভাষায় জটিল বিষয় উপস্থাপন করুন।\n৩. রেফারেন্স ও নির্ভরযোগ্য সূত্র সংযোজন করে লেখার গ্রহণযোগ্যতা বৃদ্ধি করুন।",
                    captionOrNote = "লেখকের পাঠশালা — গুরুত্বপূর্ণ শিক্ষণীয় বিষয়"
                ),
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter1Id,
                    blockType = "REFERENCE_BOX",
                    orderIndex = 4,
                    contentText = "মোহাম্মদ ওমর কর্তৃক সংকলিত ও সম্পাদিত 'বাংলা সাহিত্য ও গ্রন্থবিজ্ঞান', খণ্ড ১, প্রকাশনা ২০২৬। ডিজিটাল সংস্করণ সংরক্ষিত BoiLekho স্টুডিওতে।",
                    captionOrNote = "উৎস ও তথ্যসূত্র (Reference Note)"
                ),
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter1Id,
                    blockType = "DIVIDER",
                    orderIndex = 5,
                    contentText = "❦   ❦   ❦"
                ),
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter1Id,
                    blockType = "PARAGRAPH",
                    orderIndex = 6,
                    contentText = "বইয়ের অভ্যন্তরে রেফারেন্স বক্স ও লার্নিং বক্স সংযুক্ত করার মাধ্যমে পাঠকের জন্য পঠন অভিজ্ঞতা আরও সমৃদ্ধ ও প্রাতিষ্ঠানিক হয়ে ওঠে। BoiLekho আপনাকে সরাসরি PDF ও EPUB 3 ফরম্যাটে এসব চমৎকার লেআউটসহ প্রকাশনার স্বাধীনতা দেয়।"
                )
            )
            database.chapterBlockDao().insertBlocks(blocksCh1)

            // Chapter 2
            val chapter2Id = UUID.randomUUID().toString()
            val chapter2 = ChapterEntity(
                id = chapter2Id,
                bookId = bookId,
                sectionId = section1Id,
                title = "অধ্যায় ২: ডিজিটাল প্রকাশনা ও টাইপোগ্রাফি",
                orderIndex = 1,
                status = "COMPLETED"
            )
            database.chapterDao().insertChapter(chapter2)

            val blocksCh2 = listOf(
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter2Id,
                    blockType = "HEADING_1",
                    orderIndex = 0,
                    contentText = "বাংলা হরফের শিল্প ও পৃষ্ঠা বিন্যাস"
                ),
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter2Id,
                    blockType = "PARAGRAPH",
                    orderIndex = 1,
                    contentText = "একটি বই তখনই সফল যখন তার বিষয়বস্তুর গভীরতার সাথে সাথে পৃষ্ঠার টাইপোগ্রাফি চোখ জুড়িয়ে দেয়। মার্জিন, হরফের আকার এবং লাইনের ব্যবধান পাঠককে ক্লান্তিহীন পাঠের আনন্দ দেয়।"
                ),
                ChapterBlockEntity(
                    id = UUID.randomUUID().toString(),
                    chapterId = chapter2Id,
                    blockType = "LEARNING_BOX",
                    orderIndex = 2,
                    contentText = "কভার ডিজাইন এবং ভেতরের পৃষ্ঠার কালার প্যালেট এমন হওয়া উচিত যাতে দীর্ঘ সময় পাঠেও চোখে চাপ না পড়ে। ক্লাসিক নেভি ও গোল্ডেন এক্সেন্ট আভিজাত্যের প্রতীক।",
                    captionOrNote = "ডিজাইন দর্শন"
                )
            )
            database.chapterBlockDao().insertBlocks(blocksCh2)
        }
    }
}
