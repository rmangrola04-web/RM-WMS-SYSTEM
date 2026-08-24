package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.VehicleEntry
import com.example.ui.components.VehicleCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        VehicleCard(
          entry = VehicleEntry(
            id = 1,
            activityType = "Loading",
            vehicleNumber = "MP09 AB 1234",
            vehicleType = "32 Feet SXL / MXL",
            fromLocation = "Pithampur Plant",
            toLocation = "Mumbai Central Warehouse",
            inTime = "2026-08-21 14:00",
            status = "In-Progress",
            dockBay = "Bay 02"
          ),
          onAdvanceStatus = {},
          onEdit = {},
          onDelete = {},
          onAssignDock = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
