package com.sxdxz.liao.superscript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends Activity {

	protected static final int REQUEST_LOAD_ICON = 2;
	public static final int REQUEST_CROP_PIC = 3;
	private static final String IMAGE_UNSPECIFIED = "image/*";
	public static final String TAG = "MainActivity";
	private static final int REQUEST_PLAY_SERVICE = 4;

	private static int RESULT_LOAD_IMAGE = 1;
	private DrawIcon mDrawer;
	private Bitmap finalBitmap;

	private String imageDirectoryPath;
	private ImageView imageView;
	private Bitmap clipedBitmap;
	private ImageView loadImgBtn;
	private Button redoBtn;
	private View redBtn;
	private View yellowBtn;
	private View blueBtn;
	private View greenBtn;
	public View selectedBtn;
	private EditText inputEdit;
	private AdView mAdView;
	// private String picturePath;
	private InterstitialAd interstitialAd;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageDirectoryPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ getString(R.string.imgStoreDir) + File.separator;

		mDrawer = new DrawIcon();
		ClickListener clickListener = new ClickListener();

		View saveBtn = findViewById(R.id.button_title_mid);
		loadImgBtn = (ImageView) findViewById(R.id.buttonLoadPicture);
		View shareBtn = findViewById(R.id.button_title_right);
		imageView = (ImageView) findViewById(R.id.imgView);
		redoBtn = (Button) findViewById(R.id.button_back);
		redBtn = findViewById(R.id.button2);
		yellowBtn = findViewById(R.id.button3);
		blueBtn = findViewById(R.id.button4);
		greenBtn = findViewById(R.id.button5);
		inputEdit = (EditText) findViewById(R.id.editText1);

		redoBtn.setOnClickListener(clickListener);
		saveBtn.setOnClickListener(clickListener);
		loadImgBtn.setOnClickListener(clickListener);
		shareBtn.setOnClickListener(clickListener);

		redBtn.setOnClickListener(clickListener);
		yellowBtn.setOnClickListener(clickListener);
		blueBtn.setOnClickListener(clickListener);
		greenBtn.setOnClickListener(clickListener);

		redoBtn.setVisibility(View.GONE);
		redoBtn.setText(getString(R.string.redo));
		redBtn.setSelected(true);
		selectedBtn = redBtn;

		// Create a banner ad. The ad size and ad unit ID must be set before
		// calling loadAd.
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
		interstitialAd = new InterstitialAd(this);
		interstitialAd.setAdUnitId(getString(R.string.ad_interstitle_id));
		interstitialAd.loadAd(adRequestBuilder.build());
		mAdView.loadAd(adRequestBuilder.build());

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAdView.resume();
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
		// picturePath = null;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	// 在您准备好展示插页式广告时调用displayInterstitial()。
	public void displayInterstitial() {
		if (interstitialAd.isLoaded()) {
			interstitialAd.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (clipedBitmap == null && data == null) {
			redoBtn.setVisibility(View.GONE);
			loadImgBtn.setVisibility(View.VISIBLE);
		}


		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri uri = data.getData();
			String realPath = getRealPath(uri);
			// picturePath = realPath;
			// 拿到图片后马上进入裁剪
			startPhotoZoom(Uri.parse(realPath));
		}

		if (requestCode == REQUEST_CROP_PIC && resultCode == RESULT_OK) {
			if (data != null) {
				byte[] bis = data
						.getByteArrayExtra(CutActivity.INTENT_EXTRA_BITMAP);
				if (bis != null) {
					clipedBitmap = BitmapFactory.decodeByteArray(bis, 0,
							bis.length);
					inputEdit.setVisibility(View.VISIBLE);
					inputEdit.requestFocus();
					imageView.setImageBitmap(clipedBitmap);
					inputEdit.postDelayed(new Runnable() {

						@Override
						public void run() {
							// 弹出键盘
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.toggleSoftInput(0,
									InputMethodManager.HIDE_NOT_ALWAYS);
						}
					}, 500);

				}
			}

		}

	}

	private String getRealPath(Uri uri) {
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = getContentResolver().query(uri, filePathColumn, null,
				null, null);
		if (cursor == null) {
			Toast.makeText(this, "图片读取失败", Toast.LENGTH_SHORT).show();
			return null;
		}
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String path = cursor.getString(columnIndex);
		cursor.close();
		return path;
	}

	private class ClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button_title_right:
				refreshFinalBitmap();
				shareToFriend();
				break;
			case R.id.button_title_mid:
				refreshFinalBitmap();

				if (finalBitmap == null)
					return;
				imageView.post(new Runnable() {

					@Override
					public void run() {
						// 保存图片
						File dir = new File(imageDirectoryPath);
						if (!dir.exists())
							dir.mkdir();
						SimpleDateFormat sDateFormat = new SimpleDateFormat(
								"yyyyMMddhhmmss", Locale.getDefault());
						String date = sDateFormat.format(new java.util.Date());
						final File file = new File(imageDirectoryPath + date
								+ ".png");
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(file);
							finalBitmap.compress(Bitmap.CompressFormat.PNG, 80,
									fos);
							Toast.makeText(MainActivity.this,
									"图片已保存到：" + imageDirectoryPath,
									Toast.LENGTH_SHORT).show();
							fos.close();
							scanFile(file);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (fos != null)
								try {
									fos.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
						}
					}
				});
				break;
			case R.id.button_back:
			case R.id.buttonLoadPicture:
				//调用图库选择图片，动作为ACTION_PICK, URL为MediaStore.Images.Media中的外部URL
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, RESULT_LOAD_IMAGE);
				break;
			case R.id.button2:
				inputEdit.setBackgroundResource(R.drawable.shape_input_red);
				selectedBtn.setSelected(false);
				redBtn.setSelected(true);
				selectedBtn = redBtn;
				displayInterstitial();
				break;
			case R.id.button3:
				inputEdit.setBackgroundResource(R.drawable.shape_input_yellow);

				selectedBtn.setSelected(false);
				yellowBtn.setSelected(true);
				selectedBtn = yellowBtn;
				break;
			case R.id.button4:
				inputEdit.setBackgroundResource(R.drawable.shape_input_blue);
				selectedBtn.setSelected(false);
				blueBtn.setSelected(true);
				selectedBtn = blueBtn;
				break;
			case R.id.button5:
				inputEdit.setBackgroundResource(R.drawable.shape_input_green);
				selectedBtn.setSelected(false);
				greenBtn.setSelected(true);
				selectedBtn = greenBtn;
				break;
			}

			switch (v.getId()) {
			case R.id.buttonLoadPicture:
				v.setVisibility(View.GONE);
				redoBtn.setVisibility(View.VISIBLE);
				break;
			}

		}
	}

	private void refreshFinalBitmap() {
		if (clipedBitmap == null)
			return;

		inputEdit.setCursorVisible(false);
		//打开绘制缓存
		inputEdit.setDrawingCacheEnabled(true);
		//绘制View的图像到缓存中
		inputEdit.buildDrawingCache();
		//获取图像缓存
		Bitmap bitmap = inputEdit.getDrawingCache()
				.copy(Config.ARGB_8888, true);
		//获取后关闭缓存
		inputEdit.setDrawingCacheEnabled(false);
		inputEdit.setCursorVisible(true);

		int dimension = (int) getResources().getDimension(
				R.dimen.superscirpt_margin_top);
		finalBitmap = mDrawer.drawTwoPicture(
				clipedBitmap.copy(Config.ARGB_8888, true), bitmap, dimension,
				dimension);
	}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent(this, CutActivity.class);
		intent.setData(uri);
		startActivityForResult(intent, REQUEST_CROP_PIC);
	}

	private void scanFile(File file) {
		Uri contentUri = Uri.fromFile(file);
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
		sendBroadcast(mediaScanIntent);
	}

	private void shareToFriend() {
		if (finalBitmap == null)
			return;
		Intent intent = new Intent();
		//action 为ACTION_SEND
		intent.setAction(Intent.ACTION_SEND);
		//type为“image/*”
		intent.setType(IMAGE_UNSPECIFIED);
		//输入分享文字
		intent.putExtra(Intent.EXTRA_TEXT, "分享自" + getString(R.string.app_name));
		//这句话会让该bitmap储存在DCMI/的文件夹下面
		String pathofBmp = Images.Media.insertImage(getContentResolver(),
				finalBitmap, "title", null);
		Uri bmpUri = Uri.parse(pathofBmp);
		//指定extra为EXTRA_STREAM
		intent.putExtra(Intent.EXTRA_STREAM, bmpUri);
		//设置添加到对应的应用的task中（如果存在的话）
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//调用选择器
		startActivity(Intent.createChooser(intent, "请选择"));
	}
}