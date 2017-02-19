package com.nkming.powermenu

import android.content.Context

object PersistentViewHelper
{
	@JvmStatic
	fun startIfNecessary(context: Context): Boolean
	{
		val pref = Preference.from(context)
		if (pref.isPersistentViewEnabled
				&& !com.nkming.utils.widget.PersistentService.isRunning())
		{
			if (!ensurePermission(context))
			{
				return false
			}
			else
			{
				Log.d("$LOG_TAG.startIfNecessary", "Starting service")
				PersistentService.start(context)
				return true
			}
		}
		return false
	}

	private val LOG_TAG = PersistentViewHelper::class.java.canonicalName

	private fun ensurePermission(context: Context): Boolean
	{
		if (PermissionUtils.hasSystemAlertWindow(context))
		{
			return true
		}
		else
		{
			Log.d("$LOG_TAG.ensurePermission", "Missing overlay permission")
			PermissionUtils.requestSystemAlertWindow(context)
			return false
		}
	}
}
