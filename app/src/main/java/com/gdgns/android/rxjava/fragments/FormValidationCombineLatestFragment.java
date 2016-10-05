package com.gdgns.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.gdgns.android.rxjava.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static android.util.Patterns.EMAIL_ADDRESS;

public class FormValidationCombineLatestFragment
      extends BaseFragment {

    @Bind(R.id.btn_demo_form_valid) TextView btnValidIndicator;

    @Bind(R.id.demo_combl_email) EditText email;
    @Bind(R.id.demo_combl_password) EditText password;
    @Bind(R.id.demo_combl_num) EditText number;

    private Observable<CharSequence> emailChangeObservable;
    private Observable<CharSequence> passwordChangeObservable;
    private Observable<CharSequence> numberChangeObservable;

    private Subscription subscription = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest,
              container,
              false);
        ButterKnife.bind(this, layout);

        emailChangeObservable = RxTextView.textChanges(email).skip(1);
        passwordChangeObservable = RxTextView.textChanges(password).skip(1);
        numberChangeObservable = RxTextView.textChanges(number).skip(1);

        combineLatestEvents();

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        subscription.unsubscribe();
    }

    private void combineLatestEvents() {
        subscription = Observable.combineLatest(emailChangeObservable,
                passwordChangeObservable,
                numberChangeObservable,
              (newEmail, newPassword, newNumber) -> {

                  boolean emailValid = !isEmpty(newEmail) &&
                                       EMAIL_ADDRESS.matcher(newEmail).matches();
                  if (!emailValid) {
                      email.setError("Invalid Email!");
                  }

                  boolean passValid = !isEmpty(newPassword) && newPassword.length() > 8;
                  if (!passValid) {
                      password.setError("Invalid Password!");
                  }

                  boolean numValid = !isEmpty(newNumber);
                  if (numValid) {
                      int num = Integer.parseInt(newNumber.toString());
                      numValid = num > 0 && num <= 100;
                  }
                  if (!numValid) {
                      number.setError("Invalid Number!");
                  }

                  return emailValid && passValid && numValid;

              })
              .subscribe(new Observer<Boolean>() {
                  @Override
                  public void onCompleted() {
                      Timber.d("completed");
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "there was an error");
                  }

                  @Override
                  public void onNext(Boolean formValid) {
                      if (formValid) {
                          btnValidIndicator.setBackgroundColor(getResources().getColor(R.color.blue));
                      } else {
                          btnValidIndicator.setBackgroundColor(getResources().getColor(R.color.gray));
                      }
                  }
              });
    }
}
