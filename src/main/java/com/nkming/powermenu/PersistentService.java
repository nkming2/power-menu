/*
 * PersistentService.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.view.View;

import eu.chainfire.libsuperuser.Shell;

/**
 * Service to display the persistent view
 */
public class PersistentService extends Service
{
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		initView();
	}

	@Override
	public void onDestroy()
	{
		Log.i(Res.LOG_TAG, "onDestroy");
		super.onDestroy();
		uninitView();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}

	private void initView()
	{
		if (mView != null)
		{
			uninitView();
		}
		mView = PersistentView.create(this, R.layout.persistent_view);
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

	private void uninitView()
	{
		if (mView != null)
		{
			PersistentView.destroy(this, mView);
			mView = null;
		}
	}

	private void onViewClick()
	{
		SystemHelper.sleep(getApplicationContext());
	}

	private void onViewLongClick()
	{
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
}
