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
}
