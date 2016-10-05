package com.gdgns.android.rxjava.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.gdgns.android.rxjava.R;
import com.gdgns.android.rxjava.RxUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ConcurrencyWithSchedulersDemoFragment
      extends BaseFragment {

    @Bind(R.id.progress_operation_running) ProgressBar progress;
    @Bind(R.id.list_threading_log) ListView logsList;

    private LogAdapter adapter;
    private List<String> logs;
    private Subscription subscription;

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        RxUtils.unsubscribeIfNotNull(subscription);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupLogger();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_concurrency_schedulers, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_start_operation)
    public void startLongOperation() {

        progress.setVisibility(View.VISIBLE);
        log("Button Clicked");

        subscription = getObservable()//
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(getObserver());                             // Observer
    }

    private Observable<Boolean> getObservable() {
        return Observable.just(true).map(aBoolean -> {
            log("Within Observable");
            doSomeLongOperationThatBlocksCurrentThread();
            return aBoolean;
        });
    }

    /**
     * Observer that handles the result through the 3 important actions:
     *
     * 1. onCompleted
     * 2. onError
     * 3. onNext
     */
    private Observer<Boolean> getObserver() {
        return new Observer<Boolean>() {

            @Override
            public void onCompleted() {
                log("On complete");
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Error in RxJava Demo concurrency");
                log(String.format("Boo! Error %s", e.getMessage()));
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNext(Boolean bool) {
                log(String.format("onNext with return value \"%b\"", bool));
            }
        };
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private void doSomeLongOperationThatBlocksCurrentThread() {
        log("performing long operation");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Timber.d("Operation was interrupted");
        }
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

    private void setupLogger() {
        logs = new ArrayList<>();
        adapter = new LogAdapter(getActivity(), new ArrayList<>());
        logsList.setAdapter(adapter);
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