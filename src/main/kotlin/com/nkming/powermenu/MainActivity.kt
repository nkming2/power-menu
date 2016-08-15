package com.nkming.powermenu

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

class MainActivity : AppCompatActivity()
{
	companion object
	{
		private val LOG_TAG = MainActivity::class.java.canonicalName
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
		if (km.inKeyguardRestrictedInputMode())
		{
			_onLaunchWithKeyguard()
		}

		super.onCreate(savedInstanceState)
		PreferenceManager.setDefaultValues(this, getString(R.string.pref_file),
				Context.MODE_PRIVATE, R.xml.preference, false)

		if (!InstallHelper.isSystemApp(this))
		{
			// We don't want the animation here
			super.finish()
			startActivity(Intent(this, InstallActivity::class.java))
		}
		else
		{
			// Disable the standard activity launch animation
			overridePendingTransition(0, 0)
			setContentView(R.layout.activity_main)
			if (savedInstanceState == null)
			{
				supportFragmentManager.beginTransaction()
						.add(R.id.container, MainFragment())
						.commit()
			}
		}
	}

	override fun onStart()
	{
		super.onStart()
		if (com.nkming.utils.widget.PersistentService.isRunning())
		{
			PersistentService.hideView(this)
		}
	}

	override fun onStop()
	{
		super.onStop()
		if (com.nkming.utils.widget.PersistentService.isRunning())
		{
			PersistentService.showView(this)
		}
		else
		{
			PersistentViewHelper.startIfNecessary(this)
			SystemOverrideService.startIfNecessary(this)
		}
	}

	override fun onUserLeaveHint()
	{
		super.onUserLeaveHint()
		if (_isAnimateClose)
		{
			overridePendingTransition(0, R.anim.activity_close_exit)
		}
	}

	override fun finish()
	{
		super.finish()
		if (_isAnimateClose)
		{
			overridePendingTransition(0, R.anim.activity_close_exit)
		}
	}

	/**
	 * Called when the activity is launched with an active keyguard
	 */
	private fun _onLaunchWithKeyguard()
	{
		Log.d("$LOG_TAG", "_onLaunchWithKeyguard")
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
		setTheme(R.style.AppThemeKeyguard)
	}

	private var _isAnimateClose = true
}
