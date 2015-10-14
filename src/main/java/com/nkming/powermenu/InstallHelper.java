/*
 * InstallHelper.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;

import com.nkming.utils.str.StrUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class InstallHelper
{
	/**
	 * An AsyncTask that work with the SU shell to install this app to /system
	 */
	public static class InstallTask extends AsyncTask<Context, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Context... params)
		{
			if (params.length == 0)
			{
				Log.e(LOG_TAG + ".InstallTask", "Missing argument");
				return false;
			}
			Context context = params[0];

			String scripts[] = new String[]
					{
						"mount -o remount,rw /system",
						"cp " + getPackagePath(context) + " " + getInstallPath(),
						"error=$?",
						"chmod 644 " + getInstallPath(),
						"mount -o remount,ro /system",
						"if [ $error = \"0\" ]; then",
						"  echo \"good:)\"",
						"  reboot",
						"fi"
					};
			Log.i(LOG_TAG , "Run:\n" + StrUtils.Implode("\n",
					Arrays.asList(scripts)));
			List<String> out = Shell.run("su", scripts, (String[])null, true);
			if (out == null || out.isEmpty() || !out.get(0).equals("good:)"))
			{
				Log.e(LOG_TAG + ".InstallTask", "su failed:\n"
						+ ((out == null) ? "null" : StrUtils.Implode("\n", out)));
				return false;
			}
			else
			{
				return true;
			}
		}

	}

	/**
	 * An AsyncTask that work with the SU shell to uninstall this app from /system
	 */
	public static class UninstallTask extends AsyncTask<Context, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Context... params)
		{
			if (params.length == 0)
			{
				Log.e(LOG_TAG + ".UninstallTask", "Missing argument");
				return false;
			}
			Context context = params[0];

			String scripts[] = new String[]
					{
							"mount -o remount,rw /system",
							"rm " + getInstallPath(),
							"error=$?",
							"mount -o remount,ro /system",
							"if [ $error = \"0\" ]; then",
							"  echo \"good:)\"",
							"  reboot",
							"fi"
					};
			Log.i(LOG_TAG , "Run:\n" + StrUtils.Implode("\n",
					Arrays.asList(scripts)));
			List<String> out = Shell.run("su", scripts, (String[])null, true);
			if (out == null || out.isEmpty() || !out.get(0).equals("good:)"))
			{
				Log.e(LOG_TAG + ".UninstallTask", "su failed:\n"
						+ ((out == null) ? "null" : StrUtils.Implode("\n", out)));
				return false;
			}
			else
			{
				return true;
			}
		}

	}

	/**
	 * Return if this app is installed as privileged (KK+) system app
	 *
	 * @param context
	 * @return
	 */
	public static boolean isSystemApp(Context context)
	{
		try
		{
			PackageManager pm = context.getPackageManager();
			ApplicationInfo app = pm.getApplicationInfo(Res.PACKAGE, 0);
			boolean isSystem = ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			{
				return (isSystem && isPrivileged(app.flags));
			}
			else
			{
				return isSystem;
			}
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG + ".isSystemApp", "Error while getApplicationInfo", e);
			return false;
		}
	}

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ InstallHelper.class.getSimpleName();

	/**
	 * Return whether privileged flag is set in @a appFlags. since this involve
	 * reflection, a separate method is better
	 *
	 * @param appFlags
	 * @return
	 */
	private static boolean isPrivileged(int appFlags)
	{
		try
		{
			Field fieldFLAG_PRIVILEGED = ApplicationInfo.class.getDeclaredField(
					"FLAG_PRIVILEGED");
			fieldFLAG_PRIVILEGED.setAccessible(true);
			int FLAG_PRIVILEGED = fieldFLAG_PRIVILEGED.getInt(null);
			return ((appFlags & FLAG_PRIVILEGED) != 0);
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG + ".isPrivileged", "Error while reflection", e);
			return true;
		}
	}

	/**
	 * Return the path of this app's apk
	 *
	 * @param context
	 * @return The path, or null if failure
	 */
	private static String getPackagePath(Context context)
	{
		PackageManager pm = context.getPackageManager();
		ApplicationInfo app;
		try
		{
			app = pm.getApplicationInfo(Res.PACKAGE, 0);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			Log.e(LOG_TAG + ".getPackagePath", "Failed while getApplicationInfo",
					e);
			return null;
		}
		return app.sourceDir;
	}

	/**
	 * Return the target install path in /system
	 *
	 * @return
	 */
	private static String getInstallPath()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			return "/system/priv-app/" + Res.PACKAGE + ".apk";
		}
		else
		{
			return "/system/" + Res.PACKAGE + ".apk";
		}
	}
}
