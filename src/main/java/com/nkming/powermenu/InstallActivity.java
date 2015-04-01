/*
 * InstallActivity.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class InstallActivity extends ActionBarActivity
		implements  InstallConfirmFragment.Listener
{
	@Override
	public void onInstallConfirmed()
	{
		InstallFragment f = InstallFragment.create();
		f.show(getSupportFragmentManager(), "install");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getIntent().getDataString() != null
				&& getIntent().getDataString().equals(getString(R.string.data_uninstall)))
		{
			onCreateUninstall(savedInstanceState);
		}
		else
		{
			onCreateInstall(savedInstanceState);
		}
	}

	private void onCreateInstall(Bundle savedInstanceState)
	{
		if (savedInstanceState == null)
		{
			InstallConfirmFragment f = InstallConfirmFragment.create();
			f.show(getSupportFragmentManager(), "install_confirm");
		}
	}

	private void onCreateUninstall(Bundle savedInstanceState)
	{
		if (savedInstanceState == null)
		{
			UninstallFragment f = UninstallFragment.create();
			f.show(getSupportFragmentManager(), "uninstall");
		}
	}
}
