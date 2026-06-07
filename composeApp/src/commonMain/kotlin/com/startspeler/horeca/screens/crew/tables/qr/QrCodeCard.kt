package com.startspeler.horeca.screens.crew.tables.qr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import com.startspeler.horeca.downloadQrAsPng
import com.startspeler.horeca.printQrCode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewSurfaceVariant
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary

@Composable
fun QrCodeCard(
    content: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val matrix = remember(content) { QrMatrixGenerator.generate(content) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
            )

            if (matrix != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        val rows = matrix.size
                        val cols = matrix.firstOrNull()?.size ?: 0
                        if (rows == 0 || cols == 0) return@Canvas

                        val cellWidth = size.width / cols
                        val cellHeight = size.height / rows

                        for (y in 0 until rows) {
                            for (x in 0 until cols) {
                                if (matrix[y][x]) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(x * cellWidth, y * cellHeight),
                                        size = androidx.compose.ui.geometry.Size(cellWidth, cellHeight)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(CrewSurfaceVariant, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "QR-preview niet beschikbaar op dit platform.",
                        color = CrewTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = CrewTextSecondary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { downloadQrAsPng(content, "qr-${label}.png") },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Opslaan")
                }
                OutlinedButton(
                    onClick = { printQrCode(content, label) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Afdrukken")
                }
            }
        }
    }
}
