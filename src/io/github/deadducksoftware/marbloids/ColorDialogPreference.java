package io.github.deadducksoftware.marbloids;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class ColorDialogPreference extends DialogPreference {
	
	public interface OnColorChangedListener {
		void colorChanged(int color);
	}
	
	private int mSelectedColor;
	private int mDefaultColor;
	private OnColorChangedListener mColorChangedListener;
	private TextView mSummaryText;

	public ColorDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		mColorChangedListener = new OnColorChangedListener() {
			@Override
			public void colorChanged(int c) {
				ColorDialogPreference.this.mSelectedColor = c;
			}
		};
		builder.setView(new ColorPickerView(getContext(), mColorChangedListener));
		super.onPrepareDialogBuilder(builder);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult && mSelectedColor != 0)
			persistInt(mSelectedColor);
		super.onDialogClosed(positiveResult);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mSummaryText = (TextView) view.findViewById(android.R.id.summary);
		updateSummaryColor();
	}
	
	public void setDefaultColor(int color) {
		mDefaultColor = color;
	}
	
	public void updateSummaryColor() {
        if (mSummaryText != null)
            mSummaryText.setTextColor(getPersistedInt(mDefaultColor));
	}
	
	private class ColorPickerView extends View {
		
		private Paint mOuterPaint;
		private Paint mCenterPaint;
		private Rect mClipBounds;
		private RectF mDrawRect;
		private float centerX;
		private float centerY;
		
		private final int[] mColors = new int[] {
			0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFFFFFFFF, 0xFF000000,
			0xFF00FFFF, 0xFF00FF00,	0xFFFFFF00, 0xFFFF0000
		};

		ColorPickerView(Context context, OnColorChangedListener listener) {
			super(context);
			mOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mOuterPaint.setShader(new SweepGradient(0, 0, mColors, null));
			mOuterPaint.setStyle(Paint.Style.STROKE);
			mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCenterPaint.setColor(getPersistedInt(mDefaultColor));
			mClipBounds = new Rect();
			mDrawRect = new RectF();
		}
 		
		protected void onDraw(Canvas canvas) {
			canvas.getClipBounds(mClipBounds);
			float width = mClipBounds.right - mClipBounds.left;
			float height = mClipBounds.bottom - mClipBounds.top;
			centerX = mClipBounds.left + (width / 2); 
			centerY = mClipBounds.top + (height / 2); 
			float radius = centerX < centerY ? centerX : centerY;
			mOuterPaint.setStrokeWidth(radius / 3);
			mCenterPaint.setStrokeWidth(radius / 3);
			radius = radius - mOuterPaint.getStrokeWidth();
			mDrawRect.set(-radius, -radius, radius, radius);
			canvas.translate(centerX, centerY);
			canvas.drawOval(mDrawRect, mOuterPaint);
			canvas.drawCircle(0, 0, radius / 3, mCenterPaint);
		}

		private int ave(int s, int d, float p) {
			return s + Math.round(p * (d - s));
		}

		private int interpColor(int colors[], float unit) {
			if (unit <= 0)
				return colors[0];
			if (unit >= 1)
				return colors[colors.length - 1];
			float p = unit * (colors.length - 1);
			int i = (int) p;
			p -= i;
			// now p is just the fractional part (0...1) and i is the index
			int c0 = colors[i];
			int c1 = colors[i + 1];
			int a = ave(Color.alpha(c0), Color.alpha(c1), p);
			int r = ave(Color.red(c0), Color.red(c1), p);
			int g = ave(Color.green(c0), Color.green(c1), p);
			int b = ave(Color.blue(c0), Color.blue(c1), p);
			return Color.argb(a, r, g, b);
		}

		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX() - centerX;
			float y = event.getY() - centerY;
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					float angle = (float) java.lang.Math.atan2(y, x);
					// need to turn angle [-PI ... PI] into unit [0....1]
					float unit = (float)(angle / (2 * Math.PI));
					if (unit < 0)
						unit += 1;
					mCenterPaint.setColor(interpColor(mColors, unit));
					invalidate();
					break;
				case MotionEvent.ACTION_UP:
					ColorDialogPreference.this.mSelectedColor = mCenterPaint.getColor();
					break;
			}
			return true;
		}
	}	
}
