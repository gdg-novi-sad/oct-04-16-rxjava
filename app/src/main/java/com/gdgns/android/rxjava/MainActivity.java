package com.gdgns.android.rxjava;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.gdgns.android.rxjava.fragments.MainFragment;
public class MainActivity
      extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                  .replace(android.R.id.content, new MainFragment(), this.toString())
                  .commit();
        }
    }

}