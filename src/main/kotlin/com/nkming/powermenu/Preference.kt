package com.nkming.powermenu

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Preference(pref: SharedPreferences, context: Context)
{
	companion object
	{
		@JvmStatic
		fun from(context: Context): Preference
		{
			return Preference(context.getSharedPreferences(context.getString(
					R.string.pref_file), Context.MODE_PRIVATE), context)
		}
	}

	fun commit(): Boolean
	{
		return _editLock.withLock(
		{
			if (_edit.commit())
			{
				__edit = null
				true
			}
			else
			{
				false
			}
		})
	}

	fun apply()
	{
		_editLock.withLock(
		{
			_edit.apply()
			__edit = null
		})
	}

	var onSharedPreferenceChangeListener
			: SharedPreferences.OnSharedPreferenceChangeListener? = null
		set(v)
		{
			if (field != null)
			{
				_pref.unregisterOnSharedPreferenceChangeListener(field)
			}
			if (v != null)
			{
				_pref.registerOnSharedPreferenceChangeListener(v)
				field = v
			}
		}

	var lastVersion: Int
		get() = _pref.getInt(_lastVersionKey, -1)
		set(v)
		{
			_edit.putInt(_lastVersionKey, v)
		}

	var isPersistentViewEnabled: Boolean
		get() = _pref.getBoolean(_persistentViewKey, false)
		set(v)
		{
			_edit.putBoolean(_persistentViewKey, v)
		}

	var isOverrideSystemMenu: Boolean
		get() = _pref.getBoolean(_overrideSystemMenuKey, false)
		set(v)
		{
			_edit.putBoolean(_overrideSystemMenuKey, v)
		}

	var isSoftRebootEnabled: Boolean
		get() = _pref.getBoolean(_softRebootKey, false)
		set(v)
		{
			_edit.putBoolean(_softRebootKey, v)
		}

	var isHapticEnabled: Boolean
		get() = _pref.getBoolean(_hapticKey, true)
		set(v)
		{
			_edit.putBoolean(_hapticKey, v)
		}

	var isConfirmAction: Boolean
		get() = _pref.getBoolean(_confirmKey, false)
		set(v)
		{
			_edit.putBoolean(_confirmKey, v)
		}

	var isDarkTheme: Boolean
		get() = _pref.getBoolean(_darkThemeKey, true)
		set(v)
		{
			_edit.putBoolean(_darkThemeKey, v)
		}

	var hasRequestOverlayPermission: Boolean
		get() = _pref.getBoolean(_requestOverlayPermissionKey, false)
		set(v)
		{
			_edit.putBoolean(_requestOverlayPermissionKey, v)
		}

	var isNativeScreenshot: Boolean
		get() = _pref.getBoolean(_nativeScreenshotKey, false)
		set(v)
		{
			_edit.putBoolean(_nativeScreenshotKey, v)
		}

	private val _lastVersionKey by lazy{_context.getString(
			R.string.pref_last_version_key)}

	private val _persistentViewKey by lazy{_context.getString(
			R.string.pref_persistent_view_key)}

	private val _overrideSystemMenuKey by lazy(
	{
		_context.getString(R.string.pref_override_system_menu_key)
	})

	private val _softRebootKey by lazy{_context.getString(
			R.string.pref_soft_reboot_key)}

	private val _hapticKey by lazy{_context.getString(
			R.string.pref_haptic_key)}

	private val _confirmKey by lazy{_context.getString(
			R.string.pref_confirm_key)}

	private val _darkThemeKey by lazy{_context.getString(
			R.string.pref_dark_theme_key)}

	private val _requestOverlayPermissionKey by lazy{_context.getString(
			R.string.pref_request_overlay_permission_key)}

	private val _nativeScreenshotKey by lazy{_context.getString(
			R.string.pref_native_screenshot_key)}

	private val _context = context
	private val _pref = pref
	private val _edit: SharedPreferences.Editor
		get()
		{
			_editLock.withLock(
			{
				if (__edit == null)
				{
					__edit = _pref.edit()
				}
				return __edit!!
			})
		}
	private var __edit: SharedPreferences.Editor? = null
	private val _editLock = ReentrantLock(true)
}
