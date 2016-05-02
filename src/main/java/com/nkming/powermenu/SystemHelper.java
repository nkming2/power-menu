/*
 * SystemHelper.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.text.format.DateFormat;

import com.nkming.utils.str.StrUtils;

import java.lang.reflect.Field;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class SystemHelper
{
	public static enum RebootMode
	{
		NORMAL,
		RECOVERY,
		BOOTLOADER
	}

	public static interface SuResultListener
	{
		public void onSuResult(boolean isSuccessful);
	}

	public static interface ScreenshotResultListener
	{
		public void onScreenshotResult(boolean isSuccessful, String filepath);
	}

	public static interface StartActivityResultListener
	{
		public void onStartActivityResult(boolean isSuccessful);
	}

	public static boolean shutdown(Context context)
	{
		try
		{
			Field fieldACTION_REQUEST_SHUTDOWN = Intent.class.getDeclaredField(
					"ACTION_REQUEST_SHUTDOWN");
			fieldACTION_REQUEST_SHUTDOWN.setAccessible(true);
			String ACTION_REQUEST_SHUTDOWN =
					(String)fieldACTION_REQUEST_SHUTDOWN.get(null);

			Field fieldEXTRA_KEY_CONFIRM = Intent.class.getDeclaredField(
					"EXTRA_KEY_CONFIRM");
			fieldACTION_REQUEST_SHUTDOWN.setAccessible(true);
			String EXTRA_KEY_CONFIRM = (String)fieldEXTRA_KEY_CONFIRM.get(null);

			Intent shutdown = new Intent(ACTION_REQUEST_SHUTDOWN);
			shutdown.putExtra(EXTRA_KEY_CONFIRM, false);
			shutdown.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(shutdown);
			return true;
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG + ".shutdown", "Error while reflection", e);
			return false;
		}
	}

	/**
	 * Put the device to sleep. The operation runs in a separate thread
	 *
	 * @param context
	 * @param l Listener that get called when the operation is finished
	 * @return Always true
	 */
	public static boolean sleep(Context context, final SuResultListener l)
	{
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>()
		{
			@Override
			protected Boolean doInBackground(Void... params)
			{
				// KEYCODE_POWER == 26
				String scripts[] = new String[]
						{
							"input keyevent 26",
							"echo \"good:)\""
						};
				List<String> out = Shell.run("su", scripts, (String[])null, true);
				if (out == null || out.isEmpty() || !out.get(0).equals("good:)"))
				{
					Log.e(LOG_TAG + ".sleep", "su failed:\n"
							+ ((out == null) ? "null"
									: StrUtils.Implode("\n", out)));
					return false;
				}
				else
				{
					return true;
				}
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (l != null)
				{
					l.onSuResult(result);
				}
			}
		};
		task.execute();
		return true;
	}

	public static boolean reboot(RebootMode mode, Context context)
	{
		try
		{
			PowerManager pm = (PowerManager)context.getSystemService(
					Context.POWER_SERVICE);
			switch (mode)
			{
			default:
				Log.e(LOG_TAG + ".reboot", "Unknown mode");
				return false;

			case NORMAL:
				pm.reboot(null);
				return true;

			case RECOVERY:
				pm.reboot("recovery");
				return true;

			case BOOTLOADER:
				pm.reboot("bootloader");
				return true;
			}
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG + ".reboot", "Error while invoking reboot",
					e);
			return false;
		}
	}

	public static boolean screenshot(Context context,
			final ScreenshotResultListener l)
	{
		String filename_;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		{
			filename_ = "Screenshot_" + DateFormat.format(
					"yyyy-MM-dd-kk-mm-ss", new java.util.Date()) + ".png";
		}
		else
		{
			filename_ = "Screenshot_" + DateFormat.format(
					"yyyyMMdd-kkmmss", new java.util.Date()) + ".png";
		}
		final String filename = filename_;

		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>()
		{
			@Override
			protected Boolean doInBackground(Void... params)
			{
				String scripts[] = new String[]
						{
							"save_dir=${EXTERNAL_STORAGE}/Pictures/Screenshots",
							"mkdir -p ${save_dir}",
							"/system/bin/screencap -p ${save_dir}/" + filename,
							"echo \"good:)\"",
							"echo ${save_dir}/" + filename
						};
				List<String> out = Shell.run("su", scripts, (String[])null, true);
				if (out == null || out.isEmpty() || !out.get(0).equals("good:)")
						|| out.size() != 2)
				{
					Log.e(LOG_TAG + ".screenshot", "su failed:\n"
							+ ((out == null) ? "null"
									: StrUtils.Implode("\n", out)));
					return false;
				}
				else
				{
					mFilepath = out.get(1);
					return true;
				}
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (l != null)
				{
					l.onScreenshotResult(result, mFilepath);
				}
			}

			private String mFilepath;
		};
		task.execute();
		return true;
	}

	public static boolean killZygote(Context context,
			final SuResultListener l)
	{
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>()
		{
			@Override
			protected Boolean doInBackground(Void... params)
			{
				String scripts[] = new String[]
						{
								"busybox killall zygote",
								"echo \"good:)\""
						};
				List<String> out = Shell.run("su", scripts, (String[])null, true);
				if (out == null || out.isEmpty() || !out.get(0).equals("good:)"))
				{
					Log.e(LOG_TAG + ".killZygote", "su failed:\n"
							+ ((out == null) ? "null"
							: StrUtils.Implode("\n", out)));
					return false;
				}
				else
				{
					return true;
				}
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (l != null)
				{
					l.onSuResult(result);
				}
			}
		};
		task.execute();
		return true;
	}

	/**
	 * Start an Activity specified by @a clz
	 *
	 * @param clz
	 * @param l Listener that get called when the operation is finished
	 */
	public static void startActivity(final Class clz,
			final StartActivityResultListener l)
	{
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>()
		{
			@Override
			protected Boolean doInBackground(Void... params)
			{
				// am always return 0 even if the op has failed :/
				String scripts[] = new String[]
						{
							"am start -n " + clz.getPackage().getName() + "/"
									+ clz.getCanonicalName(),
							"echo \"good:)\""
						};
				List<String> out = Shell.run("su", scripts, (String[])null, true);
				if (out == null || out.isEmpty())
				{
					Log.e(LOG_TAG + ".startActivity", "su failed:\n"
							+ ((out == null) ? "null"
									: StrUtils.Implode("\n", out)));
					return false;
				}
				else
				{
					return true;
				}
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (l != null)
				{
					l.onStartActivityResult(result);
				}
			}
		};
		task.execute();
	}

	/**
	 * Enable/disable an Activity. As a side effect, to show/hide an activity
	 * from launcher
	 *
	 * @param context
	 * @param activityClz
	 * @param isEnable
	 */
	public static void setEnableActivity(Context context, Class activityClz,
			boolean isEnable)
	{
		PackageManager pm = context.getPackageManager();
		ComponentName com = new ComponentName(context, activityClz);
		int newState = isEnable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		pm.setComponentEnabledSetting(com, newState,
				PackageManager.DONT_KILL_APP);
	}

	public static boolean isBusyboxPresent()
	{
		String scripts[] = new String[]
				{
					"busybox",
					"echo $?"
				};
		List<String> out = Shell.run("sh", scripts, (String[])null, true);
		if (out == null || out.isEmpty() || !out.get(out.size() - 1).equals("0"))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ SystemHelper.class.getSimpleName();
}
