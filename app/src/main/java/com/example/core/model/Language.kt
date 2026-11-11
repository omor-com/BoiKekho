package com.example.core.model

enum class AppLanguage(val code: String, val displayName: String) {
    BANGLA("bn", "বাংলা"),
    ENGLISH("en", "English")
}

object StringsHelper {
    var currentLanguage: AppLanguage = AppLanguage.BANGLA

    fun get(bn: String, en: String): String {
        return if (currentLanguage == AppLanguage.BANGLA) bn else en
    }
}
