package com.sxdxz.liao.superscript;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public class SelectView extends View {
	private Paint greyPaint;
	public int borderWidth = 24;
	public int borderHeight = 24;
	public int rectWidth;
	private Paint borderPaint;

	public SelectView(Context context) {
		super(context);
		init();
	}

	public SelectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SelectView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		greyPaint = new Paint();
		greyPaint.setColor(0xaa000000);
		borderPaint = new Paint();
		borderPaint.setColor(getResources().getColor(R.color.blue));

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		rectWidth = (int) getResources().getDimension(R.dimen.photo_width);
		borderHeight = (int) getResources().getDimension(R.dimen.title_height);
		borderWidth = (dm.widthPixels - rectWidth) / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/* 这里就是绘制矩形区域 */
		int width = this.getWidth();
		int height = this.getHeight();

		// top
		canvas.drawRect(0, 0, width, borderHeight, greyPaint);
		canvas.drawLine(borderWidth, borderHeight - 1, width - borderWidth,
				borderHeight - 1, borderPaint);
		// right
		canvas.drawRect(width - borderWidth, borderHeight, width, borderHeight
				+ rectWidth, greyPaint);
		canvas.drawLine(width - borderWidth, borderHeight, width - borderWidth,
				borderHeight + rectWidth, borderPaint);
		// left
		canvas.drawRect(0, borderHeight, borderWidth, borderHeight + rectWidth,
				greyPaint);
		canvas.drawLine(borderWidth - 1, borderHeight, borderWidth - 1,
				borderHeight + rectWidth, borderPaint);
		// bottom
		canvas.drawRect(0, borderHeight + rectWidth, width, height, greyPaint);
		canvas.drawLine(borderWidth, borderHeight + rectWidth, borderWidth
				+ rectWidth, borderHeight + rectWidth, borderPaint);
	}

}
