package com.github.wuxudong.rncharts.markers;

import android.content.Context;
import android.graphics.Canvas;

import com.facebook.react.bridge.ReadableArray;
import com.github.mikephil.charting.components.MarkerImage;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.wuxudong.rncharts.highlight.HighlightWithMeta;
import com.github.wuxudong.rncharts.utils.ConversionUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allows skipping drawing of image marker based on conditions.
 * 
 * Image marker is skipped for an entry with highlight if any of these is true:
 *  1. highlight is on a excluded dataset
 *  2. entry's markerHighlightSource does not match highligh's source
 */
public class RNConditionalMarkerImage extends RNImageMarker {
    
    private boolean mSkipDraw;
    /*
     * Map of dataIndex to a set of dataSetIndex of that data, representing 
     * the datasets to be excluded for showing marker. 
     *
     * Null set indicates all dataset.
     */
    private Map<Integer, Set<Integer>> mExcludes;
    
    private Map<Integer, Set<Integer>> parseExcludesProp(ReadableArray excludesProp) {
        if (excludesProp != null) {
            List excludeList = ConversionUtil.toList(excludesProp);
            Map<Integer, Set<Integer>> excludes =  new HashMap<Integer, Set<Integer>>();

            for (Object exclude : excludeList) {
                Map excludeMap = (Map)exclude;
                List dataSetIndexesArray = (List)excludeMap.get("dataSetIndexes");
                Set<Integer> dataSetIndexes = null;
                if (dataSetIndexesArray != null) {
                    dataSetIndexes = new HashSet<Integer>();
                    for (Object dataSetIndex : dataSetIndexesArray) {
                        dataSetIndexes.add((Integer)dataSetIndex);
                    }
                }
                
                excludes.put((Integer)excludeMap.get("dataIndex"), dataSetIndexes);
            }

            return excludes;
        }

        return null;
    }

    public RNConditionalMarkerImage(Context context, ReadableArray excludesProp) {
        super(context);
        
        mSkipDraw = false;
        mExcludes = parseExcludesProp(excludesProp);
    }
    
    @Override
    public void refreshContent(Entry e, Highlight h) {
        int hDI = h.getDataIndex();
        int hDSI = h.getDataSetIndex();

        boolean excluded = false;

        if (mExcludes != null // default excludes nothing
            && mExcludes.containsKey(hDI)
            && (mExcludes.get(hDI) == null // default excludes every detaset of when given data
                || mExcludes.get(hDI).contains(hDSI))) {
            excluded = true;
        }
        
        // excludes take priority
        if (excluded) {

            mSkipDraw = true;

        } else {

            Map hMeta = (h instanceof HighlightWithMeta) ? ((HighlightWithMeta) h).getMetaData() : null;
            String hSrc = (hMeta != null && hMeta.containsKey("source")) ? (String) hMeta.get("source") : "tap";

            Map eData = (e.getData() instanceof Map) ? (Map)e.getData() : null;
            String eMarkerSrc = (String) eData.get("markerHighlightSource");

            if (eMarkerSrc != null // default don't skip
                && ("none".equals(eMarkerSrc) // none always skip
                    // otherwise skip if sources don't match
                    || !eMarkerSrc.equals(hSrc))) { 
                mSkipDraw = true;
            }
        }

        if (!mSkipDraw) super.refreshContent(e, h);
    }
    
    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        if (mSkipDraw) {
            mSkipDraw = false;
            return;
        }

        super.draw(canvas, posX, posY);
    }
}

