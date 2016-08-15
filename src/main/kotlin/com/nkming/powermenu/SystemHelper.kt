package com.nkming.powermenu

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import android.text.format.DateFormat
import android.widget.Toast
import eu.chainfire.libsuperuser.Shell
import java.io.File

object SystemHelper
{
	enum class RebootMode
	{
		NORMAL,
		RECOVERY,
		BOOTLOADER
	}

	@JvmStatic
	fun shutdown(context: Context, l: (isSuccessful: Boolean) -> Unit)
	{
		setContext(context)
		return try
		{
			val fieldACTION_REQUEST_SHUTDOWN = Intent::class.java.getDeclaredField(
					"ACTION_REQUEST_SHUTDOWN")
			fieldACTION_REQUEST_SHUTDOWN.isAccessible = true
			val ACTION_REQUEST_SHUTDOWN = fieldACTION_REQUEST_SHUTDOWN.get(null)
					as String

			val fieldEXTRA_KEY_CONFIRM = Intent::class.java.getDeclaredField(
					"EXTRA_KEY_CONFIRM")
			fieldACTION_REQUEST_SHUTDOWN.isAccessible = true
			val EXTRA_KEY_CONFIRM = fieldEXTRA_KEY_CONFIRM.get(null) as String

			val shutdown = Intent(ACTION_REQUEST_SHUTDOWN)
			shutdown.putExtra(EXTRA_KEY_CONFIRM, false)
			shutdown.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK
					or Intent.FLAG_ACTIVITY_NEW_TASK)
			context.startActivity(shutdown)
			l(true)
		}
		catch (e: Exception)
		{
			Log.e("$LOG_TAG.shutdown", "Error while reflection", e)
			l(false)
		}
	}

	/**
	 * Put the device to sleep. The operation runs in a separate thread
	 *
	 * @param context
	 * @param l Listener that get called when the operation is finished
	 */
	@JvmStatic
	fun sleep(context: Context, l: (isSuccessful: Boolean) -> Unit)
	{
		setContext(context)
		val scripts = listOf("input keyevent 26")
		doSuCommand(scripts,
				successWhere = {exitCode, output ->
						(exitCode == 0 && output.isEmpty())},
				onSuccess = {exitCode, output -> l(true)},
				onFailure = {exitCode, output -> l(false)})
	}

	@JvmStatic
	fun reboot(mode: RebootMode, context: Context,
			l: (isSuccessful: Boolean) -> Unit)
	{
		setContext(context)
		try
		{
			val pm = context.getSystemService(Context.POWER_SERVICE)
					as PowerManager
			when (mode)
			{
				RebootMode.NORMAL ->
				{
					pm.reboot(null)
					l(false)
				}

				RebootMode.RECOVERY ->
				{
					pm.reboot("recovery")
					l(false)
				}

				RebootMode.BOOTLOADER ->
				{
					pm.reboot("bootloader")
					l(false)
				}

				else ->
				{
					Log.e("$LOG_TAG.reboot", "Unknown mode")
					l(true)
				}
			}
		}
		catch (e: Exception)
		{
			Log.e("$LOG_TAG.reboot", "Error while invoking reboot", e)
			l(true)
		}
	}

	@JvmStatic
	fun screenshot(context: Context,
			l: (isSuccessful: Boolean, filepath: String) -> Unit)
	{
		setContext(context)
		val filename = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		{
			"Screenshot_${DateFormat.format("yyyy-MM-dd-kk-mm-ss",
					java.util.Date())}.png"
		}
		else
		{
			"Screenshot_${DateFormat.format("yyyyMMdd-kkmmss",
					java.util.Date())}.png"
		}

		val scripts = listOf(
				"save_dir=\${EXTERNAL_STORAGE}/Pictures/Screenshots",
				"mkdir -p \${save_dir}",
				"/system/bin/screencap -p \${save_dir}/$filename",
				"echo \":)\"",
				"echo \${save_dir}/$filename")
		doSuCommand(scripts,
				successWhere = {exitCode, output ->
						(exitCode == 0 && output.isNotEmpty()
								&& output[0] == ":)")},
				onSuccess = {exitCode, output ->
				run{
					val filepath = output[1]
					l(File(filepath).exists(), filepath)
				}},
				onFailure = {exitCode, output -> l(false, "")})
	}

	@JvmStatic
	fun killZygote(context: Context, l: (isSuccessful: Boolean) -> Unit)
	{
		setContext(context)
		val scripts = listOf("busybox killall zygote")
		doSuCommand(scripts,
				successWhere = {exitCode, output ->
						(exitCode == 0 && output.isEmpty())},
				onSuccess = {exitCode, output -> l(true)},
				onFailure = {exitCode, output -> l(false)})
	}

	/**
	 * Start an Activity specified by @a clz
	 *
	 * @param context
	 * @param clz
	 * @param l Listener that get called when the operation is finished
	 */
	@JvmStatic
	fun startActivity(context: Context, clz: Class<*>,
			l: (isSuccessful: Boolean) -> Unit)
	{
		setContext(context)
		val scripts = listOf(
				"am start -n ${clz.`package`.name}/${clz.canonicalName}")
		doSuCommand(scripts,
				// There's no obvious way to distinguish error
				successWhere = {exitCode, output ->
						(!output.any{it.contains("error", ignoreCase = true)})},
				onSuccess = {exitCode, output -> l(true)},
				onFailure = {exitCode, output -> l(false)})
	}

	/**
	 * Enable/disable an Activity. As a side effect, to show/hide an activity
	 * from launcher
	 *
	 * @param context
	 * @param activityClz
	 * @param isEnable
	 */
	@JvmStatic
	fun setEnableActivity(context: Context, activityClz: Class<*>,
			isEnable: Boolean)
	{
		setContext(context)
		val pm = context.packageManager
		val com = ComponentName(context, activityClz)
		val newState = if (isEnable)
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED
		pm.setComponentEnabledSetting(com, newState,
				PackageManager.DONT_KILL_APP)
	}

	@JvmStatic
	fun isBusyboxPresent(): Boolean
	{
		val scripts = arrayOf(
				"busybox",
				"echo $?")
		val out = Shell.run("sh", scripts, null, true)
		return out != null && out.isNotEmpty() && out.last() == "0"
	}

	private fun doSuCommand(scripts: List<String>,
			successWhere: ((exitCode: Int, output: List<String>) -> Boolean) = {
					exitCode, output -> exitCode >= 0},
			onSuccess: ((exitCode: Int, output: List<String>) -> Unit)? = null,
			onFailure: ((exitCode: Int, output: List<String>) -> Unit)? = null)
	{
		if (!_su.isRunning)
		{
			_handler.postDelayed({doSuCommand(scripts, successWhere, onSuccess,
					onFailure)}, 200)
		}
		else
		{
			_doSuCommand(scripts, successWhere, onSuccess, onFailure)
		}
	}

	private fun _doSuCommand(scripts: List<String>,
			successWhere: ((exitCode: Int, output: List<String>) -> Boolean) = {
				exitCode, output -> exitCode >= 0},
			onSuccess: ((exitCode: Int, output: List<String>) -> Unit)? = null,
			onFailure: ((exitCode: Int, output: List<String>) -> Unit)? = null)
	{
		_su.addCommand(scripts, 0, {commandCode, exitCode, output ->
			run{
				if (exitCode == Shell.OnCommandResultListener.WATCHDOG_EXIT)
				{
					Log.e("$LOG_TAG._doSuCommand", "Watchdog exception")
					_su.kill()
					_su = buildSuSession()
					_handler.postDelayed({doSuCommand(scripts, successWhere,
							onSuccess, onFailure)}, 200)
				}
				else if (!successWhere(exitCode, output))
				{
					Log.e("$LOG_TAG._doSuCommand",
							"Failed($exitCode) executing\nCommand: ${scripts.joinToString("\n")}\nOutput: ${output.joinToString("\n")}")
					onFailure?.invoke(exitCode, output)
				}
				else
				{
					onSuccess?.invoke(exitCode, output)
				}
			}
		})
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

	private val LOG_TAG = SystemHelper::class.java.canonicalName

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
