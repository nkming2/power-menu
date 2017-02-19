package com.nkming.powermenu

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

@TargetApi(Build.VERSION_CODES.N)
open class _BaseTileService : TileService()
{
	override fun onStartListening()
	{
		qsTile.state = Tile.STATE_INACTIVE
		qsTile.updateTile()
	}
}

class ShutdownTileService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = ShutdownTileService::class.java.canonicalName
	}

	override fun onClick()
	{
		Log.d(LOG_TAG, "onClick()")
		// We can't show dialog from service
		val i = Intent(this, ShutdownActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class RebootTileService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = RebootTileService::class.java.canonicalName
	}

	override fun onClick()
	{
		Log.d(LOG_TAG, "onClick()")
		// We can't show dialog from service
		val i = Intent(this, RebootActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class RebootRecoveryTileService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = RebootRecoveryTileService::class.java.canonicalName
	}

	override fun onClick()
	{
		Log.d(LOG_TAG, "onClick()")
		// We can't show dialog from service
		val i = Intent(this, RebootRecoveryActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class RebootBootloaderTileService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG =
				RebootBootloaderTileService::class.java.canonicalName
	}

	override fun onClick()
	{
		Log.d(LOG_TAG, "onClick()")
		// We can't show dialog from service
		val i = Intent(this, RebootBootloaderActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class SoftRebootTileService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = SoftRebootTileService::class.java.canonicalName
	}

	override fun onClick()
	{
		Log.d(LOG_TAG, "onClick()")
		// We can't show dialog from service
		val i = Intent(this, SoftRebootActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class SleepTileService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = SleepTileService::class.java.canonicalName
	}

	override fun onClick()
	{
		Log.d(LOG_TAG, "onClick()")
		SleepAction(applicationContext)()
	}
}

class ScreenshotTileService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = ScreenshotTileService::class.java.canonicalName
	}

	override fun onClick()
	{
		Log.d(LOG_TAG, "onClick()")
		val i = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
		sendBroadcast(i)

		// Need to wait until the drawer is closed, which is unknown
		val handler = Handler()
		handler.postDelayed(
		{
			ScreenshotAction(applicationContext)()
		}, 750)
	}
}
