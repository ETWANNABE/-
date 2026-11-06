package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.domain.SimilarityPair

@Composable
fun SimilarityGroupingDialog(
    suggestions: List<SimilarityPair>,
    allWords: List<String>,
    onDismiss: () -> Unit,
    onMergePair: (canonicalWord: String, memberWords: List<String>) -> Unit,
    onMergeAllSuggested: () -> Unit
) {
    var isManualMode by remember { mutableStateOf(false) }
    var customCanonicalWord by remember { mutableStateOf("") }
    val selectedMemberWords = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
                .testTag("similarity_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "유사어 & 동의어 정리 🧩",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "비슷한 어조나 표기가 다른 단어들을 하나의 그룹으로 묶어 워드 클라우드에 크게 표시합니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isManualMode = false },
                        modifier = Modifier.weight(1f).testTag("suggested_tab_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "자동 감지 (${suggestions.size})", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { isManualMode = true },
                        modifier = Modifier.weight(1f).testTag("manual_tab_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "직접 수동 선택", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isManualMode) {
                    if (suggestions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🎉 정리할 유사어가 없습니다!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "모든 단어가 깔끔하게 분리되어 있거나 이미 병합되었습니다.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                onMergeAllSuggested()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("merge_all_suggested_button"),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("모든 추천 유사어 1-클릭 일괄 병합하기")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(suggestions) { pair ->
                                CandidateCard(
                                    pair = pair,
                                    onMerge = {
                                        onMergePair(pair.suggestedCanonical, listOf(pair.wordA, pair.wordB))
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Manual selection mode
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = customCanonicalWord,
                            onValueChange = { customCanonicalWord = it },
                            label = { Text("대표 단어 이름 지정") },
                            placeholder = { Text("예: 행복함 (또는 긍정적 기분)") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_canonical_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "묶을 단어들 선택 (${selectedMemberWords.size}개 선택됨):",
                            style = MaterialTheme.typography.labelMedium
                        )

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 4.dp)
                        ) {
                            items(allWords) { word ->
                                val isChecked = selectedMemberWords.contains(word)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) selectedMemberWords.remove(word)
                                            else selectedMemberWords.add(word)
                                        }
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedMemberWords.add(word)
                                            else selectedMemberWords.remove(word)
                                        }
                                    )
                                    Text(
                                        text = word,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (customCanonicalWord.isNotBlank() && selectedMemberWords.isNotEmpty()) {
                                    onMergePair(customCanonicalWord, selectedMemberWords.toList())
                                    onDismiss()
                                }
                            },
                            enabled = customCanonicalWord.isNotBlank() && selectedMemberWords.size >= 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_manual_merge_button")
                        ) {
                            Icon(Icons.Default.MergeType, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("선택한 단어 그룹으로 합치기")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("close_similarity_dialog")
                ) {
                    Text("닫기")
                }
            }
        }
    }
}

@Composable
private fun CandidateCard(
    pair: SimilarityPair,
    onMerge: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pair.wordA,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " (${pair.countA}회)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " ↔ ",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = pair.wordB,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " (${pair.countB}회)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "유사도 ${(pair.similarityScore * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "대표: [${pair.suggestedCanonical}]",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onMerge,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("merge_pair_button_${pair.wordA}")
            ) {
                Text("합치기", fontSize = 12.sp)
            }
        }
    }
}
