package com.nkming.powermenu

import android.app.Activity
import android.os.Bundle

class ShutdownActivity : Activity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = ShutdownAction(applicationContext, this)
		action.onDone = {finish()}
		action()
	}
}

class RebootActivity : Activity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = RebootAction(applicationContext, this,
				SystemHelper.RebootMode.NORMAL)
		action.onDone = {finish()}
		action()
	}
}

class RebootRecoveryActivity : Activity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = RebootAction(applicationContext, this,
				SystemHelper.RebootMode.RECOVERY)
		action.onDone = {finish()}
		action()
	}
}

class RebootBootloaderActivity : Activity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = RebootAction(applicationContext, this,
				SystemHelper.RebootMode.BOOTLOADER)
		action.onDone = {finish()}
		action()
	}
}

class SoftRebootActivity : Activity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = SoftRebootAction(applicationContext, this)
		action.onDone = {finish()}
		action()
	}
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
