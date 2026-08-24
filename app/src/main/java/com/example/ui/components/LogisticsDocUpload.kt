package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.WarehouseAccentAmber
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseRed
import com.example.ui.theme.WarehouseSteelBlue

data class DocumentAttachment(
    val fileName: String,
    val fileUri: Uri? = null,
    val fileSizeFormatted: String = "Uploaded",
    val isPdf: Boolean = false
)

data class DocFormData(
    val vehicleNo: String = "",
    val invoiceFile: DocumentAttachment? = null,
    val lrFile: DocumentAttachment? = null,
    val checklistDone: Boolean = false,
    val hasDiscrepancy: Boolean = false,
    val discrepancyType: String = "",
    val discrepancyFile: DocumentAttachment? = null,
    val remarks: String = ""
)

/**
 * Logistics Documentation, Checklist, and Discrepancy reporting component.
 * Direct translation of the LogisticsDocUpload React component into Jetpack Compose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsDocUpload(
    processType: String = "Unloading",
    initialVehicleNo: String = "",
    initialFormData: DocFormData = DocFormData(vehicleNo = initialVehicleNo),
    onFormDataChange: ((DocFormData) -> Unit)? = null,
    onSubmit: ((DocFormData) -> Unit)? = null,
    onSave: (DocFormData) -> Unit = {},
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasDiscrepancy by remember { mutableStateOf(initialFormData.hasDiscrepancy) }
    var formData by remember {
        mutableStateOf(
            initialFormData.copy(
                vehicleNo = if (initialFormData.vehicleNo.isNotBlank()) initialFormData.vehicleNo else initialVehicleNo,
                hasDiscrepancy = initialFormData.hasDiscrepancy
            )
        )
    }

    fun updateData(newForm: DocFormData) {
        formData = newForm
        onFormDataChange?.invoke(newForm)
    }

    var discrepancyDropdownExpanded by remember { mutableStateOf(false) }

    // File pickers for Invoice, LR, and Discrepancy Proof
    val invoiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "invoice_document.pdf"
            val isPdf = name.endsWith(".pdf", ignoreCase = true)
            val attachment = DocumentAttachment(fileName = name, fileUri = uri, isPdf = isPdf)
            updateData(formData.copy(invoiceFile = attachment))
        }
    }

    val lrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "lr_receipt_copy.pdf"
            val isPdf = name.endsWith(".pdf", ignoreCase = true)
            val attachment = DocumentAttachment(fileName = name, fileUri = uri, isPdf = isPdf)
            updateData(formData.copy(lrFile = attachment))
        }
    }

    val discrepancyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "discrepancy_damage_proof.jpg"
            val isPdf = name.endsWith(".pdf", ignoreCase = true)
            val attachment = DocumentAttachment(fileName = name, fileUri = uri, isPdf = isPdf)
            updateData(formData.copy(discrepancyFile = attachment))
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("logistics_doc_upload_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarehouseNavy.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = WarehouseNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "$processType Documentation & Checklist",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Document Verification & Discrepancy Reporting",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_close_doc_upload")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Vehicle / Reference No
            Text(
                text = "Vehicle / Reference No:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = formData.vehicleNo,
                onValueChange = { formData = formData.copy(vehicleNo = it.uppercase()) },
                placeholder = { Text("e.g. MP-09-AB-1234", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = WarehouseNavy,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_doc_vehicle_no"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarehouseNavy,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Invoice Copy Upload
            FileUploadField(
                label = "1. Invoice Copy:",
                fileAttachment = formData.invoiceFile,
                onPickFile = { invoiceLauncher.launch("*/*") },
                onPresetSelect = { name, isPdf ->
                    formData = formData.copy(
                        invoiceFile = DocumentAttachment(fileName = name, isPdf = isPdf)
                    )
                },
                presetOptions = listOf("INV-2026-0891.pdf", "Bill_MP09_Invoice.jpg"),
                onClear = { formData = formData.copy(invoiceFile = null) },
                tagPrefix = "invoice"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. LR Copy Upload
            FileUploadField(
                label = "2. LR Copy:",
                fileAttachment = formData.lrFile,
                onPickFile = { lrLauncher.launch("*/*") },
                onPresetSelect = { name, isPdf ->
                    formData = formData.copy(
                        lrFile = DocumentAttachment(fileName = name, isPdf = isPdf)
                    )
                },
                presetOptions = listOf("LR_Receipt_0428.pdf", "LR_Consignment.jpg"),
                onClear = { formData = formData.copy(lrFile = null) },
                tagPrefix = "lr"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Checklist Completed Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .clickable { formData = formData.copy(checklistDone = !formData.checklistDone) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("row_checklist_done")
            ) {
                Checkbox(
                    checked = formData.checklistDone,
                    onCheckedChange = { formData = formData.copy(checklistDone = it) },
                    colors = CheckboxDefaults.colors(checkedColor = WarehouseEmerald),
                    modifier = Modifier.testTag("checkbox_checklist_done")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$processType Checklist Completed & Verified",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Discrepancy Toggle Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (hasDiscrepancy) Color(0xFFFEF2F2) else Color(0xFFF9FAFB)
                ),
                border = BorderStroke(
                    1.dp,
                    if (hasDiscrepancy) WarehouseRed.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_discrepancy_section")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (hasDiscrepancy) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = WarehouseRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Is there any Discrepancy (Damage/Shortage)?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasDiscrepancy) WarehouseRed else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Yes / No Radio Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    hasDiscrepancy = true
                                    formData = formData.copy(hasDiscrepancy = true)
                                }
                                .padding(end = 8.dp)
                                .testTag("radio_discrepancy_yes")
                        ) {
                            RadioButton(
                                selected = hasDiscrepancy,
                                onClick = {
                                    hasDiscrepancy = true
                                    formData = formData.copy(hasDiscrepancy = true)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = WarehouseRed)
                            )
                            Text(
                                text = "Yes",
                                fontSize = 13.sp,
                                fontWeight = if (hasDiscrepancy) FontWeight.Bold else FontWeight.Normal,
                                color = if (hasDiscrepancy) WarehouseRed else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    hasDiscrepancy = false
                                    formData = formData.copy(hasDiscrepancy = false)
                                }
                                .padding(end = 8.dp)
                                .testTag("radio_discrepancy_no")
                        ) {
                            RadioButton(
                                selected = !hasDiscrepancy,
                                onClick = {
                                    hasDiscrepancy = false
                                    formData = formData.copy(hasDiscrepancy = false)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = WarehouseEmerald)
                            )
                            Text(
                                text = "No",
                                fontSize = 13.sp,
                                fontWeight = if (!hasDiscrepancy) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Conditional Discrepancy Fields
                    AnimatedVisibility(
                        visible = hasDiscrepancy,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            HorizontalDivider(
                                color = WarehouseRed.copy(alpha = 0.25f),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            // Discrepancy Type Dropdown
                            Text(
                                text = "Discrepancy Type:",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            ExposedDropdownMenuBox(
                                expanded = discrepancyDropdownExpanded,
                                onExpandedChange = { discrepancyDropdownExpanded = !discrepancyDropdownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = when (formData.discrepancyType) {
                                        "Damage" -> "Damage / Broken"
                                        "Shortage" -> "Shortage / Missing Quantity"
                                        "Excess" -> "Excess Material"
                                        "Wrong Item" -> "Wrong Item / Label Mismatch"
                                        else -> "Select Reason"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = discrepancyDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("select_discrepancy_type"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WarehouseRed,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = discrepancyDropdownExpanded,
                                    onDismissRequest = { discrepancyDropdownExpanded = false }
                                ) {
                                    val discrepancyOptions = listOf(
                                        "Damage" to "Damage / Broken",
                                        "Shortage" to "Shortage / Missing Quantity",
                                        "Excess" to "Excess Material",
                                        "Wrong Item" to "Wrong Item / Label Mismatch"
                                    )

                                    discrepancyOptions.forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, fontSize = 13.sp) },
                                            onClick = {
                                                formData = formData.copy(discrepancyType = key)
                                                discrepancyDropdownExpanded = false
                                            },
                                            modifier = Modifier.testTag("item_disc_$key")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Discrepancy Form / Proof Upload
                            FileUploadField(
                                label = "Discrepancy Form / Proof Upload:",
                                fileAttachment = formData.discrepancyFile,
                                onPickFile = { discrepancyLauncher.launch("*/*") },
                                onPresetSelect = { name, isPdf ->
                                    formData = formData.copy(
                                        discrepancyFile = DocumentAttachment(fileName = name, isPdf = isPdf)
                                    )
                                },
                                presetOptions = listOf("Damage_Proof_Photo.jpg", "Shortage_Form.pdf"),
                                onClear = { formData = formData.copy(discrepancyFile = null) },
                                tagPrefix = "discrepancy"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Remarks Textarea
                            Text(
                                text = "Remarks:",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formData.remarks,
                                onValueChange = { formData = formData.copy(remarks = it) },
                                placeholder = { Text("Enter details of damage or shortage...", fontSize = 12.5.sp) },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("textarea_discrepancy_remarks"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WarehouseRed,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Save / Submit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onDismiss != null) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_cancel_doc_upload"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                }

                Button(
                    onClick = {
                        val finalData = formData.copy(hasDiscrepancy = hasDiscrepancy)
                        onSave(finalData)
                        onSubmit?.invoke(finalData)
                        Toast.makeText(
                            context,
                            "Documentation for ${if (finalData.vehicleNo.isNotBlank()) finalData.vehicleNo else "Vehicle"} Saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasDiscrepancy) WarehouseRed else WarehouseEmerald
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(if (onDismiss != null) 1.5f else 1f)
                        .testTag("btn_save_doc_upload")
                ) {
                    Icon(
                        imageVector = if (hasDiscrepancy) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasDiscrepancy) "Save Discrepancy Report" else "Save Documentation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Reusable File Upload field matching the React HTML file input with quick attachment chips
 */
@Composable
private fun FileUploadField(
    label: String,
    fileAttachment: DocumentAttachment?,
    onPickFile: () -> Unit,
    onPresetSelect: (String, Boolean) -> Unit,
    presetOptions: List<String>,
    onClear: () -> Unit,
    tagPrefix: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (fileAttachment != null) {
            // Attached file chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(WarehouseEmerald.copy(alpha = 0.1f))
                    .border(1.dp, WarehouseEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (fileAttachment.isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                        contentDescription = null,
                        tint = if (fileAttachment.isPdf) WarehouseRed else WarehouseEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = fileAttachment.fileName,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Attached • Ready",
                            fontSize = 10.5.sp,
                            color = WarehouseEmerald
                        )
                    }
                }

                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("btn_clear_${tagPrefix}_file")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove File",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            // File input button with demo quick chips
            Column {
                OutlinedButton(
                    onClick = onPickFile,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_choose_${tagPrefix}_file")
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = WarehouseNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Choose File (Images / PDF)",
                        fontSize = 12.5.sp,
                        color = WarehouseNavy
                    )
                }

                // Sample Presets for quick emulator testing
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sample:",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    presetOptions.forEach { presetName ->
                        val isPdf = presetName.endsWith(".pdf", ignoreCase = true)
                        FilterChip(
                            selected = false,
                            onClick = { onPresetSelect(presetName, isPdf) },
                            label = { Text(presetName, fontSize = 10.sp) },
                            shape = RoundedCornerShape(6.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .height(26.dp)
                                .testTag("preset_${tagPrefix}_$presetName")
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal Dialog container for LogisticsDocUpload
 */
@Composable
fun LogisticsDocUploadDialog(
    processType: String = "Unloading",
    initialVehicleNo: String = "",
    initialFormData: DocFormData = DocFormData(vehicleNo = initialVehicleNo),
    onSave: (DocFormData) -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
        ) {
            LogisticsDocUpload(
                processType = processType,
                initialVehicleNo = initialVehicleNo,
                initialFormData = initialFormData,
                onSave = onSave,
                onDismiss = onDismiss
            )
        }
    }
}
