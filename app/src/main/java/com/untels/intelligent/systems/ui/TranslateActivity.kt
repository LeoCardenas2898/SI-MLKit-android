package com.untels.intelligent.systems.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.untels.intelligent.systems.R
import com.untels.intelligent.systems.databinding.ActivityTranslateBinding
import com.untels.intelligent.systems.model.Language
import com.untels.intelligent.systems.viewmodel.TranslateViewModel

class TranslateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTranslateBinding
    private lateinit var viewModel: TranslateViewModel
    private lateinit var languageAdapter: ArrayAdapter<Language>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranslateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[TranslateViewModel::class.java]
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        languageAdapter =
            ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_dropdown_item,
                viewModel.availableLanguages
            )
        binding.backImageView.setOnClickListener {
            finish()
        }
        binding.inputLangSpinner.run {
            adapter = languageAdapter
            setSelection(languageAdapter.getPosition(Language("es")))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    setProgressText(binding.outputTextView)
                    viewModel.inputLanguageMutableLiveData.value = languageAdapter.getItem(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    binding.outputTextView.text = ""
                }
            }
        }
        binding.outputLangSpinner.run {
            adapter = languageAdapter
            setSelection(languageAdapter.getPosition(Language("en")))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    setProgressText(binding.outputTextView)
                    viewModel.outputLanguageMutableLiveData.value = languageAdapter.getItem(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    binding.outputTextView.text = ""
                }
            }
        }
        binding.buttonSwitchLang.setOnClickListener {
            setProgressText(binding.outputTextView)
            val sourceLangPosition = binding.inputLangSpinner.selectedItemPosition
            binding.inputLangSpinner.setSelection(binding.outputLangSpinner.selectedItemPosition)
            binding.outputLangSpinner.setSelection(sourceLangPosition)
        }
        binding.syncInputButton.setOnCheckedChangeListener { _, isChecked ->
            languageAdapter.getItem(binding.inputLangSpinner.selectedItemPosition)?.let {
                if (isChecked) {
                    viewModel.downloadLanguage(it)
                } else {
                    viewModel.deleteLanguage(it)
                }
            }
        }
        binding.syncOutputButton.setOnCheckedChangeListener { _, isChecked ->
            languageAdapter.getItem(binding.outputLangSpinner.selectedItemPosition)?.let {
                if (isChecked) {
                    viewModel.downloadLanguage(it)
                } else {
                    viewModel.deleteLanguage(it)
                }
            }
        }
        binding.inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                setProgressText(binding.outputTextView)
                viewModel.sourceText.postValue(s.toString())
            }
        })
    }

    private fun setupObservers() {
        with(viewModel) {
            translatedText.observe(this@TranslateActivity, { resultOrError ->
                resultOrError?.let {
                    if (it.error != null) {
                        binding.outputTextView.error = resultOrError.error?.localizedMessage
                    } else {
                        binding.outputTextView.text = resultOrError.result
                    }
                }
            })

            // Update sync toggle button states based on downloaded models list.
            availableModels.observe(this@TranslateActivity, { translateRemoteModels ->
                binding.downloadedModels.text = getString(R.string.downloaded_models_label, translateRemoteModels)
                translateRemoteModels?.let {
                    binding.syncInputButton.isChecked = it.contains(
                        languageAdapter.getItem(binding.inputLangSpinner.selectedItemPosition)!!.code
                    )
                    binding.syncOutputButton.isChecked = it.contains(
                        languageAdapter.getItem(binding.outputLangSpinner.selectedItemPosition)!!.code
                    )
                }
            })
        }
    }

    private fun setProgressText(tv: TextView) {
        tv.text = "Traduciendo..."
    }
}