/*
 * SystemHelper.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;

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

	public static interface SleepResultListener
	{
		public void onSleepResult(boolean isSuccessful);
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
	public static boolean sleep(Context context, final SleepResultListener l)
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
				List<String> out = Shell.SU.run(scripts);
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
					l.onSleepResult(result);
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

	private static final String LOG_TAG = Res.LOG_TAG + "."
			+ SystemHelper.class.getSimpleName();
}
