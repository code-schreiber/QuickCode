package com.schreiber.code.seamless.aperol.view.common.view;

import android.view.View;


public interface OnViewClickedListener {

//    void onViewClicked(View v, int position);

    void onPotato(View caller);

    void onTomato(View callerImage, int position);

}