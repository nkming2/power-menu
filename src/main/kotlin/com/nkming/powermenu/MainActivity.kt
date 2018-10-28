package com.nkming.powermenu

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity()
{
	companion object
	{
		private val LOG_TAG = MainActivity::class.java.canonicalName
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		_themeAdapter.onCreate(savedInstanceState)
		val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
		_isKeyguard = km.inKeyguardRestrictedInputMode()
		if (_isKeyguard)
		{
			_onLaunchWithKeyguard()
		}

		super.onCreate(savedInstanceState)
		if (!InstallHelper.isSystemApp(this))
		{
			InstallHelper.isPowerCommandAvailable(this,
					onResult = {_onPostCheckInstall(savedInstanceState, it)})
		}
		else
		{
			_onPostCheckInstall(savedInstanceState, true)
		}
	}

	override fun onStart()
	{
		super.onStart()
		if (com.nkming.utils.widget.PersistentService.isRunning())
		{
			PersistentService.hideView(this)
		}
		_isStop = false
	}

	override fun onResume()
	{
		super.onResume()
		_themeAdapter.onResume()
	}

	override fun onPause()
	{
		super.onPause()
		_themeAdapter.onPause()
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
		_isStop = true
	}

	override fun onDestroy()
	{
		super.onDestroy()
		_themeAdapter.onDestroy()
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

	private fun _onPostCheckInstall(savedInstanceState: Bundle?,
			hasInstalled: Boolean)
	{
		if (!hasInstalled)
		{
			// We don't want the animation here
			super.finish()
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			{
				// System app not recommended for M+
				Toast.makeText(this, R.string.su_failed, Toast.LENGTH_LONG).show()
			}
			else
			{
				startActivity(Intent(this, InstallActivity::class.java))
			}
		}
		else
		{
			if (_isStop)
			{
				// User has closed/hid the app
				super.finish()
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
							.commitAllowingStateLoss()
				}
			}
		}
	}

	/**
	 * Called when the activity is launched with an active keyguard
	 */
	private fun _onLaunchWithKeyguard()
	{
		Log.d(LOG_TAG, "_onLaunchWithKeyguard")
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
	}

	private var _isAnimateClose = true
	private var _isStop = false
	private var _isKeyguard = false

	private val _themeAdapter by lazy{ActivityThemeAdapter(this,
			R.style.AppTheme_Dark, R.style.AppTheme_Light)}
}
