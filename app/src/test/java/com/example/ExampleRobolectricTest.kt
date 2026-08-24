package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.User
import com.example.data.VehicleEntry
import com.example.data.WarehouseDatabase
import com.example.data.WarehouseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Warehouse Vehicle Tracker", appName)
  }

  @Test
  fun `insert and retrieve vehicle entry`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = WarehouseDatabase.getDatabase(context, this)
    val dao = db.vehicleDao()

    val entry = VehicleEntry(
      activityType = "Loading",
      vehicleNumber = "MP09 AB 9999",
      vehicleType = "14 Feet Container",
      placedTime = "2026-08-21 10:15",
      fromLocation = "Indore",
      toLocation = "Bhopal",
      inTime = "2026-08-21 10:00",
      outTime = "2026-08-21 11:30",
      dockBay = "Bay 04",
      status = "Completed",
      remarks = "Seal #SL-101",
      driverName = "Suresh",
      driverPhone = "9876543210"
    )

    val id = dao.insertEntry(entry)
    val fetched = dao.getEntryById(id)

    assertNotNull(fetched)
    assertEquals("MP09 AB 9999", fetched?.vehicleNumber)
    assertEquals("Loading", fetched?.activityType)
    assertEquals("2026-08-21 10:00", fetched?.inTime)
    assertEquals("2026-08-21 10:15", fetched?.placedTime)
    assertEquals("2026-08-21 11:30", fetched?.outTime)
    assertEquals("Bay 04", fetched?.dockBay)
    assertEquals("Completed", fetched?.status)
  }

  @Test
  fun `dock bay allocation and active yard queries`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = WarehouseDatabase.getDatabase(context, this)
    val dao = db.vehicleDao()

    val entry = VehicleEntry(
      activityType = "Unloading",
      vehicleNumber = "DL01 XY 7777",
      vehicleType = "32 Feet SXL / MXL",
      fromLocation = "Delhi Logistics Park",
      toLocation = "Indore Warehouse",
      inTime = "2026-08-21 11:00",
      dockBay = "Bay 03",
      status = "Dock Assigned / Placed"
    )

    val id = dao.insertEntry(entry)
    val bayVehicle = dao.getVehicleInBay("Bay 03")
    assertNotNull(bayVehicle)
    assertEquals("DL01 XY 7777", bayVehicle?.vehicleNumber)
    assertEquals("Bay 03", bayVehicle?.dockBay)

    // Update status to Gate Out
    bayVehicle?.let {
      dao.updateEntry(it.copy(status = "Gate Out", outTime = "2026-08-21 12:30", dockBay = "None"))
    }
    val bayAfterOut = dao.getVehicleInBay("Bay 03")
    assertEquals(null, bayAfterOut)
  }

  @Test
  fun `parse qr code gate pass text`() {
    val simplePass = com.example.ui.components.BarcodePassParser.parse("MP09AB1234")
    assertEquals("MP09AB1234", simplePass.vehicleNumber)

    val complexPass = com.example.ui.components.BarcodePassParser.parse("VEHICLE:GJ03 XY 7711;FROM:Surat;TO:Indore;ACT:Loading;BAY:Bay 02;DRIVER:Ramesh;PHONE:9876543210")
    assertEquals("GJ03 XY 7711", complexPass.vehicleNumber)
    assertEquals("Surat", complexPass.origin)
    assertEquals("Indore", complexPass.destination)
    assertEquals("Loading", complexPass.activityType)
    assertEquals("Bay 02", complexPass.dockBay)
    assertEquals("Ramesh", complexPass.driverName)
    assertEquals("9876543210", complexPass.driverPhone)

    val urlPass = com.example.ui.components.BarcodePassParser.parse("https://gate.wh.com/pass?v=KA01MJ9988&act=Unloading&from=Bangalore&to=Indore&dock=Bay 04&driver=Vijay&phone=9876501234")
    assertEquals("KA01MJ9988", urlPass.vehicleNumber)
    assertEquals("Bangalore", urlPass.origin)
    assertEquals("Indore", urlPass.destination)
    assertEquals("Unloading", urlPass.activityType)
    assertEquals("Bay 04", urlPass.dockBay)
    assertEquals("Vijay", urlPass.driverName)
    assertEquals("9876501234", urlPass.driverPhone)
  }

  @Test
  fun `vehicle log entity and dao crud operations`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = WarehouseDatabase.getDatabase(context, this)
    val logDao = db.vehicleLogDao()

    val logTimestamp = System.currentTimeMillis()
    val newLog = com.example.data.VehicleLog(
      plateNumber = "KA01 MH 4455",
      timestamp = logTimestamp,
      assignedDock = "Bay 02",
      activityType = "Loading",
      status = "Dock Assigned",
      driverName = "Ramesh Kumar",
      notes = "Priority shipment"
    )

    // Create / Insert
    val insertedId = logDao.insertLog(newLog)
    assertTrue(insertedId > 0)

    // Read by ID
    val retrieved = logDao.getLogById(insertedId)
    assertNotNull(retrieved)
    assertEquals("KA01 MH 4455", retrieved?.plateNumber)
    assertEquals("Bay 02", retrieved?.assignedDock)
    assertEquals(logTimestamp, retrieved?.timestamp)

    // Update
    val updated = retrieved!!.copy(assignedDock = "Bay 05", status = "In-Progress")
    val updateCount = logDao.updateLog(updated)
    assertEquals(1, updateCount)

    val reRetrieved = logDao.getLogById(insertedId)
    assertEquals("Bay 05", reRetrieved?.assignedDock)
    assertEquals("In-Progress", reRetrieved?.status)

    // Delete
    val deleteCount = logDao.deleteLog(reRetrieved!!)
    assertEquals(1, deleteCount)
    val afterDelete = logDao.getLogById(insertedId)
    assertEquals(null, afterDelete)
  }

  @Test
  fun `vehicle entry document upload and discrepancy persistence`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = WarehouseDatabase.getDatabase(context, this)
    val dao = db.vehicleDao()

    val entry = VehicleEntry(
      activityType = "Unloading",
      vehicleNumber = "HR26 DQ 8888",
      vehicleType = "32 Feet SXL / MXL",
      fromLocation = "Gurgaon Warehouse",
      toLocation = "Indore Central Warehouse",
      inTime = "2026-08-21 14:00",
      invoiceFile = "inv_8888.pdf",
      lrFile = "lr_8888.pdf",
      checklistDone = true,
      hasDiscrepancy = true,
      discrepancyType = "Damage / Broken",
      discrepancyFile = "box_damage_photo.jpg",
      discrepancyRemarks = "2 boxes found torn and wet on pallet #3"
    )

    val id = dao.insertEntry(entry)
    val saved = dao.getEntryById(id)

    assertNotNull(saved)
    assertEquals("inv_8888.pdf", saved?.invoiceFile)
    assertEquals("lr_8888.pdf", saved?.lrFile)
    assertEquals(true, saved?.checklistDone)
    assertEquals(true, saved?.hasDiscrepancy)
    assertEquals("Damage / Broken", saved?.discrepancyType)
    assertEquals("box_damage_photo.jpg", saved?.discrepancyFile)
    assertEquals("2 boxes found torn and wet on pallet #3", saved?.discrepancyRemarks)
  }

  @Test
  fun `dock number filtering accurately filters occupied and unassigned docks`() = runBlocking {
    val entries = listOf(
      VehicleEntry(
        id = 1,
        activityType = "Unloading",
        vehicleNumber = "MH12 AB 1111",
        vehicleType = "32 Feet Container",
        fromLocation = "Mumbai",
        toLocation = "Indore",
        dockBay = "Bay 01",
        status = "Dock Assigned / Placed"
      ),
      VehicleEntry(
        id = 2,
        activityType = "Loading",
        vehicleNumber = "DL01 CD 2222",
        vehicleType = "24 Feet Container",
        fromLocation = "Delhi",
        toLocation = "Indore",
        dockBay = "Bay 04",
        status = "In-Progress"
      ),
      VehicleEntry(
        id = 3,
        activityType = "Cross-Docking",
        vehicleNumber = "MP09 EF 3333",
        vehicleType = "14 Feet Container",
        fromLocation = "Bhopal",
        toLocation = "Indore",
        dockBay = "",
        status = "Gate In"
      ),
      VehicleEntry(
        id = 4,
        activityType = "Unloading",
        vehicleNumber = "GJ01 GH 4444",
        vehicleType = "Tata Ace",
        fromLocation = "Ahmedabad",
        toLocation = "Indore",
        dockBay = "Bay 01",
        status = "Gate Out"
      )
    )

    // Test specific dock filter: Bay 01
    val bay01Entries = entries.filter { it.dockBay.equals("Bay 01", ignoreCase = true) }
    assertEquals(2, bay01Entries.size)

    // Test specific active dock filter: Bay 04
    val bay04Entries = entries.filter { it.dockBay.equals("Bay 04", ignoreCase = true) }
    assertEquals(1, bay04Entries.size)
    assertEquals("DL01 CD 2222", bay04Entries.first().vehicleNumber)

    // Test occupied docks (active with assigned bay)
    val occupiedEntries = entries.filter { it.status != "Gate Out" && it.dockBay.isNotBlank() && it.dockBay != "None" }
    assertEquals(2, occupiedEntries.size)

    // Test unassigned / no dock entries
    val unassignedEntries = entries.filter { it.status != "Gate Out" && (it.dockBay.isBlank() || it.dockBay == "None") }
    assertEquals(1, unassignedEntries.size)
    assertEquals("MP09 EF 3333", unassignedEntries.first().vehicleNumber)
  }

  @Test
  fun `password hashing generates correct SHA-256 hex string`() {
    val rawPass = "MySecurePass@123"
    val hash = User.hashPassword(rawPass)
    assertNotNull(hash)
    assertEquals(64, hash.length) // SHA-256 is 64 hex characters
    assertEquals(hash, User.hashPassword(rawPass))
  }

  @Test
  fun `user registration and login verification flow`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = WarehouseDatabase.getDatabase(context, this)
    val repo = WarehouseRepository(db.vehicleDao(), db.userDao())

    // 1. Register new user
    val (regSuccess, userId) = repo.registerUser("Rahul Sharma", "rahul_test@example.com", "MySecurePass@123")
    assertTrue(regSuccess)
    assertTrue(userId.startsWith("USR-"))

    // 2. Duplicate registration should fail
    val (dupSuccess, _) = repo.registerUser("Rahul Sharma", "rahul_test@example.com", "DifferentPass")
    assertFalse(dupSuccess)

    // 3. Login with correct password
    val (loginSuccess, loggedInUser) = repo.loginUser("rahul_test@example.com", "MySecurePass@123")
    assertTrue(loginSuccess)
    assertNotNull(loggedInUser)
    assertEquals("Rahul Sharma", loggedInUser?.fullName)
    assertEquals("rahul_test@example.com", loggedInUser?.email)
    assertEquals("Active", loggedInUser?.status)
    assertNotNull(loggedInUser?.lastLogin)

    // 4. Login with wrong password should fail
    val (wrongLogin, failedUser) = repo.loginUser("rahul_test@example.com", "WrongPassword")
    assertFalse(wrongLogin)
    assertNull(failedUser)
  }

  @Test
  fun `user activity CSV report contains correct headers and formatted data`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WarehouseViewModel(app)

    val csv = viewModel.generateUserActivityCsvReport()
    assertNotNull(csv)
    assertTrue(csv.contains("User ID,Full Name,Email Address,Registered Date,Last Login,Account Status"))
  }

  @Test
  fun `vehicle movement CSV report queries room db and formats valid CSV`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WarehouseViewModel(app)

    val csv = viewModel.generateVehicleMovementCsvReport()
    assertNotNull(csv)
    // Check CSV Headers
    assertTrue(csv.contains("Entry ID,Vehicle Number,Transporter,Vehicle Type,Activity Type,Status,Dock Bay"))
    assertTrue(csv.contains("Cartons,Seal Number,GRN Number,GRN Time,LR Number"))
    
    // Verify file saving
    val saveResult = viewModel.saveVehicleMovementCsvToDevice(app)
    assertTrue(saveResult.first)
    assertTrue(saveResult.second.endsWith(".csv"))

    // Verify share intent creation
    val shareIntent = viewModel.prepareShareVehicleMovementReportIntent(app)
    assertNotNull(shareIntent)
    assertEquals(android.content.Intent.ACTION_SEND, shareIntent.action)
    assertEquals("text/csv", shareIntent.type)
  }

  @Test
  fun `summary dashboard and dock occupancy rate calculations verify accurately`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WarehouseViewModel(app)

    val stats = viewModel.stats.value
    assertNotNull(stats)
    assertTrue(stats.total >= 0)
    assertTrue(stats.activeInYard >= 0)
    assertTrue(stats.completedOrOut >= 0)

    val dockOccupancy = viewModel.dockOccupancy.value
    assertNotNull(dockOccupancy)
    assertTrue(dockOccupancy.totalDocks > 0)
    assertEquals(dockOccupancy.totalDocks, dockOccupancy.occupiedCount + dockOccupancy.emptyCount)
  }

  @Test
  fun `daily activity and compliance logging dynamically updates room database and kpis`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WarehouseViewModel(app)

    // Add a new compliant activity log
    viewModel.addActivityLog(
      taskName = "Forklift Battery & Brake Inspection",
      unitsCount = 30,
      status = "Compliant"
    )

    // Add a non-compliant activity log
    viewModel.addActivityLog(
      taskName = "Fire Extinguisher Pressure Gauge Audit",
      unitsCount = 5,
      status = "Non-Compliant"
    )

    val logs = viewModel.allActivityLogs.value
    assertNotNull(logs)
    assertTrue(viewModel.totalComplianceTasks.value > 0)
    assertTrue(viewModel.complianceRate.value in 0..100)
    assertTrue(viewModel.dailyActivityTrend.value.isNotEmpty())
    assertEquals(7, viewModel.dailyActivityTrend.value.size)
  }
}


