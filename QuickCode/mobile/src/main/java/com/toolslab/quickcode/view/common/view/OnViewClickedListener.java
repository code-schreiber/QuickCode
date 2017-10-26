package com.toolslab.quickcode.view.common.view;


import com.toolslab.quickcode.model.CodeFileViewModel;


public interface OnViewClickedListener {

    void onItemClicked(CodeFileViewModel item);

    void onCodeInItemClicked(CodeFileViewModel item);

    boolean onItemLongClicked(CodeFileViewModel item);

}