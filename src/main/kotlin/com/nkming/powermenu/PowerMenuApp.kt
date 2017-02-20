package com.nkming.powermenu

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager

class PowerMenuApp : Application()
{
	override fun onCreate()
	{
		super.onCreate()
		initLog()
		initDefaultPref()

		val pref = Preference.from(this)
		migrateVersion(pref)
	}

	private fun initLog()
	{
		Log.isShowDebug = BuildConfig.DEBUG
		Log.isShowVerbose = BuildConfig.DEBUG
	}

	private fun initDefaultPref()
	{
		PreferenceManager.setDefaultValues(this, getString(R.string.pref_file),
				Context.MODE_PRIVATE, R.xml.preference, false)
	}

	private fun migrateVersion(pref: Preference)
	{
		if (pref.lastVersion == BuildConfig.VERSION_CODE)
		{
			// Same version
			return
		}
		else if (pref.lastVersion == -1)
		{
			// New install or migrating from versions not supporting lastVersion
			if (pref.isSoftRebootEnabled)
			{
				// Soft reboot components has now been disabled by default in
				// manifest. We need to explicitly enabled here on upgrade
				enableSoftRebootCompat()
			}
		}
		else if (pref.lastVersion < BuildConfig.VERSION_CODE)
		{
			// Upgrade
			// Currently no migration needed
		}
		else if (pref.lastVersion > BuildConfig.VERSION_CODE)
		{
			// Downgrade o.O
		}
		pref.lastVersion = BuildConfig.VERSION_CODE
		pref.commit()
	}

	private fun enableSoftRebootCompat()
	{
		try
		{
			// Will throw on N-
			SystemHelper.setEnableComponent(this,
					SoftRebootTileService::class.java, true)
		}
		catch (e: Throwable)
		{}
		SystemHelper.setEnableComponent(this,
				SoftRebootActivity::class.java, true)
	}
}
