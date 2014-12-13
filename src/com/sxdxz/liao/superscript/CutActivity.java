package com.sxdxz.liao.superscript;

import java.io.ByteArrayOutputStream;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @author Administrator 整体思想是：截取屏幕的截图，然后截取矩形框里面的图片 代码未经优化，只是一个demo。
 */
public class CutActivity extends Activity{
	
	private enum TouchMode{
		NONE, DRAG, ZOOM
	}
	ImageView srcPic;
	Button confirmBtn;
	SelectView clipview;

	private static final String TAG = "CutActivity";
//	public static final String INTENT_PICTURE_URI = "IntentPictureUri";
	public static final String INTENT_EXTRA_BITMAP = "extra_bitmap";
	TouchMode mode = TouchMode.NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	float oldDist = 1f;
	protected boolean isTranslate;
	private AdView mAdView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clip_picture);

		Button cancelBtn = (Button) findViewById(R.id.button_back);
		cancelBtn.setText("取消");
		
		ClickTouchListener touchListener = new ClickTouchListener();
		cancelBtn.setOnClickListener(touchListener);
		srcPic = (ImageView) this.findViewById(R.id.src_pic);
		clipview = (SelectView) this.findViewById(R.id.clipview);

		srcPic.setOnTouchListener(touchListener);

		confirmBtn = (Button) findViewById(R.id.button_title_right);
		confirmBtn.setBackgroundResource(R.drawable.selector_btn_finish);
		confirmBtn.setOnClickListener(touchListener);
		
		findViewById(R.id.button_title_mid).setVisibility(View.GONE);
		
		
		mAdView = new AdView(this);
		mAdView.setAdSize(AdSize.SMART_BANNER);
		String bannerId = getString(R.string.ad_banner_id);
		mAdView.setAdUnitId(bannerId);

		// Create an ad request.
		AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

		RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.layout_root);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mAdView.setLayoutParams(lp);
		// Add the AdView to the view hierarchy.
		rootLayout.addView(mAdView);

		// // Start loading the ad.
		mAdView.loadAd(adRequestBuilder.build());

	}

	@Override
	protected void onResume() {
		super.onResume();
		mAdView.resume();
		srcPic.post(new Runnable() {

			@Override
			public void run() {
				View decorView = getWindow().getDecorView();

				Intent intent = getIntent();
				if (intent != null) {
					Uri uri = intent.getData();
					int width = decorView.getWidth();
					int height = decorView.getHeight();
					Bitmap bitmap = DrawIcon.decodeSampledBitmap(uri.getPath(),
							width/2, height/2);
					srcPic.setImageBitmap(bitmap);
				}
			}
		});

		if (!isTranslate)
			srcPic.postDelayed(new Runnable() {

				public void run() {
					Rect bounds = srcPic.getDrawable().getBounds();
					int height2 = bounds.height();
					int width2 = bounds.width();

					int height = getWindow().getDecorView().getHeight();
					int width = getWindow().getDecorView().getWidth();
					
					matrix.postTranslate((width - width2) / 2,
							(height - height2) / 2);
					srcPic.setImageMatrix(matrix);
					isTranslate = true;

				}
			}, 0);
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		mAdView.pause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdView.destroy();
	}


	private class ClickTouchListener implements OnTouchListener, OnClickListener {
		/* 这里实现了多点触摸放大缩小，和单点移动图片的功能，参考了论坛的代码 */
		@SuppressLint("ClickableViewAccessibility")
		public boolean onTouch(View v, MotionEvent event) {
			ImageView view = (ImageView) v;
			// Handle touch events here...
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				savedMatrix.set(matrix);
				// 設置初始點位置
				start.set(event.getX(), event.getY());
				mode = TouchMode.DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event);
					mode = TouchMode.ZOOM;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = TouchMode.NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == TouchMode.DRAG) {
					// ...
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY()
							- start.y);
				} else if (mode == TouchMode.ZOOM) {
					float newDist = spacing(event);
					Log.d(TAG, "newDist=" + newDist);
					if (newDist > 10f) {
						matrix.set(savedMatrix);
						float scale = newDist / oldDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
					}
				}
				break;
			}
			view.setImageMatrix(matrix);
			return true; // indicate event was handled
		}
		
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button_title_right:
				Bitmap finalBitmap = getBitmap();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				//添加bitmap到输入流之中
				finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				byte[] bitmapByte = baos.toByteArray();

				Intent intent = new Intent();
				intent.putExtra(INTENT_EXTRA_BITMAP, bitmapByte);
				setResult(RESULT_OK, intent);
				finish();
				break;
			case R.id.button_back:
				finish();
				break;
			default:
				break;
			}

		}

		/** Determine the space between the first two fingers */
		private float spacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}

		/** Calculate the mid point of the first two fingers */
		private void midPoint(PointF point, MotionEvent event) {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}

		/* 获取矩形区域内的截图 */
		private Bitmap getBitmap() {
			getBarHeight();
			Bitmap screenShoot = takeScreenShot();

			Bitmap finalBitmap = Bitmap.createBitmap(screenShoot,
					clipview.borderWidth, 2 * clipview.borderHeight
							+ statusBarHeight + titleBarHeight, clipview.rectWidth,
					clipview.rectWidth);
			return finalBitmap;
		}

		int statusBarHeight = 0;
		int titleBarHeight = 0;

		private void getBarHeight() {
			// 获取状态栏高度
			Rect frame = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			statusBarHeight = frame.top;

			int contenttop = getWindow()
					.findViewById(Window.ID_ANDROID_CONTENT).getTop();
			// statusBarHeight是上面所求的状态栏的高度
			titleBarHeight = contenttop - statusBarHeight;
		}

		// 获取Activity的截屏
		private Bitmap takeScreenShot() {
			View view = getWindow().getDecorView();
			view.setDrawingCacheEnabled(true);
			view.buildDrawingCache();
			return view.getDrawingCache();
		}

	}
}