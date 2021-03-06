package com.github.wuxudong.rncharts.data;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.wuxudong.rncharts.utils.BridgeUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import java.util.ArrayList;

/**
 * Created by xudong on 02/03/2017.
 */

public abstract class DataExtract<D extends ChartData, U extends Entry> {

    public D extract(Chart chart, ReadableMap propMap) {
        if (!BridgeUtils.validate(propMap, ReadableType.Array, "dataSets")) {
            return null;
        }

        D chartData = createData();


        ReadableArray dataSets = propMap.getArray("dataSets");
        for (int i = 0; i < dataSets.size(); i++) {
            ReadableMap dataSetReadableMap = dataSets.getMap(i);

            // TODO validation
            ReadableArray values = dataSetReadableMap.getArray("values");
            String label = dataSetReadableMap.getString("label");

            ArrayList<U> entries = createEntries(values);

            IDataSet<U> dataSet = createDataSet(entries, label);

            if (BridgeUtils.validate(dataSetReadableMap, ReadableType.Map, "config")) {
                dataSetConfig(chart, dataSet, dataSetReadableMap.getMap("config"));
            }

            chartData.addDataSet(dataSet);
        }

        if (BridgeUtils.validate(propMap, ReadableType.Map, "config")) {
            dataConfig(chartData, propMap.getMap("config"));
        }


        return chartData;
    }

    abstract D createData();

    void dataConfig(D data, ReadableMap config) {
    }

    abstract IDataSet<U> createDataSet(ArrayList<U> entries, String label);

    abstract void dataSetConfig(Chart chart, IDataSet<U> dataSet, ReadableMap config);

    public ArrayList<U> createEntries(ReadableArray yValues) {
        ArrayList<U> entries = new ArrayList<>(yValues.size());
        for (int j = 0; j < yValues.size(); j++) {
            if (!yValues.isNull(j)) {
                entries.add(createEntry(yValues, j));
            }
        }
        return entries;
    }

    abstract U createEntry(ReadableArray values, int index);


    private void drawBitmap(Context context, Canvas c, ReadableMap icon) {
        Drawable drawable = null;
        Bitmap b = null;
        String path = icon.hasKey("path") ? icon.getString("path") : null;

        if (path == null) {
            return;
        }

        int resId = context.getResources().getIdentifier(path, "drawable", context.getPackageName());
        if (resId == 0) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resId, null);
        }
        else {
            drawable = context.getResources().getDrawable(resId);
        }

        int color = icon.hasKey("color") ? icon.getInt("color") : Color.WHITE;
        float size = icon.hasKey("size") ? (float) icon.getDouble("size") : 100f;
        float offsetLeft = icon.hasKey("left") ? (float) icon.getDouble("left") : 0f;
        float offsetTop = icon.hasKey("top") ? (float) icon.getDouble("top") : 0f;

        try {
            b = BitmapFactory.decodeResource(context.getResources(), resId);
            int ow = b.getWidth();
            int oh = b.getHeight();

            Matrix m = new Matrix();
            m.postScale(size / ow, size / oh);
            m.postTranslate(offsetLeft, offsetTop);

            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setDither(true);
            p.setFilterBitmap(true);
            p.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
            c.drawBitmap(b, m, p);
        } finally {
            if (b != null) { b.recycle(); }
        }
    }

    private void drawSquare(Canvas c, ReadableMap icon) {
        int color = icon.hasKey("color") ? icon.getInt("color") : Color.WHITE;
        float size = icon.hasKey("size") ? (float) icon.getDouble("size") : 100f;
        float cx = icon.hasKey("cx") ? (float) icon.getDouble("cx") : 0f;
        float cy = icon.hasKey("cy") ? (float) icon.getDouble("cy") : 0f;
        float r = icon.hasKey("r") ? (float) icon.getDouble("r") : 0f;

        float d = size * 0.5f;
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setFilterBitmap(true);
        p.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        c.drawRoundRect(cx - d, cy - d, cx + d, cy + d, r, r, p);
    }

    private void drawText(Context context, Canvas c, ReadableMap icon) {
        String fontPath = icon.hasKey("path") ? icon.getString("path") : null;
        String text = icon.hasKey("text") ? icon.getString("text") : null;
        int color = icon.hasKey("color") ? icon.getInt("color") : Color.WHITE;
        float size = icon.hasKey("size") ? (float) icon.getDouble("size") : 10f;

        float cx = icon.hasKey("cx") ? (float) icon.getDouble("cx") : 0f;
        float cy = icon.hasKey("cy") ? (float) icon.getDouble("cy") : 0f;

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setFilterBitmap(true);

        if (fontPath != null) {
            Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
            p.setTypeface(tf);
        }
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(size);
        p.setColor(color);

        if (text != null) {
            c.drawText(text, cx, cy - (p.ascent() / 2), p);
        }
    }

    protected Drawable getIconDrawable(Context context, ReadableMap map) {
        if (!map.hasKey("icons")) {
            return null;
        }

        ReadableArray icons = map.getArray("icons");
        int size = map.hasKey("iconSize") ? map.getInt("iconSize") : 100;

        Drawable drawable = null;
        Bitmap b = null;
        if (context != null && icons != null) {
            b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            for (int i = 0; i < icons.size(); i++) {
                ReadableMap icon = icons.getMap(i);
                String type = icon.hasKey("type") ? icon.getString("type") : "bitmap";

                switch (type) {
                case "square": drawSquare(c, icon); break;
                case "text": drawText(context, c, icon); break;
                default: drawBitmap(context, c, icon); break;
                }
            }

            BitmapDrawable bd = new BitmapDrawable(context.getResources(), b);
            bd.setAntiAlias(true);
            bd.setFilterBitmap(true);
            drawable = (Drawable) bd;

        }
        return drawable;
    }
}
