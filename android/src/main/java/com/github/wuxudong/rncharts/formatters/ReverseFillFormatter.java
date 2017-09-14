package com.github.wuxudong.rncharts.formatters;


import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

/**
 * Fill formatter that fills above the line instead of below.
 * Same as DefaultFillFormatter from native library, other than reversing chartMaxY and chartMinY.
 *
 * See https://github.com/PhilJay/MPAndroidChart/blob/v3.0.2/MPChartLib/src/main/java/com/github/mikephil/charting/formatter/DefaultFillFormatter.java
 * and https://github.com/PhilJay/MPAndroidChart/issues/1178

 * @author jlo1 08/29/2017
 */
public class ReverseFillFormatter implements IFillFormatter
{

    @Override
    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {

        float fillMin = 0f;
        float chartMaxY = dataProvider.getYChartMin();
        float chartMinY = dataProvider.getYChartMax();

        LineData data = dataProvider.getLineData();

        if (dataSet.getYMax() > 0 && dataSet.getYMin() < 0) {
            fillMin = 0f;
        } else {

            float max, min;

            if (data.getYMax() > 0)
                max = 0f;
            else
                max = chartMaxY;
            if (data.getYMin() < 0)
                min = 0f;
            else
                min = chartMinY;

            fillMin = dataSet.getYMin() >= 0 ? min : max;
        }

        return fillMin;
    }
}
