package com.gdgns.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.gdgns.android.rxjava.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import rx.Subscription;
import rx.subjects.PublishSubject;

import static android.text.TextUtils.isEmpty;

public class DoubleBindingTextViewFragment
      extends BaseFragment {

    @Bind(R.id.double_binding_num1) EditText number1;
    @Bind(R.id.double_binding_num2) EditText number2;
    @Bind(R.id.double_binding_result) TextView result;

    Subscription subscription;
    PublishSubject<Float> resultEmitterSubject;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_double_binding_textview, container, false);
        ButterKnife.bind(this, layout);

        resultEmitterSubject = PublishSubject.create();
        subscription = resultEmitterSubject//
              .asObservable()//
              .subscribe(aFloat -> {
                  result.setText(String.valueOf(aFloat));
              });

        onNumberChanged();
        number2.requestFocus();

        return layout;
    }

    @OnTextChanged({R.id.double_binding_num1, R.id.double_binding_num2})
    public void onNumberChanged() {
        float num1 = 0;
        float num2 = 0;

        if (!isEmpty(number1.getText().toString())) {
            num1 = Float.parseFloat(number1.getText().toString());
        }

        if (!isEmpty(number2.getText().toString())) {
            num2 = Float.parseFloat(number2.getText().toString());
        }

        resultEmitterSubject.onNext(num1 + num2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        subscription.unsubscribe();
        ButterKnife.unbind(this);
    }
}
