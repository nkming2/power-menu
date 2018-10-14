package com.nkming.powermenu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * This class is a proxy class such that the launcher icon could be disabled
 */
class LauncherActivity : AppCompatActivity()
{
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}
}
