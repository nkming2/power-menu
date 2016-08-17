package com.nkming.powermenu

import android.app.Application

class PowerMenuApp : Application()
{
	override fun onCreate()
	{
		super.onCreate()
		Log.isShowDebug = false
		Log.isShowVerbose = false
	}
}
