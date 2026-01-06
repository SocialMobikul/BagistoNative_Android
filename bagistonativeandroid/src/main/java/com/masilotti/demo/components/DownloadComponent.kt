package com.masilotti.demo.components

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.masilotti.demo.utils.PermissionUtils
import dev.hotwire.core.bridge.BridgeComponent
import dev.hotwire.core.bridge.BridgeDelegate
import dev.hotwire.core.bridge.Message
import dev.hotwire.navigation.destinations.HotwireDestination
import dev.hotwire.navigation.fragments.HotwireFragment
import org.json.JSONObject

class DownloadComponent(
    name: String,
    private val bridgeDelegate: BridgeDelegate<HotwireDestination>
) : BridgeComponent<HotwireDestination>(name, bridgeDelegate) {
    private val TAG = "DownloadComponent"
    private val fragment: HotwireFragment
        get() = bridgeDelegate.destination.fragment as HotwireFragment

    private var downloadId: Long = -1
    private var downloadCompleteReceiver: BroadcastReceiver? = null
    private var isReceiverRegistered = false

    override fun onReceive(message: Message) {
        Log.d(TAG, "DownloadComponent message -> ${message}")
        when(message.event) {
            "download" -> handleDownload(message)
        }
    }

    private fun handleDownload(message: Message) {
        val jsonData = JSONObject(message.jsonData)
        val downloadLink = jsonData.getString("downloadLink")
        val fileName = jsonData.optString("fileName", "downloaded_file_${System.currentTimeMillis()}")

        PermissionUtils.checkAndRequestStoragePermission(fragment.requireActivity()) { granted ->
            if (granted) {
                startDownload(downloadLink, fileName)
            } else {
                Toast.makeText(
                    fragment.requireContext(),
                    "Storage permission denied. Cannot download file.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startDownload(downloadUrl: String, fileName: String) {
        try {
            // Clean up any existing receiver first
            unregisterReceiver()

            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("File Download")
                .setDescription("Downloading file")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = fragment.requireContext()
                .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager.enqueue(request)

            downloadCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        Toast.makeText(
                            fragment.requireContext(),
                            "File downloaded to Downloads folder",
                            Toast.LENGTH_LONG
                        ).show()
                        unregisterReceiver()
                    }
                }
            }

            fragment.requireActivity().registerReceiver(
                downloadCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )

            isReceiverRegistered = true

            Toast.makeText(
                fragment.requireContext(),
                "Download started...",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Toast.makeText(
                fragment.requireContext(),
                "Download failed: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            unregisterReceiver()
        }
    }

    private fun unregisterReceiver() {
        try {
            if (isReceiverRegistered && downloadCompleteReceiver != null) {
                fragment.requireActivity().unregisterReceiver(downloadCompleteReceiver)
                isReceiverRegistered = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        downloadCompleteReceiver = null
    }
}