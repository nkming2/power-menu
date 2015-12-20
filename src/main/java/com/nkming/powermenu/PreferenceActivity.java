/*
 * PreferenceActivity.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

public class PreferenceActivity extends ActionBarActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, getString(R.string.pref_file),
				Context.MODE_PRIVATE, R.xml.preference, false);

		setContentView(R.layout.activity_main);
		if (savedInstanceState == null)
		{
			Fragment f = PreferenceFragment.create();
			getFragmentManager().beginTransaction().add(R.id.container, f)
					.commit();
		}

		PersistentViewHelper.startIfNecessary(this);
		SystemOverrideService.startIfNecessary(this);
	}
}
