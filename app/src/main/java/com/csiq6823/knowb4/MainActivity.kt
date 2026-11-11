package com.csiq6823.knowb4

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.csiq6823.knowb4.Data.ThreatRepository
import com.csiq6823.knowb4.ui.ThreatViewModel
import com.csiq6823.knowb4.ui.ThreatViewModel.ThreatViewModelFactory

class MainActivity : AppCompatActivity() {

    private val viewModel: ThreatViewModel by viewModels {
        ThreatViewModelFactory((application as SecurityApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScanListFragment())
                .commit()

            if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
                handleSharedText(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            handleSharedText(intent)
        }
    }

    private fun handleSharedText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return

        // TODO: STUDENT GAP (Sub-Task 3.2)
        // Read window system context identifiers (this.getReferrer()) and write package name parsing rules
        // Transform "com.twitter.android" -> "X", "com.reddit.frontpage" -> "Reddit", "com.google.android.apps.messaging" -> "SMS"
        val extractedSourceApp = resolveSourceApp(this.referrer)

        // TODO: STUDENT GAP (Sub-Task 3.3)
        // Evaluate the text composition profile. If it consists *only* of a URL with zero descriptive text,
        // prompt the user using an explicit window Toast message notification.
        if (isUrlOnly(sharedText)) {
            Toast.makeText(this, R.string.toast_url_only, Toast.LENGTH_LONG).show()
        }

        viewModel.insertSharedText(sharedText, extractedSourceApp)
    }

    private fun resolveSourceApp(referrer: Uri?): String {
        val packageName = referrer?.takeIf { it.scheme == "android-app" }?.host
            ?: referrer?.host
            ?: return UNKNOWN_SOURCE

        mapPackageToSource(packageName)?.let { return it }

        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_clear_history -> {
            AlertDialog.Builder(this)
                .setTitle(R.string.clear_history_title)
                .setMessage(R.string.clear_history_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.clear_history_confirm) { _, _ -> viewModel.clearHistory() }
                .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        const val UNKNOWN_SOURCE = "Unknown Source"

        private val KNOWN_SOURCES = mapOf(
            "com.twitter.android" to "X",
            "com.reddit.frontpage" to "Reddit",
            "com.google.android.apps.messaging" to "SMS",
            "com.samsung.android.messaging" to "SMS",
            "com.android.mms" to "SMS",
            "com.android.messaging" to "SMS"
        )
        fun mapPackageToSource(packageName: String): String? = KNOWN_SOURCES[packageName]

        fun isUrlOnly(text: String): Boolean {
            val url = ThreatRepository.extractFirstUrl(text) ?: return false
            val remainder = text.replaceFirst(url, "")
            return remainder.none { it.isLetterOrDigit() }
        }
    }
}
