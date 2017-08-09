package com.github.wuxudong.rncharts.listener;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.ChartTouchListener;

import android.view.MotionEvent;
import java.lang.ref.WeakReference;

/**
 * Created by jlo1 on 08/08/2017
 */

public class RNOnChartGestureListener implements OnChartGestureListener {

    private WeakReference<Chart> mWeakChart;

    public RNOnChartGestureListener(Chart chart) {
        mWeakChart = new WeakReference<>(chart);
    }

    private static WritableMap viewPortToWritableMap(Chart chart, String eventName) {
        WritableMap map = new WritableNativeMap();
        map.putString("eventName", eventName);
        map.putDouble("transX", chart.getViewPortHandler().getTransX());
        map.putDouble("transY", chart.getViewPortHandler().getTransY());
        map.putDouble("scaleX", chart.getViewPortHandler().getScaleX());
        map.putDouble("scaleY", chart.getViewPortHandler().getScaleY());
        map.putDouble("chartWidth", chart.getViewPortHandler().getChartWidth());
        map.putDouble("chartHeight", chart.getViewPortHandler().getChartHeight());

        return map;
    }

    private void handleEvent(String eventName) {
        if (mWeakChart != null) {
            Chart chart = mWeakChart.get();

            ReactContext reactContext = (ReactContext) chart.getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                    chart.getId(),
                    "topMessage",
                    viewPortToWritableMap(chart, eventName));
        }
    }
    
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        handleEvent("onGestureStart");
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        handleEvent("onGestureEnd");
    }

    
    @Override
    public void onChartLongPressed(MotionEvent me) { handleEvent("onLongPressed"); }

    @Override
    public void onChartDoubleTapped(MotionEvent me) { handleEvent("onDoubleTapped"); }

    @Override
    public void onChartSingleTapped(MotionEvent me) { handleEvent("onSingleTapped"); }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) { handleEvent("onFling"); }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) { handleEvent("onScale"); }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) { handleEvent("onTranslate"); }

}
