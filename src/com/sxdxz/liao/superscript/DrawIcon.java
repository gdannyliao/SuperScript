package com.sxdxz.liao.superscript;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.text.TextUtils;

public class DrawIcon {
	Paint paint = new Paint();
	private String text = "";
	private static float radius = 60;

	public DrawIcon() {
		paint.setAntiAlias(true);
		paint.setTextSize(radius - 10);
		paint.setColor(Color.RED);
	}

	public void setColor(int color) {
		paint.setColor(color);
	}

	public void setText(String text) {
		this.text = text;
	}

	public Bitmap drawTwoPicture(Bitmap pic1, Bitmap pic2, int topOffset, int rightOffset) {
		Canvas canvas = new Canvas(pic1);
		canvas.drawBitmap(pic2, pic1.getWidth()-rightOffset-pic2.getWidth(), topOffset, paint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return pic1;
	}
	
	@Deprecated
	public Bitmap drawInPicture(String picturePath, int picWidth, int picHeight) {
		if (TextUtils.isEmpty(picturePath))
			return null;

		Bitmap bm = decodeSampledBitmap(picturePath, picWidth, picHeight).copy(
				Config.ARGB_8888, true);

		return drawInPicture(bm, picWidth, picHeight);
	}

	@Deprecated
	public Bitmap drawInPicture(Bitmap bm, int picWidth, int picHeight) {
		if (bm == null)
			return null;

		Bitmap copy = bm.copy(Config.RGB_565, true);
		Canvas canvas = new Canvas(copy);

		int len = text.getBytes().length;
		canvas.drawCircle(bm.getWidth() - radius, radius, radius, paint);
		paint.setColor(Color.WHITE);
		canvas.drawText(text,
				bm.getWidth() - (radius + radius * (len) * 0.20f),
				radius * 1.3f, paint);

		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		setColor(Color.RED);
		return copy;
	}

	public static Bitmap decodeSampledBitmap(String picturePath, int reqWidth,
			int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
		return rotateBitmap(bitmap, readPictureDegree(picturePath));
	}

	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	
	private static int readPictureDegree(String path) {  
	       int degree  = 0;  
	       try {  
	               ExifInterface exifInterface = new ExifInterface(path);  
	               int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);  
	               switch (orientation) {  
	               case ExifInterface.ORIENTATION_ROTATE_90:  
	                       degree = 90;  
	                       break;  
	               case ExifInterface.ORIENTATION_ROTATE_180:  
	                       degree = 180;  
	                       break;  
	               case ExifInterface.ORIENTATION_ROTATE_270:  
	                       degree = 270;  
	                       break;  
	               }  
	       } catch (IOException e) {  
	               e.printStackTrace();  
	       }  
	       return degree;  
	   } 
	
	private static Bitmap rotateBitmap(Bitmap bitmap, int rotate){
		if(bitmap == null)
			return null ;
		
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		// Setting post rotate to 90
		Matrix mtx = new Matrix();
		mtx.postRotate(rotate);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}
	
}
