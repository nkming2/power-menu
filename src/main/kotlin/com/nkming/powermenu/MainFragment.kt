package com.nkming.powermenu

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import com.nkming.utils.graphic.BitmapLoader
import com.nkming.utils.graphic.DrawableUtils
import com.nkming.utils.graphic.FillSizeCalc
import com.nkming.utils.type.Size
import com.nkming.utils.unit.DimensionUtils
import com.shamanland.fab.FloatingActionButton
import java.io.File

class MainFragment : Fragment()
{
	companion object
	{
		private val LOG_TAG = MainFragment::class.java.canonicalName

		const val NOTIFICATION_SCREENSHOT = 1

		private const val ACTION_ON_DESTROY = "${Res.PACKAGE}.ACTION_ON_DESTROY"
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
			savedInstanceState: Bundle?): View
	{
		_root = inflater.inflate(R.layout.frag_main, container, false)
		_initRoot()
		_initButton()
		return _root
	}

	override fun onDestroy()
	{
		super.onDestroy()
		LocalBroadcastManager.getInstance(_appContext)
				.sendBroadcast(Intent(ACTION_ON_DESTROY))
	}

	private data class ActionButtonMeta
	(
		val btn: FloatingActionButton,
		val bound: View,
		val onClick: () -> Unit
	)

	private data class RestartButtonMeta
	(
		val btn: FloatingActionButton,
		val bound: View,
		val label: View,
		val onClick: () -> Unit
	)

	private fun _initRoot()
	{
		_root.setOnClickListener(
		{
			activity.finish()
		})
	}

	private fun _initButton()
	{
		for ((i, btn) in _actionBtns.withIndex())
		{
			btn.bound.setOnClickListener(
			{
				btn.onClick()
			})
			btn.bound.scaleX = 0f
			btn.bound.scaleY = 0f
			btn.bound.animate().scaleX(1f).scaleY(1f)
					.setInterpolator(DecelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST.toLong())
					.setStartDelay(Res.ANIMATION_FAST.toLong() / 2 * i)
		}
	}

	private fun _initRestartBtns()
	{
		for (b in _restartMenuBtns)
		{
			b.bound.setOnClickListener(
			{
				b.onClick()
			})
		}

		val pref = activity.getSharedPreferences(getString(R.string.pref_file),
				Context.MODE_PRIVATE)
		if (pref.getBoolean(getString(R.string.pref_soft_reboot_key), false)
				&& SystemHelper.isBusyboxPresent())
		{
			_restartNormalBtn.bound.isHapticFeedbackEnabled =
					pref.getBoolean(getString(R.string.pref_haptic_key), true)
			_restartNormalBtn.bound.setOnLongClickListener(
			{
				_onRestartNormalLongClick()
				true
			})
		}
	}

	private fun _onShutdownClick()
	{
		_startReveal(_shutdownBtn.btn, R.color.shutdown_bg, true,
		{
			// App probably closed
			if (activity == null)
			{
				return@_startReveal
			}
			if (!SystemHelper.shutdown(activity))
			{
				Toast.makeText(activity, R.string.shutdown_fail,
						Toast.LENGTH_LONG).show()
				activity.finish()
			}
		})

		_shutdownBtn.btn.isShadow = false
		_dismissOtherBounds(_shutdownBtn)
		// Disable all click bounds
		_disableOtherBounds(null as ActionButtonMeta?)
	}

	private fun _onSleepClick()
	{
		_startReveal(_sleepBtn.btn, R.color.sleep_bg, true, null)

		// Finish activity on screen off
		val receiver = object: BroadcastReceiver()
		{
			override fun onReceive(context: Context?, intent: Intent?)
			{
				activity?.finish()
				activity?.unregisterReceiver(this)
			}
		}
		activity.registerReceiver(receiver, IntentFilter(Intent.ACTION_SCREEN_ON))

		// Sleep will run on a new thread and involve su, that takes quite some
		// time so do it at once
		SystemHelper.sleep(_appContext,
		{
			if (!it)
			{
				Toast.makeText(_appContext, R.string.sleep_fail,
						Toast.LENGTH_LONG).show()
				activity?.finish()
				activity?.unregisterReceiver(receiver)
			}
		})

		_sleepBtn.btn.isShadow = false
		_dismissOtherBounds(_sleepBtn)
		_disableOtherBounds(null as ActionButtonMeta?)
	}

