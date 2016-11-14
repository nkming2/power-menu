package com.nkming.powermenu

import android.app.Application

class PowerMenuApp : Application()
{
	override fun onCreate()
	{
		super.onCreate()
		Log.isShowDebug = BuildConfig.DEBUG
		Log.isShowVerbose = BuildConfig.DEBUG
	}
}
