/*
 * MainActivity.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.WindowManager;

public class MainActivity extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		KeyguardManager km = (KeyguardManager)getSystemService(
				Context.KEYGUARD_SERVICE);
		if (km.inKeyguardRestrictedInputMode())
		{
			onLaunchWithKeyguard();
		}

		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, getString(R.string.pref_file),
				Context.MODE_PRIVATE, R.xml.preference, false);

		if (!InstallHelper.isSystemApp(this))
		{
			// We don't want the animation here
			super.finish();
			startActivity(new Intent(this, InstallActivity.class));
		}
		else
		{
			// Disable the standard activity launch animation
			overridePendingTransition(0, 0);
			setContentView(R.layout.activity_main);
			if (savedInstanceState == null)
			{
				getSupportFragmentManager().beginTransaction()
						.add(R.id.container, new MainFragment()).commit();
			}
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if (PersistentService.isRunning())
		{
			PersistentService.hideView(this);
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if (PersistentService.isRunning())
		{
			PersistentService.showView(this);
		}
		else
		{
			PersistentViewHelper.startIfNecessary(this);
			SystemOverrideService.startIfNecessary(this);
		}
	}

	@Override
	protected void onUserLeaveHint()
	{
		super.onUserLeaveHint();
		if (mIsAnimateClose)
		{
			overridePendingTransition(0, R.anim.activity_close_exit);
		}
	}

	@Override
	public void finish()
	{
		super.finish();
		if (mIsAnimateClose)
		{
			overridePendingTransition(0, R.anim.activity_close_exit);
		}
	}

	private static final String LOG_TAG = MainActivity.class.getCanonicalName();

	/**
	 * Called when the activity is launched with an active keyguard
	 */
	private void onLaunchWithKeyguard()
	{
		Log.d(LOG_TAG, "onLaunchWithKeyguard");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		setTheme(R.style.AppThemeKeyguard);
	}

	private boolean mIsAnimateClose = true;
}