	private fun _onRestartClick()
	{
		for ((i, b) in _actionBtns.withIndex())
		{
			if (b === _restartBtn)
			{
				// Keep restart btn
				continue
			}

			b.bound.animate().xBy(100f).alpha(0f)
					.setInterpolator(AccelerateInterpolator())
					.setDuration(Res.ANIMATION_FAST.toLong())
					.setStartDelay((50 * i).toLong())
		}
		_disableOtherBounds(_restartBtn)

		_restartBtn.btn.animate().rotationBy(180f)
				.setInterpolator(AccelerateDecelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST.toLong())
				.setStartDelay(0)
		_restartBtn.bound.setOnClickListener(null)
		_showRestartMenu(Res.ANIMATION_FAST / 2,
		{
			_initRestartBtns()
		})
	}

	private fun _onScreenshotClick()
	{
		_startReveal(_screenshotBtn.btn, R.color.screenshot_bg, true,
		{
			// App probably closed
			if (activity == null)
			{
				return@_startReveal
			}
			activity.finish()

			val l = SystemHelper.ScreenshotResultListener(
			{
				isSuccessful, filepath ->
				{
					if (isSuccessful)
					{
						_onScreenshotSuccess(filepath)
					}
					else
					{
						Toast.makeText(_appContext, R.string.screenshot_fail,
								Toast.LENGTH_LONG).show()
					}
				}()
			})

			val receiver = object: BroadcastReceiver()
			{
				override fun onReceive(context: Context?, intent: Intent?)
				{
					LocalBroadcastManager.getInstance(_appContext)
							.unregisterReceiver(this)
					SystemHelper.screenshot(_appContext, l)
				}
			}
			LocalBroadcastManager.getInstance(_appContext)
					.registerReceiver(receiver, IntentFilter(ACTION_ON_DESTROY))
		})

		_screenshotBtn.btn.isShadow = false
		_dismissOtherBounds(_screenshotBtn)
		_disableOtherBounds(null as ActionButtonMeta?)
	}

