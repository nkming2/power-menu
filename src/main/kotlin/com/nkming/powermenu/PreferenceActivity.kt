package com.nkming.powermenu

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity

class PreferenceActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		PreferenceManager.setDefaultValues(this, getString(R.string.pref_file),
				Context.MODE_PRIVATE, R.xml.preference, false)

		setContentView(R.layout.activity_main)
		if (savedInstanceState == null)
		{
			val f = PreferenceFragment.create()
			fragmentManager.beginTransaction().add(R.id.container, f).commit()
		}

		PersistentViewHelper.startIfNecessary(this)
		SystemOverrideService.startIfNecessary(this)
	}
}
