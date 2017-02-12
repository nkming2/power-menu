package com.nkming.powermenu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object PermissionUtils
{
	fun hasSystemAlertWindow(context: Context): Boolean
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		{
			// Always granted on M-
			return true
		}
		else
		{
			return Settings.canDrawOverlays(context)
		}
	}

	fun requestSystemAlertWindow(context: Context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		{
			Log.w("$LOG_TAG.requestSystemAlertWindow", "Invoking method on M-")
			return
		}
		Toast.makeText(context, R.string.overlay_permission_required,
				Toast.LENGTH_LONG).show()
		Log.d("$LOG_TAG.requestSystemAlertWindow",
				"Requesting overlay permission")
		val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
		i.data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
		context.startActivity(i)
	}

	private val LOG_TAG = PermissionUtils::class.java.canonicalName
}
