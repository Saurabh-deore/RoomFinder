package com.develophub.roomfinder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.view.View.MeasureSpec;

public class Utility {

    /**
     * NestedScrollView के अंदर होने पर GridView की पूरी हाइट को सेट करता है।
     * यह सुनिश्चित करता है कि GridView के सभी आइटम दिखें और नीचे कोई अनावश्यक गैप न हो।
     * * यह लॉजिक GridView के width को 0 न होने की स्थिति को मानकर चलता है (जब gridView.post() में कॉल किया जाता है)।
     */
    public static void setGridViewHeightBasedOnChildren(GridView gridView) {
        ListAdapter listAdapter = gridView.getAdapter();

        if (listAdapter == null || listAdapter.getCount() == 0) {
            ViewGroup.LayoutParams params = gridView.getLayoutParams();
            params.height = 0;
            gridView.setLayoutParams(params);
            gridView.requestLayout();
            return;
        }

        int items = listAdapter.getCount();
        int columns = gridView.getNumColumns();
        int totalHeight = 0;
        int maxRowHeight = 0;
        int rowNumber = 0;

        // चौड़ाई (width) के लिए EXACTLY स्पेसिफिकेशन तैयार करें
        // itemWidth की गणना GridView की वास्तविक चौड़ाई के आधार पर करें
        int gridViewWidth = gridView.getWidth();
        if (gridViewWidth == 0) {
            // अगर चौड़ाई 0 है, तो हम हाइट सेट नहीं कर सकते।
            return;
        }

        // Horizontal spacing को ध्यान में रखते हुए आइटम की चौड़ाई की गणना करें (लगभग)
        int horizontalSpacing = gridView.getHorizontalSpacing();
        int itemWidth = (gridViewWidth - (columns - 1) * horizontalSpacing) / columns;

        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < items; i++) {
            View listItem = listAdapter.getView(i, null, gridView);

            // आइटम को सही चौड़ाई और UNMEASURED हाइट के साथ मापें
            listItem.measure(widthMeasureSpec, heightMeasureSpec);

            int itemHeight = listItem.getMeasuredHeight();

            // अधिकतम पंक्ति की हाइट ट्रैक करें
            if (itemHeight > maxRowHeight) {
                maxRowHeight = itemHeight;
            }

            // जब पंक्ति समाप्त हो जाए या यह अंतिम आइटम हो
            if ((i + 1) % columns == 0 || (i + 1) == items) {
                // पिछली पंक्ति की अधिकतम हाइट जोड़ें
                totalHeight += maxRowHeight;

                // वर्टिकल स्पेसिंग जोड़ें (अगर यह पहली पंक्ति नहीं है)
                if (rowNumber > 0) {
                    totalHeight += gridView.getVerticalSpacing();
                }

                // पंक्ति की गिनती बढ़ाएं और हाइट रीसेट करें
                rowNumber++;
                maxRowHeight = 0;
            }
        }

        // GridView की बॉटम पैडिंग (paddingBottom="16dp" from XML) को जोड़ें
        totalHeight += gridView.getPaddingBottom();

        // ⭐ CardView शैडो के लिए एक छोटा सा बफर (BUFFER) जोड़ें
        // 6dp elevation के लिए 12-16 पिक्सल काफी हैं।
        final int CARDVIEW_SHADOW_BUFFER = 16;
        totalHeight += CARDVIEW_SHADOW_BUFFER;


        // GridView की फाइनल हाइट सेट करें
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
        gridView.requestLayout();
    }
}