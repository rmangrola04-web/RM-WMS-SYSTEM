package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(entities = [VehicleEntry::class, VehicleLog::class, User::class, ActivityLog::class], version = 8, exportSchema = false)
abstract class WarehouseDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun vehicleLogDao(): VehicleLogDao
    abstract fun userDao(): UserDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: WarehouseDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WarehouseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WarehouseDatabase::class.java,
                    "warehouse_movement_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(WarehouseDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class WarehouseDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.vehicleDao(), database.userDao(), database.activityLogDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: VehicleDao, userDao: UserDao, activityDao: ActivityLogDao) {
                // Populate default auth demo user from backend
                val defaultUser = User(
                    userId = "USR-A1B2C3",
                    fullName = "Rahul Sharma",
                    category = "Supervisor",
                    mobileNumber = "9826011223",
                    email = "rahul@example.com",
                    passwordHash = User.hashPassword("MySecurePass@123"),
                    createdAt = User.currentTimestamp(),
                    lastLogin = User.currentTimestamp(),
                    status = "Active"
                )
                userDao.insertUser(defaultUser)

                // Populate initial compliance & activity logs matching dashboard
                val initialActivityLogs = listOf(
                    ActivityLog(taskName = "Inbound Goods Safety & Seal Verification", unitsCount = 45, status = "Compliant", dayLabel = "Mon"),
                    ActivityLog(taskName = "Dock Bay Pallet Quality Inspection", unitsCount = 52, status = "Compliant", dayLabel = "Tue"),
                    ActivityLog(taskName = "Cross-Dock Barcode Scanning Check", unitsCount = 58, status = "Compliant", dayLabel = "Wed"),
                    ActivityLog(taskName = "Hazardous Material Label Review", unitsCount = 40, status = "Pending", dayLabel = "Thu"),
                    ActivityLog(taskName = "High-Value Electronic Goods Tally", unitsCount = 65, status = "Compliant", dayLabel = "Fri"),
                    ActivityLog(taskName = "Outbound Lorry Lashing & Fastening", unitsCount = 50, status = "Compliant", dayLabel = "Sat"),
                    ActivityLog(taskName = "Gate Pass RFID & Driver License Audit", unitsCount = 35, status = "Compliant", dayLabel = "Today"),
                    ActivityLog(taskName = "Cold Chain Temperature Sensor Audit", unitsCount = 15, status = "Non-Compliant", dayLabel = "Today", remarks = "Temp deviation observed 4.2°C"),
                    ActivityLog(taskName = "Weight Bridge Pre-Calibration Check", unitsCount = 10, status = "Pending", dayLabel = "Today", remarks = "Review by QA Engineer pending")
                )
                activityDao.insertAll(initialActivityLogs)
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val now = Calendar.getInstance()

                fun getTimeOffset(minutesAgo: Int): String {
                    val cal = Calendar.getInstance()
                    cal.time = now.time
                    cal.add(Calendar.MINUTE, -minutesAgo)
                    return sdf.format(cal.time)
                }

                val sampleList = listOf(
                    VehicleEntry(
                        activityType = "Unloading",
                        vehicleNumber = "MP09 AB 1234",
                        vehicleType = "32 Feet Single Axle",
                        transporter = "V-Trans India Ltd",
                        placedTime = getTimeOffset(85),
                        fromLocation = "Pithampur Industrial Area",
                        toLocation = "Indore Central Warehouse",
                        nextDestination = "Dewas DC (Empty Gate Out)",
                        inTime = getTimeOffset(120),
                        outTime = "",
                        status = "In-Progress",
                        remarks = "Material in good condition, pallet tally ongoing",
                        dockBay = "Bay 02",
                        driverName = "Ramesh Kumar",
                        driverPhone = "9826011223",
                        grnNumber = "GRN-10293",
                        grnTime = "10:15",
                        lrNumber = "LR-IND-4501",
                        operationStartTime = "09:30",
                        operationEndTime = ""
                    ),
                    VehicleEntry(
                        activityType = "Loading",
                        vehicleNumber = "MH04 CD 5678",
                        vehicleType = "24 Feet Single Axle",
                        transporter = "TCI Express",
                        placedTime = getTimeOffset(40),
                        fromLocation = "Indore Central Warehouse",
                        toLocation = "Bhiwandi Hub Mumbai",
                        inTime = getTimeOffset(60),
                        outTime = "",
                        status = "Dock Assigned / Placed",
                        remarks = "Priority Outbound Dispatch / Lorry Checked",
                        dockBay = "Bay 05",
                        driverName = "Sunil Patil",
                        driverPhone = "9988776655",
                        cartonsCount = 450,
                        sealNumber = "SL-982341",
                        operationStartTime = "11:00",
                        operationEndTime = ""
                    ),
                    VehicleEntry(
                        activityType = "Cross-Docking",
                        vehicleNumber = "DL01 XY 9012",
                        vehicleType = "40 Feet Trailer",
                        transporter = "Safechem Logistics",
                        placedTime = "",
                        fromLocation = "Delhi NCR Logistics Park",
                        toLocation = "Pune Distribution Center",
                        inTime = getTimeOffset(30),
                        outTime = "",
                        status = "Gate In",
                        remarks = "Waiting for Dock Bay 03 allocation",
                        dockBay = "None",
                        driverName = "Jaswant Singh",
                        driverPhone = "9811002233",
                        cartonsCount = 600,
                        sealNumber = "SL-44120"
                    ),
                    VehicleEntry(
                        activityType = "Unloading",
                        vehicleNumber = "GJ06 ZZ 4321",
                        vehicleType = "Tata Ace / Pick-up",
                        transporter = "Blue Dart Express",
                        placedTime = getTimeOffset(140),
                        fromLocation = "Ahmedabad Gateway",
                        toLocation = "Indore Central Warehouse",
                        nextDestination = "Return Branch",
                        inTime = getTimeOffset(160),
                        outTime = "",
                        status = "Completed",
                        remarks = "Goods Verified 100% / Bay 01 Ready for Gate Out",
                        dockBay = "Bay 01",
                        driverName = "Mohan Patel",
                        driverPhone = "9722334455",
                        grnNumber = "GRN-88192",
                        grnTime = "08:45",
                        lrNumber = "LR-AHM-902",
                        operationStartTime = "08:15",
                        operationEndTime = "08:50"
                    ),
                    VehicleEntry(
                        activityType = "Loading",
                        vehicleNumber = "RJ14 MN 7890",
                        vehicleType = "Container",
                        transporter = "Gati KWE",
                        placedTime = getTimeOffset(240),
                        fromLocation = "Indore Central Warehouse",
                        toLocation = "Jaipur Depot",
                        inTime = getTimeOffset(280),
                        outTime = getTimeOffset(15),
                        status = "Gate Out",
                        remarks = "Dispatched / Seal SL-8849 Verified",
                        dockBay = "None",
                        driverName = "Vikram Sharma",
                        driverPhone = "9414009988",
                        cartonsCount = 320,
                        sealNumber = "SL-8849",
                        operationStartTime = "06:30",
                        operationEndTime = "07:45"
                    )
                )
                dao.insertAll(sampleList)
            }
        }
    }
}
