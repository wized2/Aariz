package com.endroid.aariz.ui.chat

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast

/** Saves a generated image to the gallery (Pictures/Aariz) with the system download manager. */
object ImageSaver {

    fun fileName(now: Long = System.currentTimeMillis()): String = "aariz-$now.jpg"

    fun save(context: Context, url: String) {
        try {
            val name = fileName()
            val req = DownloadManager.Request(Uri.parse(url))
                .setTitle(name)
                .setDescription("Aariz image")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // No storage permission is needed for downloads on Android 10+.
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Aariz/$name")
            } else {
                // Older Android would need a storage permission, so use the app's own folder.
                req.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_PICTURES, name)
            }
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(req)
            val where = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "Pictures/Aariz" else "app Pictures folder"
            Toast.makeText(context, "Saving to $where…", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Couldn't save the image", Toast.LENGTH_SHORT).show()
        }
    }
}
