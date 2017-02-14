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
				return true
			}
			else
			{
				return false
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

	var isOverrideSystemMenu: Boolean
		get()
		{
			return _pref.getBoolean(_overrideSystemMenuKey, false)
		}
		set(v: Boolean)
		{
			_edit.putBoolean(_overrideSystemMenuKey, v)
		}

	var isSoftRebootEnabled: Boolean
		get()
		{
			return _pref.getBoolean(_softRebootKey, false)
		}
		set(v: Boolean)
		{
			_edit.putBoolean(_softRebootKey, v)
		}

	var isHapticEnabled: Boolean
		get()
		{
			return _pref.getBoolean(_hapticKey, true)
		}
		set(v: Boolean)
		{
			_edit.putBoolean(_hapticKey, v)
		}

	var isConfirmAction: Boolean
		get()
		{
			return _pref.getBoolean(_confirmKey, false)
		}
		set(v)
		{
			_edit.putBoolean(_confirmKey, v)
		}

	var hasRequestOverlayPermission: Boolean
		get()
		{
			return _pref.getBoolean(_requestOverlayPermissionKey, false)
		}
		set(v)
		{
			_edit.putBoolean(_requestOverlayPermissionKey, v)
		}

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

	private val _requestOverlayPermissionKey by lazy{_context.getString(
			R.string.pref_request_overlay_permission)}

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
