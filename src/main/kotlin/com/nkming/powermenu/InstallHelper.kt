package com.nkming.powermenu

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import com.nkming.utils.str.StrUtils
import eu.chainfire.libsuperuser.Shell
import java.util.*

object InstallHelper
{
	/**
	 * An AsyncTask that work with the SU shell to install this app to /system
	 */
	open class InstallTask : AsyncTask<Context, Void, Boolean>()
	{
		override fun doInBackground(vararg params: Context?): Boolean
		{
			if (params.size == 0)
			{
				Log.e("$LOG_TAG.InstallTask", "Missing argument")
				return false
			}
			val context = params[0]!!

			val scripts = arrayOf(
					"mount -o remount,rw /system",
					"cat ${getPackagePath(context)} > ${getInstallPath()}",
					"error=$?",
					"chmod 644 ${getInstallPath()}",
					"mount -o remount,ro /system",
					"if [ \$error = \"0\" ]; then",
					"  echo \"good:)\"",
					"  reboot",
					"fi")
			Log.i(LOG_TAG, "Run:\n${StrUtils.Implode("\n", Arrays.asList(scripts))}")
			val out = Shell.run("su", scripts, null, true)
			return if (out == null || out.isEmpty() || out[0] != "good:)")
			{
				Log.e("$LOG_TAG.InstallTask", "su failed:\n"
						+ (if (out == null) "null" else StrUtils.Implode("\n",
								out)))
				false
			}
			else
			{
				true
			}
		}
	}

	/**
	 * An AsyncTask that work with the SU shell to uninstall this app from
	 * /system
	 */
	open class UninstallTask : AsyncTask<Context, Void, Boolean>()
	{
		override fun doInBackground(vararg params: Context?): Boolean
		{
			if (params.size == 0)
			{
				Log.e("$LOG_TAG.UninstallTask", "Missing argument")
				return false
			}
			val context = params[0]!!

			val scripts = arrayOf(
					"mount -o remount,rw /system",
					"rm ${getInstallPath()}",
					"error=$?",
					"mount -o remount,ro /system",
					"if [ \$error = \"0\" ]; then",
					"  echo \"good:)\"",
					"  reboot",
					"fi")
			Log.i(LOG_TAG, "Run:\n${StrUtils.Implode("\n", Arrays.asList(scripts))}")
			val out = Shell.run("su", scripts, null, true)
			return if (out == null || out.isEmpty() || out[0] != "good:)")
			{
				Log.e("$LOG_TAG.UninstallTask", "su failed:\n"
						+ (if (out == null) "null" else StrUtils.Implode("\n",
								out)))
				false
			}
			else
			{
				true
			}
		}
	}

	@JvmStatic
	fun isPowerCommandAvailable(context: Context,
			onResult: (isAvailable: Boolean) -> Unit)
	{
		if (_isPowerCommandAvailable != null)
		{
			onResult(_isPowerCommandAvailable!!)
			return
		}

		val scripts = listOf("svc power")
		SuHelper.doSuCommand(context, scripts,
				successWhere = {exitCode, output -> true},
				onSuccess = {exitCode, output ->
				run{
					var isRebootAvailable = false
					var isShutdownAvailable = false
					for (o in output)
					{
						if (o.contains("svc power reboot"))
						{
							isRebootAvailable = true
						}
						else if (o.contains("svc power shutdown"))
						{
							isShutdownAvailable = true
						}
					}
					_isPowerCommandAvailable = (isRebootAvailable
							&& isShutdownAvailable)
					onResult(_isPowerCommandAvailable!!)
				}},
				onFailure = {exitCode, output ->
				run{
					_isPowerCommandAvailable = false
					onResult(false)
				}})
	}

	/**
	 * Return if this app is installed as privileged (KK+) system app
	 *
	 * @param context
	 * @return
	 */
	@JvmStatic
	fun isSystemApp(context: Context): Boolean
	{
		try
		{
			val pm = context.packageManager
			val app = pm.getApplicationInfo(Res.PACKAGE, 0)
			val isSystem = ((app.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
			return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			{
				(isSystem && isPrivilegedM(app))
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			{
				(isSystem && isPrivileged(app))
			}
			else
			{
				isSystem
			}
		}
		catch (e: Exception)
		{
			Log.e("$LOG_TAG.isSystemApp", "Error while getApplicationInfo", e)
			return false
		}
	}

	private val LOG_TAG = InstallHelper::class.java.simpleName

	/**
	 * Return whether privileged flag is set in @a appFlags. since this involve
	 * reflection, a separate method is better
	 *
	 * @param app
	 * @return
	 */
	private fun isPrivileged(app: ApplicationInfo): Boolean
	{
		try
		{
			val fieldFLAG_PRIVILEGED = ApplicationInfo::class.java
					.getDeclaredField("FLAG_PRIVILEGED")
			fieldFLAG_PRIVILEGED.isAccessible = true
			val FLAG_PRIVILEGED = fieldFLAG_PRIVILEGED.getInt(null)
			return ((app.flags and FLAG_PRIVILEGED) != 0)
		}
		catch (e: Exception)
		{
			Log.e("$LOG_TAG.isPrivileged", "Error while reflection", e)
			return true
		}
	}

	/**
	 * isPrivileged() that is compatible with M+
	 *
	 * @param app
	 * @see isPrivileged()
	 * @return
	 */
	private fun isPrivilegedM(app: ApplicationInfo): Boolean
	{
		try
		{
			val fieldPRIVATE_FLAG_PRIVILEGED = ApplicationInfo::class.java
					.getDeclaredField("PRIVATE_FLAG_PRIVILEGED")
			fieldPRIVATE_FLAG_PRIVILEGED.isAccessible = true
			val PRIVATE_FLAG_PRIVILEGED = fieldPRIVATE_FLAG_PRIVILEGED.getInt(
					null)

			val fieldPrivateFlags = ApplicationInfo::class.java.getDeclaredField(
					"privateFlags")
			fieldPrivateFlags.isAccessible = true
			val privateFlags = fieldPrivateFlags.getInt(app)
			return ((privateFlags and PRIVATE_FLAG_PRIVILEGED) != 0)
		}
		catch (e: Exception)
		{
			Log.e("$LOG_TAG.isPrivileged", "Error while reflection", e)
			return true
		}
	}

	/**
	 * Return the path of this app's apk
	 *
	 * @param context
	 * @return The path, or null if failure
	 */
	private fun getPackagePath(context: Context): String?
	{
		val pm = context.packageManager
		return try
		{
			val app = pm.getApplicationInfo(Res.PACKAGE, 0)
			app.sourceDir
		}
		catch (e: PackageManager.NameNotFoundException)
		{
			Log.e("$LOG_TAG.getPackagePath", "Failed while getApplicationInfo",
					e)
			null
		}
	}

	/**
	 * Return the target install path in /system
	 *
	 * @return
	 */
	private fun getInstallPath(): String
	{
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			"/system/priv-app/" + Res.PACKAGE + ".apk"
		}
		else
		{
			"/system/app/" + Res.PACKAGE + ".apk"
		}
	}

	// Shell command is expansive, cache the result
	private var _isPowerCommandAvailable: Boolean? = null
}
