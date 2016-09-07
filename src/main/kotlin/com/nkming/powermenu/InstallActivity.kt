package com.nkming.powermenu

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class InstallActivity : AppCompatActivity(), InstallConfirmFragment.Listener
{
	override fun onInstallConfirmed()
	{
		val f = InstallFragment.create()
		f.show(supportFragmentManager, "install")
	}

	protected override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		if (InstallHelper.isSystemApp(this))
		{
			_onCreateUninstall(savedInstanceState)
		}
		else
		{
			_onCreateInstall(savedInstanceState)
		}
	}

	private fun _onCreateInstall(savedInstanceState: Bundle?)
	{
		if (savedInstanceState == null)
		{
			val f = InstallConfirmFragment.create()
			f.show(supportFragmentManager, "install_confirm")
		}
	}

	private fun _onCreateUninstall(savedInstanceState: Bundle?)
	{
		if (savedInstanceState == null)
		{
			val f = UninstallFragment.create()
			f.show(supportFragmentManager, "uninstall")
		}
	}
}
