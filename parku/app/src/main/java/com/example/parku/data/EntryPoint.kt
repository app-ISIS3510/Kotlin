package com.example.parku.data

// BQ15: entry point a parking lot's details was opened from.
enum class EntryPoint(val value: String) {
    MAP("map"),
    LIST("list"),
    SEARCH("search"),
    FAVORITES("favorites"),
    COMPARE("compare"),
}
