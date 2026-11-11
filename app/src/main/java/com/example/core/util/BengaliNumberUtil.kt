package com.example.core.util

import com.example.core.model.AppLanguage
import com.example.core.model.StringsHelper

object BengaliNumberUtil {
    private val englishToBengaliDigits = mapOf(
        '0' to '০',
        '1' to '১',
        '2' to '২',
        '3' to '৩',
        '4' to '৪',
        '5' to '৫',
        '6' to '৬',
        '7' to '৭',
        '8' to '৮',
        '9' to '৯'
    )

    fun toBengaliDigits(number: Long): String {
        return number.toString().map { englishToBengaliDigits[it] ?: it }.joinToString("")
    }

    fun toBengaliDigits(number: Int): String {
        return toBengaliDigits(number.toLong())
    }

    fun formatNumber(number: Int, language: AppLanguage = StringsHelper.currentLanguage): String {
        return if (language == AppLanguage.BANGLA) {
            toBengaliDigits(number)
        } else {
            String.format("%,d", number)
        }
    }

    fun formatReadingTime(words: Int, language: AppLanguage = StringsHelper.currentLanguage): String {
        val minutes = maxOf(1, (words / 200.0).toInt())
        return if (language == AppLanguage.BANGLA) {
            "~${toBengaliDigits(minutes)} মিনিট"
        } else {
            "~$minutes min read"
        }
    }
}
