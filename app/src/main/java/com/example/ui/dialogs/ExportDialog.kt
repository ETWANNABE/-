package com.example.ui.dialogs

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.AggregatedWordData

@Composable
fun ExportDialog(
    projectTitle: String,
    aggregatedData: AggregatedWordData?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val wordCounts = aggregatedData?.wordCounts ?: emptyMap()
    val totalCount = aggregatedData?.rawResponseCount ?: 0

    val sortedEntries = wordCounts.entries.sortedByDescending { it.value }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.80f)
                .padding(12.dp)
                .testTag("export_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "분석 리포트 & 데이터 공유 📊",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "'$projectTitle' - 총 ${totalCount}개 응답 분석 결과",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("단어 / 표현", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("빈도수 (비율)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(sortedEntries) { index, entry ->
                        val ratio = if (totalCount > 0) (entry.value.toFloat() / totalCount) * 100f else 0f
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${index + 1}. ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = entry.key,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }

                            Text(
                                text = "${entry.value}회 (${String.format("%.1f", ratio)}%)",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        shareReportText(context, projectTitle, sortedEntries, totalCount)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_text_report_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("텍스트 분석 리포트 공유하기")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("닫기")
                }
            }
        }
    }
}

private fun shareReportText(
    context: Context,
    title: String,
    entries: List<Map.Entry<String, Int>>,
    totalCount: Int
) {
    val sb = java.lang.StringBuilder()
    sb.append("📊 [워드 클라우드 분석 리포트]\n")
    sb.append("주제: $title\n")
    sb.append("총 응답 수: ${totalCount}개\n\n")
    sb.append("--- 주요 키워드 순위 ---\n")

    entries.take(20).forEachIndexed { index, entry ->
        val ratio = if (totalCount > 0) (entry.value.toFloat() / totalCount) * 100f else 0f
        sb.append("${index + 1}. ${entry.key}: ${entry.value}회 (${String.format("%.1f", ratio)}%)\n")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "$title - 워드 클라우드 리포트")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "분석 리포트 공유"))
}
