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
import android.os.Build;

import java.lang.reflect.Field;

public class InstallHelper
{
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
}
