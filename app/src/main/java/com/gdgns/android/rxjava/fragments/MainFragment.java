package com.gdgns.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gdgns.android.rxjava.R;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainFragment
      extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_demo_schedulers)
    void demoConcurrencyWithSchedulers() {
        clickedOn(new ConcurrencyWithSchedulersDemoFragment());
    }

    @OnClick(R.id.btn_demo_debounce)
    void demoThrottling() {
        clickedOn(new DebounceSearchEmitterFragment());
    }

    @OnClick(R.id.btn_demo_retrofit)
    void demoRetrofitCalls() {
        clickedOn(new RetrofitFragment());
    }

    @OnClick(R.id.btn_demo_polling)
    void demoPolling() {
        clickedOn(new PollingFragment());
    }

    @OnClick(R.id.btn_demo_double_binding_textview)
    void demoDoubleBindingWithPublishSubject() {
        clickedOn(new DoubleBindingTextViewFragment());
    }


    @OnClick(R.id.btn_demo_form_validation_combinel)
    void formValidation() {
        clickedOn(new FormValidationCombineLatestFragment());
    }


    private void clickedOn(@NonNull Fragment fragment) {
        final String tag = fragment.getClass().toString();
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(tag)
              .replace(android.R.id.content, fragment, tag)
              .commit();
    }
}
