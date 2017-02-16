package com.nkming.powermenu

import android.app.Activity
import android.os.Bundle

class ShutdownActivity : Activity()
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

class RebootActivity : Activity()
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

class RebootRecoveryActivity : Activity()
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

class RebootBootloaderActivity : Activity()
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

class SoftRebootActivity : Activity()
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

class SleepActivity : Activity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		SleepAction(applicationContext)()
		finish()
	}
}

class ScreenshotActivity : Activity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		ScreenshotAction(applicationContext)()
		finish()
	}
}
