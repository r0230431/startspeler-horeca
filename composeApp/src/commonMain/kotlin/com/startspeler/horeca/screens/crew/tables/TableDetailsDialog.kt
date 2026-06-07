package com.startspeler.horeca.screens.crew.tables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.tables.TableResponse
import com.startspeler.horeca.screens.crew.tables.qr.QrCodeCard
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary

@Composable
fun TableDetailsDialog(
    table: TableResponse,
    canShareQr: Boolean,
    canPrintQr: Boolean,
    shareQrLabel: String,
    printQrLabel: String,
    onShareQr: () -> Unit,
    onPrintQr: () -> Unit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(modifier = Modifier
                .fillMaxWidth()) {
                Text(
                    text = "Tafel ${table.tableNumber}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Sluiten"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(30.dp)
                ) {
                    Text("Aantal zitplaatsen:", fontWeight = FontWeight.SemiBold)
                    Text(table.seatCount.toString(), fontWeight = FontWeight.SemiBold, color = CrewTextSecondary)
                }

                if (!table.note.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(72.dp)
                    ) {
                        Text("Opmerking:", fontWeight = FontWeight.SemiBold)
                        Text(table.note, color = CrewTextSecondary)
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Verwijderen",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Verwijderen")
                    }
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrewAccent,
                            contentColor = CrewOnAccent,
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Wijzigen",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Wijzigen")
                    }
                }

                QrCodeCard(
                    content = generateTableQrContent(table.tableNumber),
                    label = "QR-code voor tafel ${table.tableNumber}",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canShareQr) {
                        Button(
                            onClick = onShareQr,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrewAccent,
                                contentColor = CrewOnAccent,
                            )
                        ) {
                            Text(shareQrLabel)
                        }
                    }

                    if (canPrintQr) {
                        Button(
                            onClick = onPrintQr,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(printQrLabel)
                        }
                    }
                }

            }
        },
        confirmButton = {},
        dismissButton = {},
    )
}
