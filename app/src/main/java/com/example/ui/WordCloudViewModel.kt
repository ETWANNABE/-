package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.domain.PlacedWord
import com.example.data.domain.SimilarityPair
import com.example.data.local.WordCloudDatabase
import com.example.data.local.WordCloudProjectEntity
import com.example.data.repository.AggregatedWordData
import com.example.data.repository.WordCloudRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordCloudViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WordCloudDatabase.getInstance(application)
    private val repository = WordCloudRepository(db.wordCloudDao())

    val projects: StateFlow<List<WordCloudProjectEntity>> = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentProject: StateFlow<WordCloudProjectEntity?> = _selectedProjectId
        .flatMapLatest { id ->
            if (id != null) repository.getProjectById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAggregatedData: StateFlow<AggregatedWordData?> = _selectedProjectId
        .flatMapLatest { id ->
            if (id != null) repository.getAggregatedWordDataFlow(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _similarSuggestions = MutableStateFlow<List<SimilarityPair>>(emptyList())
    val similarSuggestions: StateFlow<List<SimilarityPair>> = _similarSuggestions.asStateFlow()

    private val _selectedWordDetail = MutableStateFlow<PlacedWord?>(null)
    val selectedWordDetail: StateFlow<PlacedWord?> = _selectedWordDetail.asStateFlow()

    fun selectProject(projectId: Long?) {
        _selectedProjectId.value = projectId
        if (projectId != null) {
            loadSimilarSuggestions(projectId)
        }
    }

    fun loadSimilarSuggestions(projectId: Long) {
        viewModelScope.launch {
            _similarSuggestions.value = repository.getSimilarSuggestions(projectId)
        }
    }

    fun createProject(title: String, description: String, shape: String, palette: String) {
        viewModelScope.launch {
            val newId = repository.createProject(title, description, shape, palette)
            selectProject(newId)
        }
    }

    fun updateProjectSettings(
        shape: String,
        colorPalette: String,
        minFontSize: Int,
        maxFontSize: Int,
        allowRotation: Boolean
    ) {
        val proj = currentProject.value ?: return
        viewModelScope.launch {
            repository.updateProject(
                proj.copy(
                    shape = shape,
                    colorPalette = colorPalette,
                    minFontSize = minFontSize,
                    maxFontSize = maxFontSize,
                    allowRotation = allowRotation
                )
            )
        }
    }

    fun updateFilters(
        excludedSet: Set<String>,
        includedSet: Set<String>,
        minFrequencyFilter: Int
    ) {
        val proj = currentProject.value ?: return
        viewModelScope.launch {
            repository.updateProject(
                proj.copy(
                    excludedWordsJson = repository.toJsonArrayString(excludedSet),
                    includedOnlyWordsJson = repository.toJsonArrayString(includedSet),
                    minFrequencyFilter = minFrequencyFilter
                )
            )
        }
    }

    fun addSingleResponse(rawText: String, submitter: String = "") {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.addSingleResponse(projId, rawText, submitter)
            loadSimilarSuggestions(projId)
        }
    }

    fun addBatchResponses(textBlock: String, submitter: String = "") {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.addBatchTextResponses(projId, textBlock, submitter)
            loadSimilarSuggestions(projId)
        }
    }

    fun setWordWeightBoost(word: String, boostFactor: Float) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.setWordWeightBoost(projId, word, boostFactor)
        }
    }

    fun mergeWords(canonicalWord: String, memberWords: List<String>) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.mergeSimilarWords(projId, canonicalWord, memberWords)
            loadSimilarSuggestions(projId)
        }
    }

    fun deleteWordResponses(word: String) {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.deleteResponseWord(projId, word)
            _selectedWordDetail.value = null
            loadSimilarSuggestions(projId)
        }
    }

    fun clearAllResponses() {
        val projId = _selectedProjectId.value ?: return
        viewModelScope.launch {
            repository.clearAllResponses(projId)
            loadSimilarSuggestions(projId)
        }
    }

    fun setSelectedWordDetail(word: PlacedWord?) {
        _selectedWordDetail.value = word
    }

    fun deleteProject(project: WordCloudProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (_selectedProjectId.value == project.id) {
                _selectedProjectId.value = null
            }
        }
    }
}
