package com.nkming.powermenu

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.Preference
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
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)
		_init()
	}

	override fun onResume()
	{
		super.onResume()
		preferenceManager.sharedPreferences
				.registerOnSharedPreferenceChangeListener(this)
		if (_prefFile.hasRequestOverlayPermission
				&& !_persistentViewPref.isChecked
				&& PermissionUtils.hasSystemAlertWindow(activity))
		{
			// Permission requested and granted
			_persistentViewPref.isChecked = true
			PermissionUtils.forceRequestSystemAlertWindowNextTime(activity)
		}
		else if (_persistentViewPref.isChecked
				&& !PermissionUtils.hasSystemAlertWindow(activity))
		{
			// User revoked our permission
			_persistentViewPref.isChecked = false
			PermissionUtils.forceRequestSystemAlertWindowNextTime(activity)
		}
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
			SystemHelper.setEnableComponent(activity,
					LauncherActivity::class.java, !pref.getBoolean(key, false))
		}
		else if (key == getString(R.string.pref_haptic_key))
		{
			PersistentService.setEnableHaptic(activity, pref.getBoolean(key,
					true))
		}
		else if (key == getString(R.string.pref_soft_reboot_key))
		{
			val isEnable = pref.getBoolean(key, false)
			try
			{
				// Will throw on N-
				SystemHelper.setEnableComponent(activity,
						SoftRebootTileService::class.java, isEnable)
			}
			catch (e: Throwable)
			{}
			SystemHelper.setEnableComponent(activity,
					SoftRebootActivity::class.java, isEnable)
		}
		else if (key == getString(R.string.pref_override_system_menu_key))
		{
			_onOverrideSystemMenuChange(pref, key)
		}
	}

	private fun _init()
	{
		_initEnablePersistentViewPref()
		_initAlphaPref()
		_initInstallPref()
		_initSoftRebootPref()
		_initAbout()
	}

	private fun _initEnablePersistentViewPref()
	{
		val l = Preference.OnPreferenceChangeListener{preference, newValue ->
		run{
			// User modified preference, could request again
			PermissionUtils.forceRequestSystemAlertWindowNextTime(activity)
			if (newValue as Boolean
					&& !PermissionUtils.hasSystemAlertWindow(activity))
			{
				PermissionUtils.requestSystemAlertWindow(activity)
				false
			}
			else
			{
				true
			}
		}}
		_persistentViewPref.onPreferenceChangeListener = l
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

	private fun _initInstallPref()
	{
		val pref = findPreference(getString(R.string.pref_install_key))
		if (InstallHelper.isSystemApp(activity))
		{
			pref.setTitle(R.string.pref_uninstall_title)
			pref.setSummary(R.string.pref_uninstall_summary)
		}
		else
		{
			pref.setTitle(R.string.pref_install_title)
			pref.setSummary(R.string.pref_install_summary)
		}
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

	private fun _initAbout()
	{
		val aboutVersion = findPreference(getString(R.string.about_version_key))
		aboutVersion.summary = BuildConfig.VERSION_NAME

		if (getString(R.string.about_translator_credit).isNullOrEmpty())
		{
			val aboutTranslator = findPreference(getString(
					R.string.about_translator_key))
			aboutTranslator.summary = getString(R.string.about_translator_help)
			aboutTranslator.onPreferenceClickListener =
					Preference.OnPreferenceClickListener{
						val i = Intent(Intent.ACTION_VIEW)
						i.data = Uri.parse(getString(
								R.string.about_translator_help_url))
						startActivity(i)
						return@OnPreferenceClickListener true
					}
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

	private val _prefFile by lazy{com.nkming.powermenu.Preference.from(activity)}
	private val _persistentViewPref by lazy{findPreference(getString(
			R.string.pref_persistent_view_key)) as CheckBoxPreference}
}
