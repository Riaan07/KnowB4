package com.csiq6823.knowb4.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.csiq6823.knowb4.R
import com.csiq6823.knowb4.Data.ThreatRepository
import com.csiq6823.knowb4.Data.ThreatScan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanAdapter : ListAdapter<ThreatScan, ScanAdapter.ScanViewHolder>(ScanDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_scan, parent, false)
        return ScanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val scan = getItem(position)
        val context = holder.itemView.context

        holder.messageSnippet.text = scan.sharedMessage
        holder.timestamp.text = context.getString(
            R.string.scan_meta_format,
            DATE_FORMAT.get()!!.format(Date(scan.capturedTimestamp)),
            scan.sourceApp
        )
        holder.verdictBadge.text = scan.verdict

        val url = ThreatRepository.extractFirstUrl(scan.sharedMessage)?.let { ThreatRepository.enforceHttps(it) }

        holder.itemView.setOnClickListener(null)
        holder.itemView.setOnLongClickListener(null)
        holder.itemView.isClickable = false
        holder.itemView.isLongClickable = false

        when {
            scan.verdict == ThreatScan.SAFE -> {
                holder.setBadgeColor(R.color.verdict_safe)
                holder.messageSnippet.setTextColor(ContextCompat.getColor(context, R.color.link_blue))
                holder.setUnderline(url != null)

                if (url != null) {
                    holder.itemView.setOnClickListener { openInBrowser(it.context, url) }
                }
            }

            ThreatScan.isThreat(scan.verdict) -> {
                holder.setBadgeColor(R.color.verdict_threat)
                holder.messageSnippet.setTextColor(ContextCompat.getColor(context, R.color.threat_text))
                holder.setUnderline(false)

                val flaggedTarget = url ?: scan.sharedMessage
                holder.itemView.setOnLongClickListener {
                    shareSecurityAdvisory(it.context, flaggedTarget, scan.verdict)
                    true
                }

                holder.itemView.isClickable = false
            }

            else -> {
                holder.setBadgeColor(R.color.verdict_unknown)
                holder.messageSnippet.setTextColor(ContextCompat.getColor(context, R.color.body_text))
                holder.setUnderline(false)
            }
        }
    }

    private fun openInBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareSecurityAdvisory(context: Context, url: String, verdict: String) {
        val advisory = context.getString(R.string.security_advisory_format, url, verdict)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.security_advisory_subject))
            putExtra(Intent.EXTRA_TEXT, advisory)
        }
        context.startActivity(Intent.createChooser(send, context.getString(R.string.share_advisory_title)))
    }

    class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageSnippet: TextView = itemView.findViewById(R.id.scan_message_snippet)
        val timestamp: TextView = itemView.findViewById(R.id.scan_timestamp)
        val verdictBadge: TextView = itemView.findViewById(R.id.scan_verdict_badge)

        fun setBadgeColor(colorRes: Int) {
            verdictBadge.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(itemView.context, colorRes))
        }

        fun setUnderline(underlined: Boolean) {
            messageSnippet.paintFlags = if (underlined) {
                messageSnippet.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                messageSnippet.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }
    }

    private object ScanDiffCallback : DiffUtil.ItemCallback<ThreatScan>() {
        override fun areItemsTheSame(oldItem: ThreatScan, newItem: ThreatScan): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ThreatScan, newItem: ThreatScan): Boolean =
            oldItem == newItem
    }

    private companion object {
        val DATE_FORMAT = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        }
    }
}
