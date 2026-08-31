package com.darkmintis.gitstore.core.data.services

import com.darkmintis.gitstore.core.domain.model.TranslationLanguage
import com.darkmintis.gitstore.core.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeTranslationRepository : TranslationRepository {
    private val _targetLang = MutableStateFlow(TranslationLanguage.FRENCH)
    private val _autoTranslate = MutableStateFlow(true)

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
    private lateinit var service: MyMemoryTranslationService

    @BeforeTest
    fun setup() {
        fakeRepo = FakeTranslationRepository()
        fakeLoc = FakeLocalizationManager()
        service = MyMemoryTranslationService(fakeRepo, fakeLoc)
    }

    @Test
    fun `test language resolution`() = runTest {
        assertEquals("fr", service.getEffectiveLanguageCode())

        fakeRepo.setTargetLanguage(TranslationLanguage.ENGLISH)
        assertEquals("en", service.getEffectiveLanguageCode())

        fakeRepo.setTargetLanguage(TranslationLanguage.SYSTEM)
        assertEquals("en", service.getEffectiveLanguageCode())
    }

    @Test
    fun `test non latin detection`() {
        assertTrue(service.containsNonLatin("这是一个测试"))
        assertTrue(service.containsNonLatin("これはテストです"))
        assertTrue(service.containsNonLatin("이것은 테스트입니다"))
        assertTrue(service.containsNonLatin("Это тест"))
        assertTrue(service.containsNonLatin("هذا اختبار"))
        assertFalse(service.containsNonLatin("This is a simple English sentence."))
    }

    @Test
    fun `test chinese detection`() {
        assertTrue(service.isChinese("这是一个测试"))
        assertFalse(service.isChinese("This is English"))
    }

    @Test
    fun `test translation language from code`() {
        assertEquals(TranslationLanguage.FRENCH, TranslationLanguage.fromCode("fr"))
        assertEquals(TranslationLanguage.ENGLISH, TranslationLanguage.fromCode("en"))
        assertEquals(TranslationLanguage.SPANISH, TranslationLanguage.fromCode("es"))
        assertEquals(TranslationLanguage.GERMAN, TranslationLanguage.fromCode("de"))
        assertEquals(TranslationLanguage.FRENCH, TranslationLanguage.fromCode("unknown_code"))
    }
}
