package com.nkming.powermenu

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

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
				ContextCompat.startForegroundService(context, intent)
			}
		}

		@JvmStatic
		fun stop(context: Context)
		{
			Log.d("$LOG_TAG.stop", "Stopping service")
			val intent = Intent(context, SystemOverrideService::class.java)
			context.stopService(intent)
		}

		@JvmStatic
		@TargetApi(Build.VERSION_CODES.O)
		fun initNotifChannel(context: Context, nm: NotificationManager)
		{
			val ch = NotificationChannel(CHANNEL_ID,
					context.getString(R.string.system_override_notif_channel_name),
					NotificationManager.IMPORTANCE_MIN)
			ch.description = context.getString(
					R.string.system_override_notif_channel_description)
			ch.lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
			ch.setShowBadge(false)
			nm.createNotificationChannel(ch)
		}

		private val LOG_TAG = SystemOverrideService::class.java.canonicalName

		// From platform/frameworks/base/+/lollipop-mr1-release/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java
		// L+: platform/frameworks/base/+/master/services/core/java/com/android/server/policy/PhoneWindowManager.java
		private const val SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions"

		private var _isRunning = false
		private const val CHANNEL_ID = "system_override_service"
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

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		if (!_hasInit)
		{
			_hasInit = true
			// Background service is no longer allowed in O+
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				initForegroundService()
			}
		}
		return START_STICKY
	}

	override fun onDestroy()
	{
		super.onDestroy()
		Log.d("$LOG_TAG", "onDestroy()")
		_isRunning = false

		unregisterReceiver(_receiver)
	}

	private fun initForegroundService()
	{
		val builder = NotificationCompat.Builder(this, CHANNEL_ID)
		builder.setContentTitle(getString(R.string.system_override_notif_title))
				.setContentText(getString(R.string.system_override_notif_text))
				.setLocalOnly(true)
				.setOnlyAlertOnce(true)
				.setSmallIcon(R.drawable.ic_action_shutdown)
				.setColor(ContextCompat.getColor(this, R.color.primary_light))
				.setTicker(getString(R.string.notification_ticker))

		val activity = Intent(this, PreferenceActivity::class.java)
		activity.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		val pi = PendingIntent.getActivity(this, 0, activity,
				PendingIntent.FLAG_UPDATE_CURRENT)
		builder.setContentIntent(pi)

		val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
		intent.putExtra(Settings.EXTRA_APP_PACKAGE,
				BuildConfig.APPLICATION_ID)
		intent.putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
		val pendingIntent = PendingIntent.getActivity(this,
				Res.PENDING_INTENT_REQ_HIDE_OVERRIDE_NOTIF, intent,
				PendingIntent.FLAG_UPDATE_CURRENT)
		builder.addAction(R.drawable.outline_visibility_off_white_24,
				getString(R.string.notification_action_hide),
				pendingIntent)

		startForeground(Res.NOTIF_OVERRIDE_SERVICE, builder.build())
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
	private var _hasInit = false
}
