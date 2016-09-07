package com.nkming.powermenu

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.nkming.utils.preference.SeekBarPreference

class PreferenceFragment : android.preference.PreferenceFragment(),
		SharedPreferences.OnSharedPreferenceChangeListener
{
	companion object
	{
		@JvmStatic
		fun create(): PreferenceFragment
		{
			return PreferenceFragment()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		preferenceManager.sharedPreferencesName = getString(R.string.pref_file)
		addPreferencesFromResource(R.xml.preference)
		_init()
	}

	override fun onResume()
	{
		super.onResume()
		preferenceManager.sharedPreferences
				.registerOnSharedPreferenceChangeListener(this)
	}

	override fun onPause()
	{
		super.onPause()
		preferenceManager.sharedPreferences
				.unregisterOnSharedPreferenceChangeListener(this)
	}

	override fun onSharedPreferenceChanged(pref: SharedPreferences, key: String)
	{
		if (key == getString(R.string.pref_persistent_view_key))
		{
			_onPersistentViewChange(pref, key)
		}
		else if (key == getString(R.string.pref_autohide_persistent_view_key))
		{
			PersistentService.setAutohideView(activity, pref.getBoolean(key,
					false))
		}
		else if (key == getString(R.string.pref_alpha_key))
		{
			PersistentService.setAlpha(activity, pref.getInt(key, 100) / 100.0f)
		}
		else if (key == getString(R.string.pref_hide_launcher_key))
		{
			SystemHelper.setEnableActivity(activity,
					LauncherActivity::class.java, !pref.getBoolean(key, false))
		}
		else if (key == getString(R.string.pref_haptic_key))
		{
			PersistentService.setEnableHaptic(activity, pref.getBoolean(key,
					true))
		}
		else if (key == getString(R.string.pref_override_system_menu_key))
		{
			_onOverrideSystemMenuChange(pref, key)
		}
	}

	private fun _init()
	{
		_initAlphaPref()
		_initSoftRebootPref()
	}

	private fun _initAlphaPref()
	{
		val pref = findPreference(getString(R.string.pref_alpha_key)) as SeekBarPreference
		pref.setPreviewListener(object: SeekBarPreference.DefaultPreviewListener()
		{
			override fun onPreviewChange(preview: View, value: Int)
			{
				val v = preview as TextView
				v.text = String.format("%.2f", value / 100.0f)
			}
		})
	}

	private fun _initSoftRebootPref()
	{
		val pref = findPreference(getString(R.string.pref_soft_reboot_key))
		if (SystemHelper.isBusyboxPresent())
		{
			pref.isEnabled = true
			pref.setSummary(R.string.pref_soft_reboot_summary)
		}
		else
		{
			pref.isEnabled = false
			pref.setSummary(R.string.pref_soft_reboot_na_summary)
		}
	}

	private fun _onPersistentViewChange(pref: SharedPreferences, key: String)
	{
		if (pref.getBoolean(key, false))
		{
			PersistentViewHelper.startIfNecessary(activity)
		}
		else
		{
			PersistentService.stop(activity)
		}
	}

	private fun _onOverrideSystemMenuChange(pref: SharedPreferences, key: String)
	{
		if (pref.getBoolean(key, false))
		{
			SystemOverrideService.startIfNecessary(activity)
		}
		else
		{
			SystemOverrideService.stop(activity)
		}
	}
}
