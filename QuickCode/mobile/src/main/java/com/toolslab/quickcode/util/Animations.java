package com.toolslab.quickcode.util;


import android.view.animation.Animation;
import android.view.animation.RotateAnimation;


public class Animations {

    private Animations() {
        // Hide utility class constructor
    }

    public static RotateAnimation getRotatingAnimation(int repeatCount) {
        RotateAnimation animation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(repeatCount > 0 ? repeatCount - 1 : 0);
        animation.setDuration(1000 / 2);
        return animation;
    }

}
