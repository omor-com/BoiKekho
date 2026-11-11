package com.example

import com.example.core.model.AppLanguage
import com.example.core.model.BlockType
import com.example.core.util.BengaliNumberUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testBengaliNumberConversion() {
        assertEquals("১", BengaliNumberUtil.toBengaliDigits(1))
        assertEquals("১২৩৪৫৬৭৮৯০", BengaliNumberUtil.toBengaliDigits(1234567890L))
    }

    @Test
    fun testReadingTimeCalculation() {
        val bengaliReadingTime = BengaliNumberUtil.formatReadingTime(400, AppLanguage.BANGLA)
        assertEquals("~২ মিনিট", bengaliReadingTime)

        val englishReadingTime = BengaliNumberUtil.formatReadingTime(600, AppLanguage.ENGLISH)
        assertEquals("~3 min read", englishReadingTime)
    }

    @Test
    fun testBlockTypes() {
        assertEquals("রেফারেন্স বক্স", BlockType.REFERENCE_BOX.titleBn)
        assertEquals("শিক্ষণীয় বক্স / সারসংক্ষেপ", BlockType.LEARNING_BOX.titleBn)
    }
}
