/*
 * PreferenceFragment.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.content.SharedPreferences;
import android.os.Bundle;

public class PreferenceFragment extends android.preference.PreferenceFragment
		implements SharedPreferences.OnSharedPreferenceChangeListener
{
	public static PreferenceFragment create()
	{
		return new PreferenceFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(getString(
				R.string.pref_file));
		addPreferencesFromResource(R.xml.preference);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key)
	{
		if (key.equals(getString(R.string.pref_persistent_view_key)))
		{
			onPersistentViewChange(pref, key);
		}
		else if (key.equals(getString(R.string.pref_autohide_persistent_view_key)))
		{
			PersistentService.setAutohideView(getActivity(),
					pref.getBoolean(key, false));
		}
	}

	private void onPersistentViewChange(SharedPreferences pref, String key)
	{
		if (pref.getBoolean(key, false))
		{
			PersistentService.start(getActivity());
		}
		else
		{
			PersistentService.stop(getActivity());
		}
	}
}
