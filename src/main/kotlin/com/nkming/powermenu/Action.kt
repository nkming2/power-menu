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

abstract class DangerousAction(appContext: Context, activity: Activity)
		: Action(appContext)
{
	override fun invoke()
	{
		if (shouldConfirm)
		{
			val dialog = buildConfirmDialog{onPostConfirm()}
			dialog.show()
		}
		else
		{
			onPostConfirm()
		}
	}

	open val shouldConfirm: Boolean
		get()
		{
			val pref = Preference.from(_context)
			return pref.isConfirmAction
		}

	var onCancel: (() -> Unit)? = null

	protected abstract fun buildConfirmDialog(onPositive: () -> Unit): Dialog
	protected abstract fun onPostConfirm()

	protected val _activity = activity
}

open class ShutdownAction(appContext: Context, activity: Activity)
		: DangerousAction(appContext, activity)
{
	override fun buildConfirmDialog(onPositive: () -> Unit): Dialog
	{
		return MaterialDialog.Builder(_activity)
				.title(R.string.shutdown_confirm_title)
				.content(R.string.shutdown_confirm_content)
				.theme(Theme.LIGHT)
				.positiveText(android.R.string.yes)
				.onPositive{materialDialog, dialogAction -> onPositive()}
				.negativeText(android.R.string.no)
				.onNegative{materialDialog, dialogAction -> onCancel?.invoke()}
				.cancelListener{onCancel?.invoke()}
				.build()
	}

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
}

open class RebootAction(appContext: Context, activity: Activity,
		rebootMode: SystemHelper.RebootMode)
		: DangerousAction(appContext, activity)
{
	override fun buildConfirmDialog(onPositive: () -> Unit): Dialog
	{
		return MaterialDialog.Builder(_activity)
				.title(R.string.restart_confirm_title)
				.content(R.string.restart_confirm_content)
				.theme(Theme.LIGHT)
				.positiveText(android.R.string.yes)
				.onPositive{materialDialog, dialogAction -> onPositive()}
				.negativeText(android.R.string.no)
				.onNegative{materialDialog, dialogAction -> onCancel?.invoke()}
				.cancelListener{onCancel?.invoke()}
				.build()
	}

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

	private val _rebootMode = rebootMode
}

open class SoftRebootAction(appContext: Context, activity: Activity)
		: DangerousAction(appContext, activity)
{
	override fun buildConfirmDialog(onPositive: () -> Unit): Dialog
	{
		return MaterialDialog.Builder(_activity)
				.title(R.string.restart_confirm_title)
				.content(R.string.restart_confirm_content)
				.theme(Theme.LIGHT)
				.positiveText(android.R.string.yes)
				.onPositive{materialDialog, dialogAction -> onPositive()}
				.negativeText(android.R.string.no)
				.onNegative{materialDialog, dialogAction -> onCancel?.invoke()}
				.cancelListener{onCancel?.invoke()}
				.build()
	}

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
	override fun invoke()
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
			}
			else
			{
				val textId = if (error
						== SystemHelper.ScreenshotError.SCREENCAP_FAILURE)
				{
					R.string.screenshot_fail_screencap
				}
				else
				{
					R.string.screenshot_fail_file
				}
				Toast.makeText(_context, textId, Toast.LENGTH_LONG).show()
				onFailed?.invoke()
			}
			onDone?.invoke()
		}})
	}
}
