/*
 * PersistentService.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Service to display the persistent view
 */
public class PersistentService extends com.nkming.utils.widget.PersistentService
{
	public static void start(Context context)
	{
		Intent intent = new Intent(context, PersistentService.class);
		context.startService(createStart(intent));
	}

	public static void stop(Context context)
	{
		Intent intent = new Intent(context, PersistentService.class);
		context.startService(createStop(intent));
	}

	public static void showView(Context context)
	{
		Intent intent = new Intent(context, PersistentService.class);
		context.startService(createShowView(intent));
	}

	public static void hideView(Context context)
	{
		Intent intent = new Intent(context, PersistentService.class);
		context.startService(createHideView(intent));
	}

	public static void setAutohideView(Context context, boolean flag)
	{
		Intent intent = new Intent(context, PersistentService.class);
		context.startService(createSetAutohideView(intent, flag));
	}

	public static void setAlpha(Context context, float alpha)
	{
		Intent intent = new Intent(context, PersistentService.class);
		context.startService(createSetAlpha(intent, alpha));
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.persistent_view;
	}

	@Override
	protected Notification getForegroundNotification()
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.notification_title))
				.setContentText(getString(R.string.notification_text))
				.setLocalOnly(true)
				.setOnlyAlertOnce(true)
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.setSmallIcon(R.drawable.ic_action_shutdown)
				.setColor(getResources().getColor(R.color.color_primary))
				.setTicker(getString(R.string.notification_ticker));

		Intent activity = new Intent(this, PreferenceActivity.class);
		activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pi = PendingIntent.getActivity(this, 0, activity,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pi);

		return builder.build();
	}

	@Override
	protected void onInitView()
	{
		super.onInitView();
		SharedPreferences pref = getSharedPreferences(getString(
				R.string.pref_file), Context.MODE_PRIVATE);
		getView().setAutohide(pref.getBoolean(getString(
				R.string.pref_autohide_persistent_view_key), false));
		getView().setAlpha(pref.getInt(getString(R.string.pref_alpha_key), 100)
				/ 100.0f);
	}

	@Override
	protected void onViewClick()
	{
		super.onViewClick();
		SystemHelper.sleep(getApplicationContext(),
				new SystemHelper.SuResultListener()
		{
			@Override
			public void onSuResult(boolean isSuccessful)
			{
				if (!isSuccessful)
				{
					Toast.makeText(PersistentService.this, R.string.sleep_fail,
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	protected void onViewLongClick()
	{
		super.onViewLongClick();
		SystemHelper.startActivity(MainActivity.class,
				new SystemHelper.StartActivityResultListener()
		{
			@Override
			public void onStartActivityResult(boolean isSuccessful)
			{
				if (!isSuccessful)
				{
					Toast.makeText(PersistentService.this,
							R.string.start_activity_fail, Toast.LENGTH_LONG)
							.show();
				}
			}
		});
	}
}
