package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.local.CloudShape
import com.example.data.local.ColorPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudSettingsDialog(
    currentShape: CloudShape,
    currentPalette: ColorPalette,
    currentMinSize: Int,
    currentMaxSize: Int,
    currentRotation: Boolean,
    onDismiss: () -> Unit,
    onSaveSettings: (shape: CloudShape, palette: ColorPalette, minSize: Int, maxSize: Int, allowRotation: Boolean) -> Unit
) {
    var selectedShape by remember { mutableStateOf(currentShape) }
    var selectedPalette by remember { mutableStateOf(currentPalette) }
    var minSize by remember { mutableFloatStateOf(currentMinSize.toFloat()) }
    var maxSize by remember { mutableFloatStateOf(currentMaxSize.toFloat()) }
    var allowRotation by remember { mutableStateOf(currentRotation) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("cloud_settings_dialog"),
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
                        Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "워드 클라우드 스타일 설정 ⚙️",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("구름 형태 (Shape)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CloudShape.values().forEach { shape ->
                        val isSelected = shape == selectedShape
                        Card(
                            modifier = Modifier
                                .clickable { selectedShape = shape }
                                .testTag("shape_picker_${shape.name}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = shape.displayName,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("컬러 팔레트 (Palette)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorPalette.values().forEach { palette ->
                        val isSelected = palette == selectedPalette
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                                .clickable { selectedPalette = palette }
                                .padding(8.dp)
                                .testTag("palette_picker_${palette.name}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = palette.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.width(120.dp)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                palette.hexColors.take(5).forEach { hex ->
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(hex))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "글자 크기 범위 (${minSize.toInt()}sp ~ ${maxSize.toInt()}sp)",
                    style = MaterialTheme.typography.titleMedium
                )

                Slider(
                    value = maxSize,
                    onValueChange = { maxSize = it },
                    valueRange = 36f..80f,
                    modifier = Modifier.fillMaxWidth().testTag("max_font_size_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("세로 글자 회전 허용", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = allowRotation,
                        onCheckedChange = { allowRotation = it },
                        modifier = Modifier.testTag("rotation_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                            onSaveSettings(selectedShape, selectedPalette, minSize.toInt(), maxSize.toInt(), allowRotation)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_cloud_settings_button")
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }
}
