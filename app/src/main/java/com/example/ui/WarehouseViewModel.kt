package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityLog
import com.example.data.User
import com.example.data.VehicleConstants
import com.example.data.VehicleEntry
import com.example.data.WarehouseDatabase
import com.example.data.WarehouseRepository
import com.example.ui.components.DocFormData
import com.example.ui.components.DocumentAttachment
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.map

sealed class FormEvent {
    data class Success(val message: String) : FormEvent()
    data class Error(val message: String) : FormEvent()
}

data class YardStats(
    val total: Int = 0,
    val activeInYard: Int = 0,
    val docked: Int = 0,
    val completedOrOut: Int = 0
)

data class DockOccupancyInfo(
    val bayNumber: String,
    val isOccupied: Boolean,
    val vehicleNumber: String = "",
    val activityType: String = "",
    val status: String = "",
    val entryId: Long = 0L
)

data class DockOccupancySummary(
    val totalDocks: Int = 10,
    val occupiedCount: Int = 0,
    val emptyCount: Int = 10,
    val docks: List<DockOccupancyInfo> = emptyList()
)

class WarehouseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WarehouseRepository

    // Current Authenticated Operator / User
    val currentUser = MutableStateFlow<User?>(null)
    val isAuthLoading = MutableStateFlow(false)

    init {
        val database = WarehouseDatabase.getDatabase(application, viewModelScope)
        repository = WarehouseRepository(database.vehicleDao(), database.userDao(), database.activityLogDao())
        
        // Auto-login default user for seamless experience
        viewModelScope.launch {
            val defaultUser = repository.getUserByEmail("rahul@example.com")
            if (defaultUser != null) {
                currentUser.value = defaultUser
            }
        }
    }

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Activity & Compliance Logs from Room DB
    val allActivityLogs: StateFlow<List<ActivityLog>> = repository.allActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Compliance KPI: Total Tasks Logged (default sample baseline 12 + dynamic added tasks)
    val totalComplianceTasks: StateFlow<Int> = allActivityLogs.map { logs ->
        // Return count of logged tasks
        if (logs.isNotEmpty()) logs.size else 12
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 12)

    // Compliance KPI: Compliance Rate % = (Compliant / Total) * 100
    val complianceRate: StateFlow<Int> = allActivityLogs.map { logs ->
        if (logs.isEmpty()) 85
        else {
            val compliantCount = logs.count { it.status == "Compliant" }
            if (logs.isNotEmpty()) (compliantCount * 100) / logs.size else 85
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 85)

    // Compliance KPI: Pending + Non-Compliant Count
    val pendingOrDefectCount: StateFlow<Int> = allActivityLogs.map { logs ->
        if (logs.isEmpty()) 2
        else logs.count { it.status == "Pending" || it.status == "Non-Compliant" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    // Compliance Distribution (Compliant, Pending, Non-Compliant counts)
    val complianceDistribution: StateFlow<Triple<Int, Int, Int>> = allActivityLogs.map { logs ->
        if (logs.isEmpty()) {
            Triple(17, 2, 1)
        } else {
            val comp = logs.count { it.status == "Compliant" }.coerceAtLeast(1)
            val pend = logs.count { it.status == "Pending" }
            val nonComp = logs.count { it.status == "Non-Compliant" }
            Triple(comp, pend, nonComp)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(17, 2, 1))

    // 7-Day Activity Output Trend Data Points
    val dailyActivityTrend: StateFlow<List<Pair<String, Int>>> = allActivityLogs.map { logs ->
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Today")
        val baseValues = mutableMapOf(
            "Mon" to 45,
            "Tue" to 52,
            "Wed" to 58,
            "Thu" to 40,
            "Fri" to 65,
            "Sat" to 50,
            "Today" to 60
        )
        // Add dynamic units to corresponding days
        logs.forEach { log ->
            val d = log.dayLabel.ifBlank { "Today" }
            if (baseValues.containsKey(d)) {
                // If it's dynamically added today, accumulate
                if (d == "Today" && log.id > 9) {
                    baseValues[d] = (baseValues[d] ?: 60) + log.unitsCount
                }
            }
        }
        days.map { day -> Pair(day, baseValues[day] ?: 0) }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf(
            Pair("Mon", 45),
            Pair("Tue", 52),
            Pair("Wed", 58),
            Pair("Thu", 40),
            Pair("Fri", 65),
            Pair("Sat", 50),
            Pair("Today", 60)
        )
    )

    // Add new activity / compliance log
    fun addActivityLog(taskName: String, unitsCount: Int, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val log = ActivityLog(
                taskName = taskName.trim(),
                unitsCount = unitsCount,
                status = status,
                dayLabel = "Today",
                timestamp = System.currentTimeMillis()
            )
            repository.insertActivityLog(log)
            _eventFlow.emit(FormEvent.Success("Activity log added: $taskName ($unitsCount Units - $status)"))
            onComplete()
        }
    }

    // Save Godown Activity matching ICH Hub - Google Apps Script Portal schema
    fun saveGodownActivity(
        hubLocation: String = "ICH Central Hub",
        auditorName: String,
        category: String,
        subActivity: String,
        duckLocation: String,
        status: String,
        readingValue: String,
        remarks: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                val qtyInt = readingValue.filter { it.isDigit() }.toIntOrNull() ?: 1
                val log = ActivityLog(
                    taskName = if (subActivity.isNotBlank()) "$category: $subActivity" else category,
                    unitsCount = qtyInt,
                    status = if (status.contains("Compliant", ignoreCase = true) || status.contains("OK", ignoreCase = true)) "Compliant"
                             else if (status.contains("Pending", ignoreCase = true)) "Pending"
                             else "Non-Compliant",
                    dayLabel = "Today",
                    timestamp = System.currentTimeMillis(),
                    remarks = remarks.trim(),
                    auditorName = auditorName.trim(),
                    category = category.trim(),
                    subActivity = subActivity.trim(),
                    locationId = hubLocation.trim(),
                    refNumber = readingValue.trim(),
                    quantity = readingValue.trim(),
                    hubLocation = hubLocation.trim(),
                    duckLocation = duckLocation.trim()
                )
                repository.insertActivityLog(log)
                val successMsg = "डाटा सफलतापूर्वक ICH रिकॉर्ड में दर्ज हो गया!"
                _eventFlow.emit(FormEvent.Success(successMsg))
                onResult(true, successMsg)
            } catch (e: Exception) {
                val errMsg = "त्रुटि: ${e.localizedMessage}"
                _eventFlow.emit(FormEvent.Error(errMsg))
                onResult(false, errMsg)
            }
        }
    }

    fun deleteActivityLog(log: ActivityLog) {
        viewModelScope.launch {
            repository.deleteActivityLog(log)
            _eventFlow.emit(FormEvent.Success("Activity log deleted"))
        }
    }

    // 2. Register New User
    fun registerUser(fullName: String, email: String, password: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            isAuthLoading.value = true
            try {
                val (success, result) = repository.registerUser(fullName, email, password)
                if (success) {
                    val created = repository.getUserById(result)
                    if (created != null) {
                        currentUser.value = created
                    }
                    _eventFlow.emit(FormEvent.Success("User created successfully! User ID: $result"))
                    onResult(true, result)
                } else {
                    _eventFlow.emit(FormEvent.Error(result))
                    onResult(false, result)
                }
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Registration error"
                _eventFlow.emit(FormEvent.Error("Error: $err"))
                onResult(false, err)
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    // 3. User Login Verification (Email & Password)
    fun loginUser(email: String, password: String, onResult: (Boolean, User?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            isAuthLoading.value = true
            try {
                val (success, user) = repository.loginUser(email, password)
                if (success && user != null) {
                    currentUser.value = user
                    _eventFlow.emit(FormEvent.Success("Login successful! Welcome ${user.fullName} (${user.category})"))
                    onResult(true, user)
                } else {
                    _eventFlow.emit(FormEvent.Error("Invalid email or password!"))
                    onResult(false, null)
                }
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Login error"
                _eventFlow.emit(FormEvent.Error("Login error: $err"))
                onResult(false, null)
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    // 4. Warehouse Mobile OTP Login Flow
    val otpSent = MutableStateFlow(false)
    val generatedOtp = MutableStateFlow("1234")

    fun sendOtp(fullName: String, category: String, mobileNumber: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val trimmedName = fullName.trim()
        val trimmedCategory = category.trim()
        val trimmedMobile = mobileNumber.trim()

        if (trimmedName.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(FormEvent.Error("कृपया अपना नाम दर्ज करें। (Enter full name)")) }
            onResult(false, "Name is required")
            return
        }
        if (trimmedCategory.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(FormEvent.Error("कृपया अपनी कैटेगरी चुनें। (Select category)")) }
            onResult(false, "Category is required")
            return
        }
        if (trimmedMobile.length != 10 || !trimmedMobile.all { it.isDigit() }) {
            viewModelScope.launch { _eventFlow.emit(FormEvent.Error("कृपया सही 10 अंकों का मोबाइल नंबर दर्ज करें। (Enter valid 10-digit mobile)")) }
            onResult(false, "Valid 10-digit mobile number required")
            return
        }

        generatedOtp.value = "1234"
        otpSent.value = true
        viewModelScope.launch {
            _eventFlow.emit(FormEvent.Success("OTP आपके मोबाइल नंबर ($trimmedMobile) पर भेज दिया गया है। (टेस्ट के लिए OTP: 1234)"))
        }
        onResult(true, "1234")
    }

    fun verifyOtpAndLogin(
        fullName: String,
        category: String,
        mobileNumber: String,
        enteredOtp: String,
        onSuccess: (User) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val otp = enteredOtp.trim()
        if (otp != "1234" && otp != generatedOtp.value) {
            val err = "गलत OTP! कृपया सही OTP (1234) दर्ज करें।"
            viewModelScope.launch { _eventFlow.emit(FormEvent.Error(err)) }
            onError(err)
            return
        }

        viewModelScope.launch {
            isAuthLoading.value = true
            try {
                val user = repository.loginOrRegisterOtpUser(fullName, category, mobileNumber)
                currentUser.value = user
                otpSent.value = false
                _eventFlow.emit(FormEvent.Success("लॉगिन सफल! स्वागत है ${user.fullName} (${user.category})"))
                onSuccess(user)
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Login failed"
                _eventFlow.emit(FormEvent.Error("Login Error: $err"))
                onError(err)
            } finally {
                isAuthLoading.value = false
            }
        }
    }

    fun resetOtpFlow() {
        otpSent.value = false
    }

    fun loginOrRegisterOtp(fullName: String, category: String, mobileNumber: String) {
        viewModelScope.launch {
            try {
                val user = repository.loginOrRegisterOtpUser(fullName, category, mobileNumber)
                currentUser.value = user
                _eventFlow.emit(FormEvent.Success("Switched operator to ${user.fullName} (${user.category})"))
            } catch (e: Exception) {
                _eventFlow.emit(FormEvent.Error("Error: ${e.localizedMessage}"))
            }
        }
    }

    fun logout() {
        val name = currentUser.value?.fullName ?: "User"
        currentUser.value = null
        otpSent.value = false
        viewModelScope.launch {
            _eventFlow.emit(FormEvent.Success("$name logged out successfully"))
        }
    }

    val allEntries: StateFlow<List<VehicleEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow("All")
    val selectedActivityFilter = MutableStateFlow("All")
    val selectedDockFilter = MutableStateFlow("All") // "All", "Occupied", "Empty", or specific "Bay 01"..."Bay 10"

    val dockOccupancy: StateFlow<DockOccupancySummary> = allEntries.map { entries ->
        val activeDocked = entries
            .filter { it.status != "Gate Out" && it.dockBay.isNotBlank() && it.dockBay != "None" }
            .associateBy { it.dockBay }

        val allBays = VehicleConstants.DOCK_BAYS.filter { it != "None" }
        val dockList = allBays.map { bay ->
            val v = activeDocked[bay]
            DockOccupancyInfo(
                bayNumber = bay,
                isOccupied = v != null,
                vehicleNumber = v?.vehicleNumber ?: "",
                activityType = v?.activityType ?: "",
                status = v?.status ?: "",
                entryId = v?.id ?: 0L
            )
        }
        val occupied = dockList.count { it.isOccupied }
        DockOccupancySummary(
            totalDocks = allBays.size,
            occupiedCount = occupied,
            emptyCount = allBays.size - occupied,
            docks = dockList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DockOccupancySummary())

    val filteredEntries: StateFlow<List<VehicleEntry>> = combine(
        allEntries,
        searchQuery,
        selectedStatusFilter,
        selectedActivityFilter,
        selectedDockFilter
    ) { entries, query, statusFilter, actFilter, dockFilter ->
        entries.filter { entry ->
            val matchesQuery = query.isBlank() ||
                entry.vehicleNumber.contains(query, ignoreCase = true) ||
                entry.fromLocation.contains(query, ignoreCase = true) ||
                entry.toLocation.contains(query, ignoreCase = true) ||
                entry.remarks.contains(query, ignoreCase = true) ||
                entry.dockBay.contains(query, ignoreCase = true) ||
                entry.driverName.contains(query, ignoreCase = true)

            val matchesStatus = statusFilter == "All" || entry.status.equals(statusFilter, ignoreCase = true)
            val matchesActivity = actFilter == "All" || entry.activityType.equals(actFilter, ignoreCase = true)

            val matchesDock = when (dockFilter) {
                "All" -> true
                "Occupied" -> entry.status != "Gate Out" && entry.dockBay.isNotBlank() && entry.dockBay != "None"
                "Empty", "Unassigned", "No Dock" -> entry.status != "Gate Out" && (entry.dockBay.isBlank() || entry.dockBay == "None")
                else -> entry.dockBay.equals(dockFilter, ignoreCase = true)
            }

            matchesQuery && matchesStatus && matchesActivity && matchesDock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDockFilter(dock: String) {
        selectedDockFilter.value = dock
    }

    fun resetFilters() {
        searchQuery.value = ""
        selectedStatusFilter.value = "All"
        selectedActivityFilter.value = "All"
        selectedDockFilter.value = "All"
    }

    // Yard KPIs
    val stats: StateFlow<YardStats> = allEntries.combine(MutableStateFlow(Unit)) { entries, _ ->
        val total = entries.size
        val active = entries.count { it.status != "Gate Out" }
        val docked = entries.count { it.status == "Dock Assigned / Placed" || it.status == "In-Progress" }
        val completed = entries.count { it.status == "Completed" || it.status == "Gate Out" }
        YardStats(total, active, docked, completed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), YardStats())

    // UI Events (Snackbar / Alerts)
    private val _eventFlow = MutableSharedFlow<FormEvent>()
    val eventFlow: SharedFlow<FormEvent> = _eventFlow.asSharedFlow()

    // Form State
    var editingEntryId: Long? = null
        private set

    val formActivityType = MutableStateFlow("Loading")
    val formVehicleNumber = MutableStateFlow("")
    val formVehicleType = MutableStateFlow("14 Feet")
    val formTransporter = MutableStateFlow("")
    val formPlacedTime = MutableStateFlow("")
    val formFromLocation = MutableStateFlow("")
    val formToLocation = MutableStateFlow("")
    val formNextDestination = MutableStateFlow("")
    val formInTime = MutableStateFlow(VehicleConstants.currentFormattedDateTime())
    val formOutTime = MutableStateFlow("")
    val formStatus = MutableStateFlow("Gate In")
    val formRemarks = MutableStateFlow("")
    val formDockBay = MutableStateFlow("None")
    val formDriverName = MutableStateFlow("")
    val formDriverPhone = MutableStateFlow("")
    val formCartonsCount = MutableStateFlow("")
    val formSealNumber = MutableStateFlow("")
    val formOpStartTime = MutableStateFlow("")
    val formOpEndTime = MutableStateFlow("")
    val formGrnNumber = MutableStateFlow("")
    val formGrnTime = MutableStateFlow("")
    val formLrNumber = MutableStateFlow("")
    
    val formInvoiceFile = MutableStateFlow<DocumentAttachment?>(null)
    val formLrFile = MutableStateFlow<DocumentAttachment?>(null)
    val formChecklistDone = MutableStateFlow(false)
    val formHasDiscrepancy = MutableStateFlow(false)
    val formDiscrepancyType = MutableStateFlow("")
    val formDiscrepancyFile = MutableStateFlow<DocumentAttachment?>(null)
    val formDiscrepancyRemarks = MutableStateFlow("")

    val isSubmitting = MutableStateFlow(false)

    fun setActivityType(type: String) {
        formActivityType.value = type
        if (type == "Unloading") {
            if (formToLocation.value.isBlank() || formToLocation.value !in VehicleConstants.HUB_LOCATIONS) {
                formToLocation.value = VehicleConstants.HUB_LOCATIONS.firstOrNull() ?: "ICH Indore"
            }
            if (formFromLocation.value in VehicleConstants.HUB_LOCATIONS) {
                formFromLocation.value = ""
            }
        } else if (type == "Loading") {
            if (formFromLocation.value.isBlank() || formFromLocation.value !in VehicleConstants.HUB_LOCATIONS) {
                formFromLocation.value = VehicleConstants.HUB_LOCATIONS.firstOrNull() ?: "ICH Indore"
            }
            if (formToLocation.value in VehicleConstants.HUB_LOCATIONS) {
                formToLocation.value = ""
            }
        }
    }

    fun startNewEntry() {
        editingEntryId = null
        formActivityType.value = "Loading"
        formVehicleNumber.value = ""
        formVehicleType.value = "14 Feet"
        formTransporter.value = ""
        formPlacedTime.value = ""
        formFromLocation.value = "ICH Indore"
        formToLocation.value = ""
        formNextDestination.value = ""
        formInTime.value = VehicleConstants.currentFormattedDateTime()
        formOutTime.value = ""
        formStatus.value = "Gate In"
        formRemarks.value = ""
        formDockBay.value = "None"
        formDriverName.value = ""
        formDriverPhone.value = ""
        formCartonsCount.value = ""
        formSealNumber.value = ""
        formOpStartTime.value = ""
        formOpEndTime.value = ""
        formGrnNumber.value = ""
        formGrnTime.value = ""
        formLrNumber.value = ""
        formInvoiceFile.value = null
        formLrFile.value = null
        formChecklistDone.value = false
        formHasDiscrepancy.value = false
        formDiscrepancyType.value = ""
        formDiscrepancyFile.value = null
        formDiscrepancyRemarks.value = ""
    }

    fun populateForEdit(entry: VehicleEntry) {
        editingEntryId = entry.id
        formActivityType.value = entry.activityType
        formVehicleNumber.value = entry.vehicleNumber
        formVehicleType.value = entry.vehicleType
        formTransporter.value = entry.transporter
        formPlacedTime.value = entry.placedTime
        formFromLocation.value = entry.fromLocation
        formToLocation.value = entry.toLocation
        formNextDestination.value = entry.nextDestination
        formInTime.value = entry.inTime
        formOutTime.value = entry.outTime
        formStatus.value = entry.status
        formRemarks.value = entry.remarks
        formDockBay.value = if (entry.dockBay.isBlank()) "None" else entry.dockBay
        formDriverName.value = entry.driverName
        formDriverPhone.value = entry.driverPhone
        formCartonsCount.value = if (entry.cartonsCount > 0) entry.cartonsCount.toString() else ""
        formSealNumber.value = entry.sealNumber
        formOpStartTime.value = entry.operationStartTime
        formOpEndTime.value = entry.operationEndTime
        formGrnNumber.value = entry.grnNumber
        formGrnTime.value = entry.grnTime
        formLrNumber.value = entry.lrNumber
        formInvoiceFile.value = if (entry.invoiceFile.isNotBlank()) DocumentAttachment(fileName = entry.invoiceFile, isPdf = entry.invoiceFile.endsWith(".pdf", ignoreCase = true)) else null
        formLrFile.value = if (entry.lrFile.isNotBlank()) DocumentAttachment(fileName = entry.lrFile, isPdf = entry.lrFile.endsWith(".pdf", ignoreCase = true)) else null
        formChecklistDone.value = entry.checklistDone
        formHasDiscrepancy.value = entry.hasDiscrepancy
        formDiscrepancyType.value = entry.discrepancyType
        formDiscrepancyFile.value = if (entry.discrepancyFile.isNotBlank()) DocumentAttachment(fileName = entry.discrepancyFile, isPdf = entry.discrepancyFile.endsWith(".pdf", ignoreCase = true)) else null
        formDiscrepancyRemarks.value = entry.discrepancyRemarks
    }

    fun submitForm(onComplete: () -> Unit = {}) {
        val number = formVehicleNumber.value.trim().uppercase()
        val from = formFromLocation.value.trim()
        val to = formToLocation.value.trim()
        val activity = formActivityType.value.trim()
        val vehicleType = formVehicleType.value.trim()
        val transporter = formTransporter.value.trim()

        if (number.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(FormEvent.Error("Vehicle Number is required! (गाड़ी नंबर आवश्यक है)"))
            }
            return
        }
        if (activity == "Loading" && to.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(FormEvent.Error("Destination Location is required for Loading!"))
            }
            return
        }
        if (activity == "Unloading" && from.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(FormEvent.Error("Source Location is required for Unloading!"))
            }
            return
        }

        val parsedCartons = formCartonsCount.value.trim().toIntOrNull() ?: 0

        viewModelScope.launch {
            isSubmitting.value = true
            try {
                val entry = VehicleEntry(
                    id = editingEntryId ?: 0L,
                    activityType = activity,
                    vehicleNumber = number,
                    vehicleType = vehicleType,
                    transporter = transporter,
                    placedTime = formPlacedTime.value.trim(),
                    fromLocation = if (from.isNotBlank()) from else "Indore Warehouse",
                    toLocation = if (to.isNotBlank()) to else "Indore Warehouse",
                    nextDestination = formNextDestination.value.trim(),
                    inTime = if (formInTime.value.isBlank()) VehicleConstants.currentFormattedDateTime() else formInTime.value.trim(),
                    outTime = formOutTime.value.trim(),
                    status = formStatus.value,
                    remarks = formRemarks.value.trim(),
                    dockBay = if (formDockBay.value == "None") "" else formDockBay.value,
                    driverName = formDriverName.value.trim(),
                    driverPhone = formDriverPhone.value.trim(),
                    cartonsCount = parsedCartons,
                    sealNumber = formSealNumber.value.trim(),
                    operationStartTime = formOpStartTime.value.trim(),
                    operationEndTime = formOpEndTime.value.trim(),
                    grnNumber = formGrnNumber.value.trim(),
                    grnTime = formGrnTime.value.trim(),
                    lrNumber = formLrNumber.value.trim(),
                    invoiceFile = formInvoiceFile.value?.fileName ?: "",
                    lrFile = formLrFile.value?.fileName ?: "",
                    checklistDone = formChecklistDone.value,
                    hasDiscrepancy = formHasDiscrepancy.value,
                    discrepancyType = if (formHasDiscrepancy.value) formDiscrepancyType.value.trim() else "",
                    discrepancyFile = if (formHasDiscrepancy.value) (formDiscrepancyFile.value?.fileName ?: "") else "",
                    discrepancyRemarks = if (formHasDiscrepancy.value) formDiscrepancyRemarks.value.trim() else "",
                    operatorName = currentUser.value?.fullName ?: "Operator",
                    operatorCategory = currentUser.value?.category ?: "Operator",
                    operatorMobile = currentUser.value?.mobileNumber ?: ""
                )

                if (editingEntryId == null) {
                    repository.insertEntry(entry)
                    _eventFlow.emit(FormEvent.Success("Vehicle entry saved successfully for $number"))
                } else {
                    repository.updateEntry(entry)
                    _eventFlow.emit(FormEvent.Success("Vehicle entry updated successfully for $number"))
                }
                startNewEntry()
                onComplete()
            } catch (e: Exception) {
                _eventFlow.emit(FormEvent.Error("Error: ${e.localizedMessage}"))
            } finally {
                isSubmitting.value = false
            }
        }
    }

    fun advanceStatus(entry: VehicleEntry) {
        viewModelScope.launch {
            val now = VehicleConstants.currentFormattedDateTime()
            val nextStatus = when (entry.status) {
                "Gate In" -> "Dock Assigned / Placed"
                "Dock Assigned / Placed" -> "In-Progress"
                "In-Progress" -> "Completed"
                "Completed" -> "Gate Out"
                else -> null
            }

            if (nextStatus != null) {
                repository.updateStatus(entry.id, nextStatus, now)
                _eventFlow.emit(FormEvent.Success("${entry.vehicleNumber} status updated to $nextStatus"))
            }
        }
    }

    fun updateStatusDirectly(id: Long, newStatus: String) {
        viewModelScope.launch {
            val now = VehicleConstants.currentFormattedDateTime()
            repository.updateStatus(id, newStatus, now)
            _eventFlow.emit(FormEvent.Success("Status updated to $newStatus"))
        }
    }

    fun assignBay(id: Long, bay: String) {
        viewModelScope.launch {
            repository.assignDockBay(id, bay)
            _eventFlow.emit(FormEvent.Success("Dock assigned: $bay"))
        }
    }

    fun deleteEntry(entry: VehicleEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            _eventFlow.emit(FormEvent.Success("Entry for ${entry.vehicleNumber} deleted"))
        }
    }

    fun updateDocumentation(entry: VehicleEntry, docData: DocFormData) {
        viewModelScope.launch {
            try {
                val updated = entry.copy(
                    invoiceFile = docData.invoiceFile?.fileName ?: entry.invoiceFile,
                    lrFile = docData.lrFile?.fileName ?: entry.lrFile,
                    checklistDone = docData.checklistDone,
                    hasDiscrepancy = docData.hasDiscrepancy,
                    discrepancyType = if (docData.hasDiscrepancy) docData.discrepancyType else "",
                    discrepancyFile = if (docData.hasDiscrepancy) (docData.discrepancyFile?.fileName ?: "") else "",
                    discrepancyRemarks = if (docData.hasDiscrepancy) docData.remarks else "",
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateEntry(updated)
                val msg = if (docData.hasDiscrepancy) {
                    "Discrepancy logged for ${entry.vehicleNumber} (${docData.discrepancyType})"
                } else {
                    "Documents saved for ${entry.vehicleNumber}"
                }
                _eventFlow.emit(FormEvent.Success(msg))
            } catch (e: Exception) {
                _eventFlow.emit(FormEvent.Error("Document update error: ${e.localizedMessage}"))
            }
        }
    }

    fun saveOrSyncDocumentation(docData: DocFormData) {
        viewModelScope.launch {
            try {
                val targetPlate = docData.vehicleNo.trim().uppercase()
                val existing = allEntries.value.firstOrNull { it.vehicleNumber.equals(targetPlate, ignoreCase = true) }
                if (existing != null) {
                    updateDocumentation(existing, docData)
                } else {
                    _eventFlow.emit(FormEvent.Success("Documents and checklist saved for $targetPlate"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(FormEvent.Error("Error: ${e.localizedMessage}"))
            }
        }
    }

    fun generateGatePassText(entry: VehicleEntry): String {
        return buildString {
            appendLine("═════════════════════════════════")
            appendLine("  WAREHOUSE VEHICLE GATE PASS   ")
            appendLine("═════════════════════════════════")
            appendLine("Vehicle No : ${entry.vehicleNumber}")
            appendLine("Activity   : ${entry.activityType}")
            appendLine("Type       : ${entry.vehicleType}")
            if (entry.transporter.isNotBlank()) appendLine("Transporter: ${entry.transporter}")
            appendLine("Route      : ${entry.fromLocation} ➜ ${entry.toLocation}")
            if (entry.nextDestination.isNotBlank()) appendLine("Next Dest  : ${entry.nextDestination}")
            appendLine("Gate In    : ${entry.inTime}")
            if (entry.placedTime.isNotBlank()) appendLine("Dock Placed: ${entry.placedTime} (${entry.dockBay.ifBlank { "Unassigned" }})")
            if (entry.cartonsCount > 0) appendLine("Cartons    : ${entry.cartonsCount}")
            if (entry.sealNumber.isNotBlank()) appendLine("Seal No    : ${entry.sealNumber}")
            if (entry.grnNumber.isNotBlank()) appendLine("GRN No     : ${entry.grnNumber} (Time: ${entry.grnTime})")
            if (entry.lrNumber.isNotBlank()) appendLine("LR / Bilty : ${entry.lrNumber}")
            if (entry.outTime.isNotBlank()) appendLine("Gate Out   : ${entry.outTime}")
            appendLine("Status     : ${entry.status}")
            if (entry.remarks.isNotBlank()) appendLine("Remarks    : ${entry.remarks}")
            if (entry.driverName.isNotBlank()) appendLine("Driver     : ${entry.driverName} (${entry.driverPhone})")
            appendLine("═════════════════════════════════")
            appendLine("Verified by Warehouse Gate Security")
        }
    }

    fun generateFullReportExport(): String {
        val entries = allEntries.value
        return buildString {
            appendLine("WAREHOUSE VEHICLE MOVEMENT DAILY REPORT")
            appendLine("Generated on: ${VehicleConstants.currentFormattedDateTime()}")
            appendLine("Total Entries: ${entries.size}")
            appendLine("══════════════════════════════════════════════════════")
            appendLine("Vehicle No | Transporter | Activity | Status | Dock | Cartons/GRN | Gate In | Gate Out | Origin -> Destination")
            appendLine("──────────────────────────────────────────────────────")
            entries.forEach { e ->
                val opDetail = if (e.activityType == "Loading") "Cartons: ${e.cartonsCount}" else "GRN: ${e.grnNumber.ifBlank { "-" }}"
                appendLine("${e.vehicleNumber} | ${e.transporter.ifBlank { "-" }} | ${e.activityType} | ${e.status} | ${e.dockBay.ifBlank { "-" }} | $opDetail | ${e.inTime} | ${e.outTime.ifBlank { "In Yard" }} | ${e.fromLocation} -> ${e.toLocation}")
            }
        }
    }

    // 1. Generate User Activity CSV Report
    fun generateUserActivityCsvReport(): String {
        val users = allUsers.value
        return buildString {
            // Headers: User ID, Full Name, Email Address, Registered Date, Last Login, Account Status
            appendLine("User ID,Full Name,Email Address,Registered Date,Last Login,Account Status")
            users.forEach { u ->
                val id = escapeCsv(u.userId)
                val name = escapeCsv(u.fullName)
                val email = escapeCsv(u.email)
                val regDate = escapeCsv(u.createdAt)
                val lastLogin = escapeCsv(u.lastLogin ?: "N/A")
                val status = escapeCsv(u.status)
                appendLine("$id,$name,$email,$regDate,$lastLogin,$status")
            }
        }
    }

    // Comprehensive Combined Warehouse & User Activity Report
    fun generateCombinedWarehouseCsvReport(): String {
        val users = allUsers.value
        val entries = allEntries.value
        return buildString {
            appendLine("=== WAREHOUSE SYSTEM REPORT ===")
            appendLine("Generated At,${VehicleConstants.currentFormattedDateTime()}")
            appendLine()
            appendLine("--- USER ACTIVITY & OPERATORS ---")
            appendLine("User ID,Full Name,Email Address,Registered Date,Last Login,Account Status")
            users.forEach { u ->
                appendLine("${escapeCsv(u.userId)},${escapeCsv(u.fullName)},${escapeCsv(u.email)},${escapeCsv(u.createdAt)},${escapeCsv(u.lastLogin ?: "N/A")},${escapeCsv(u.status)}")
            }
            appendLine()
            appendLine("--- VEHICLE MOVEMENTS ---")
            appendLine("Vehicle No,Transporter,Type,Activity,Status,Dock Bay,Cartons,Seal No,GRN No,LR No,Gate In,Dock Placed,Gate Out,From,To,Next Dest,Driver,Phone,Remarks")
            entries.forEach { e ->
                appendLine("${escapeCsv(e.vehicleNumber)},${escapeCsv(e.transporter)},${escapeCsv(e.vehicleType)},${escapeCsv(e.activityType)},${escapeCsv(e.status)},${escapeCsv(e.dockBay)},${e.cartonsCount},${escapeCsv(e.sealNumber)},${escapeCsv(e.grnNumber)},${escapeCsv(e.lrNumber)},${escapeCsv(e.inTime)},${escapeCsv(e.placedTime)},${escapeCsv(e.outTime)},${escapeCsv(e.fromLocation)},${escapeCsv(e.toLocation)},${escapeCsv(e.nextDestination)},${escapeCsv(e.driverName)},${escapeCsv(e.driverPhone)},${escapeCsv(e.remarks)}")
            }
        }
    }

    // Google Sheets 'Godown_Logs' matching export (ICH Hub - 9 columns)
    fun generateGodownLogsCsvReport(): String {
        val logs = allActivityLogs.value
        return buildString {
            appendLine("Timestamp,Location / Hub,Auditor Name,Category,Checklist Point,Duck / Area / Room,Status,Reading / Ref No.,Remarks / Action")
            logs.forEach { log ->
                val ts = escapeCsv(log.formattedTimestamp)
                val hub = escapeCsv(log.hubLocation.ifBlank { "ICH Central Hub" })
                val auditor = escapeCsv(log.auditorName.ifBlank { currentUser.value?.fullName ?: "Warehouse Auditor" })
                val cat = escapeCsv(log.category.ifBlank { "General Activity" })
                val subAct = escapeCsv(log.subActivity.ifBlank { log.taskName })
                val duck = escapeCsv(log.duckLocation.ifBlank { log.locationId.ifBlank { "Main Floor" } })
                val stat = escapeCsv(log.status)
                val refNo = escapeCsv(log.refNumber.ifBlank { log.quantity.ifBlank { "-" } })
                val rem = escapeCsv(log.remarks.ifBlank { "OK" })
                appendLine("$ts,$hub,$auditor,$cat,$subAct,$duck,$stat,$refNo,$rem")
            }
        }
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            "\"$clean\""
        } else {
            clean
        }
    }

    // 1. VEHICLE MOVEMENT LOGS CSV EXPORT
    fun generateVehicleMovementCsvReport(): String {
        val entries = allEntries.value
        return buildString {
            // CSV Header row
            appendLine("Entry ID,Vehicle Number,Transporter,Vehicle Type,Activity Type,Status,Dock Bay,Cartons,Seal Number,GRN Number,GRN Time,LR Number,Op Start Time,Op End Time,Gate In Time,Dock Placed Time,Gate Out Time,From Location,To Location,Next Destination,Driver Name,Driver Phone,Remarks,Created At")
            entries.forEach { e ->
                val id = escapeCsv(e.id.toString())
                val vehicleNo = escapeCsv(e.vehicleNumber)
                val transporter = escapeCsv(e.transporter)
                val vehicleType = escapeCsv(e.vehicleType)
                val activityType = escapeCsv(e.activityType)
                val status = escapeCsv(e.status)
                val dockBay = escapeCsv(e.dockBay.ifBlank { "None" })
                val cartons = escapeCsv(if (e.cartonsCount > 0) e.cartonsCount.toString() else "")
                val sealNo = escapeCsv(e.sealNumber)
                val grnNo = escapeCsv(e.grnNumber)
                val grnTime = escapeCsv(e.grnTime)
                val lrNo = escapeCsv(e.lrNumber)
                val opStart = escapeCsv(e.operationStartTime)
                val opEnd = escapeCsv(e.operationEndTime)
                val inTime = escapeCsv(e.inTime)
                val placedTime = escapeCsv(e.placedTime.ifBlank { "N/A" })
                val outTime = escapeCsv(e.outTime.ifBlank { "N/A" })
                val from = escapeCsv(e.fromLocation)
                val to = escapeCsv(e.toLocation)
                val nextDest = escapeCsv(e.nextDestination)
                val driver = escapeCsv(e.driverName.ifBlank { "N/A" })
                val phone = escapeCsv(e.driverPhone.ifBlank { "N/A" })
                val remarks = escapeCsv(e.remarks.ifBlank { "N/A" })
                val createdAt = escapeCsv(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(e.createdAt)))

                appendLine("$id,$vehicleNo,$transporter,$vehicleType,$activityType,$status,$dockBay,$cartons,$sealNo,$grnNo,$grnTime,$lrNo,$opStart,$opEnd,$inTime,$placedTime,$outTime,$from,$to,$nextDest,$driver,$phone,$remarks,$createdAt")
            }
        }
    }

    // Save CSV to device storage (Downloads or App Documents directory)
    fun saveVehicleMovementCsvToDevice(context: Context): Pair<Boolean, String> {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "Vehicle_Movement_Report_$timestamp.csv"
            val content = generateVehicleMovementCsvReport()

            // Try public downloads or fallback to app-specific external files dir
            val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.filesDir, "downloads").apply { if (!exists()) mkdirs() }

            val targetFile = File(targetDir, filename)
            FileWriter(targetFile).use { writer ->
                writer.write(content)
            }

            // Also mirror in cache reports dir for immediate FileProvider access
            exportReportToFile(context, filename, content)

            Pair(true, targetFile.absolutePath)
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Failed to save report to device")
        }
    }

    // Share Vehicle Movement CSV via Email or Native Share Chooser
    fun prepareShareVehicleMovementReportIntent(
        context: Context,
        receiverEmail: String = ""
    ): Intent {
        val dateStr = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "Vehicle_Movement_Report_$timestamp.csv"
        val content = generateVehicleMovementCsvReport()
        val file = exportReportToFile(context, filename, content)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val subject = "🚚 Warehouse Vehicle Movement Logs Report - $dateStr"
        val body = """
Hello,

Please find attached the detailed CSV report of warehouse vehicle movements and gate logs.

📊 Summary:
• Total Vehicles Logged: ${allEntries.value.size}
• Active in Yard: ${stats.value.activeInYard}
• Docked at Bay: ${stats.value.docked}
• Dispatched / Completed: ${stats.value.completedOrOut}
• Report Generated: ${VehicleConstants.currentFormattedDateTime()}

Regards,
Automated Warehouse Vehicle Management System
        """.trimIndent()

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            if (receiverEmail.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(receiverEmail.trim()))
            }
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun exportReportToFile(context: Context, filename: String, content: String): File {
        val reportsDir = File(context.cacheDir, "reports").apply {
            if (!exists()) mkdirs()
        }
        val file = File(reportsDir, filename)
        FileWriter(file).use { writer ->
            writer.write(content)
        }
        return file
    }

    // 2. Email report dispatch helper
    fun prepareEmailReportIntent(
        context: Context,
        receiverEmail: String,
        isUserReportOnly: Boolean = false,
        customSubject: String? = null,
        customBody: String? = null
    ): Intent {
        val dateStr = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
        val defaultSubject = if (isUserReportOnly) {
            "📊 User Activity & Login Report - $dateStr"
        } else {
            "📊 Daily User & System Activity Report - $dateStr"
        }

        val defaultBody = """
Hello Admin,

Please find attached the latest system user activity and login report.
You can review the attached CSV file for detailed timestamps and operations.

Summary:
• Total Operators: ${allUsers.value.size}
• Total Vehicle Entries: ${allEntries.value.size}
• Active Yard Vehicles: ${stats.value.activeInYard}
• Docked Vehicles: ${stats.value.docked}

Regards,
Automated Warehouse System Bot
        """.trimIndent()

        val subject = customSubject?.takeIf { it.isNotBlank() } ?: defaultSubject
        val body = customBody?.takeIf { it.isNotBlank() } ?: defaultBody

        val filename = if (isUserReportOnly) "User_Activity_Report.csv" else "Warehouse_System_Activity_Report.csv"
        val content = if (isUserReportOnly) generateUserActivityCsvReport() else generateCombinedWarehouseCsvReport()
        val file = exportReportToFile(context, filename, content)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            if (receiverEmail.isNotBlank()) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(receiverEmail.trim()))
            }
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
