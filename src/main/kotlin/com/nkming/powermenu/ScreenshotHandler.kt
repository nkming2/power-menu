package com.nkming.powermenu

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.view.Surface
import android.widget.Toast
import com.nkming.utils.graphic.BitmapLoader
import com.nkming.utils.graphic.BitmapUtils
import com.nkming.utils.graphic.DrawableUtils
import com.nkming.utils.graphic.FillSizeCalc
import com.nkming.utils.type.Size
import com.nkming.utils.unit.DimensionUtils
import java.io.File

class ScreenshotHandler(context: Context)
{
	companion object
	{
		private val LOG_TAG = ScreenshotHandler::class.java.canonicalName
	}

	fun onScreenshotSuccess(filepath: String, rotation: Int)
	{
		val l =
		{
			val callback = MediaScannerConnection.OnScanCompletedListener{
					_, uri ->
					run{
						_notifyScreenshot(filepath, uri)
					}}
			// Add the file to media store
			MediaScannerConnection.scanFile(_context, arrayOf(filepath), null,
					callback)
		}

		if (rotation == Surface.ROTATION_0
				|| _isScreenshotOrientationGood(filepath, rotation))
		{
			// Do nothing
			l()
		}
		else
		{
			_fixScreenshotOrientation(filepath, rotation, l)
		}
	}

	/**
	 * See if the screnshot is correctly oriented. Notice however that if the
	 * device is rotated 180 degrees, this would fail
	 *
	 * @param filepath
	 * @param rotation
	 * @return
	 */
	private fun _isScreenshotOrientationGood(filepath: String, rotation: Int)
			: Boolean
	{
		val size = BitmapUtils.getSize(filepath) ?: return false
		val isScreenshotPortrait = (size.h > size.w)
		val isDevicePortrait = (rotation == Surface.ROTATION_0
				|| rotation == Surface.ROTATION_180)
		return (isScreenshotPortrait == isDevicePortrait)
	}

