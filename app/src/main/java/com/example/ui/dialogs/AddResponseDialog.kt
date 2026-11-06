package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddResponseDialog(
    onDismiss: () -> Unit,
    onAddSingle: (text: String, submitter: String) -> Unit,
    onAddBatch: (textBlock: String, submitter: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Single, 1 = Batch
    var singleText by remember { mutableStateOf("") }
    var batchText by remember { mutableStateOf("") }
    var submitterName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_response_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "응답 및 단어 추가하기 ➕",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "사람들이 제출한 단어를 직접 입력하거나 텍스트를 붙여넣으세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("개별 단어 입력") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("일괄 붙여넣기") },
                        icon = { Icon(Icons.Default.ContentPaste, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = submitterName,
                    onValueChange = { submitterName = it },
                    label = { Text("응답자 이름/그룹 (선택 사항)") },
                    placeholder = { Text("예: 익명, A팀원, 수강생") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("submitter_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = singleText,
                        onValueChange = { singleText = it },
                        label = { Text("응답 단어 / 표현") },
                        placeholder = { Text("예: 행복함, 가성비, 마라탕") },
                        modifier = Modifier.fillMaxWidth().testTag("single_word_input"),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(
                        value = batchText,
                        onValueChange = { batchText = it },
                        label = { Text("전체 설문/의견 텍스트 붙여넣기") },
                        placeholder = { Text("설문 결과 텍스트나 여러 단어를 줄바꿈 또는 쉼표(,)로 구분해서 붙여넣으세요.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("batch_text_input")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("cancel_button")
                    ) {
                        Text("취소")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (selectedTab == 0 && singleText.isNotBlank()) {
                                onAddSingle(singleText, submitterName)
                                onDismiss()
                            } else if (selectedTab == 1 && batchText.isNotBlank()) {
                                onAddBatch(batchText, submitterName)
                                onDismiss()
                            }
                        },
                        enabled = (selectedTab == 0 && singleText.isNotBlank()) || (selectedTab == 1 && batchText.isNotBlank()),
                        modifier = Modifier.weight(1f).testTag("confirm_add_button")
                    ) {
                        Text("추가하기")
                    }
                }
            }
        }
    }
}
