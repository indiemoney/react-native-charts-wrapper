package com.github.wuxudong.rncharts.data;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

/**
 * Created by idealllee on 4/3/18.
 */
public abstract class DataSetLocate {

    protected abstract void onLocateDataset(IDataSet dataset, ChartData data, ReadableMap dsProp);

    /**
     * Given an array of maps each containing optional dataIndex and dataSetIndex keys,
     * invoke onLocateDataset for each dataset found.
     * @param chart
     * @param dsArray
     */
    public void start(Chart chart, ReadableArray dsArray) {
        for (int i = 0; i < dsArray.size(); i++) {
            ReadableMap dsProp = dsArray.getMap(i);
            // locate dataset
            int dataIndex = dsProp.hasKey("dataIndex") ? dsProp.getInt("dataIndex") : 0;
            int dataSetIndex = dsProp.hasKey("dataSetIndex") ? dsProp.getInt("dataSetIndex") : 0;

            ChartData data =
                    (chart.getData() instanceof CombinedData) ?
                            ((CombinedData) chart.getData()).getAllData().get(dataIndex) :
                            chart.getData();

            IDataSet dataset = (IDataSet) data.getDataSets().get(dataSetIndex);

            onLocateDataset(dataset, data, dsArray.getMap(i));
        }
    }
}
