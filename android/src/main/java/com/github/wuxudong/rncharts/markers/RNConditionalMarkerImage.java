package com.github.wuxudong.rncharts.markers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Canvas;

import com.facebook.react.bridge.ReadableMap;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerImage;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.wuxudong.rncharts.R;
import com.github.wuxudong.rncharts.markers.IMarkerConditionFn;

import java.util.List;
import java.util.Map;

/**
 * Allows skipping drawing of image marker based on custom conditions.
 * 
 */
public class RNConditionalMarkerImage extends MarkerImage {
    
    private boolean mSkipDraw;
    private IMarkerConditionFn mConditionFn;
    
    
    public RNConditionalMarkerImage(Context context, int drawableResourceId, IMarkerConditionFn conditionFn) {
        super(context, drawableResourceId);
        
        mSkipDraw = false;
        mConditionFn = conditionFn;
    }
    
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        mSkipDraw = mConditionFn.call(e, highlight);
        
        super.refreshContent(e, highlight);
    }
    
    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        if (mSkipDraw) return;

        super.draw(canvas, posX, posY);
    }
}

