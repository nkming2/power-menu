package com.nkming.powermenu

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class ShutdownActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val appContext = applicationContext
		if (!SystemHelper.shutdown(appContext))
		{
			Toast.makeText(appContext, R.string.shutdown_fail, Toast.LENGTH_LONG)
					.show()
		}
		finish()
	}
}

class RebootActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val appContext = applicationContext
		if (!SystemHelper.reboot(SystemHelper.RebootMode.NORMAL, appContext))
		{
			Toast.makeText(appContext, R.string.restart_fail, Toast.LENGTH_LONG)
					.show()
		}
		finish()
	}
}

class RebootRecoveryActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val appContext = applicationContext
		if (!SystemHelper.reboot(SystemHelper.RebootMode.RECOVERY, appContext))
		{
			Toast.makeText(appContext, R.string.restart_fail, Toast.LENGTH_LONG)
					.show()
		}
		finish()
	}
}

class RebootBootloaderActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val appContext = applicationContext
		if (!SystemHelper.reboot(SystemHelper.RebootMode.BOOTLOADER, appContext))
		{
			Toast.makeText(appContext, R.string.restart_fail, Toast.LENGTH_LONG)
					.show()
		}
		finish()
	}
}

class SoftRebootActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val appContext = applicationContext
		SystemHelper.killZygote(appContext,
		{
			if (!it)
			{
				Toast.makeText(appContext, R.string.soft_reboot_fail,
						Toast.LENGTH_LONG).show();
			}
		})
		finish()
	}
}

class SleepActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val appContext = applicationContext
		SystemHelper.sleep(appContext,
		{
			if (!it)
			{
				Toast.makeText(appContext, R.string.sleep_fail,
						Toast.LENGTH_LONG).show()
			}
		})
		finish()
	}
}
