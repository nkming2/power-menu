package com.nkming.powermenu

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme

class InstallConfirmFragment : DialogFragment()
{
	interface Listener
	{
		fun onInstallConfirmed()
	}

	companion object
	{
		@JvmStatic
		fun create(): InstallConfirmFragment
		{
			return InstallConfirmFragment()
		}

		private val LOG_TAG = InstallConfirmFragment::class.java.canonicalName
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		return MaterialDialog.Builder(activity)
				.title(R.string.install_confirm_title)
				.content(R.string.install_confirm_content)
				.theme(if (_pref.isDarkTheme) Theme.DARK else Theme.LIGHT)
				.positiveText(R.string.install_confirm_positive)
				.onPositive({_, _ ->
				run{
					if (!isDetached)
					{
						_listener.onInstallConfirmed()
					}
					_isNoFinish = true
				}})
				.negativeText(android.R.string.cancel)
				.build()
	}

	override fun onAttach(activity: Activity?)
	{
		super.onAttach(activity)
		if (activity !is Listener)
		{
			Log.wtf("$LOG_TAG.onActivityCreated",
					"Activity must implement Listener")
			// In case wtf doesn't throw
			throw IllegalStateException()
		}
		else
		{
			_listener = activity
		}
	}

	override fun onDismiss(dialog: DialogInterface?)
	{
		super.onDismiss(dialog)
		if (activity != null && !_isNoFinish)
		{
			activity.finish()
		}
	}

	private lateinit var _listener: Listener
	private var _isNoFinish = false
	private val _pref by lazy{Preference.from(context)}
}