	private fun _onScreenshotSuccess(filepath: String)
	{
		// Add the file to media store
		MediaScannerConnection.scanFile(_appContext, arrayOf(filepath), null,
				null)

		object: AsyncTask<String, Unit, Unit>()
		{
			override fun doInBackground(vararg params: String)
			{
				val uri = Uri.fromFile(File(filepath))
				val iconW = _appContext.resources.getDimensionPixelSize(
						android.R.dimen.notification_large_icon_width)
				val iconH = _appContext.resources.getDimensionPixelSize(
						android.R.dimen.notification_large_icon_height)
				val thumbnail = BitmapLoader(_appContext)
						.setTargetSize(Size(iconW, iconH))
						.setSizeCalc(FillSizeCalc())
						.loadUri(uri)

				val dp512 = DimensionUtils.dpToPx(_appContext, 512f)
				val bmp = BitmapLoader(_appContext)
						.setTargetSize(Size(dp512.toInt(), (dp512 / 2f).toInt()))
						.setSizeCalc(FillSizeCalc())
						.loadUri(uri)

				val openIntent = Intent(Intent.ACTION_VIEW)
				openIntent.setDataAndType(uri, "image/png")
				val openPendingIntent = PendingIntent.getActivity(_appContext,
						0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT)

				val shareIntent = Intent(Intent.ACTION_SEND)
				shareIntent.type = "image/png"
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
				val shareChooser = Intent.createChooser(shareIntent,
						_appContext.getString(
								R.string.screenshot_notification_share_chooser))
				val sharePendingIntent = PendingIntent.getActivity(_appContext,
						1, shareChooser, PendingIntent.FLAG_UPDATE_CURRENT)

				val deleteIntent = Intent(_appContext,
						DeleteScreenshotService::class.java)
				deleteIntent.putExtra(DeleteScreenshotService.EXTRA_FILEPATH,
						filepath)
				val deletePendingIntent = PendingIntent.getService(_appContext,
						2, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)

				val n = NotificationCompat.Builder(_appContext)
						.setTicker(_appContext.getString(
								R.string.screenshot_notification_ticker))
						.setContentTitle(_appContext.getString(
								R.string.screenshot_notification_title))
						.setContentText(_appContext.getString(
								R.string.screenshot_notification_text))
						.setContentIntent(openPendingIntent)
						.setWhen(System.currentTimeMillis())
						.setSmallIcon(R.drawable.ic_photo_white_24dp)
						.setLargeIcon(thumbnail)
						.setStyle(NotificationCompat.BigPictureStyle()
								.bigPicture(bmp)
								.bigLargeIcon(_getBigLargeIcon()))
						.addAction(R.drawable.ic_screenshot_notification_share,
								_appContext.getString(
										R.string.screenshot_notification_share),
								sharePendingIntent)
						.addAction(R.drawable.ic_screenshot_notification_delete,
								_appContext.getString(
										R.string.screenshot_notification_delete),
								deletePendingIntent)
						.setOnlyAlertOnce(false)
						.setAutoCancel(true)
						.build()
				val ns = NotificationManagerCompat.from(_appContext)
				ns.notify(NOTIFICATION_SCREENSHOT, n)
			}

			private fun _getBigLargeIcon(): Bitmap
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
					return _getBigLargeIconL()
				}
				else
				{
					return DrawableUtils.toBitmap(_appContext.resources
							.getDrawable(R.drawable.ic_photo_white_24dp))
				}
			}

