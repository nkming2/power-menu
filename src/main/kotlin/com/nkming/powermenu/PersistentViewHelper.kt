package com.nkming.powermenu

import android.content.Context

object PersistentViewHelper
{
	@JvmStatic
	fun startIfNecessary(context: Context): Boolean
	{
		val pref = context.getSharedPreferences(context.getString(
				R.string.pref_file), Context.MODE_PRIVATE)
		if (pref.getBoolean(context.getString(R.string.pref_persistent_view_key),
				false) && !com.nkming.utils.widget.PersistentService.isRunning())
		{
			Log.d("$LOG_TAG.startIfNecessary", "Starting service")
			if (!ensurePermission(context))
			{
				return false
			}
			else
			{
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
			PermissionUtils.requestSystemAlertWindow(context)
			return false
		}
	}
}
