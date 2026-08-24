package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ForwardToInbox
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.WarehouseViewModel
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseSteelBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmailReportDialog(
    viewModel: WarehouseViewModel,
    onDismiss: () -> Unit,
    initialReceiverEmail: String = "rmangrola04@gmail.com",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val allUsers by viewModel.allUsers.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()

    var isUserReportOnly by remember { mutableStateOf(true) }
    var receiverEmail by remember { mutableStateOf(initialReceiverEmail) }

    val dateStr = remember { SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date()) }
    var subject by remember {
        mutableStateOf("📊 Daily User & System Activity Report - $dateStr")
    }

    var body by remember {
        mutableStateOf(
            """
Hello Admin,

Please find attached the latest user activity and login report.
You can download and review the attached CSV / Excel file.

Regards,
Automated System Bot
            """.trimIndent()
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("email_report_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(WarehouseNavy.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ForwardToInbox,
                                contentDescription = null,
                                tint = WarehouseNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Send Email Report",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Excel / CSV Email Automation",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("email_report_close_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Report Type Selector Chips
                Text(
                    text = "Select Report Type:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isUserReportOnly,
                        onClick = {
                            isUserReportOnly = true
                            subject = "📊 User Activity & Login Report - $dateStr"
                        },
                        label = { Text("1. User Activity (${allUsers.size})", fontSize = 11.5.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarehouseNavy,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_user_report")
                    )

                    FilterChip(
                        selected = !isUserReportOnly,
                        onClick = {
                            isUserReportOnly = false
                            subject = "📊 Daily User & System Activity Report - $dateStr"
                        },
                        label = { Text("2. Complete Warehouse (${allEntries.size})", fontSize = 11.5.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarehouseNavy,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_full_report")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Attachment Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarehouseEmerald.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = WarehouseEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isUserReportOnly) "Attachment: User_Activity_Report.csv" else "Attachment: Warehouse_System_Activity_Report.csv",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = WarehouseNavy
                            )
                            Text(
                                text = "Headers: User ID, Full Name, Email Address, Registered Date, Last Login, Status",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Receiver Email
                OutlinedTextField(
                    value = receiverEmail,
                    onValueChange = { receiverEmail = it },
                    label = { Text("Receiver Email") },
                    placeholder = { Text("admin_email@example.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WarehouseNavy) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_receiver_email")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subject
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_email_subject")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Message Body
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Body Message") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_email_body")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val content = if (isUserReportOnly) {
                                viewModel.generateUserActivityCsvReport()
                            } else {
                                viewModel.generateCombinedWarehouseCsvReport()
                            }
                            clipboardManager.setText(AnnotatedString(content))
                            Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_copy_csv_content")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy CSV", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            try {
                                val emailIntent = viewModel.prepareEmailReportIntent(
                                    context = context,
                                    receiverEmail = receiverEmail,
                                    isUserReportOnly = isUserReportOnly,
                                    customSubject = subject,
                                    customBody = body
                                )
                                val chooser = Intent.createChooser(emailIntent, "Send Report via Email")
                                context.startActivity(chooser)
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("btn_dispatch_email_report")
                    ) {
                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Email", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}