			private fun _getBigLargeIconL(): Bitmap
			{
				val dp48 = DimensionUtils.dpToPx(_appContext, 48f)
				val bmp = Bitmap.createBitmap(dp48.toInt(), dp48.toInt(),
						Bitmap.Config.ARGB_8888)
				val c = Canvas(bmp)

				val bgPaint = Paint()
				bgPaint.color = _appContext.resources.getColor(
						R.color.md_blue_grey_500)
				bgPaint.isAntiAlias = true
				bgPaint.style = Paint.Style.FILL
				c.drawCircle((dp48 - 1) / 2f, (dp48 - 1) / 2f, dp48 / 2f,
						bgPaint)

				val dp12 = DimensionUtils.dpToPx(_appContext, 12f)
				val icon = DrawableUtils.toBitmap(_appContext.resources
						.getDrawable(R.drawable.ic_photo_white_24dp))
				val iconPaint = Paint()
				iconPaint.isAntiAlias = true
				iconPaint.style = Paint.Style.FILL
				c.drawBitmap(icon, dp12, dp12, iconPaint)

				return bmp
			}
		}.execute()
	}

	private fun _onRestartMenuClick(meta: RestartButtonMeta,
			rebootMode: SystemHelper.RebootMode)
	{
		_startReveal(meta.btn, R.color.restart_bg, true,
		{
			// App probably closed
			if (activity == null)
			{
				return@_startReveal
			}
			if (!SystemHelper.reboot(rebootMode, activity))
			{
				Toast.makeText(activity, R.string.restart_fail,
						Toast.LENGTH_LONG).show()
				activity.finish()
			}
		})

		meta.btn.isShadow = false
		_dismissOtherBounds(meta)
		_dismissOtherLabels(null)
		_disableOtherBounds(null as RestartButtonMeta?)
	}

	private fun _onRestartNormalClick()
	{
		_onRestartMenuClick(_restartNormalBtn, SystemHelper.RebootMode.NORMAL)
	}

	private fun _onRestartNormalLongClick()
	{
		val l = SystemHelper.SuResultListener(
		{
			if (!it)
			{
				Toast.makeText(_appContext, R.string.soft_reboot_fail,
						Toast.LENGTH_LONG).show();
				activity?.finish();
			}
			// If succeeded, we'll get killed anyway
		})
		_startReveal(_restartNormalBtn.btn, R.color.restart_bg, true,
		{
			// App probably closed
			if (activity == null)
			{
				return@_startReveal
			}
			SystemHelper.killZygote(activity, l)
		})

		_restartNormalBtn.btn.isShadow = false
		_dismissOtherBounds(_restartNormalBtn)
		_dismissOtherLabels(null)
		_disableOtherBounds(null as RestartButtonMeta?)
	}

	private fun _onRestartRecoveryClick()
	{
		_onRestartMenuClick(_restartRecoveryBtn,
				SystemHelper.RebootMode.RECOVERY)
	}

	private fun _onRestartBootloaderClick()
	{
		_onRestartMenuClick(_restartBootloaderBtn,
				SystemHelper.RebootMode.BOOTLOADER)
	}

	/**
	 * Start the reveal animation
	 *
	 * @param atView Center the effect at the location of this view
	 * @param colorId The color resource of the effect
	 * @param isDelayCallback Whether to delay the callback after the animation
	 * finished
	 * @param callback The callback to be run after the animation
	 */
	private fun _startReveal(atView: View, colorId: Int,
			isDelayCallback: Boolean, callback: (() -> Unit)?)
	{
		_reveal.setColor(resources.getColor(colorId))

		val location = IntArray(2)
		atView.getLocationInWindow(location)
		val revealLocation = IntArray(2)
		_reveal.getLocationInWindow(revealLocation)
		var x = (location[0] - revealLocation[0]).toFloat()
		var y = (location[1] - revealLocation[1]).toFloat()
		val viewRadius = Math.sqrt(Math.pow(atView.width / 2.0, 2.0) * 2)
		val viewRotationRadian = Math.toRadians(atView.rotation + 135.0)
		x -= (viewRadius * Math.cos(viewRotationRadian)).toFloat()
		y -= -(viewRadius * Math.sin(viewRotationRadian)).toFloat()
		_reveal.setCenter(x, y)

		var l: Animator.AnimatorListener? = null
		if (callback != null)
		{
			l = object : AnimatorListenerAdapter()
			{
				override fun onAnimationEnd(animation: Animator?)
				{
					if (isDelayCallback)
					{
						_handler.postDelayed(callback, 20)
					}
					else
					{
						callback()
					}
				}
			}
		}
		_reveal.reveal(Res.ANIMATION_MID, l)
	}

	/**
	 * Show the restart menu
	 *
	 * @param delay Delay the show animation
	 * @param callback Call after the animation finished
	 */
	private fun _showRestartMenu(delay: Int, callback: (() -> Unit)?)
	{
		for (b in _restartMenuBtns)
		{
			if (b === _restartNormalBtn)
			{
				// Skip the normal button
				continue
			}
			for (v in arrayOf(b.bound, b.label))
			{
				v.visibility = View.VISIBLE
				v.alpha = 0f
				v.y = v.y + 50
				v.animate().alpha(1f).yBy(-50f)
						.setInterpolator(DecelerateInterpolator())
						.setDuration(Res.ANIMATION_FAST.toLong())
						.setStartDelay(delay.toLong())
			}
		}

		_restartNormalBtn.label.visibility = View.VISIBLE
		_restartNormalBtn.label.alpha = 0f
		_restartNormalBtn.label.animate().alpha(1f)
				.setInterpolator(DecelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST.toLong())
				.setStartDelay(delay.toLong())
		// All animations start and end at the same time, so we only need to set
		// once here
		if (callback != null)
		{
			_restartNormalBtn.label.animate()
					.setListener(object: AnimatorListenerAdapter()
					{
						override fun onAnimationEnd(animation: Animator?)
						{
							callback()
						}
					})
		}
	}

	private fun _dismissOtherBounds(keep: ActionButtonMeta?)
	{
		for (b in _actionBtns)
		{
			if (b.bound != keep?.bound)
			{
				_dismissView(b.bound)
			}
		}
	}

	private fun _dismissOtherBounds(keep: RestartButtonMeta?)
	{
		for (b in _restartMenuBtns)
		{
			if (b.bound != keep?.bound)
			{
				_dismissView(b.bound)
			}
		}
	}

	private fun _dismissOtherLabels(keep: RestartButtonMeta?)
	{
		for (b in _restartMenuBtns)
		{
			if (b.label != keep?.label)
			{
				_dismissView(b.label)
			}
		}
	}

	private fun _dismissView(v: View)
	{
		v.animate().alpha(0f)
				.setInterpolator(AccelerateInterpolator())
				.setDuration(Res.ANIMATION_FAST.toLong())
				.setStartDelay(0)
	}

	private fun _disableOtherBounds(keep: ActionButtonMeta?)
	{
		for (b in _actionBtns)
		{
			if (b.bound != keep?.bound)
			{
				_disableBound(b.bound)
			}
		}
	}

	private fun _disableOtherBounds(keep: RestartButtonMeta?)
	{
		for (b in _actionBtns)
		{
			if (b.bound != keep?.bound)
			{
				_disableBound(b.bound)
			}
		}
	}

	private fun _disableBound(bound: View)
	{
		bound.setOnClickListener(null)
		bound.isFocusable = false
		bound.isClickable = false
		bound.isEnabled = false
	}

	private val _appContext by lazy({activity.applicationContext})
	private val _handler by lazy({Handler()})

	private lateinit var _root: View
	private val _actionBtns by lazy(
	{
		arrayOf(ActionButtonMeta(
						_root.findViewById(R.id.shutdown_id)
								as FloatingActionButton,
						_root.findViewById(R.id.shutdown_btn_bound),
						{_onShutdownClick()}),
				ActionButtonMeta(
						_root.findViewById(R.id.sleep_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.sleep_btn_bound),
						{_onSleepClick()}),
				ActionButtonMeta(
						_root.findViewById(R.id.restart_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.restart_btn_bound),
						{_onRestartClick()}),
				ActionButtonMeta(
						_root.findViewById(R.id.screenshot_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.screenshot_btn_bound),
						{_onScreenshotClick()}))
	})

	private val _shutdownBtn: ActionButtonMeta
		get() = _actionBtns[0]

	private val _sleepBtn: ActionButtonMeta
		get() = _actionBtns[1]

	private val _restartBtn: ActionButtonMeta
		get() = _actionBtns[2]

	private val _screenshotBtn: ActionButtonMeta
		get() = _actionBtns[3]

	private val _restartMenuBtns by lazy(
	{
		arrayOf(RestartButtonMeta(
						_restartBtn.btn,
						_restartBtn.bound,
						_root.findViewById(R.id.restart_normal_label),
						{_onRestartNormalClick()}),
				RestartButtonMeta(
						_root.findViewById(R.id.restart_recovery_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.restart_recovery_btn_bound),
						_root.findViewById(R.id.restart_recovery_label),
						{_onRestartRecoveryClick()}),
				RestartButtonMeta(
						_root.findViewById(R.id.restart_bootloader_btn)
								as FloatingActionButton,
						_root.findViewById(R.id.restart_bootloader_btn_bound),
						_root.findViewById(R.id.restart_bootloader_label),
						{_onRestartBootloaderClick()}))
	})

	private val _restartNormalBtn: RestartButtonMeta
		get() = _restartMenuBtns[0]

	private val _restartRecoveryBtn: RestartButtonMeta
		get() = _restartMenuBtns[1]

	private val _restartBootloaderBtn: RestartButtonMeta
		get() = _restartMenuBtns[2]

	private val _reveal by lazy({_root.findViewById(R.id.reveal) as RevealView})
}
