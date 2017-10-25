package com.schreiber.code.seamless.aperol.view.common.view;


import com.schreiber.code.seamless.aperol.model.CodeFileViewModel;


public interface OnViewClickedListener {

    void onItemClicked(CodeFileViewModel item);

    void onCodeInItemClicked(CodeFileViewModel item);

    boolean onItemLongClicked(CodeFileViewModel item);

}