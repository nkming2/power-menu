package com.nkming.powermenu

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.text.format.DateFormat
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

	enum class ScreenshotError
	{
		NO_ERROR,
		SCREENCAP_FAILURE,
		FILE_FAILURE,
	}

	@JvmStatic
	fun shutdown(context: Context, l: (isSuccessful: Boolean) -> Unit)
	{
		if (InstallHelper.isSystemApp(context))
		{
			shutdownSystem(context, l)
		}
		else
		{
			shutdownSvc(context, l)
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
		val scripts = listOf("input keyevent 26")
		SuHelper.doSuCommand(context, scripts,
				successWhere = {exitCode, output ->
						(exitCode == 0 && output.isEmpty())},
				onSuccess = {exitCode, output -> l(true)},
				onFailure = {exitCode, output -> l(false)})
	}

	@JvmStatic
	fun reboot(mode: RebootMode, context: Context,
			l: (isSuccessful: Boolean) -> Unit)
	{
		if (InstallHelper.isSystemApp(context))
		{
			rebootSystem(mode, context, l)
		}
		else
		{
			rebootSvc(mode, context, l)
		}
	}

	@JvmStatic
	fun screenshot(context: Context,
			l: (error: ScreenshotError, filepath: String) -> Unit)
	{
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
				"echo $?",
				"echo \${save_dir}/$filename")
		SuHelper.doSuCommand(context, scripts,
				successWhere = {exitCode, output ->
						(exitCode == 0 && output.isNotEmpty()
								&& output[0] == "0")},
				onSuccess = {exitCode, output ->
				run{
					val filepath = output[1]
					if (!File(filepath).exists())
					{
						l(ScreenshotError.FILE_FAILURE, filepath)
					}
					else
					{
						l(ScreenshotError.NO_ERROR, filepath)
					}
				}},
				onFailure = {exitCode, output ->
				run{
					// screencap failed
					try
					{
						// Remove the 0B file left behind
						File(output[1]).delete()
					}
					catch (e: Exception)
					{}
					l(ScreenshotError.SCREENCAP_FAILURE, "")
				}})
	}

	@JvmStatic
	fun killZygote(context: Context, l: (isSuccessful: Boolean) -> Unit)
	{
		val scripts = listOf("busybox killall zygote")
		SuHelper.doSuCommand(context, scripts,
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
		val scripts = listOf(
				"am start -n ${clz.`package`.name}/${clz.canonicalName}")
		SuHelper.doSuCommand(context, scripts,
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

	private val LOG_TAG = SystemHelper::class.java.canonicalName

	@JvmStatic
	private fun shutdownSystem(context: Context,
			l: (isSuccessful: Boolean) -> Unit)
	{
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

	@JvmStatic
	private fun shutdownSvc(context: Context, l: (isSuccessful: Boolean) -> Unit)
	{
		// See https://android.googlesource.com/platform/frameworks/base/+/master/cmds/svc/src/com/android/commands/svc/PowerCommand.java
		val scripts = listOf("svc power shutdown")
		SuHelper.doSuCommand(context, scripts,
				successWhere = {exitCode, output -> output.isEmpty()},
				onSuccess = {exitCode, output -> l(true)},
				onFailure = {exitCode, output -> l(false)})
	}

	@JvmStatic
	fun rebootSystem(mode: RebootMode, context: Context,
			l: (isSuccessful: Boolean) -> Unit)
	{
		try
		{
			val pm = context.getSystemService(Context.POWER_SERVICE)
					as PowerManager
			when (mode)
			{
				RebootMode.NORMAL ->
				{
					pm.reboot(null)
					l(true)
				}

				RebootMode.RECOVERY ->
				{
					pm.reboot("recovery")
					l(true)
				}

				RebootMode.BOOTLOADER ->
				{
					pm.reboot("bootloader")
					l(true)
				}

				else ->
				{
					Log.e("$LOG_TAG.reboot", "Unknown mode")
					l(false)
				}
			}
		}
		catch (e: Exception)
		{
			Log.e("$LOG_TAG.reboot", "Error while invoking reboot", e)
			l(false)
		}
	}

	@JvmStatic
	private fun rebootSvc(mode: RebootMode, context: Context,
			l: (isSuccessful: Boolean) -> Unit)
	{
		val modeStr = when (mode)
		{
			RebootMode.NORMAL -> ""
			RebootMode.RECOVERY -> "recovery"
			RebootMode.BOOTLOADER -> "bootloader"
			else ->
			{
				Log.e("$LOG_TAG.reboot", "Unknown mode")
				l(false)
				return
			}
		}

		val scripts = listOf("svc power reboot $modeStr")
		SuHelper.doSuCommand(context, scripts,
				successWhere = {exitCode, output -> output.isEmpty()},
				onSuccess = {exitCode, output -> l(true)},
				onFailure = {exitCode, output -> l(false)})
	}
}
