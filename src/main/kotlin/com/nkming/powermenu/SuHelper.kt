package com.nkming.powermenu

import android.content.Context
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
		requestSuSession(onFailure).addCommand(scripts, 0, {
				commandCode, exitCode, output ->
				run{
					val output_ = output ?: listOf()
					if (exitCode == Shell.OnCommandResultListener.WATCHDOG_EXIT)
					{
						Log.e("$LOG_TAG._doSuCommand", "Watchdog exception")
						// Script deadlock?
						_su?.kill()
						onFailure?.invoke(exitCode, output_)
					}
					else if (!successWhere(exitCode, output_))
					{
						Log.e("$LOG_TAG.__doSuCommand",
								"Failed($exitCode) executing\nCommand: ${scripts.joinToString("\n")}\nOutput: ${output_.joinToString("\n")}")
						onFailure?.invoke(exitCode, output_)
					}
					else
					{
						onSuccess?.invoke(exitCode, output_)
					}
				}})
	}

	private fun requestSuSession(
			onFailure: ((exitCode: Int, output: List<String>) -> Unit)? = null)
			: Shell.Interactive
	{
		synchronized(this)
		{
			Log.d(LOG_TAG, "buildSuSession()")
			if (_isSuStarting || (_su?.isRunning ?: false))
			{
				return _su!!
			}

			_isSuStarting = true
			val su = Shell.Builder()
					.useSU()
					//.setWantSTDERR(true)
					.setWatchdogTimeout(5)
					.setMinimalLogging(true)
					.open({commandCode, exitCode, output ->
					run{
						Log.d("$LOG_TAG.buildSuSession",
								"Shell start status: $exitCode")
						if (exitCode
								!= Shell.OnCommandResultListener.SHELL_RUNNING)
						{
							Log.e("$LOG_TAG.buildSuSession",
									"Failed opening root shell (exitCode: $exitCode)")
							if (_appContext != null)
							{
								Toast.makeText(_appContext, R.string.su_failed,
										Toast.LENGTH_LONG).show()
							}
							onFailure?.invoke(exitCode, output ?: listOf())
						}
						else
						{
							Log.i("$LOG_TAG.buildSuSession", "Successful")
						}
						_isSuStarting = false
					}})
			_su = su
			return su
		}
	}

	private fun setContext(context: Context)
	{
		if (_appContext == null)
		{
			_appContext = context.applicationContext
		}
	}

	private val LOG_TAG = SuHelper::class.java.canonicalName

	private var _appContext: Context? = null

	private var _su: Shell.Interactive? = null
	private var _isSuStarting: Boolean = false
}
