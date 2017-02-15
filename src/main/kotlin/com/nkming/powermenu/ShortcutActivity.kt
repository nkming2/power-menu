package com.nkming.powermenu

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class ShutdownActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = ShutdownAction(applicationContext, this)
		action.onDone = {finish()}
		action()
	}
}

class RebootActivity : AppCompatActivity()
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

class RebootRecoveryActivity : AppCompatActivity()
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

class RebootBootloaderActivity : AppCompatActivity()
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

class SoftRebootActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = SoftRebootAction(applicationContext, this)
		action.onDone = {finish()}
		action()
	}
}

class SleepActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		SleepAction(applicationContext)()
		finish()
	}
}

class ScreenshotActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		ScreenshotAction(applicationContext)()
		finish()
	}
}
