package com.nkming.powermenu

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder

class SystemOverrideService : Service()
{
	companion object
	{
		@JvmStatic
		fun startIfNecessary(context: Context)
		{
			if (Preference.from(context).isOverrideSystemMenu && !_isRunning)
			{
				Log.d("$LOG_TAG.startIfNecessary", "Starting service")
				val intent = Intent(context, SystemOverrideService::class.java)
				context.startService(intent)
			}
		}

		@JvmStatic
		fun stop(context: Context)
		{
			Log.d("$LOG_TAG.stop", "Stopping service")
			val intent = Intent(context, SystemOverrideService::class.java)
			context.stopService(intent)
		}

		private val LOG_TAG = SystemOverrideService::class.java.canonicalName

		// From platform/frameworks/base/+/lollipop-mr1-release/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java
		private const val SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions"

		private var _isRunning = false
	}

	override fun onBind(intent: Intent?): IBinder?
	{
		throw UnsupportedOperationException()
	}

	override fun onCreate()
	{
		super.onCreate()
		Log.d("$LOG_TAG", "onCreate()")
		_isRunning = true

		registerReceiver(_receiver,
				IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
	}

	override fun onDestroy()
	{
		super.onDestroy()
		Log.d("$LOG_TAG", "onDestroy()")
		_isRunning = false

		unregisterReceiver(_receiver)
	}

	private fun _onLongPressPowerButton()
	{
		_closeSystemMenu.run()
		// Close few more times to make sure the dialog disappear
		_handler.postDelayed(_closeSystemMenu, 1000)
		_handler.postDelayed(_closeSystemMenu, 2000)
		_startActivity()
	}

	private fun _startActivity()
	{
		val intent = Intent(this, MainActivity::class.java)
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TOP)
		startActivity(intent)
	}

	private val _closeSystemMenu = Runnable(
	{
		val intent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
		sendBroadcast(intent)
	})

	private val _receiver = object: BroadcastReceiver()
	{
		override fun onReceive(context: Context, intent: Intent?)
		{
			Log.d("$LOG_TAG", "onReceive()")
			if (intent != null && intent.hasExtra("reason"))
			{
				val reason = intent.getStringExtra("reason")
				Log.d("$LOG_TAG.onReceive", reason)
				if (reason == SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS)
				{
					_onLongPressPowerButton()
				}
			}
		}
	}

	private val _handler = Handler()
}
