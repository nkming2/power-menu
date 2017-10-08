package com.nkming.powermenu

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle

class ActivityThemeAdapter(activity: Activity, darkTheme: Int, lightTheme: Int)
{
	fun onCreate(savedInstanceState: Bundle?)
	{
		_activity.setTheme(_theme)
		_pref.onSharedPreferenceChangeListener =
				SharedPreferences.OnSharedPreferenceChangeListener{_, key ->
				run{
					if (key == _activity.getString(R.string.pref_dark_theme_key))
					{
						_activity.recreate()
					}
				}}
	}

	fun onDestroy()
	{
		_pref.onSharedPreferenceChangeListener = null
	}

	private val _theme: Int
		get()
		{
			return if (_pref.isDarkTheme) _darkTheme else _lightTheme
		}

	private val _activity = activity
	private val _darkTheme = darkTheme
	private val _lightTheme = lightTheme
	private val _pref by lazy{Preference.from(_activity)}
}
