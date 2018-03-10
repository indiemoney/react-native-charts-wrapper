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

    private static WritableMap viewPortToWritableMap(Chart chart, String eventName, MotionEvent me) {
        WritableMap map = new WritableNativeMap();
        map.putString("eventName", eventName);
        map.putDouble("transX", chart.getViewPortHandler().getTransX());
        map.putDouble("transY", chart.getViewPortHandler().getTransY());
        map.putDouble("scaleX", chart.getViewPortHandler().getScaleX());
        map.putDouble("scaleY", chart.getViewPortHandler().getScaleY());
        map.putDouble("chartWidth", chart.getViewPortHandler().getChartWidth());
        map.putDouble("chartHeight", chart.getViewPortHandler().getChartHeight());
        map.putDouble("touchX", me.getX());
        map.putDouble("touchY", me.getY());

        return map;
    }

    private void handleEvent(String eventName, MotionEvent me) {
        if (mWeakChart != null) {
            Chart chart = mWeakChart.get();

            ReactContext reactContext = (ReactContext) chart.getContext();
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                    chart.getId(),
                    "topMessage",
                    viewPortToWritableMap(chart, eventName, me));
        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        handleEvent("onGestureStart", me);
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        handleEvent("onGestureEnd", me);
    }

    
    @Override
    public void onChartLongPressed(MotionEvent me) { handleEvent("onLongPressed", me); }

    @Override
    public void onChartDoubleTapped(MotionEvent me) { handleEvent("onDoubleTapped", me); }

    @Override
    public void onChartSingleTapped(MotionEvent me) { handleEvent("onSingleTapped", me); }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) { handleEvent("onFling", me1); }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) { handleEvent("onScale", me); }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) { handleEvent("onTranslate", me); }

}
