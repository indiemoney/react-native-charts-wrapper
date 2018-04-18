package com.github.wuxudong.rncharts.formatters;


import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

/**
 * Fill formatter that fills above the line instead of below.
 * 

 * @author jlo1 04/19/2018
 */
public class MinFillFormatter implements IFillFormatter
{

    @Override
    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
        return dataProvider.getYChartMin();
    }
}
