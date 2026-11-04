package com.adioquino.nothingcalendar.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.adioquino.nothingcalendar.R

class SettingsActivity : AppCompatActivity() {
    private lateinit var permissionStatus: TextView
    private lateinit var permissionExplanation: TextView
    private lateinit var permissionButton: Button

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        updatePermissionUi()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        permissionStatus = findViewById(R.id.permission_status)
        permissionExplanation = findViewById(R.id.permission_explanation)
        permissionButton = findViewById(R.id.permission_button)

        permissionButton.setOnClickListener { handlePermissionAction() }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionUi()
    }

    private fun handlePermissionAction() {
        if (hasCalendarPermission()) return

        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)) {
            permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        } else if (getPreferences(MODE_PRIVATE).getBoolean(PERMISSION_REQUESTED, false)) {
            openAppSettings()
        } else {
            getPreferences(MODE_PRIVATE).edit {
                putBoolean(PERMISSION_REQUESTED, true)
            }
            permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }

    private fun updatePermissionUi() {
        val granted = hasCalendarPermission()
        permissionStatus.setText(
            if (granted) R.string.permission_granted else R.string.permission_not_granted,
        )
        permissionExplanation.visibility = if (granted) View.GONE else View.VISIBLE
        permissionButton.visibility = if (granted) View.GONE else View.VISIBLE

        if (!granted) {
            val requested = getPreferences(MODE_PRIVATE).getBoolean(PERMISSION_REQUESTED, false)
            permissionButton.setText(
                if (requested && !shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)) {
                    R.string.open_app_settings
                } else {
                    R.string.grant_calendar_permission
                },
            )
        }
    }

    private fun hasCalendarPermission(): Boolean = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_CALENDAR,
    ) == PackageManager.PERMISSION_GRANTED

    private fun openAppSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            ),
        )
    }

    private companion object {
        const val PERMISSION_REQUESTED = "calendar_permission_requested"
    }
}
