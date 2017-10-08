package com.nkming.powermenu

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme

abstract class Action(appContext: Context)
{
	abstract operator fun invoke()

	var onSuccessful: (() -> Unit)? = null
	var onFailed: (() -> Unit)? = null
	var onDone: (() -> Unit)? = null

	protected val _context = appContext
}

abstract class DangerousAction(appContext: Context, activity: Activity,
		isExplicitTheming: Boolean)
		: Action(appContext)
{
	override fun invoke()
	{
		if (shouldConfirm)
		{
			val dialog = buildConfirmDialog{onPostConfirm()}
			dialog.show()
			_dialog = dialog
		}
		else
		{
			onPostConfirm()
		}
	}

	fun dismissConfirm()
	{
		_dialog?.dismiss()
		_dialog = null
	}

	open val shouldConfirm: Boolean
		get()
		{
			val pref = Preference.from(_context)
			return pref.isConfirmAction
		}

	var onCancel: (() -> Unit)? = null

	/**
	 * Return the confirmation dialog. Override method to return custom dialog
	 *
	 * @param onPositive Callback on positive button click
	 * @return
	 */
	protected open fun buildConfirmDialog(onPositive: () -> Unit): Dialog
	{
		val builder = MaterialDialog.Builder(_activity)
				.title(_dialogTitle)
				.content(_dialogContent)
				.theme(if (_pref.isDarkTheme) Theme.DARK else Theme.LIGHT)
				.positiveText(android.R.string.yes)
				.onPositive{_, _ -> onPositive()}
				.negativeText(android.R.string.no)
				.onNegative{_, _ -> onCancel?.invoke()}
				.cancelListener{onCancel?.invoke()}
		if (_isExplicitTheming)
		{
			if (_pref.isDarkTheme)
			{
				builder.positiveColorRes(R.color.accent_dark)
				builder.negativeColorRes(R.color.accent_dark)
			}
			else
			{
				builder.positiveColorRes(R.color.accent_light)
				builder.negativeColorRes(R.color.accent_light)
			}
		}
		return builder.build()
	}

	/**
	 * Called when the action is being confirmed by the user
	 */
	protected abstract fun onPostConfirm()

	/**
	 * Title res used in the default confirmation dialog, if custom dialog is
	 * used instead, this value need not to be overridden
	 */
	protected open val _dialogTitle: Int = 0
	protected open val _dialogContent: Int = 0
	protected val _activity = activity

	private val _isExplicitTheming = isExplicitTheming
	private val _pref by lazy{Preference.from(_context)}
	private var _dialog: Dialog? = null
}

open class ShutdownAction(appContext: Context, activity: Activity,
		isExplicitTheming: Boolean = false)
		: DangerousAction(appContext, activity, isExplicitTheming)
{
	override fun onPostConfirm()
	{
		SystemHelper.shutdown(_context,
		{
			if (!it)
			{
				Toast.makeText(_context, R.string.shutdown_fail,
						Toast.LENGTH_LONG).show()
				onFailed?.invoke()
			}
			else
			{
				onSuccessful?.invoke()
			}
			onDone?.invoke()
		})
	}

	protected override val _dialogTitle: Int = R.string.shutdown_confirm_title
	protected override val _dialogContent: Int =
			R.string.shutdown_confirm_content
}

open class RebootAction(appContext: Context, activity: Activity,
		rebootMode: SystemHelper.RebootMode, isExplicitTheming: Boolean = false)
		: DangerousAction(appContext, activity, isExplicitTheming)
{
	override fun onPostConfirm()
	{
		SystemHelper.reboot(_rebootMode, _context,
		{
			if (!it)
			{
				Toast.makeText(_context, R.string.restart_fail,
						Toast.LENGTH_LONG).show()
				onFailed?.invoke()
			}
			else
			{
				onSuccessful?.invoke()
			}
			onDone?.invoke()
		})
	}

	protected override val _dialogTitle: Int = R.string.restart_confirm_title
	protected override val _dialogContent: Int =
			R.string.restart_confirm_content

	private val _rebootMode = rebootMode
}

open class SoftRebootAction(appContext: Context, activity: Activity,
		isExplicitTheming: Boolean = false)
		: DangerousAction(appContext, activity, isExplicitTheming)
{
	override fun onPostConfirm()
	{
		SystemHelper.killZygote(_context,
		{
			if (!it)
			{
				Toast.makeText(_context, R.string.soft_reboot_fail,
						Toast.LENGTH_LONG).show()
				onFailed?.invoke()
			}
			else
			{
				onSuccessful?.invoke()
			}
			onDone?.invoke()
		})
	}

	protected override val _dialogTitle: Int = R.string.restart_confirm_title
	protected override val _dialogContent: Int =
			R.string.restart_confirm_content
}

open class SleepAction(appContext: Context) : Action(appContext)
{
	override fun invoke()
	{
		SystemHelper.sleep(_context,
		{
			if (!it)
			{
				Toast.makeText(_context, R.string.sleep_fail, Toast.LENGTH_LONG)
						.show()
				onFailed?.invoke()
			}
			else
			{
				onSuccessful?.invoke()
			}
			onDone?.invoke()
		})
	}
}

open class ScreenshotAction(appContext: Context) : Action(appContext)
{
	companion object
	{
		val LOG_TAG = ScreenshotAction::class.java.canonicalName
	}

	override fun invoke()
	{
		if (!_pref.isNativeScreenshot)
		{
			invokeLegacy()
		}
		else
		{
			invokeNative()
		}
	}

	private fun invokeLegacy()
	{
		val wm = _context.getSystemService(Context.WINDOW_SERVICE)
				as WindowManager
		val rotation = wm.defaultDisplay.rotation
		val screenshotHandler = ScreenshotHandler(_context)
		SystemHelper.screenshot(_context, {error, filepath ->
		run{
			if (error == SystemHelper.ScreenshotError.NO_ERROR)
			{
				screenshotHandler.onScreenshotSuccess(filepath, rotation)
				onSuccessful?.invoke()
				onDone?.invoke()
			}
			else
			{
				// Now try our new shiny method
				Log.i("$LOG_TAG.invokeLegacy",
						"Legacy screenshot failed, switching to native")
				_pref.isNativeScreenshot = true
				_pref.apply()
				invokeNative()
			}
		}})
	}

	private fun invokeNative()
	{
		SystemHelper.screenshotNative(_context,
		{
			if (it)
			{
				onSuccessful?.invoke()
			}
			else
			{
				Toast.makeText(_context, R.string.screenshot_fail_screencap,
						Toast.LENGTH_LONG).show()
				onFailed?.invoke()
			}
			onDone?.invoke()
		})
	}

	private val _pref by lazy{Preference.from(_context)}
}
