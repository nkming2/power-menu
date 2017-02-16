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
	override fun onClick()
	{
		// We can't show dialog from service
		val i = Intent(this, ShutdownActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class RebootService : _BaseTileService()
{
	override fun onClick()
	{
		// We can't show dialog from service
		val i = Intent(this, RebootActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class RebootRecoveryService : _BaseTileService()
{
	override fun onClick()
	{
		// We can't show dialog from service
		val i = Intent(this, RebootRecoveryActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class RebootBootloaderService : _BaseTileService()
{
	override fun onClick()
	{
		// We can't show dialog from service
		val i = Intent(this, RebootBootloaderActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class SoftRebootService : _BaseTileService()
{
	override fun onClick()
	{
		// We can't show dialog from service
		val i = Intent(this, SoftRebootActivity::class.java)
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		startActivity(i)
	}
}

class SleepTileService : _BaseTileService()
{
	override fun onClick()
	{
		SleepAction(applicationContext)()
	}
}

class ScreenshotTileService : _BaseTileService()
{
	override fun onClick()
	{
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
