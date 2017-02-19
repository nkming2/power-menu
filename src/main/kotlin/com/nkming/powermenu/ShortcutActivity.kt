package com.nkming.powermenu

import android.app.Activity
import android.os.Bundle

open class _BaseShortcutActivity : Activity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		_themeAdapter.onCreate(savedInstanceState)
	}

	override fun onDestroy()
	{
		super.onDestroy()
		_themeAdapter.onDestroy()
	}

	private val _themeAdapter by lazy{ActivityThemeAdapter(this,
			R.style.AppThemeNoDisplay_Dark, R.style.AppThemeNoDisplay_Light)}
}

class ShutdownActivity : _BaseShortcutActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		_action.onDone = {finish()}
		_action.onCancel = {finish()}
		_action()
	}

	override fun onStop()
	{
		super.onStop()
		_action.dismissConfirm()
	}

	private val _action by lazy{ShutdownAction(applicationContext, this)}
}

class RebootActivity : _BaseShortcutActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		_action.onDone = {finish()}
		_action.onCancel = {finish()}
		_action()
	}

	override fun onStop()
	{
		super.onStop()
		_action.dismissConfirm()
	}

	private val _action by lazy{RebootAction(applicationContext, this,
			SystemHelper.RebootMode.NORMAL)}
}

class RebootRecoveryActivity : _BaseShortcutActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		_action.onDone = {finish()}
		_action.onCancel = {finish()}
		_action()
	}

	override fun onStop()
	{
		super.onStop()
		_action.dismissConfirm()
	}

	private val _action by lazy{RebootAction(applicationContext, this,
			SystemHelper.RebootMode.RECOVERY)}
}

class RebootBootloaderActivity : _BaseShortcutActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		_action.onDone = {finish()}
		_action.onCancel = {finish()}
		_action()
	}

	override fun onStop()
	{
		super.onStop()
		_action.dismissConfirm()
	}

	private val _action by lazy{RebootAction(applicationContext, this,
			SystemHelper.RebootMode.BOOTLOADER)}
}

class SoftRebootActivity : _BaseShortcutActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		_action.onDone = {finish()}
		_action.onCancel = {finish()}
		_action()
	}

	override fun onStop()
	{
		super.onStop()
		_action.dismissConfirm()
	}

	private val _action by lazy{SoftRebootAction(applicationContext, this)}
}

class SleepActivity : _BaseShortcutActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		SleepAction(applicationContext)()
		finish()
	}
}

class ScreenshotActivity : _BaseShortcutActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		ScreenshotAction(applicationContext)()
		finish()
	}
}
