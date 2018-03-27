package com.github.wuxudong.rncharts.highlight;

import com.github.mikephil.charting.highlight.Highlight;

public class HighlightWithMeta extends Highlight {

    private Object mMetaData;

    public HighlightWithMeta(float x, int dataSetIndex, Object metaData) {
        super(x, Float.NaN, dataSetIndex);
        mMetaData = metaData;
    }

    public Object getMetaData() {
    	return mMetaData;
    }

    public void setMetaData(Object metaData) {
    	mMetaData = metaData;
    }

}
