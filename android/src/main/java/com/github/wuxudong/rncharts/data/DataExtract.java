package com.github.wuxudong.rncharts.data;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.uimanager.ThemedReactContext;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.wuxudong.rncharts.utils.BridgeUtils;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by xudong on 02/03/2017.
 */

public abstract class DataExtract<D extends ChartData, U extends Entry> {

    public D extract(ReadableMap propMap) {
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
                dataSetConfig(dataSet, dataSetReadableMap.getMap("config"));
            }

            chartData.addDataSet(dataSet);
        }

        if (BridgeUtils.validate(propMap, ReadableType.Map, "config")) {
            dataConfig(chartData, propMap.getMap("config"));
        }


        return chartData;
    }

    abstract D createData();

    void dataConfig(D data, ReadableMap config) {}

    abstract IDataSet<U> createDataSet(ArrayList<U> entries, String label);

    abstract void dataSetConfig(IDataSet<U> dataSet, ReadableMap config);

    ArrayList<U> createEntries(ReadableArray yValues) {
        ArrayList<U> entries = new ArrayList<>(yValues.size());
        for (int j = 0; j < yValues.size(); j++) {
            if (!yValues.isNull(j)) {
                entries.add(createEntry(yValues, j));
            }
        }
        return entries;
    }

    abstract U createEntry(ReadableArray values, int index);


    protected Drawable getIconDrawable(ThemedReactContext context, ReadableMap map) {
        if (!map.hasKey("icon")) {
            return null;
        }

        String iconName = map.getString("icon");

        Drawable drawable = null;
        InputStream is = null;
        Bitmap b = null;
        Bitmap sb = null;
        if (context != null && iconName != null) {
            try {
                int color = map.hasKey("iconColor") ? map.getInt("iconColor") : Color.WHITE;
                int size = map.hasKey("iconSize") ? map.getInt("iconSize") : 100;
                is = context.getAssets().open(iconName);
                BitmapDrawable bd = new BitmapDrawable(context.getResources(), is);
                b = bd.getBitmap();
                sb = Bitmap.createScaledBitmap(b, size, size, false);
                bd = new BitmapDrawable(context.getResources(), sb);
                drawable = (Drawable) bd;
                drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
            } catch (IOException e) {
                Log.e("wuxudong/react-native-charts-wrapper", "Error creating icon drawable", e);
            } finally {
                if (is != null) {
                    try { is.close(); }
                    catch (IOException e) { Log.e("wuxudong/react-native-charts-wrapper", "Error closing stream", e); } }
                if (b != null) { b.recycle(); }
            }
        }
        return drawable;
    }
}
