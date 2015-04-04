/*
 * PersistentService.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import eu.chainfire.libsuperuser.Shell;

/**
 * Service to display the persistent view
 */
public class PersistentService extends Service
{
	/**
	 * Start the service
	 *
	 * @param context
	 */
	public static void start(Context context)
	{
		Intent intent = new Intent(context, PersistentService.class);
		context.startService(intent);
	}

	/**
	 * Stop the service
	 *
	 * @param context
	 */
	public static void stop(Context context)
	{
		Intent intent = new Intent(context, PersistentService.class);
		intent.setAction(ACTION_STOP);
		context.startService(intent);
	}

	/**
	 * Return if the service is running or not
	 *
	 * @return
	 */
	public static boolean isRunning()
	{
		return mIsRunning;
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
		initView();
		initForeground();
		initOrientationReceiver();
	}

	@Override
	public void onDestroy()
	{
		Log.d(LOG_TAG, "onDestroy");
		mIsRunning = false;
		super.onDestroy();
		uninitOrientationReceiver();
		uninitForeground();
		uninitView();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent.getAction() != null && intent.getAction().equals(ACTION_STOP))
		{
			mView.hide(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					stopSelf();
				}
			});
		}
		return START_STICKY;
	}

	private static final String LOG_TAG =
			PersistentService.class.getCanonicalName();
	private static final String ACTION_STOP = "stop";

	private void initView()
	{
		if (mView != null)
		{
			uninitView();
		}

		PersistentView.Config conf = new PersistentView.Config();
		conf.handler = new Handler();
		conf.context = this;
		conf.resId = R.layout.persistent_view;
		conf.alpha = 0.5f;
		conf.hiddenW = 0.35f;
		mView = new PersistentView(conf);
		mView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onViewClick();
			}
		});
		mView.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				onViewLongClick();
				return true;
			}
		});
	}

	private void initForeground()
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.notification_title))
				.setContentText(getString(R.string.notification_text))
				.setLocalOnly(true)
				.setOnlyAlertOnce(true)
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.setSmallIcon(R.drawable.ic_action_shutdown)
				.setTicker(getString(R.string.notification_ticker));

		Intent activity = new Intent(this, PreferenceActivity.class);
		activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pi = PendingIntent.getActivity(this, 0, activity,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pi);

		startForeground(1, builder.build());
	}

	private void initOrientationReceiver()
	{
		mConfigurationReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if (getResources().getConfiguration().orientation
						== Configuration.ORIENTATION_LANDSCAPE)
				{
					mView.onOrientationChange(false);
				}
				else
				{
					mView.onOrientationChange(true);
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		registerReceiver(mConfigurationReceiver, filter);
	}

	private void uninitView()
	{
		if (mView != null)
		{
			mView.destroy();
			mView = null;
		}
	}

	private void uninitForeground()
	{
		stopForeground(true);
	}

	private void uninitOrientationReceiver()
	{
		unregisterReceiver(mConfigurationReceiver);
	}

	private void onViewClick()
	{
		Log.d(LOG_TAG, "onViewClick");
		SystemHelper.sleep(getApplicationContext());
	}

	private void onViewLongClick()
	{
		Log.d(LOG_TAG, "onViewLongClick");
		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				Shell.SU.run("am start -n "
						+ MainActivity.class.getPackage().getName()
						+ "/" + MainActivity.class.getCanonicalName());
				return null;
			}
		}.execute();
	}

	private PersistentView mView;
	private BroadcastReceiver mConfigurationReceiver;

	private static boolean mIsRunning = false;
}
