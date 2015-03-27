/*
 * MainActivity.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class MainActivity extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Disable the standard activity launch animation
		overridePendingTransition(0, 0);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null)
		{
			mFrag = new MainFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, mFrag)
					.commit();
		}
	}

	@Override
	protected void onUserLeaveHint()
	{
		super.onUserLeaveHint();
		overridePendingTransition(0, R.anim.activity_close_exit);
	}

	@Override
	public void finish()
	{
		super.finish();
		overridePendingTransition(0, R.anim.activity_close_exit);
	}

	private MainFragment mFrag;
}
