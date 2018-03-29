package com.github.wuxudong.rncharts.highlight;

import com.github.mikephil.charting.highlight.Highlight;
import java.util.Map;

public class HighlightWithMeta extends Highlight {

    private Map mMetaData;

    public HighlightWithMeta(float x, int dataSetIndex, Map metaData) {
        super(x, Float.NaN, dataSetIndex);
        mMetaData = metaData;
    }

    public Map getMetaData() {
    	return mMetaData;
    }

    public void setMetaData(Map metaData) {
    	mMetaData = metaData;
    }

}
