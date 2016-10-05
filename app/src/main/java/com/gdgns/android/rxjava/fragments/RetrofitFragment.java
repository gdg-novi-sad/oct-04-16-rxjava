package com.gdgns.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.gdgns.android.rxjava.R;
import com.gdgns.android.rxjava.retrofit.Contributor;
import com.gdgns.android.rxjava.retrofit.GithubApi;
import com.gdgns.android.rxjava.retrofit.GithubService;
import com.gdgns.android.rxjava.retrofit.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.format;

public class RetrofitFragment
      extends Fragment {

    @Bind(R.id.demo_retrofit_contributors_username) EditText username;
    @Bind(R.id.demo_retrofit_contributors_repository) EditText repo;
    @Bind(R.id.log_list) ListView resultList;

    private ArrayAdapter<String> adapter;
    private GithubApi githubService;
    private CompositeSubscription subscriptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String githubToken = getResources().getString(R.string.github_oauth_token);
        githubService = GithubService.createGithubService(githubToken);

        subscriptions = new CompositeSubscription();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_retrofit, container, false);
        ButterKnife.bind(this, layout);

        adapter = new ArrayAdapter<>(getActivity(), R.layout.item_log, R.id.item_log, new ArrayList<>());
        resultList.setAdapter(adapter);

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    @OnClick(R.id.btn_demo_retrofit_contributors)
    public void onListContributorsClicked() {
        adapter.clear();

        subscriptions.add(//
              githubService.contributors(username.getText().toString(), repo.getText().toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Contributor>>() {
                        @Override
                        public void onCompleted() {
                            Timber.d("Retrofit call 1 completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.e(e, "error while getting the list of contributors");
                        }

                        @Override
                        public void onNext(List<Contributor> contributors) {
                            for (Contributor contributor : contributors) {
                                adapter.add(format("%s has made %d contributions to %s",
                                      contributor.login,
                                      contributor.contributions,
                                      repo.getText().toString()));

                                Timber.d("%s has made %d contributions to %s",
                                      contributor.login,
                                      contributor.contributions,
                                      repo.getText().toString());
                            }
                        }
                    }));
    }

    @OnClick(R.id.btn_demo_retrofit_contributors_with_user_info)
    public void onListContributorsWithFullUserInfoClicked() {
        adapter.clear();

        subscriptions.add(githubService.contributors(username.getText().toString(), repo.getText().toString())
              .flatMap(Observable::from)
              .flatMap(contributor -> {
                  Observable<User> userObservable = githubService.user(contributor.login)
                        .filter(user -> !isEmpty(user.name) && !isEmpty(user.email));

                  return Observable.zip(userObservable,
                        Observable.just(contributor),
                        Pair::new);
              })
              .subscribeOn(Schedulers.newThread())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Subscriber<Pair>() {
                  @Override
                  public void onCompleted() {
                      Timber.d("Retrofit call 2 completed ");
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "error while getting the list of contributors along with full " + "names");
                  }

                  @Override
                  public void onNext(Pair pair) {
                      User user = ((Pair<User, Contributor>)pair).first;
                      Contributor contributor = ((Pair<User, Contributor>)pair).second;

                      adapter.add(format("%s(%s) has made %d contributions to %s",
                            user.name,
                            user.email,
                            contributor.contributions,
                            repo.getText().toString()));

                      adapter.notifyDataSetChanged();

                      Timber.d("%s(%s) has made %d contributions to %s",
                            user.name,
                            user.email,
                            contributor.contributions,
                            repo.getText().toString());
                  }
              }));
    }
}
