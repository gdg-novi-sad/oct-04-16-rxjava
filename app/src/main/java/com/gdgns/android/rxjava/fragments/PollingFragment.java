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

import com.gdgns.android.rxjava.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PollingFragment
      extends BaseFragment {

    private static final int INITIAL_DELAY = 0;
    private static final int POLLING_INTERVAL = 1000;
    private static final int POLL_COUNT = 8;

    @Bind(R.id.list_threading_log) ListView logsList;

    private LogAdapter adapter;
    private List<String> logs;

    private CompositeSubscription subscriptions;
    private int counter = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        subscriptions = new CompositeSubscription();
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
        View layout = inflater.inflate(R.layout.fragment_polling, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_start_simple_polling)
    public void onStartSimplePollingClicked() {

        final int pollCount = POLL_COUNT;

        subscriptions.add(//
              Observable.interval(INITIAL_DELAY, POLLING_INTERVAL, TimeUnit.MILLISECONDS)
                    .map(this::doNetworkCallAndGetStringResult)//
                    .take(pollCount)
                    .doOnSubscribe(() ->
                          log(String.format("Start simple polling - %s", counter)))
                    .subscribe(taskName -> {
                        log(String.format(Locale.US, "Executing polled task [%s] now time : [xx:%02d]",
                              taskName, getSecondHand()));
                    })
        );
    }

    @OnClick(R.id.btn_start_increasingly_delayed_polling)
    public void onStartIncreasinglyDelayedPolling() {
        setupLogger();

        final int pollingInterval = POLLING_INTERVAL;
        final int pollCount = POLL_COUNT;

        log(String.format(Locale.US, "Start increasingly delayed polling now time: [xx:%02d]",
              getSecondHand()));

        subscriptions.add(//
              Observable.just(1)
                    .repeatWhen(new RepeatWithDelay(pollCount, pollingInterval))
                    .subscribe(o -> {
                        log(String.format(Locale.US, "Executing polled task now time : [xx:%02d]",
                              getSecondHand()));
                    }, e -> {
                        Timber.d(e, "arrrr. Error");
                    })
        );
    }


    private String doNetworkCallAndGetStringResult(long attempt) {
        try {
            if (attempt == 4) {
                // randomly make one event super long so we test that the repeat logic waits
                // and accounts for this.
                Thread.sleep(9000);
            } else {
                Thread.sleep(3000);
            }

        } catch (InterruptedException e) {
            Timber.d("Operation was interrupted");
        }
        counter++;

        return String.valueOf(counter);
    }

    // -----------------------------------------------------------------------------------
    // Method that help wiring up the example (irrelevant to RxJava)

    private int getSecondHand() {
        long millis = System.currentTimeMillis();
        return (int) (TimeUnit.MILLISECONDS.toSeconds(millis) -
                      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
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
        counter = 0;
    }

    private boolean isCurrentlyOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    //public static class RepeatWithDelay but not static because of log :) demo only
    public  class RepeatWithDelay
          implements Func1<Observable<? extends Void>, Observable<?>> {

        private final int repeatLimit;
        private final int pollingInterval;
        private int repeatCount = 1;

        RepeatWithDelay(int repeatLimit, int pollingInterval) {
            this.pollingInterval = pollingInterval;
            this.repeatLimit = repeatLimit;
        }

        // this is a notificationhandler, all we care about is
        // the emission "type" not emission "content"
        // only onNext triggers a re-subscription

        @Override
        public Observable<?> call(Observable<? extends Void> inputObservable) {

            // it is critical to use inputObservable in the chain for the result
            // ignoring it and doing your own thing will break the sequence

            return inputObservable.flatMap(new Func1<Void, Observable<?>>() {
                @Override
                public Observable<?> call(Void blah) {


                    if (repeatCount >= repeatLimit) {
                        // terminate the sequence cause we reached the limit
                        log("Completing sequence");
                        return Observable.empty();
                    }

                    // since we don't get an input
                    // we store state in this handler to tell us the point of time we're firing
                    repeatCount++;

                    return Observable.timer(repeatCount * pollingInterval,
                          TimeUnit.MILLISECONDS);
                }
            });
        }
    }

    private class LogAdapter
          extends ArrayAdapter<String> {

        public LogAdapter(Context context, List<String> logs) {
            super(context, R.layout.item_log, R.id.item_log, logs);
        }
    }
}