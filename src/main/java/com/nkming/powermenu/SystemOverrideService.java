/*
 * SystemOverrideService.java
 *
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

public class SystemOverrideService extends Service
{
	public static void startIfNecessary(Context context)
	{
		SharedPreferences pref = context.getSharedPreferences(context.getString(
				R.string.pref_file), Context.MODE_PRIVATE);
		if (pref.getBoolean(context.getString(
				R.string.pref_override_system_menu_key), false) && !mIsRunning)
		{
			Log.d(LOG_TAG + ".startIfNecessary", "Starting service");
			Intent intent = new Intent(context, SystemOverrideService.class);
			context.startService(intent);
		}
	}

	public static void stop(Context context)
	{
		Log.d(LOG_TAG + ".stop", "Stopping service");
		Intent intent = new Intent(context, SystemOverrideService.class);
		context.stopService(intent);
	}

	public SystemOverrideService()
	{
		mHandler = new Handler();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		Log.d(LOG_TAG, "onCreate");
		mIsRunning = true;
		super.onCreate();

		mReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				Log.d(LOG_TAG, "onReceive");
				if (intent.hasExtra("reason"))
				{
					String reason = intent.getStringExtra("reason");
					Log.d(LOG_TAG + "onReceive", reason);
					if (reason.equals(SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS))
					{
						mHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								onLongPressPowerButton();
							}
						});
					}
				}
			}
		};
		registerReceiver(mReceiver,
				new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}

	@Override
	public void onDestroy()
	{
		Log.d(LOG_TAG, "onDestroy");
		mIsRunning = false;
		super.onDestroy();

		unregisterReceiver(mReceiver);
	}

	private static final String LOG_TAG =
			SystemOverrideService.class.getCanonicalName();

	// From platform/frameworks/base/+/lollipop-mr1-release/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java
	private static final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS =
			"globalactions";

	private void onLongPressPowerButton()
	{
		closeSystemMenu();
		startActivity();
	}

	private void closeSystemMenu()
	{
		Intent i = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		sendBroadcast(i);
	}

	private void startActivity()
	{
		Intent i = new Intent(this, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	private BroadcastReceiver mReceiver;
	private Handler mHandler;

	private static boolean mIsRunning = false;
}
