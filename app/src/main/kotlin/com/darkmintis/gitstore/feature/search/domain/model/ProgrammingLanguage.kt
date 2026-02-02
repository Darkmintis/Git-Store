package com.darkmintis.gitstore.feature.search.domain.model;

import com.darkmintis.gitstore.R

enum class ProgrammingLanguage(val queryValue: String?) {
    All(null),
    Kotlin("kotlin"),
    Java("java"),
    JavaScript("javascript"),
    TypeScript("typescript"),
    Python("python"),
    Swift("swift"),
    Rust("rust"),
    Go("go"),
    CSharp("c#"),
    CPlusPlus("c++"),
    C("c"),
    Dart("dart"),
    Ruby("ruby"),
    PHP("php");

    fun label(): Int = when (this) {
        All -> R.string.language_all
        Kotlin -> R.string.language_kotlin
        Java -> R.string.language_java
        JavaScript -> R.string.language_javascript
        TypeScript -> R.string.language_typescript
        Python -> R.string.language_python
        Swift -> R.string.language_swift
        Rust -> R.string.language_rust
        Go -> R.string.language_go
        CSharp -> R.string.language_csharp
        CPlusPlus -> R.string.language_cpp
        C -> R.string.language_c
        Dart -> R.string.language_dart
        Ruby -> R.string.language_ruby
        PHP -> R.string.language_php
    }

    companion object {
        fun fromLanguageString(lang: String?): ProgrammingLanguage {
            if (lang == null) return All
            return entries.find {
                it.queryValue?.equals(lang, ignoreCase = true) == true
            } ?: All
        }
    }
}




