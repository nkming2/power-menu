/*
 * PreferenceFragment.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.os.Bundle;

public class PreferenceFragment extends android.preference.PreferenceFragment
{
	public static PreferenceFragment create()
	{
		return new PreferenceFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
	}
}
