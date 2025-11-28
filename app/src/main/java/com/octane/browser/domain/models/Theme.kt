package com.octane.browser.domain.models;

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM;
    
    companion object {
        fun fromOrdinal(ordinal: Int): Theme {
            return values().getOrNull(ordinal) ?: SYSTEM
        }
    }
}