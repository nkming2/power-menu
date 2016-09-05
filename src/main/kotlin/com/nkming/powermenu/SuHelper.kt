package com.nkming.powermenu

import android.content.Context
import android.os.Handler
import android.widget.Toast
import eu.chainfire.libsuperuser.Shell

object SuHelper
{
	fun doSuCommand(context: Context, scripts: List<String>,
			successWhere: ((exitCode: Int, output: List<String>) -> Boolean) = {
					exitCode, output -> exitCode >= 0},
			onSuccess: ((exitCode: Int, output: List<String>) -> Unit)? = null,
			onFailure: ((exitCode: Int, output: List<String>) -> Unit)? = null)
	{
		setContext(context)
		_doSuCommand(scripts, successWhere, onSuccess, onFailure)
	}

	private fun _doSuCommand(scripts: List<String>,
			successWhere: ((exitCode: Int, output: List<String>) -> Boolean) = {
					exitCode, output -> exitCode >= 0},
			onSuccess: ((exitCode: Int, output: List<String>) -> Unit)? = null,
			onFailure: ((exitCode: Int, output: List<String>) -> Unit)? = null)
	{
		if (!_su.isRunning)
		{
			_handler.postDelayed({_doSuCommand(scripts, successWhere, onSuccess,
					onFailure)}, 200)
		}
		else
		{
			__doSuCommand(scripts, successWhere, onSuccess, onFailure)
		}
	}

	private fun __doSuCommand(scripts: List<String>,
			successWhere: ((exitCode: Int, output: List<String>) -> Boolean) = {
					exitCode, output -> exitCode >= 0},
			onSuccess: ((exitCode: Int, output: List<String>) -> Unit)? = null,
			onFailure: ((exitCode: Int, output: List<String>) -> Unit)? = null)
	{
		_su.addCommand(scripts, 0, {commandCode, exitCode, output ->
		run{
			if (exitCode == Shell.OnCommandResultListener.WATCHDOG_EXIT)
			{
				Log.e("$LOG_TAG.__doSuCommand", "Watchdog exception")
				_su.kill()
				_su = buildSuSession()
				_handler.postDelayed({_doSuCommand(scripts, successWhere,
						onSuccess, onFailure)}, 200)
			}
			else if (!successWhere(exitCode, output))
			{
				Log.e("$LOG_TAG.__doSuCommand",
						"Failed($exitCode) executing\nCommand: ${scripts.joinToString("\n")}\nOutput: ${output.joinToString("\n")}")
				onFailure?.invoke(exitCode, output)
			}
			else
			{
				onSuccess?.invoke(exitCode, output)
			}
		}})
	}

	private fun buildSuSession(): Shell.Interactive
	{
		Log.d(LOG_TAG, "buildSuSession()")
		_isSuStarting = true
		return Shell.Builder()
				.useSU()
				.setWantSTDERR(true)
				.setWatchdogTimeout(5)
				.setMinimalLogging(true)
				.open({commandCode, exitCode, output ->
				run{
					// FIXME not being called?
					Log.d("$LOG_TAG.buildSuSession",
							"Shell start status: $exitCode")
					if (exitCode
							!= Shell.OnCommandResultListener.SHELL_RUNNING)
					{
						Log.e("$LOG_TAG.init",
								"Failed opening root shell (exitCode: $exitCode)")
						if (_appContext != null)
						{
							Toast.makeText(_appContext, R.string.su_failed,
									Toast.LENGTH_LONG).show()
						}
					}
					_isSuStarting = false
				}})
	}

	private fun setContext(context: Context)
	{
		if (_appContext == null)
		{
			_appContext = context.applicationContext
		}
	}

	private val LOG_TAG = SuHelper::class.java.canonicalName

	private val _handler = Handler()
	private var _appContext: Context? = null

	private var _su: Shell.Interactive = buildSuSession()
		get()
		{
			if (!field.isRunning && !_isSuStarting)
			{
				field = buildSuSession()
			}
			return field
		}

	private var _isSuStarting: Boolean = false
}
