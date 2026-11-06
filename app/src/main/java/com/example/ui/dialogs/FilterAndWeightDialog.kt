package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterAndWeightDialog(
    initialExcluded: Set<String>,
    initialIncluded: Set<String>,
    initialMinFreq: Int,
    allWordsWithCounts: Map<String, Int>,
    currentBoosts: Map<String, Float>,
    onDismiss: () -> Unit,
    onSaveFilters: (excluded: Set<String>, included: Set<String>, minFreq: Int) -> Unit,
    onSetBoost: (word: String, boostFactor: Float) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0=Excluded, 1=Included, 2=Frequency, 3=Weight Boost

    val excludedList = remember { mutableStateListOf<String>().apply { addAll(initialExcluded) } }
    val includedList = remember { mutableStateListOf<String>().apply { addAll(initialIncluded) } }
    var minFreq by remember { mutableIntStateOf(initialMinFreq) }

    var newExcludeWord by remember { mutableStateOf("") }
    var newIncludeWord by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
                .testTag("filter_and_weight_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FilterAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "필터링 & 단어 강조 가중치 🎛️",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("제외할 단어 (${excludedList.size})") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("지정 포함 단어 (${includedList.size})") }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("최소 빈도 (${minFreq}회)") }
                    )
                    Tab(
                        selected = selectedTabIndex == 3,
                        onClick = { selectedTabIndex = 3 },
                        text = { Text("단어 크기 강조 ⚡") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTabIndex) {
                    0 -> { // Excluded Words
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "워드 클라우드에서 감출 조사, 부사, 또는 불필요한 단어를 추가하세요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newExcludeWord,
                                    onValueChange = { newExcludeWord = it },
                                    label = { Text("제외할 단어 입력") },
                                    placeholder = { Text("예: 은, 는, 이, 가, 그리고") },
                                    modifier = Modifier.weight(1f).testTag("add_exclude_input"),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val clean = newExcludeWord.trim().lowercase()
                                        if (clean.isNotEmpty() && !excludedList.contains(clean)) {
                                            excludedList.add(clean)
                                            newExcludeWord = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("add_exclude_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "추가")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                item {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        excludedList.forEach { word ->
                                            FilterChip(
                                                selected = true,
                                                onClick = { excludedList.remove(word) },
                                                label = { Text(word) },
                                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "제거") }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Included Words Whitelist
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "지정한 키워드만 워드 클라우드에 표시합니다. (비어있으면 모든 단어 표시)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newIncludeWord,
                                    onValueChange = { newIncludeWord = it },
                                    label = { Text("포함할 키워드 입력") },
                                    placeholder = { Text("예: 행복, 디자인, AI") },
                                    modifier = Modifier.weight(1f).testTag("add_include_input"),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val clean = newIncludeWord.trim().lowercase()
                                        if (clean.isNotEmpty() && !includedList.contains(clean)) {
                                            includedList.add(clean)
                                            newIncludeWord = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("add_include_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "추가")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                item {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        includedList.forEach { word ->
                                            FilterChip(
                                                selected = true,
                                                onClick = { includedList.remove(word) },
                                                label = { Text(word) },
                                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "제거") }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Frequency Filter
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "최소 N회 이상 언급된 주요 단어만 골라내어 워드 클라우드를 단순화합니다.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "최소 언급 횟수: ${minFreq}회 이상",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            Slider(
                                value = minFreq.toFloat(),
                                onValueChange = { minFreq = it.toInt() },
                                valueRange = 1f..10f,
                                steps = 8,
                                modifier = Modifier.fillMaxWidth().testTag("min_frequency_slider")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val filteredCount = allWordsWithCounts.count { it.value >= minFreq }
                            Text(
                                text = "현재 조건으로 표시될 단어 수: 총 ${filteredCount}개",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    3 -> { // Word Weight Boost
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "특정 중요한 키워드의 중요도(크기 배율)를 높여 워드 클라우드에서 시각적으로 크게 강조하세요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(allWordsWithCounts.keys.toList()) { word ->
                                    val currentBoost = currentBoosts[word] ?: 1.0f
                                    var sliderVal by remember(word) { mutableFloatStateOf(currentBoost) }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = word,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = "강조 배율: ${String.format("%.1f", sliderVal)}x",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            Slider(
                                                value = sliderVal,
                                                onValueChange = {
                                                    sliderVal = it
                                                    onSetBoost(word, it)
                                                },
                                                valueRange = 1.0f..3.0f,
                                                steps = 3,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveFilters(excludedList.toSet(), includedList.toSet(), minFreq)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_filters_button")
                    ) {
                        Text("적용하기")
                    }
                }
            }
        }
    }
}
