package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.WarehouseViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {}
) {
    var name by remember { mutableStateOf("Rahul Sharma") }
    var selectedCategory by remember { mutableStateOf("Supervisor") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var mobile by remember { mutableStateOf("9826011223") }
    var otpInput by remember { mutableStateOf("1234") }
    
    val otpSent by viewModel.otpSent.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    val primaryNavy = Color(0xFF1E3A8A)
    val primaryHover = Color(0xFF172554)
    val lightBg = Color(0xFFF8FAFC)
    val cardBorder = Color(0xFFCBD5E1)
    val textDark = Color(0xFF1E293B)
    val successEmerald = Color(0xFF059669)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = lightBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Card Container max width 540dp
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .testTag("login_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Warehouse App Brand Icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(primaryNavy, Color(0xFF2563EB))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = "Warehouse Logo",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Warehouse Vehicle Tracker",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryNavy,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "गाड़ी आवागमन ट्रैकिंग सिस्टम - लॉगिन",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "अपनी जानकारी भरकर मोबाइल OTP से लॉगिन करें",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Full Name Field
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name (आपका नाम) *") },
                            placeholder = { Text("e.g. Ramesh Kumar") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = primaryNavy)
                            },
                            singleLine = true,
                            enabled = !otpSent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_name_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryNavy,
                                focusedLabelColor = primaryNavy
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // User Category Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = User.CATEGORIES.find { it.first == selectedCategory }?.second ?: selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("User Category (पद/कैटेगरी) *") },
                                leadingIcon = {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = primaryNavy)
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { if (!otpSent) categoryDropdownExpanded = !categoryDropdownExpanded },
                                        enabled = !otpSent
                                    ) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
                                    }
                                },
                                enabled = !otpSent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_category_selector")
                                    .clickable(enabled = !otpSent) { categoryDropdownExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryNavy,
                                    focusedLabelColor = primaryNavy,
                                    disabledBorderColor = cardBorder,
                                    disabledTextColor = textDark
                                )
                            )

                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                User.CATEGORIES.forEach { (catKey, catLabel) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(catLabel, fontWeight = if (selectedCategory == catKey) FontWeight.Bold else FontWeight.Normal)
                                                if (selectedCategory == catKey) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = primaryNavy, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = catKey
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mobile Number Input
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { 
                                if (it.length <= 10 && it.all { ch -> ch.isDigit() }) {
                                    mobile = it
                                }
                            },
                            label = { Text("Mobile Number (मोबाइल नंबर) *") },
                            placeholder = { Text("10 अंकों का नंबर (e.g. 9826011223)") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = primaryNavy)
                            },
                            prefix = {
                                Text("+91 ", fontWeight = FontWeight.SemiBold, color = primaryNavy)
                            },
                            singleLine = true,
                            enabled = !otpSent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_mobile_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryNavy,
                                focusedLabelColor = primaryNavy
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = if (otpSent) ImeAction.Next else ImeAction.Done
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Send OTP Button (When OTP not yet sent)
                        if (!otpSent) {
                            Button(
                                onClick = {
                                    viewModel.sendOtp(
                                        fullName = name,
                                        category = selectedCategory,
                                        mobileNumber = mobile
                                    ) { success, _ ->
                                        if (success) {
                                            otpInput = "1234"
                                        }
                                    }
                                },
                                enabled = !isAuthLoading && name.isNotBlank() && mobile.length == 10,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("send_otp_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryNavy,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ओटीपी भेजा जा रहा है...")
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send OTP (ओटीपी भेजें)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                }
                            }
                        }

                        // OTP Section (Animated on OTP Sent)
                        AnimatedVisibility(
                            visible = otpSent,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(successEmerald.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = successEmerald,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "OTP Sent to +91 $mobile",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = textDark
                                        )
                                        Text(
                                            text = "सुरक्षा कोड भेजा गया (Test OTP: 1234)",
                                            fontSize = 11.sp,
                                            color = successEmerald,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) otpInput = it },
                                    label = { Text("Enter OTP (ओटीपी दर्ज करें) *") },
                                    placeholder = { Text("1234") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = primaryNavy)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_input_field"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryNavy,
                                        focusedLabelColor = primaryNavy,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.NumberPassword,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (otpInput.isNotBlank()) {
                                                viewModel.verifyOtpAndLogin(
                                                    fullName = name,
                                                    category = selectedCategory,
                                                    mobileNumber = mobile,
                                                    enteredOtp = otpInput,
                                                    onSuccess = { onLoginSuccess() }
                                                )
                                            }
                                        }
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        viewModel.verifyOtpAndLogin(
                                            fullName = name,
                                            category = selectedCategory,
                                            mobileNumber = mobile,
                                            enteredOtp = otpInput,
                                            onSuccess = { onLoginSuccess() }
                                        )
                                    },
                                    enabled = !isAuthLoading && otpInput.length == 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("verify_login_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = successEmerald,
                                        contentColor = Color.White
                                    )
                                ) {
                                    if (isAuthLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("सत्यापित किया जा रहा है...")
                                    } else {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Verify & Login (लॉगिन करें)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { viewModel.resetOtpFlow() }
                                    ) {
                                        Text("नंबर बदलें (Edit Info)", fontSize = 12.sp, color = primaryNavy)
                                    }

                                    TextButton(
                                        onClick = {
                                            viewModel.sendOtp(name, selectedCategory, mobile) { _, _ ->
                                                otpInput = "1234"
                                            }
                                        }
                                    ) {
                                        Text("Resend OTP (पुनः भेजें)", fontSize = 12.sp, color = primaryNavy)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Quick Test User Presets
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "⚡ Quick Demo Roles (एक क्लिक से रोल चुनें):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuickRoleChip(
                                    title = "👮‍♂️ Guard (Ramesh)",
                                    onClick = {
                                        name = "Ramesh Kumar"
                                        selectedCategory = "Security Guard"
                                        mobile = "9826099001"
                                        otpInput = "1234"
                                    }
                                )
                                QuickRoleChip(
                                    title = "👨‍💼 Supervisor (Rahul)",
                                    onClick = {
                                        name = "Rahul Sharma"
                                        selectedCategory = "Supervisor"
                                        mobile = "9826011223"
                                        otpInput = "1234"
                                    }
                                )
                                QuickRoleChip(
                                    title = "⚡ Operator (Amit)",
                                    onClick = {
                                        name = "Amit Verma"
                                        selectedCategory = "Operator"
                                        mobile = "9826044556"
                                        otpInput = "1234"
                                    }
                                )
                                QuickRoleChip(
                                    title = "📊 Manager (Vikram)",
                                    onClick = {
                                        name = "Vikram Singh"
                                        selectedCategory = "Manager"
                                        mobile = "9826077889"
                                        otpInput = "1234"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickRoleChip(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        shadowElevation = 1.dp
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E3A8A),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}
