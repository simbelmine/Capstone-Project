package com.app.eisenflow;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created on 12/19/17.
 */

public class ParallaxPageTransformer implements ViewPager.PageTransformer  {
    public void transformPage(View view, float position) {
        View page = view.findViewById(R.id.splash_screen_content);

        if(position <= -1.0f || position >= 1.0f) {
        } else if( position == 0.0f ) {
        } else {
            if (page != null) {
                page.setAlpha(1.0f - Math.abs(position));
            }
        }
    }
}
