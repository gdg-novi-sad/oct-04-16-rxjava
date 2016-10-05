package com.gdgns.android.rxjava.fragments;

import android.support.v4.app.Fragment;
import com.gdgns.android.rxjava.MyApp;
import com.squareup.leakcanary.RefWatcher;

public class BaseFragment
      extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MyApp.getRefWatcher();
        refWatcher.watch(this);
    }
}
