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
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.nkming.utils.sys.DeviceInfo;
import com.nkming.utils.type.Size;
import com.nkming.utils.unit.DimensionUtils;

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
		mView = LayoutInflater.from(this).inflate(R.layout.persistent_view, null);
		mView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onViewClick();
			}
		});

		Size screenSize = DeviceInfo.GetScreenPx(this);
		final float dp48 = DimensionUtils.dpToPx(this, 48);
		final int x = (int)(screenSize.w() - dp48 * 0.75f);
		final int y = (int)(screenSize.h() * 0.15f);
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				(int)dp48, (int)dp48, x, y,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.TOP | Gravity.LEFT;
		wm.addView(mView, params);
	}

	private void uninitView()
	{
		if (mView != null)
		{
			WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
			wm.removeView(mView);
		}
	}

	private void onViewClick()
	{
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private View mView;
}
