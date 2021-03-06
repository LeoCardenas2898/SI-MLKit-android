package com.untels.intelligent.systems.viewmodel

import android.app.Application
import android.util.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*
import com.untels.intelligent.systems.model.Language

class TranslateViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        /* This specifies the number of translators instance we want to keep in our LRU cache.
         * Each instance of the translator is built with different options based on the source
         * language and the target language, and since we want to be able to manage the number of
         * translator instances to keep around, an LRU cache is an easy way to achieve this. */
        private const val NUM_TRANSLATORS = 3
    }

    val inputLanguageMutableLiveData = MutableLiveData<Language>()
    val outputLanguageMutableLiveData = MutableLiveData<Language>()
    val sourceText = MutableLiveData<String>()
    val translatedText = MediatorLiveData<ResultOrError>()
    val availableModels = MutableLiveData<List<String>>()

    private val modelManager = RemoteModelManager.getInstance()
    private val translators =
        object : LruCache<TranslatorOptions, Translator>(NUM_TRANSLATORS) {
            override fun create(options: TranslatorOptions): Translator = Translation.getClient(options)

            override fun entryRemoved(
                evicted: Boolean,
                key: TranslatorOptions,
                oldValue: Translator,
                newValue: Translator?
            ) {
                oldValue.close()
            }
        }

    val availableLanguages = TranslateLanguage.getAllLanguages().map { Language(code = it) }

    inner class ResultOrError(var result: String?, var error: Exception?)

    // Starts downloading a remote model for local translation.
    internal fun downloadLanguage(language: Language) {
        val model = getModel(TranslateLanguage.fromLanguageTag(language.code)!!)
        modelManager.download(model, DownloadConditions.Builder().build())
            .addOnCompleteListener { fetchDownloadedModels() }
    }

    // Updates the list of downloaded models available for local translation.
    private fun fetchDownloadedModels() {
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { remoteModels ->
                availableModels.value =
                    remoteModels.sortedBy { it.language }.map { it.language }
            }
    }

    // Deletes a locally stored translation model.
    internal fun deleteLanguage(language: Language) {
        val model = getModel(TranslateLanguage.fromLanguageTag(language.code)!!)
        modelManager.deleteDownloadedModel(model).addOnCompleteListener { fetchDownloadedModels() }
    }

    private fun getModel(languageCode: String): TranslateRemoteModel {
        return TranslateRemoteModel.Builder(languageCode).build()
    }

    init {
        // Create a translation result or error object.
        val processTranslation = OnCompleteListener<String> { task ->
            if (task.isSuccessful) {
                translatedText.value = ResultOrError(task.result, null)
            } else {
                translatedText.value = ResultOrError(null, task.exception)
            }
            // Update the list of downloaded models as more may have been
            // automatically downloaded due to requested translation.
            fetchDownloadedModels()
        }
        // Start translation if any of the following change: input text, source lang, target lang.
        translatedText.addSource(sourceText) { translate().addOnCompleteListener(processTranslation) }
        val languageObserver =
            Observer<Language> { translate().addOnCompleteListener(processTranslation) }
        translatedText.addSource(inputLanguageMutableLiveData, languageObserver)
        translatedText.addSource(outputLanguageMutableLiveData, languageObserver)

        // Update the list of downloaded models.
        fetchDownloadedModels()
    }

    private fun translate(): Task<String> {
        val text = sourceText.value
        val source = inputLanguageMutableLiveData.value
        val target = outputLanguageMutableLiveData.value
        if (source == null || target == null || text == null || text.isEmpty()) {
            return Tasks.forResult("")
        }
        val sourceLangCode = TranslateLanguage.fromLanguageTag(source.code)!!
        val targetLangCode = TranslateLanguage.fromLanguageTag(target.code)!!
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()
        return translators[options].downloadModelIfNeeded().continueWithTask { task ->
            if (task.isSuccessful) {
                translators[options].translate(text)
            } else {
                Tasks.forException<String>(
                    task.exception
                        ?: Exception("Error desconocido. Inténtelo en otro momento.")
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Each new instance of a translator needs to be closed appropriately. Here we utilize the
        // ViewModel's onCleared() to clear our LruCache and close each Translator instance when
        // this ViewModel is no longer used and destroyed.
        translators.evictAll()
    }
}