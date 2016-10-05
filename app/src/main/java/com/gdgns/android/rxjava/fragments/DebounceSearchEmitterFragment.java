package com.gdgns.android.rxjava.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.gdgns.android.rxjava.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static co.kaush.core.util.CoreNullnessUtils.isNotNullOrEmpty;
import static java.lang.String.format;

public class DebounceSearchEmitterFragment
      extends BaseFragment {

    @Bind(R.id.list_threading_log) ListView logsList;
    @Bind(R.id.input_txt_debounce) EditText inputSearchText;

    private LogAdapter adapter;
    private List<String> logs;

    private Subscription subscription;

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
        ButterKnife.unbind(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_debounce, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @OnClick(R.id.clr_debounce)
    public void onClearLog() {
        logs = new ArrayList<>();
        adapter.clear();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        setupLogger();

        subscription = RxTextView.textChangeEvents(inputSearchText)
              .debounce(400, TimeUnit.MILLISECONDS)// default Scheduler is Computation
              .filter(changes -> !TextUtils.isEmpty(inputSearchText.getText().toString()))
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(getSearchObserver());
    }

    // -----------------------------------------------------------------------------------
    // Main Rx entities

    private Observer<TextViewTextChangeEvent> getSearchObserver() {
        return new Observer<TextViewTextChangeEvent>() {
            @Override
            public void onCompleted() {
                Timber.d(" onComplete");
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, " on error!");
                log("Dang error. check your logs");
            }

            @Override
            public void onNext(TextViewTextChangeEvent onTextChangeEvent) {
                log(format("Searching for %s", onTextChangeEvent.text().toString()));
            }
        };
    }


   //irrelevant to RxJava

    private void setupLogger() {
        logs = new ArrayList<>();
        adapter = new LogAdapter(getActivity(), new ArrayList<>());
        logsList.setAdapter(adapter);
    }

    private void log(String logMsg) {

        if (isCurrentlyOnMainThread()) {
            logs.add(0, logMsg + " (main thread) ");
            adapter.clear();
            adapter.addAll(logs);
        } else {
            logs.add(0, logMsg + " (NOT main thread) ");

            // You can only do below stuff on main thread.
            new Handler(Looper.getMainLooper()).post(() -> {
                adapter.clear();
                adapter.addAll(logs);
            });
        }
    }

    private boolean isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private class LogAdapter
          extends ArrayAdapter<String> {

        public LogAdapter(Context context, List<String> logs) {
            super(context, R.layout.item_log, R.id.item_log, logs);
        }
    }
}