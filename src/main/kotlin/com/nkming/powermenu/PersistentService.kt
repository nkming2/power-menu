package com.nkming.powermenu

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast

class PersistentService : com.nkming.utils.widget.PersistentService()
{
	companion object
	{
		@JvmStatic
		fun start(context: Context)
		{
			val intent = Intent(context, PersistentService::class.java)
			context.startService(createStart(intent))
			isStarting = true
		}

		@JvmStatic
		fun stop(context: Context)
		{
			if (isRunning() || isStarting)
			{
				val intent = Intent(context, PersistentService::class.java)
				context.startService(createStop(intent))
			}
		}

		@JvmStatic
		fun showView(context: Context)
		{
			if (isRunning() || isStarting)
			{
				val intent = Intent(context, PersistentService::class.java)
				context.startService(createShowView(intent))
			}
		}

		@JvmStatic
		fun hideView(context: Context)
		{
			if (isRunning() || isStarting)
			{
				val intent = Intent(context, PersistentService::class.java)
				context.startService(createHideView(intent))
			}
		}

		@JvmStatic
		fun setAutohideView(context: Context, flag: Boolean)
		{
			if (isRunning() || isStarting)
			{
				val intent = Intent(context, PersistentService::class.java)
				context.startService(createSetAutohideView(intent, flag))
			}
		}

		@JvmStatic
		fun setAlpha(context: Context, alpha: Float)
		{
			if (isRunning() || isStarting)
			{
				val intent = Intent(context, PersistentService::class.java)
				context.startService(createSetAlpha(intent, alpha))
			}
		}

		@JvmStatic
		fun setEnableHaptic(context: Context, flag: Boolean)
		{
			if (isRunning() || isStarting)
			{
				val intent = Intent(context, PersistentService::class.java)
				context.startService(createSetEnableHaptic(intent, flag))
			}
		}

		private var isStarting = false
	}

	override fun onCreate()
	{
		super.onCreate()
		isStarting = false
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		if (!PermissionUtils.hasSystemAlertWindow(this))
		{
			// Permission being revoked
			stopSelf()
			return START_STICKY
		}
		else
		{
			return super.onStartCommand(intent, flags, startId)
		}
	}

	override fun getLayoutId(): Int
	{
		return R.layout.persistent_view
	}

	override fun getForegroundNotification(): Notification?
	{
		val builder = NotificationCompat.Builder(this)
		builder.setContentTitle(getString(R.string.notification_title))
				.setContentText(getString(R.string.notification_text))
				.setLocalOnly(true)
				.setOnlyAlertOnce(true)
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.setSmallIcon(R.drawable.ic_action_shutdown)
				.setColor(ContextCompat.getColor(this, R.color.color_primary))
				.setTicker(getString(R.string.notification_ticker))

		val activity = Intent(this, PreferenceActivity::class.java)
		activity.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
				or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		val pi = PendingIntent.getActivity(this, 0, activity,
				PendingIntent.FLAG_UPDATE_CURRENT)
		builder.setContentIntent(pi)

		return builder.build()
	}

	override fun onInitView()
	{
		super.onInitView()
		val pref = getSharedPreferences(getString(R.string.pref_file),
				Context.MODE_PRIVATE)
		view.setAutohide(pref.getBoolean(getString(
				R.string.pref_autohide_persistent_view_key), false))
		view.setAlpha(pref.getInt(getString(R.string.pref_alpha_key),
				100) / 100.0f)
		view.setEnableHaptic(pref.getBoolean(getString(R.string.pref_haptic_key),
				true))
	}

	override fun onViewClick()
	{
		super.onViewClick()
		val appContext = applicationContext
		SystemHelper.sleep(appContext,
		{
			if (!it)
			{
				Toast.makeText(appContext, R.string.sleep_fail,
						Toast.LENGTH_LONG).show()
			}
		})
	}

	override fun onViewLongClick()
	{
		super.onViewLongClick()
		val appContext = applicationContext
		SystemHelper.startActivity(appContext, MainActivity::class.java,
		{
			if (!it)
			{
				Toast.makeText(appContext, R.string.start_activity_fail,
						Toast.LENGTH_LONG).show();
			}
		})
	}
}
