package com.github.wuxudong.rncharts.charts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.github.mikephil.charting.animation.Easing.EasingOption;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineScatterCandleRadarDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.utils.FSize;
import com.github.wuxudong.rncharts.data.DataExtract;
import com.github.wuxudong.rncharts.data.DataSetLocate;
import com.github.wuxudong.rncharts.highlight.HighlightWithMeta;
import com.github.wuxudong.rncharts.markers.RNConditionalMarkerImage;
import com.github.wuxudong.rncharts.markers.RNRectangleMarkerView;
import com.github.wuxudong.rncharts.utils.BridgeUtils;
import com.github.wuxudong.rncharts.utils.ConversionUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class ChartBaseManager<T extends Chart, U extends Entry> extends SimpleViewManager {

    /**
     * Stores props that can only be set after chart data is set. 
     * Key is the prop name and value is the ReadableMap or ReadableArray passed in from js.
     *
     * Note: RN does not gurantee in what order the prop setters defined via @ReactProp
     * are called (e.g may not be the same order props are specified).
     */
    private Map<String, Object> mDataDependentProps = new HashMap<String, Object>();

    /**
     * Sets the data dependent props stored in mDataDependentProps. 
     * Clears mDataDependentProps afterwards.
     */
    private void setDataDependentProps(Chart chart) {

        if (chart.getData() == null) {
            throw new IllegalStateException("chart data must be set before setting data dependent props");
        }

        Iterator<Map.Entry<String, Object>> it = mDataDependentProps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pair = it.next();
            String propName = pair.getKey();
            switch (propName) {
                case "highlightValue":
                    setHighlightValue(chart, (ReadableMap)pair.getValue());
                    break;
                case "highlightEnabled":
                    setHighlightEnabled(chart, (ReadableArray)pair.getValue());
                    break;
                default:
                    throw new IllegalArgumentException("Undefined data dependent prop: " + propName);
            }
        }

        mDataDependentProps.clear();
    }

    abstract DataExtract getDataExtract(Context context);

    /**
     * More details about legend customization: https://github.com/PhilJay/MPAndroidChart/wiki/Legend
     */
    @ReactProp(name = "legend")
    public void setLegend(T chart, ReadableMap propMap) {
        Legend legend = chart.getLegend();

        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "enabled")) {
            legend.setEnabled(propMap.getBoolean("enabled"));
        }

        // Styling
        if (BridgeUtils.validate(propMap, ReadableType.Number, "textColor")) {
            legend.setTextColor(propMap.getInt("textColor"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "textSize")) {
            legend.setTextSize((float) propMap.getDouble("textSize"));
        }

        // Wrapping / clipping avoidance
        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "wordWrapEnabled")) {
            legend.setWordWrapEnabled(propMap.getBoolean("wordWrapEnabled"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "maxSizePercent")) {
            legend.setMaxSizePercent((float) propMap.getDouble("maxSizePercent"));
        }

        // Customizing
        if (BridgeUtils.validate(propMap, ReadableType.String, "position")) {
            legend.setPosition(LegendPosition.valueOf(propMap.getString("position").toUpperCase()));
        }
        if (BridgeUtils.validate(propMap, ReadableType.String, "form")) {
            legend.setForm(LegendForm.valueOf(propMap.getString("form").toUpperCase()));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "formSize")) {
            legend.setFormSize((float) propMap.getDouble("formSize"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "xEntrySpace")) {
            legend.setXEntrySpace((float) propMap.getDouble("xEntrySpace"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "yEntrySpace")) {
            legend.setYEntrySpace((float) propMap.getDouble("yEntrySpace"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "formToTextSpace")) {
            legend.setFormToTextSpace((float) propMap.getDouble("formToTextSpace"));
        }

        // Custom labels & colors
        if (BridgeUtils.validate(propMap, ReadableType.Map, "custom")) {
            ReadableMap customMap = propMap.getMap("custom");
            if (BridgeUtils.validate(customMap, ReadableType.Array, "colors") &&
                    BridgeUtils.validate(customMap, ReadableType.Array, "labels")) {

                ReadableArray colorsArray = customMap.getArray("colors");
                ReadableArray labelsArray = customMap.getArray("labels");

                if (colorsArray.size() == labelsArray.size()) {
                    // TODO null label should start a group
                    // TODO -2 color should avoid drawing a form
                    String[] labels = BridgeUtils.convertToStringArray(labelsArray);
                    int[] colorsParsed = BridgeUtils.convertToIntArray(colorsArray);

                    LegendEntry[] legendEntries = new LegendEntry[labels.length];
                    for (int i = 0; i < legendEntries.length; i++) {
                        legendEntries[i] = new LegendEntry();
                        legendEntries[i].formColor = colorsParsed[i];
                        legendEntries[i].label = labels[i];
                    }

                    legend.setCustom(legendEntries);
                }
            }
        }

        // TODO resetCustom function
        // TODO extra

        chart.invalidate();     // TODO is this necessary? Looks like enabled is not refreshing without it
    }

    @ReactProp(name = "logEnabled")
    public void setLogEnabled(Chart chart, boolean enabled) {
        chart.setLogEnabled(enabled);
    }

    @ReactProp(name = "chartBackgroundColor")
    public void setChartBackgroundColor(Chart chart, Integer color) {
        chart.setBackgroundColor(color);
    }

    @ReactProp(name = "chartDescription")
    public void setChartDescription(Chart chart, ReadableMap propMap) {

        Description description = new Description();

        if (BridgeUtils.validate(propMap, ReadableType.String, "text")) {
            description.setText(propMap.getString("text"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "textColor")) {
            description.setTextColor(propMap.getInt("textColor"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "textSize")) {
            description.setTextSize((float) propMap.getDouble("textSize"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "positionX") &&
                BridgeUtils.validate(propMap, ReadableType.Number, "positionY")) {
            description.setPosition((float) propMap.getDouble("positionX"), (float) propMap.getDouble("positionY"));
        }

        chart.setDescription(description);
    }

    @ReactProp(name = "noDataText")
    public void setNoDataText(Chart chart, String noDataText) {
        chart.setNoDataText(noDataText);
    }

    @ReactProp(name = "touchEnabled")
    public void setTouchEnabled(Chart chart, boolean enabled) {
        chart.setTouchEnabled(enabled);
    }

    @ReactProp(name = "dragDecelerationEnabled")
    public void setDragDecelerationEnabled(Chart chart, boolean enabled) {
        chart.setDragDecelerationEnabled(enabled);
    }

    @ReactProp(name = "dragDecelerationFrictionCoef")
    public void setDragDecelerationFrictionCoef(Chart chart, float coef) {
        chart.setDragDecelerationFrictionCoef(coef);
    }

    @ReactProp(name = "highlightPerTapEnabled")
    public void setHighlightPerTapEnabled(Chart chart, boolean enabled) {
        chart.setHighlightPerTapEnabled(enabled);
    }

    @ReactProp(name = "maxHighlightDistance")
    public void setMaxHighlightDistance(Chart chart, float dist) {
        chart.setMaxHighlightDistance(dist);
    }

    @ReactProp(name = "resetHighlightValue")
    public void resetHighlightValue(Chart chart, boolean reset) {
        if (reset) {
            chart.highlightValue(null, false);
        }
    }

    /**
     * Animations docs: https://github.com/PhilJay/MPAndroidChart/wiki/Animations
     */
    @ReactProp(name = "animation")
    public void setAnimation(Chart chart, ReadableMap propMap) {
        Integer durationX = null;
        Integer durationY = null;
        EasingOption easingX = EasingOption.Linear;
        EasingOption easingY = EasingOption.Linear;

        if (BridgeUtils.validate(propMap, ReadableType.Number, "durationX")) {
            durationX = propMap.getInt("durationX");
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "durationY")) {
            durationY = propMap.getInt("durationY");
        }
        if (BridgeUtils.validate(propMap, ReadableType.String, "easingX")) {
            easingX = EasingOption.valueOf(propMap.getString("easingX"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.String, "easingY")) {
            easingY = EasingOption.valueOf(propMap.getString("easingY"));
        }

        if (durationX != null && durationY != null) {
            chart.animateXY(durationX, durationY, easingX, easingY);
        } else if (durationX != null) {
            chart.animateX(durationX, easingX);
        } else if (durationY != null) {
            chart.animateY(durationY, easingY);
        }
    }

    /**
     * xAxis config details: https://github.com/PhilJay/MPAndroidChart/wiki/XAxis
     */
    @ReactProp(name = "xAxis")
    public void setXAxis(Chart chart, ReadableMap propMap) {
        XAxis axis = chart.getXAxis();

        setCommonAxisConfig(chart, axis, propMap);

        if (BridgeUtils.validate(propMap, ReadableType.Number, "labelRotationAngle")) {
            axis.setLabelRotationAngle((float) propMap.getDouble("labelRotationAngle"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "avoidFirstLastClipping")) {
            axis.setAvoidFirstLastClipping(propMap.getBoolean("avoidFirstLastClipping"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.String, "position")) {
            axis.setPosition(XAxisPosition.valueOf(propMap.getString("position")));
        }
    }

    @ReactProp(name = "marker")
    public void setMarker(Chart chart, ReadableMap propMap) {
        if (!BridgeUtils.validate(propMap, ReadableType.Boolean, "enabled") || !propMap.getBoolean("enabled")) {
            chart.setMarker(null);
            return;
        }

        RNRectangleMarkerView marker = new RNRectangleMarkerView(chart.getContext());
        marker.setChartView(chart);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                BridgeUtils.validate(propMap, ReadableType.Number, "markerColor")) {
            marker.getTvContent()
                    .setBackgroundTintList(
                            ColorStateList.valueOf(propMap.getInt("markerColor"))
                    );
        }

        if (BridgeUtils.validate(propMap, ReadableType.Number, "textColor")) {
            marker.getTvContent().setTextColor(propMap.getInt("textColor"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "textSize")) {
            marker.getTvContent().setTextSize(propMap.getInt("textSize"));
        }

        chart.setMarker(marker);
    }

    @ReactProp(name = "imageMarker")
    public void setImageMarker(Chart chart, ReadableMap propMap) {
        if (!BridgeUtils.validate(propMap, ReadableType.Boolean, "enabled") || !propMap.getBoolean("enabled")) {
            chart.setMarker(null);
            return;
        }

        Float offsetX = 0f;
        Float offsetY = 0f;
        Float width = 0f;
        Float height = 0f;
        ReadableArray excludes = null;

        if (BridgeUtils.validate(propMap, ReadableType.Number, "offsetX")) {
            offsetX = (float)propMap.getDouble("offsetX");
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "offsetY")) {
            offsetY = (float)propMap.getDouble("offsetY");
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "width")) {
            width = (float)propMap.getDouble("width");
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "height")) {
            height = (float)propMap.getDouble("height");
        }
        if (BridgeUtils.validate(propMap, ReadableType.Array, "excludes")) {
            excludes = propMap.getArray("excludes");
        }

        RNConditionalMarkerImage marker = new RNConditionalMarkerImage(
            chart.getContext(),
            excludes);
        
        marker.setOffset(offsetX, offsetY);
        marker.setSize(new FSize(width, height));

        chart.setMarker(marker);
    }

    /**
     * General axis config details: https://github.com/PhilJay/MPAndroidChart/wiki/The-Axis
     */
    protected void setCommonAxisConfig(Chart chart, AxisBase axis, ReadableMap propMap) {
        // what is drawn
        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "enabled")) {
            axis.setEnabled(propMap.getBoolean("enabled"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "drawLabels")) {
            axis.setDrawLabels(propMap.getBoolean("drawLabels"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "drawAxisLine")) {
            axis.setDrawAxisLine(propMap.getBoolean("drawAxisLine"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "drawGridLines")) {
            axis.setDrawGridLines(propMap.getBoolean("drawGridLines"));
        }

        // style
        if (BridgeUtils.validate(propMap, ReadableType.Number, "textColor")) {
            axis.setTextColor(propMap.getInt("textColor"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "textSize")) {
            axis.setTextSize((float) propMap.getDouble("textSize"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "gridColor")) {
            axis.setGridColor(propMap.getInt("gridColor"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "gridLineWidth")) {
            axis.setGridLineWidth((float) propMap.getDouble("gridLineWidth"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "axisLineColor")) {
            axis.setAxisLineColor(propMap.getInt("axisLineColor"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "axisLineWidth")) {
            axis.setAxisLineWidth((float) propMap.getDouble("axisLineWidth"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Map, "gridDashedLine")) {
            ReadableMap gridDashedLine = propMap.getMap("gridDashedLine");
            float lineLength = 0;
            float spaceLength = 0;
            float phase = 0;

            if (BridgeUtils.validate(gridDashedLine, ReadableType.Number, "lineLength")) {
                lineLength = (float) gridDashedLine.getDouble("lineLength");
            }
            if (BridgeUtils.validate(gridDashedLine, ReadableType.Number, "spaceLength")) {
                spaceLength = (float) gridDashedLine.getDouble("spaceLength");
            }
            if (BridgeUtils.validate(gridDashedLine, ReadableType.Number, "phase")) {
                phase = (float) gridDashedLine.getDouble("phase");
            }

            axis.enableGridDashedLine(lineLength, spaceLength, phase);
        }

        // limit lines
        if (BridgeUtils.validate(propMap, ReadableType.Array, "limitLines")) {
            ReadableArray limitLines = propMap.getArray("limitLines");

            for (int i = 0; i < limitLines.size(); i++) {
                if (!ReadableType.Map.equals(limitLines.getType(i))) {
                    continue;
                }

                ReadableMap limitLineMap = limitLines.getMap(i);
                if (BridgeUtils.validate(limitLineMap, ReadableType.Number, "limit")) {
                    LimitLine limitLine = new LimitLine((float) limitLineMap.getDouble("limit"));

                    if (BridgeUtils.validate(limitLineMap, ReadableType.String, "label")) {
                        limitLine.setLabel(limitLineMap.getString("label"));
                    }
                    if (BridgeUtils.validate(limitLineMap, ReadableType.Number, "lineColor")) {
                        limitLine.setLineColor(limitLineMap.getInt("lineColor"));
                    }
                    if (BridgeUtils.validate(limitLineMap, ReadableType.Number, "lineWidth")) {
                        limitLine.setLineWidth((float) limitLineMap.getDouble("lineWidth"));
                    }

                    axis.addLimitLine(limitLine);
                }

            }
        }
        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "drawLimitLinesBehindData")) {
            axis.setDrawLimitLinesBehindData(propMap.getBoolean("drawLimitLinesBehindData"));
        }

        if (BridgeUtils.validate(propMap, ReadableType.Number, "axisMaximum")) {
            axis.setAxisMaximum((float) propMap.getDouble("axisMaximum"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "axisMinimum")) {
            axis.setAxisMinimum((float) propMap.getDouble("axisMinimum"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "granularity")) {
            axis.setGranularity((float) propMap.getDouble("granularity"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "granularityEnabled")) {
            axis.setGranularityEnabled(propMap.getBoolean("granularityEnabled"));
        }
        if (BridgeUtils.validate(propMap, ReadableType.Number, "labelCount")) {
            boolean labelCountForce = false;
            if (BridgeUtils.validate(propMap, ReadableType.Boolean, "labelCountForce")) {
                labelCountForce = propMap.getBoolean("labelCountForce");
            }
            axis.setLabelCount(propMap.getInt("labelCount"), labelCountForce);
        }

        // formatting
        if (BridgeUtils.validate(propMap, ReadableType.String, "valueFormatter")) {
            String valueFormatter = propMap.getString("valueFormatter");

            if ("largeValue".equals(valueFormatter)) {
                axis.setValueFormatter(new LargeValueFormatter());
            } else if ("percent".equals(valueFormatter)) {
                axis.setValueFormatter(new PercentFormatter());
            } else {
                axis.setValueFormatter(new CustomFormatter(valueFormatter));
            }
        } else if (BridgeUtils.validate(propMap, ReadableType.Array, "valueFormatter")) {
            axis.setValueFormatter(new IndexAxisValueFormatter(BridgeUtils.convertToStringArray(propMap.getArray("valueFormatter"))));
        }

        if (BridgeUtils.validate(propMap, ReadableType.Boolean, "centerAxisLabels")) {
            axis.setCenterAxisLabels(propMap.getBoolean("centerAxisLabels"));
        }
    }

    /**
     * Dataset config details: https://github.com/PhilJay/MPAndroidChart/wiki/DataSet-classes-in-detail
     */
    @ReactProp(name = "data")
    public void setData(Chart chart, ReadableMap propMap) {
        ChartData data = getDataExtract(chart.getContext()).extract(propMap);
        // data maybe null when the prop is removed temporarily from js side
        if (data != null) {
            chart.setData(data);
            chart.invalidate();
            setDataDependentProps(chart);
        }
    }

    /**
     * update dataset entries
     */
    @ReactProp(name = "updatedEntries")
    public void setUpdatedEntries(Chart chart, final ReadableArray dsArray) {
        final DataExtract de = getDataExtract(chart.getContext());

        new DataSetLocate() {

            @Override
            protected void onLocateDataset(IDataSet dataset, ChartData data, ReadableMap dsProp) {
                // apply updates
                if (dsProp.hasKey("updates")) {
                    ReadableArray updateArray = dsProp.getArray("updates");
                    List<Entry> updatedEntries = de.createEntries(updateArray);
                    for (int j = 0; j < updateArray.size(); j++) {
                        float x = (float) updateArray.getMap(j).getDouble("x");
                        Entry newE = updatedEntries.get(j);

                        List<Entry> entries = dataset.getEntriesForXValue(x);
                        if (entries.isEmpty()) {
                            throw new IllegalArgumentException("Attempt to update invalid entry at " + x);
                        } else {
                            Entry e = entries.get(0);
                            e.setY(newE.getY());
                            e.setData(newE.getData());
                            e.setIcon(newE.getIcon());
                        }
                    }
                }

                // apply removes
                if (dsProp.hasKey("removes")) {
                    ReadableArray removeArray = dsProp.getArray("removes");
                    for (int j = 0; j < removeArray.size(); j++) {
                        ReadableMap entryMap = removeArray.getMap(j);
                        float x = (float) entryMap.getDouble("x");

                        List<Entry> entries = dataset.getEntriesForXValue(x);
                        if (entries.isEmpty()) {
                            throw new IllegalArgumentException("Attempt to remove invalid entry at " + x);
                        } else {
                            dataset.removeEntry(entries.get(0));
                        }
                    }
                }
                    
                // apply adds
                if (dsProp.hasKey("adds")) {
                    ReadableArray addArray = dsProp.getArray("adds");
                    List<Entry> addedEntries = de.createEntries(addArray);
                    for (int j = 0; j < addArray.size(); j++) {
                        Entry newE = addedEntries.get(j);
                        
                        if (newE == null) {
                            throw new IllegalArgumentException("Attempt to add invalid entry");
                        }
                        dataset.addEntryOrdered(newE);

                        int idx = dataset.getEntryIndex(newE.getX(), Float.NaN, DataSet.Rounding.UP);
                        if (dataset.getEntryForIndex(idx) != newE) {
                            // Note (jessica): This is not working properly as of MPAndroidChart v3.0.3
                            // See GH issue: https://github.com/PhilJay/MPAndroidChart/issues/4052
                            throw new IllegalStateException("Did not properly add entry ordered");
                        }
                    }
                }
                
                // notify changes
                data.notifyDataChanged();
            }
        }.start(chart, dsArray);

        chart.invalidate();
    }

    @ReactProp(name = "highlightIndicators")
    public void setHighlighIndicators(Chart chart, ReadableArray dsArray) {

        new DataSetLocate() {

            @Override
            protected void onLocateDataset(IDataSet dataset, ChartData data, ReadableMap dsProp) {
                if (!(dataset instanceof LineScatterCandleRadarDataSet))
                    return;

                ((LineScatterCandleRadarDataSet)dataset).setDrawHorizontalHighlightIndicator(
                        dsProp.hasKey("horizontalEnabled") && dsProp.getBoolean("horizontalEnabled"));
                ((LineScatterCandleRadarDataSet)dataset).setDrawVerticalHighlightIndicator(
                        dsProp.hasKey("verticalEnabled") && dsProp.getBoolean("verticalEnabled"));
            }
        }.start(chart, dsArray);
    }

    /**
     * details: https://github.com/PhilJay/MPAndroidChart/wiki/Highlighting
     */
    @ReactProp(name = "highlightValue")
    public void setHighlightValue(Chart chart, ReadableMap propMap) {
        // chart.highlightValue must be called after data is set or a NPE would occur
        if (chart.getData() == null) {
            mDataDependentProps.put("highlightValue", propMap);
        } else {
            Map meta = new HashMap<String, Object>();
            meta.put("source", "program");
            if (BridgeUtils.validate(propMap, ReadableType.Map, "meta")) {
                meta.putAll(ConversionUtil.toMap(propMap.getMap("meta")));
            }

            HighlightWithMeta h = new HighlightWithMeta(
                (float) propMap.getDouble("x"), 
                propMap.hasKey("dataSetIndex") ? propMap.getInt("dataSetIndex") : 0,
                meta
            );

            // dataIndex required for CombinedChart to pin point a highlight position
            Integer dataIndex = null;
            if (BridgeUtils.validate(propMap, ReadableType.Number, "dataIndex")) {
                dataIndex = propMap.getInt("dataIndex");
            }
            if (dataIndex != null) {
                h.setDataIndex(dataIndex);
            }

            chart.highlightValue(
                h,
                propMap.hasKey("callListener") ? propMap.getBoolean("callListener") : false
            );
        }
    }

    /**
     * toggle highlightEnabled. propMap is an array of:
     * enabled - boolean
     * dataSetIndex - optional int, apply to all dataSets of given data if not specified
     * dataIndex - optional int, required for CombinedChart
     */
    @ReactProp(name = "highlightEnabled")
    public void setHighlightEnabled(Chart chart, ReadableArray readableArray) {
        // chart.highlightEnabled must be called after data is set or a NPE would occur
        if (chart.getData() == null) {
            mDataDependentProps.put("highlightEnabled", readableArray);
        } else {
            new DataSetLocate() {

                @Override
                protected void onLocateDataset(IDataSet dataset, ChartData data, ReadableMap dsProp) {
                    dataset.setHighlightEnabled(dsProp.getBoolean("enabled"));
                }
            }.start(chart, readableArray);

        }
    }

}
