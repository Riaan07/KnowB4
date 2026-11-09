package com.csiq6823.knowb4

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.csiq6823.knowb4.data.UrlTools
import com.csiq6823.knowb4.ui.ScanListFragment
import com.csiq6823.knowb4.ui.ThreatViewModel
import com.csiq6823.knowb4.ui.ThreatViewModelFactory

class MainActivity : AppCompatActivity() {
    private val viewModel: ThreatViewModel by viewModels {
        ThreatViewModelFactory((application as SecurityApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.scan_fragment_container, ScanListFragment()).commit()
        }
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(incomingIntent: Intent?) {
        if (incomingIntent?.action != Intent.ACTION_SEND || incomingIntent.type != "text/plain") return
        val sharedText = incomingIntent.getStringExtra(Intent.EXTRA_TEXT)?.trim().orEmpty()
        if (sharedText.isBlank()) return
        // Referrer is a platform-provided hint, not a cryptographically verified identity.
        val sender = referrer?.host ?: incomingIntent.getStringExtra(Intent.EXTRA_REFERRER_NAME)
            ?.removePrefix("android-app://") ?: ""
        val sourceApp = when (sender.lowercase()) {
            "com.twitter.android" -> "X"
            "com.reddit.frontpage" -> "Reddit"
            "com.google.android.apps.messaging" -> "SMS"
            else -> "Unknown Source"
        }
        if (UrlTools.isUrlOnly(sharedText)) {
            Toast.makeText(this, "URL without context: verify before opening", Toast.LENGTH_LONG).show()
        }
        viewModel.insertSharedText(sharedText, sourceApp)
        // Avoid re-processing the same share during activity recreation.
        setIntent(Intent(this, MainActivity::class.java))
    }
}


