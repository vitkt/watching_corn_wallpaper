package ru.vitkt.watchingcorn;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class CornWallpaper extends WallpaperService {

	@Override
	public Engine onCreateEngine() {

		return new CornWallpaperEngine();
	}

	private class CornWallpaperEngine extends WallpaperService.Engine {

		// Координаты глаз
		final float FIRST_EYE_X = 190f;
		final float FIRST_EYE_Y = 349f;

		final float SECOND_EYE_X = 280f;
		final float SECOND_EYE_Y = 349f;

		/*
		 * Соотношения части картинки при ландшафтной ориентации ко всей
		 * картинке
		 */
		final float LANDSCAPE_VERT_PART_OFFSET = 241f / 540f;
		final float LANDSCAPE_VERT_PART_SIZE = (540f - 241f) / 540f;
		final float LANDSCAPE_VERT_PART_SIZE_K = (540f - 241f) / 907f;
		final float PORTRAIT_VERT_PART_OFFSET = 20f / 540f;

		/*
		 * Размер поверхности
		 */
		float _surfaceWidth = 0f;
		float _surfaceHeight = 0f;

		/*
		 * Изображение кукурузы
		 */
		Bitmap _cornBitmap;
		Bitmap _cornNormal;
		Bitmap _cornNewYear;
		boolean nyMode = false;

		void updateFromPreferences() {

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(CornWallpaper.this);

			nyMode = prefs.getBoolean("cornMode", true);
			if (nyMode)
				_cornBitmap = _cornNewYear;
			else
				_cornBitmap = _cornNormal;

		}

		public CornWallpaperEngine() {
			

			_cornNormal = BitmapFactory.decodeResource(
					CornWallpaper.this.getResources(),
					R.drawable.watching_corn_no_eyes);
			
			_cornNewYear = BitmapFactory.decodeResource(
					CornWallpaper.this.getResources(),
					R.drawable.watching_corn_new_year);
			
			updateFromPreferences();

			eyePaint.setStyle(Paint.Style.FILL);
			eyePaint.setColor(Color.BLACK);
		}

		float offsetX, offsetY;

		float destWidth, destHeight;

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {

			super.onSurfaceChanged(holder, format, width, height);
			_surfaceWidth = width;
			_surfaceHeight = height;

			int bitmapWidth = _cornBitmap.getWidth();
			int bitmapHeight = _cornBitmap.getHeight();

			// 490 for small
			float k = 1f;

			destWidth = bitmapWidth;
			destHeight = bitmapHeight;

			if (height > width)
				destHeight = _surfaceHeight;
			else
				destHeight = (_surfaceHeight * LANDSCAPE_VERT_PART_SIZE)
						/ LANDSCAPE_VERT_PART_SIZE_K;

			destWidth = (destHeight / (float) bitmapHeight) * bitmapWidth;

			offsetX = (_surfaceWidth / 2f) - (destWidth / 2f);

			// offsetY+=20;
			offsetY = (_surfaceHeight / 2f) - (destHeight / 2f);
			k = (destHeight / (float) bitmapHeight);

			if (height > width) {
				offsetY = (_surfaceHeight / 2f) - (destHeight / 2f);
				offsetY += 20;
			} else
				offsetY += _surfaceHeight * LANDSCAPE_VERT_PART_OFFSET;// (590f
																		// -
																		// 349f)
																		// * k;
			// else
			//
			// offsetY+=height;

			// p.setAntiAlias(false);

			// if (height > bitmapHeight || (height > width)) {
			// k = (_surfaceHeight / (float) bitmapHeight);
			//
			// destHeight = _surfaceHeight;
			// destWidth = (destHeight / (float) bitmapHeight) * bitmapWidth;
			//
			// offsetX = (_surfaceWidth / 2f) - (destWidth / 2f);
			// offsetY = (_surfaceHeight / 2f) - (destHeight / 2f);
			// offsetY += 20f * k;
			// } else {
			// offsetX = (_surfaceWidth / 2f) - (destWidth / 2f);
			// offsetY = (_surfaceHeight / 2f) - (destHeight / 2f);
			// offsetY += (590f - 349f) * k;
			// }

			// 190,349
			// 280,349

			// 590
			radius = cRadius * k;

			constEye1 = new PointF(190 * k + offsetX, 349 * k + offsetY);
			constEye2 = new PointF(280 * k + offsetX, 349 * k + offsetY);

		}

		Paint p = new Paint();
		Paint eyePaint = new Paint();

		@Override
		public void onTouchEvent(MotionEvent event) {

			super.onTouchEvent(event);

			if (event.getAction() == MotionEvent.ACTION_DOWN
					|| event.getAction() == MotionEvent.ACTION_MOVE) {
				PointF eye1 = new PointF(event.getX(), event.getY());
				PointF eye2 = new PointF(event.getX(), event.getY());

				if (!(dist(eye1, constEye1) <= radius))
					eye1 = getPointOnRadius(constEye1, eye1);

				if (!(dist(eye2, constEye2) <= radius))
					eye2 = getPointOnRadius(constEye2, eye2);

				current = new Eyes(eye1, eye2);

			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				current = null;
			}
		}

		Eyes current;

		float dist(PointF p1, PointF p2) {
			float x = p1.x - p2.x;
			float y = p1.y - p2.y;
			return (float) Math.sqrt(x * x + y * y);
		}

		PointF constEye1;
		PointF constEye2;
		final float cRadius = 30f;
		float radius = 30f;

		void drawPointF(Canvas canvas, PointF point) {
			canvas.drawCircle(point.x, point.y, 5f, eyePaint);
		}

		PointF getPointOnRadius(PointF center, PointF p) {
			float d = dist(center, p);
			float k = d / radius;

			return new PointF(center.x + ((p.x - center.x) / k), center.y
					+ ((p.y - center.y) / k));
		}

		protected void draw() {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();

				if (canvas != null) {

					canvas.drawColor(Color.WHITE);
					Log.i("corn", "offx = " + offsetX + " offy = " + offsetY
							+ " destW = " + destWidth + " destH = "
							+ destHeight);
					canvas.drawBitmap(_cornBitmap, null, new Rect(
							(int) offsetX, (int) offsetY,
							(int) (offsetX + destWidth),
							(int) (offsetY + destHeight)), p);

					// 190,349
					// 280,349

					if (current == null) {

						drawPointF(canvas, constEye1);
						drawPointF(canvas, constEye2);
					} else {
						Eyes _eyes = current;
						drawPointF(canvas, _eyes.eye1);
						drawPointF(canvas, _eyes.eye2);
					}

				}
			} finally {
				if (canvas != null)
					holder.unlockCanvasAndPost(canvas);
			}

			if (visible) {
				handler.postDelayed(drawRunner, 5);

			}

		}

		private final Runnable drawRunner = new Runnable() {
			@Override
			public void run() {
				draw();
			}

		};
		private final Handler handler = new Handler();
		boolean visible = false;

		class Eyes {
			public PointF eye1 = new PointF();
			public PointF eye2 = new PointF();

			Eyes(PointF e1, PointF e2) {
				eye1 = e1;
				eye2 = e2;
			}
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			updateFromPreferences();
			super.onVisibilityChanged(visible);
			this.visible = visible;

			if (visible) {
				handler.post(drawRunner);
			} else {
				handler.removeCallbacks(drawRunner);
			}
		}
	}
}
