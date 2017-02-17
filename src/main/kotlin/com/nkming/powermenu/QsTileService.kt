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

class ShutdownService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = ShutdownService::class.java.canonicalName
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

class RebootService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = RebootService::class.java.canonicalName
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

class RebootRecoveryService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = RebootRecoveryService::class.java.canonicalName
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

class RebootBootloaderService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = RebootBootloaderService::class.java.canonicalName
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

class SoftRebootService : _BaseTileService()
{
	companion object
	{
		private val LOG_TAG = SoftRebootService::class.java.canonicalName
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
