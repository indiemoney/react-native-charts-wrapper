package com.github.wuxudong.rncharts.markers;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

public interface IMarkerConditionFn {

    /**
     * @return if a marker should be drawn for a given highlight entry.
     */
    boolean call(Entry e, Highlight h);
}
