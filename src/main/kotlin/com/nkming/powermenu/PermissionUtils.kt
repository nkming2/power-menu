package com.nkming.powermenu

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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

	@TargetApi(Build.VERSION_CODES.M)
	fun requestSystemAlertWindow(context: Context)
	{
		// We don't want to keep bothering the user -- if permission is
		// explicitly rejected, we won't ask again
		val pref = Preference.from(context)
		if (!shouldRequestSystemAlertWindow(context, pref))
		{
			Log.d("$LOG_TAG.requestSystemAlertWindow",
					"Requested permission already, skipping")
			return
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		{
			Log.w("$LOG_TAG.requestSystemAlertWindow", "Invoking method on M-")
			return
		}
		Log.d("$LOG_TAG.requestSystemAlertWindow",
				"Requesting overlay permission")
		try
		{
			val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
			i.data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
			context.startActivity(i)
			Toast.makeText(context, R.string.overlay_permission_required,
					Toast.LENGTH_LONG).show()
		}
		catch (e: ActivityNotFoundException)
		{
			Log.e("$LOG_TAG.requestSystemAlertWindow",
					"Activity ACTION_MANAGE_OVERLAY_PERMISSION not found", e)
			Toast.makeText(context,
					R.string.overlay_permission_settings_open_failed,
					Toast.LENGTH_LONG).show()
		}

		pref.hasRequestOverlayPermission = true
		pref.apply()
	}

	fun forceRequestSystemAlertWindowNextTime(context: Context)
	{
		val pref = Preference.from(context)
		if (pref.hasRequestOverlayPermission)
		{
			pref.hasRequestOverlayPermission = false
			pref.apply()
		}
	}

	fun hasWriteExternalStorage(context: Context): Boolean
	{
		val p = ContextCompat.checkSelfPermission(context,
				Manifest.permission.WRITE_EXTERNAL_STORAGE)
		return (p == PackageManager.PERMISSION_GRANTED)
	}

	fun requestWriteExternalStorage(activity: Activity, reqCode: Int = 1)
	{
		Toast.makeText(activity, R.string.write_storage_required,
				Toast.LENGTH_LONG).show()
		ActivityCompat.requestPermissions(activity,
				arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), reqCode)
	}

	fun requestWriteExternalStorage(frag: Fragment, reqCode: Int = 1)
	{
		Toast.makeText(frag.context, R.string.write_storage_required,
				Toast.LENGTH_LONG).show()
		frag.requestPermissions(
				arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), reqCode)
	}

	private val LOG_TAG = PermissionUtils::class.java.canonicalName

	private fun shouldRequestSystemAlertWindow(context: Context,
			pref: Preference): Boolean
	{
		return !pref.hasRequestOverlayPermission
	}
}
