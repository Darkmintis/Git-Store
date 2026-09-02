package com.darkmintis.gitstore.core.data.services

import com.darkmintis.gitstore.core.domain.model.TranslationLanguage
import com.darkmintis.gitstore.core.domain.repository.TranslationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeTranslationRepository : TranslationRepository {
    private val _targetLang = MutableStateFlow(TranslationLanguage.SYSTEM)
    private val _autoTranslate = MutableStateFlow(false)

    override fun getTargetLanguage(): Flow<TranslationLanguage> = _targetLang
    override suspend fun setTargetLanguage(language: TranslationLanguage) { _targetLang.value = language }
    override fun isAutoTranslateEnabled(): Flow<Boolean> = _autoTranslate
    override suspend fun setAutoTranslateEnabled(enabled: Boolean) { _autoTranslate.value = enabled }
}

class FakeLocalizationManager : LocalizationManager {
    override fun getCurrentLanguageCode(): String = "en"
    override fun getPrimaryLanguageCode(): String = "en"
}

class TranslationServiceTest {

    private lateinit var fakeRepo: FakeTranslationRepository
    private lateinit var fakeLoc: FakeLocalizationManager

    @BeforeTest
    fun setup() {
        fakeRepo = FakeTranslationRepository()
        fakeLoc = FakeLocalizationManager()
    }

    private fun createService(): MyMemoryTranslationService =
        MyMemoryTranslationService(
            fakeRepo,
            fakeLoc,
            diskCache = null,
            scope = CoroutineScope(Dispatchers.Unconfined),
        )

    @Test
    fun `test language resolution uses system locale`() = runTest {
        assertEquals("en", createService().getEffectiveLanguageCode())

        fakeRepo.setTargetLanguage(TranslationLanguage.FRENCH)
        assertEquals("fr", createService().getEffectiveLanguageCode())

        fakeRepo.setTargetLanguage(TranslationLanguage.SYSTEM)
        assertEquals("en", createService().getEffectiveLanguageCode())
    }

    @Test
    fun `test needsTranslation when content language differs from target`() = runTest {
        fakeRepo.setTargetLanguage(TranslationLanguage.ENGLISH)
        val englishTarget = createService()

        assertTrue(englishTarget.needsTranslation("这是一个测试"))
        assertFalse(
            englishTarget.needsTranslation(
                "This is the app for you to install and use from the store."
            )
        )

        fakeRepo.setTargetLanguage(TranslationLanguage.FRENCH)
        val frenchTarget = createService()
        assertTrue(
            frenchTarget.needsTranslation(
                "This is the app for you to install and use from the store."
            )
        )
    }

    @Test
    fun `test needsTranslation is false when language cannot be detected`() = runTest {
        fakeRepo.setTargetLanguage(TranslationLanguage.ENGLISH)
        assertFalse(createService().needsTranslation("v1.2.3"))
    }

    @Test
    fun `test translation language from code defaults to system`() {
        assertEquals(TranslationLanguage.SYSTEM, TranslationLanguage.fromCode(null))
        assertEquals(TranslationLanguage.FRENCH, TranslationLanguage.fromCode("fr"))
        assertEquals(TranslationLanguage.ENGLISH, TranslationLanguage.fromCode("en"))
        assertEquals(TranslationLanguage.SYSTEM, TranslationLanguage.fromCode("unknown_code"))
    }

    @Test
    fun `picker options start with system then english`() {
        assertEquals(TranslationLanguage.SYSTEM, TranslationLanguage.pickerOptions.first())
        assertEquals(TranslationLanguage.ENGLISH, TranslationLanguage.pickerOptions[1])
        assertEquals(8, TranslationLanguage.pickerOptions.size)
    }
}
