package com.nkming.powermenu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class InstallActivity : AppCompatActivity(), InstallConfirmFragment.Listener
{
	override fun onInstallConfirmed()
	{
		val f = InstallFragment.create()
		f.show(supportFragmentManager, "install")
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		_themeAdapter.onCreate(savedInstanceState)
		if (InstallHelper.isSystemApp(this))
		{
			_onCreateUninstall(savedInstanceState)
		}
		else
		{
			_onCreateInstall(savedInstanceState)
		}
	}

	override fun onDestroy()
	{
		super.onDestroy()
		_themeAdapter.onDestroy()
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

	private val _themeAdapter by lazy{ActivityThemeAdapter(this,
			R.style.AppTheme_Dark_Dialog, R.style.AppTheme_Light_Dialog)}
}
