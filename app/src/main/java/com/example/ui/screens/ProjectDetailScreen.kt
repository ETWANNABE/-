package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.domain.PlacedWord
import com.example.data.domain.SimilarityPair
import com.example.data.local.CloudShape
import com.example.data.local.ColorPalette
import com.example.data.local.WordCloudProjectEntity
import com.example.data.repository.AggregatedWordData
import com.example.ui.components.WordCloudCanvas
import com.example.ui.dialogs.AddResponseDialog
import com.example.ui.dialogs.CloudSettingsDialog
import com.example.ui.dialogs.ExportDialog
import com.example.ui.dialogs.FilterAndWeightDialog
import com.example.ui.dialogs.SimilarityGroupingDialog
import com.example.ui.dialogs.WordDetailSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    project: WordCloudProjectEntity,
    aggregatedData: AggregatedWordData?,
    similarSuggestions: List<SimilarityPair>,
    selectedWordDetail: PlacedWord?,
    onBack: () -> Unit,
    onAddSingleResponse: (text: String, submitter: String) -> Unit,
    onAddBatchResponses: (textBlock: String, submitter: String) -> Unit,
    onUpdateSettings: (shape: String, palette: String, minSize: Int, maxSize: Int, allowRotation: Boolean) -> Unit,
    onUpdateFilters: (excluded: Set<String>, included: Set<String>, minFreq: Int) -> Unit,
    onSetWordBoost: (word: String, boostFactor: Float) -> Unit,
    onMergeWords: (canonical: String, members: List<String>) -> Unit,
    onMergeAllSuggested: () -> Unit,
    onDeleteWord: (word: String) -> Unit,
    onSelectWord: (PlacedWord?) -> Unit,
    onClearAllResponses: () -> Unit
) {
    BackHandler { onBack() }

    var showAddDialog by remember { mutableStateOf(false) }
    var showSimilarityDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val shapeEnum = remember(project.shape) {
        try { CloudShape.valueOf(project.shape) } catch (e: Exception) { CloudShape.CLOUD }
    }
    val paletteEnum = remember(project.colorPalette) {
        try { ColorPalette.valueOf(project.colorPalette) } catch (e: Exception) { ColorPalette.VIBRANT }
    }

    val totalResponses = aggregatedData?.rawResponseCount ?: 0
    val uniqueWords = aggregatedData?.wordCounts?.size ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "총 ${totalResponses}개 응답 (${uniqueWords}개 키워드)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onClearAllResponses,
                        modifier = Modifier.testTag("clear_all_responses_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "전체 데이터 초기화", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomBarActionButton(
                        icon = Icons.Default.Add,
                        label = "응답 추가",
                        testTag = "action_add_response",
                        onClick = { showAddDialog = true }
                    )

                    BottomBarActionButton(
                        icon = Icons.Default.Psychology,
                        label = "유사어 정리",
                        badgeCount = similarSuggestions.size,
                        testTag = "action_similarity_grouping",
                        onClick = { showSimilarityDialog = true }
                    )

                    BottomBarActionButton(
                        icon = Icons.Default.FilterAlt,
                        label = "필터 & 강조",
                        testTag = "action_filter_weight",
                        onClick = { showFilterDialog = true }
                    )

                    BottomBarActionButton(
                        icon = Icons.Default.Palette,
                        label = "스타일",
                        testTag = "action_settings",
                        onClick = { showSettingsDialog = true }
                    )

                    BottomBarActionButton(
                        icon = Icons.Default.Share,
                        label = "리포트",
                        testTag = "action_export",
                        onClick = { showExportDialog = true }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (totalResponses == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("☁️", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("아직 입력된 응답이 없습니다", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "하단의 '응답 추가' 버튼을 눌러 단어를 추가하면 구름 모양 워드 클라우드가 자동으로 생성됩니다!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("empty_add_response_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("첫 번째 응답 입력하기")
                        }
                    }
                }
            } else {
                WordCloudCanvas(
                    aggregatedData = aggregatedData,
                    shape = shapeEnum,
                    palette = paletteEnum,
                    minFontSize = project.minFontSize,
                    maxFontSize = project.maxFontSize,
                    allowRotation = project.allowRotation,
                    selectedWordId = selectedWordDetail?.id,
                    onWordClick = { word -> onSelectWord(word) }
                )
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddResponseDialog(
            onDismiss = { showAddDialog = false },
            onAddSingle = { text, submitter -> onAddSingleResponse(text, submitter) },
            onAddBatch = { textBlock, submitter -> onAddBatchResponses(textBlock, submitter) }
        )
    }

    if (showSimilarityDialog) {
        SimilarityGroupingDialog(
            suggestions = similarSuggestions,
            allWords = aggregatedData?.wordCounts?.keys?.toList() ?: emptyList(),
            onDismiss = { showSimilarityDialog = false },
            onMergePair = { canonical, members -> onMergeWords(canonical, members) },
            onMergeAllSuggested = { onMergeAllSuggested() }
        )
    }

    if (showFilterDialog) {
        FilterAndWeightDialog(
            initialExcluded = aggregatedData?.excludedWords ?: emptySet(),
            initialIncluded = aggregatedData?.includedOnlyWords ?: emptySet(),
            initialMinFreq = project.minFrequencyFilter,
            allWordsWithCounts = aggregatedData?.wordCounts ?: emptyMap(),
            currentBoosts = aggregatedData?.weightBoosts ?: emptyMap(),
            onDismiss = { showFilterDialog = false },
            onSaveFilters = { excluded, included, minFreq -> onUpdateFilters(excluded, included, minFreq) },
            onSetBoost = { word, boost -> onSetWordBoost(word, boost) }
        )
    }

    if (showSettingsDialog) {
        CloudSettingsDialog(
            currentShape = shapeEnum,
            currentPalette = paletteEnum,
            currentMinSize = project.minFontSize,
            currentMaxSize = project.maxFontSize,
            currentRotation = project.allowRotation,
            onDismiss = { showSettingsDialog = false },
            onSaveSettings = { shape, palette, minSize, maxSize, rotation ->
                onUpdateSettings(shape.name, palette.name, minSize, maxSize, rotation)
            }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            projectTitle = project.title,
            aggregatedData = aggregatedData,
            onDismiss = { showExportDialog = false }
        )
    }

    if (selectedWordDetail != null) {
        val currentBoost = aggregatedData?.weightBoosts?.get(selectedWordDetail.word) ?: 1.0f
        WordDetailSheet(
            word = selectedWordDetail,
            currentBoost = currentBoost,
            onDismiss = { onSelectWord(null) },
            onSetBoost = { word, boost -> onSetWordBoost(word, boost) },
            onDeleteWord = { word -> onDeleteWord(word) }
        )
    }
}

@Composable
private fun BottomBarActionButton(
    icon: ImageVector,
    label: String,
    badgeCount: Int = 0,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge { Text(badgeCount.toString()) }
                }
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (badgeCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