	private fun _fixScreenshotOrientation(filepath: String, rotation: Int,
			l: () -> Unit)
	{
		object: AsyncTask<Unit, Unit, Unit>()
		{
			override fun doInBackground(vararg params: Unit)
			{
				try
				{
					_doInBackground()
				}
				catch (e: Exception)
				{
					Log.e("$LOG_TAG._fixScreenshotOrientation", "Failed", e)
					_onFailureFallback()
				}
			}

			override fun onPostExecute(result: Unit)
			{
				l()
			}

			private fun _doInBackground()
			{
				val uri = Uri.fromFile(File(filepath))
				val bmp = BitmapLoader(_context).loadUri(uri)
				val mat = Matrix()
				mat.postRotate(_getRotationValue(rotation))
				val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width,
						bmp.height, mat, true)
				BitmapUtils.saveBitmap(rotated, filepath,
						Bitmap.CompressFormat.PNG, 100)
			}

			private fun _getRotationValue(rotation: Int): Float
			{
				return when (rotation)
				{
					Surface.ROTATION_0 -> 0f
					Surface.ROTATION_90 -> -90f
					Surface.ROTATION_180 -> 180f
					Surface.ROTATION_270 -> 90f
					else -> 0f
				}
			}
		}.execute()
	}

	private fun _notifyScreenshot(filepath: String, uri: Uri?)
	{
		if (uri == null)
		{
			Log.e("$LOG_TAG._notifyScreenshot", "Uri is null")
			_onFailureFallback()
			return
		}

		object: AsyncTask<Unit, Unit, Boolean>()
		{
			override fun doInBackground(vararg params: Unit): Boolean
			{
				return try
				{
					_doInBackground()
					true
				}
				catch (e: Exception)
				{
					Log.e("$LOG_TAG._notifyScreenshot", "Failed", e)
					false
				}
			}

			override fun onPostExecute(result: Boolean)
			{
				if (!result)
				{
					_onFailureFallback()
				}
			}

			private fun _doInBackground()
			{
				val fileUri = Uri.fromFile(File(filepath))
				val thumbnail = _getThumbnail(fileUri)

				val dp512 = DimensionUtils.dpToPx(_context, 512f)
				// Somehow the bitmap couldn't be load on some device, not sure
				// why...
				val bmp = BitmapLoader(_context)
						.setTargetSize(Size(dp512.toInt(), (dp512 / 2f).toInt()))
						.setSizeCalc(FillSizeCalc())
						.loadUri(fileUri)

				val openIntent = Intent(Intent.ACTION_VIEW)
				openIntent.setDataAndType(uri, "image/png")
				val openPendingIntent = PendingIntent.getActivity(_context,
						0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT)

				val shareIntent = Intent(Intent.ACTION_SEND)
				shareIntent.type = "image/png"
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
				val shareChooser = Intent.createChooser(shareIntent,
						_context.getString(
								R.string.screenshot_notification_share_chooser))
				val sharePendingIntent = PendingIntent.getActivity(_context,
						1, shareChooser, PendingIntent.FLAG_UPDATE_CURRENT)

				val deleteIntent = Intent(_context,
						DeleteScreenshotService::class.java)
				deleteIntent.putExtra(DeleteScreenshotService.EXTRA_FILEPATH,
						filepath)
				val deletePendingIntent = PendingIntent.getService(_context,
						2, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)

				val n = NotificationCompat.Builder(_context)
						.setTicker(_context.getString(
								R.string.screenshot_notification_ticker))
						.setContentTitle(_context.getString(
								R.string.screenshot_notification_title))
						.setContentText(_context.getString(
								R.string.screenshot_notification_text))
						.setContentIntent(openPendingIntent)
						.setWhen(System.currentTimeMillis())
						.setSmallIcon(R.drawable.ic_photo_white_24dp)
						.setColor(_context.resources.getColor(
								R.color.primary_light))
						.setLargeIcon(thumbnail)
						.setStyle(NotificationCompat.BigPictureStyle()
								.bigPicture(bmp)
								.bigLargeIcon(_getBigLargeIcon()))
						.addAction(R.drawable.ic_screenshot_notification_share,
								_context.getString(
										R.string.screenshot_notification_share),
								sharePendingIntent)
						.addAction(R.drawable.ic_screenshot_notification_delete,
								_context.getString(
										R.string.screenshot_notification_delete),
								deletePendingIntent)
						.setOnlyAlertOnce(false)
						.setAutoCancel(true)
						.setLocalOnly(true)
						.build()
				val ns = NotificationManagerCompat.from(_context)
				ns.notify(Res.NOTIF_SCREENSHOT, n)
			}

			private fun _getThumbnail(uri: Uri): Bitmap
			{
				val iconW = _context.resources.getDimensionPixelSize(
						android.R.dimen.notification_large_icon_width)
				val iconH = _context.resources.getDimensionPixelSize(
						android.R.dimen.notification_large_icon_height)
				val thumbnail = BitmapLoader(_context)
						.setTargetSize(Size(iconW, iconH))
						.setSizeCalc(FillSizeCalc())
						.loadUri(uri)

				val bmp = Bitmap.createBitmap(iconW, iconH,
						Bitmap.Config.ARGB_8888)
				val c = Canvas(bmp)
				val paint = Paint()
				paint.isAntiAlias = true
				paint.isFilterBitmap = true
				paint.style = Paint.Style.FILL

				val rect = Rect(0, 0, thumbnail.width, thumbnail.height)
				if (thumbnail.height > iconH)
				{
					val diff = thumbnail.height - iconH
					val diffHalf = diff / 2
					rect.top = diffHalf
					rect.bottom = thumbnail.height - diffHalf
				}
				if (thumbnail.width > iconW)
				{
					val diff = thumbnail.width - iconW
					val diffHalf = diff / 2
					rect.left = diffHalf
					rect.right = thumbnail.width - diffHalf
				}
				c.drawBitmap(thumbnail, rect, Rect(0, 0, iconW, iconH), paint)
				return bmp
			}

			private fun _getBigLargeIcon(): Bitmap
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
					return _getBigLargeIconL()
				}
				else
				{
					return DrawableUtils.toBitmap(_context.resources
							.getDrawable(R.drawable.ic_photo_white_24dp))
				}
			}

			private fun _getBigLargeIconL(): Bitmap
			{
				val dp48 = DimensionUtils.dpToPx(_context, 48f)
				val bmp = Bitmap.createBitmap(dp48.toInt(), dp48.toInt(),
						Bitmap.Config.ARGB_8888)
				val c = Canvas(bmp)

				val bgPaint = Paint()
				bgPaint.color = _context.resources.getColor(
						R.color.md_blue_grey_500)
				bgPaint.isAntiAlias = true
				bgPaint.style = Paint.Style.FILL
				c.drawCircle(dp48 / 2f, dp48 / 2f, dp48 / 2f, bgPaint)

				val dp12 = DimensionUtils.dpToPx(_context, 12f)
				val icon = DrawableUtils.toBitmap(_context.resources
						.getDrawable(R.drawable.ic_photo_white_24dp))
				val iconPaint = Paint()
				iconPaint.isAntiAlias = true
				iconPaint.style = Paint.Style.FILL
				c.drawBitmap(icon, dp12, dp12, iconPaint)

				return bmp
			}
		}.execute()
	}

	private fun _onFailureFallback()
	{
		Toast.makeText(_context, R.string.screenshot_succeed_fallback,
				Toast.LENGTH_LONG).show()
	}

	private val _context = context
}
