package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.WordCloudViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProjectDetailScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WordCloudApp()
                }
            }
        }
    }
}

@Composable
fun WordCloudApp(viewModel: WordCloudViewModel = viewModel()) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val currentAggregatedData by viewModel.currentAggregatedData.collectAsStateWithLifecycle()
    val similarSuggestions by viewModel.similarSuggestions.collectAsStateWithLifecycle()
    val selectedWordDetail by viewModel.selectedWordDetail.collectAsStateWithLifecycle()

    val project = currentProject

    if (project == null) {
        HomeScreen(
            projects = projects,
            onSelectProject = { projectId -> viewModel.selectProject(projectId) },
            onCreateProject = { title, desc, shape, palette ->
                viewModel.createProject(title, desc, shape, palette)
            },
            onDeleteProject = { proj -> viewModel.deleteProject(proj) }
        )
    } else {
        ProjectDetailScreen(
            project = project,
            aggregatedData = currentAggregatedData,
            similarSuggestions = similarSuggestions,
            selectedWordDetail = selectedWordDetail,
            onBack = { viewModel.selectProject(null) },
            onAddSingleResponse = { text, submitter ->
                viewModel.addSingleResponse(text, submitter)
            },
            onAddBatchResponses = { textBlock, submitter ->
                viewModel.addBatchResponses(textBlock, submitter)
            },
            onUpdateSettings = { shape, palette, minSize, maxSize, allowRotation ->
                viewModel.updateProjectSettings(shape, palette, minSize, maxSize, allowRotation)
            },
            onUpdateFilters = { excluded, included, minFreq ->
                viewModel.updateFilters(excluded, included, minFreq)
            },
            onSetWordBoost = { word, boostFactor ->
                viewModel.setWordWeightBoost(word, boostFactor)
            },
            onMergeWords = { canonical, members ->
                viewModel.mergeWords(canonical, members)
            },
            onMergeAllSuggested = {
                similarSuggestions.forEach { pair ->
                    viewModel.mergeWords(pair.suggestedCanonical, listOf(pair.wordA, pair.wordB))
                }
            },
            onDeleteWord = { word ->
                viewModel.deleteWordResponses(word)
            },
            onSelectWord = { word ->
                viewModel.setSelectedWordDetail(word)
            },
            onClearAllResponses = {
                viewModel.clearAllResponses()
            }
        )
    }
}
