package com.nkming.powermenu

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler

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

	fun onResume()
	{
		_isResumed = true
		if (_isRecreatePending)
		{
			_handler.post{
				// Why isn't recreate() working? :/
				_activity.startActivity(Intent(_activity, javaClass))
				_activity.finish()
			}
		}
	}

	fun onPause()
	{
		_isResumed = false
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
	private val _handler = Handler()
	private var _isResumed = false
	private var _isRecreatePending = false
}
