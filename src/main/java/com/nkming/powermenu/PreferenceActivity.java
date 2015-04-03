/*
 * PreferenceActivity.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public class PreferenceActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null)
		{
			Fragment f = PreferenceFragment.create();
			getFragmentManager().beginTransaction().add(R.id.container, f)
					.commit();
		}

		PersistentViewHelper.startIfNecessary(this);
	}
}
