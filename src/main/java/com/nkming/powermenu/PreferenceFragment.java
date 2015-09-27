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
import android.preference.Preference;
import android.view.View;
import android.widget.TextView;

import com.nkming.utils.preference.SeekBarPreference;

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
		init();
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
		else if (key.equals(getString(R.string.pref_alpha_key)))
		{
			PersistentService.setAlpha(getActivity(),
					pref.getInt(key, 100) / 100.0f);
		}
		else if (key.equals(getString(R.string.pref_hide_launcher_key)))
		{
			SystemHelper.setEnableActivity(getActivity(), LauncherActivity.class,
					!pref.getBoolean(key, false));
		}
	}

	private void init()
	{
		initAlphaPref();
		initSoftRebootPref();
	}

	private void initAlphaPref()
	{
		SeekBarPreference pref = (SeekBarPreference)findPreference(getString(
				R.string.pref_alpha_key));
		pref.setPreviewListener(new SeekBarPreference.DefaultPreviewListener()
		{
			@Override
			public void onPreviewChange(View preview, int value)
			{
				TextView v = (TextView)preview;
				v.setText(String.format("%.2f", value / 100.0f));
			}
		});
	}

	private void initSoftRebootPref()
	{
		Preference pref = findPreference(getString(R.string.pref_soft_reboot_key));
		if (SystemHelper.isBusyboxPresent())
		{
			pref.setEnabled(true);
			pref.setSummary(R.string.pref_soft_reboot_summary);
		}
		else
		{
			pref.setEnabled(false);
			pref.setSummary(R.string.pref_soft_reboot_na_summary);
		}
	}

	private void onPersistentViewChange(SharedPreferences pref, String key)
	{
		if (pref.getBoolean(key, false))
		{
			PersistentViewHelper.startIfNecessary(getActivity());
		}
		else
		{
			PersistentService.stop(getActivity());
		}
	}
}
