/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.android.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.AndroidSubscriptions;
import rx.functions.Action0;

/**
 * Initiate creation of {@link Account} via {@link AccountManager} and subscribe to results.
 *<p>
 * You will typically want to use {@link AccountObservable#requireAccount(Builder)}
 * to create Account on-demand, only if it does not exist yet.
 *<p>
 * This method uses {@link AccountManagerCallback} to avoid creation of separate thread
 * for blocking call, and as such, won't work for non-graphical AccountAuthenticator, as well
 * as implementations, incompatible with {@link AccountAuthenticatorActivity}.
 * If you want to create such AccountAuthenticator (which is generally a bad idea) or need to
 * work around issues of existing one, use {@link AccountManagerFuture#getResult()}
 * instead.
 */
public final class OnSubscribeAddAccount implements Observable.OnSubscribe<Account> {
    private Activity activity;

    private final String accountType;
    private final Handler handler;

    private final String authTokenType;
    private final String[] requiredFeatures;
    private final Bundle addAccountOptions;

    /**
     * Performs {@link AccountManager#addAccount(String, String, String[], Bundle, Activity, AccountManagerCallback, Handler)}
     * to create an account without additional parameters. Account creation and Observable callbacks will
     * be invoked on current thread or main thread (if current thread does not have a {@link Looper})
     *<p>
     * Use {@link #OnSubscribeAddAccount(Activity, String, Handler)} if you don't want
     * current thread to be used.
     *<p>
     * Use {@link Builder} if you want to provide account creation parameters.
     *
     * @param activity used by AccountManager to launch Account creation Activity.
     * @param accountType type of created Account.
     */
    public OnSubscribeAddAccount(@NonNull Activity activity, @NonNull String accountType) {
        this(activity, accountType, new Handler(chooseLooper()));
    }

    /**
     * Performs {@link AccountManager#addAccount(String, String, String[], Bundle, Activity, AccountManagerCallback, Handler)}
     * to create an account without additional parameters. Account creation and Observable callbacks will
     * be invoked on thread, associated with specified {@link Looper}.
     *<p>
     * Use {@link Builder} if you want to provide account creation parameters.
     *
     * @param activity used by AccountManager to launch account creation Activity.
     * @param accountType type of created Account.
     * @param handler used to invoke account creation and callbacks.
     */
    public OnSubscribeAddAccount(@NonNull Activity activity, @NonNull String accountType, @NonNull Handler handler) {
        this(activity, accountType, handler, null, null, null);
    }

    private OnSubscribeAddAccount(Activity activity, String accountType, Handler handler,
                                  String authTokenType,
                                  String[] requiredFeatures,
                                  Bundle addAccountOptions) {
        if (activity == null || TextUtils.isEmpty(accountType))
            throw new IllegalArgumentException("activity and account type must be specified");

        this.activity = activity;
        this.accountType = accountType;
        this.handler = handler;
        this.authTokenType = authTokenType;
        this.requiredFeatures = requiredFeatures;
        this.addAccountOptions = addAccountOptions;
    }

    @Override
    public void call(final Subscriber<? super Account> subscriber) {
        if (Thread.currentThread() == handler.getLooper().getThread()) {
            subscribe(subscriber);
        } else {
            // follow the Observable contract if isn't subscribed to on the same thread as created
            handler.post(new Runnable() {
                @Override
                public void run() {
                    subscribe(subscriber);
                }
            });
        }
    }

    private void subscribe(final Subscriber<? super Account> subscriber) {
        final Context appContext = activity.getApplication();
        final AccountManager am = AccountManager.get(appContext);
        final AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                if (subscriber.isUnsubscribed())
                    return;

                Exception errorDescription = null;
                try {
                    final Bundle results = future.getResult();

                    Account[] accs = am.getAccountsByType(accountType);

                    if (accs.length == 0) {
                        errorDescription = new IllegalStateException("No Account created; results: " + results);
                    } else {
                        for (Account acc:accs) {
                            subscriber.onNext(acc);
                        }

                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    errorDescription = e;
                } finally {
                    if (errorDescription != null) {
                        subscriber.onError(errorDescription);
                    }
                }
            }
        };

        final AccountManagerFuture<Bundle> f = am.addAccount(accountType,
                authTokenType, requiredFeatures, addAccountOptions, activity, callback, handler);

        // slightly reduce chance of leak, if something goes terribly wrong
        activity = null;

        subscriber.add(AndroidSubscriptions.unsubscribeInHandlerThread(new Action0() {
            @Override
            public void call() {
                f.cancel(true);
            }
        }, handler));
    }

    public final static class Builder {
        final Activity activity;
        final String accountType;

        private Handler handler;
        private String authTokenType;
        private String[] requiredFeatures;
        private Bundle addAccountOptions;

        public Builder(@NonNull Activity activity, @NonNull String accountType) {
            this.activity = activity;
            this.accountType = accountType;
        }

        public Builder setHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public Builder setAuthTokenType(String authTokenType) {
            this.authTokenType = authTokenType;
            return this;
        }

        public Builder setAddAccountOptions(Bundle addAccountOptions) {
            this.addAccountOptions = addAccountOptions;
            return this;
        }

        public Builder setRequiredFeatures(String... requiredFeatures) {
            this.requiredFeatures = requiredFeatures;
            return this;
        }

        public OnSubscribeAddAccount build() {
            return new OnSubscribeAddAccount(
                    activity,
                    accountType,
                    handler == null ? new Handler(chooseLooper()) : handler,
                    authTokenType,
                    requiredFeatures,
                    addAccountOptions);
        }
    }

    private static Looper chooseLooper() {
        return Looper.myLooper() == null ? Looper.getMainLooper() : Looper.myLooper();
    }
}
