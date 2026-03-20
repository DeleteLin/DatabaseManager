package space.xiaoxiao.databasemanager.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalizationState {
    private val _language = MutableStateFlow(Language.CHINESE)
    val language: StateFlow<Language> = _language.asStateFlow()

    var isLoaded: Boolean = false
        private set

    fun loadFromConfig(lang: String?) {
        val language = when (lang) {
            "en" -> Language.ENGLISH
            else -> Language.CHINESE
        }
        setLanguage(language)
        isLoaded = true
    }

    fun setLanguage(lang: Language) {
        _language.value = lang
    }

    fun toConfigLanguage(): String {
        return when (_language.value) {
            Language.CHINESE -> "zh"
            Language.ENGLISH -> "en"
        }
    }
}