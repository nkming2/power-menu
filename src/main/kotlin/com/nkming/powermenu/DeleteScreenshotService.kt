package com.nkming.powermenu

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.app.NotificationManagerCompat
import java.io.File

class DeleteScreenshotService : Service()
{
	companion object
	{
		const val EXTRA_FILEPATH = "filepath"

		private val LOG_TAG = DeleteScreenshotService::class.java.canonicalName
	}

	override fun onBind(intent: Intent?): IBinder?
	{
		throw UnsupportedOperationException()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		if (intent != null)
		{
			_delete(intent)
		}

		// Remove notification
		val ns = NotificationManagerCompat.from(this)
		ns.cancel(MainFragment.NOTIFICATION_SCREENSHOT)

		stopSelf()
		return START_NOT_STICKY
	}

	private fun _delete(intent: Intent)
	{
		if (!intent.hasExtra(EXTRA_FILEPATH))
		{
			Log.e("$LOG_TAG._delete", "Missing extra")
			return
		}
		val filepath = intent.getStringExtra(EXTRA_FILEPATH)
		val f = File(filepath)
		if (!f.exists() || !f.isFile)
		{
			Log.e("$LOG_TAG._delete", "Not a file")
			return
		}
		f.delete()

		try
		{
			// Remove the file from media store
			contentResolver.delete(MediaStore.Files.getContentUri("external"),
					"${MediaStore.Files.FileColumns.DATA}=?",
					arrayOf(f.canonicalPath))
		}
		catch (e: Exception)
		{
			Log.e("$LOG_TAG._delete", "Failed while ContentResolver.delete", e)
		}
	}
}
