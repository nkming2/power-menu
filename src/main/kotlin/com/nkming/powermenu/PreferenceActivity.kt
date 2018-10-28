package com.nkming.powermenu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PreferenceActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		_themeAdapter.onCreate(savedInstanceState)
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)
		if (savedInstanceState == null)
		{
			val f = PreferenceFragment.create()
			fragmentManager.beginTransaction().add(R.id.container, f).commit()
		}

		PersistentViewHelper.startIfNecessary(this)
		SystemOverrideService.startIfNecessary(this)
	}

	override fun onResume()
	{
		super.onResume()
		_themeAdapter.onResume()
	}

	override fun onPause()
	{
		super.onPause()
		_themeAdapter.onPause()
	}

	override fun onDestroy()
	{
		super.onDestroy()
		_themeAdapter.onDestroy()
	}

	private val _themeAdapter by lazy{ActivityThemeAdapter(this,
			R.style.AppThemePreference_Dark, R.style.AppThemePreference_Light)}
}
