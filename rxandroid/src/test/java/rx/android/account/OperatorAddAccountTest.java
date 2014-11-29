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
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.observers.TestObserver;

import java.io.IOException;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static rx.android.account.AcountObservableTest.ACCOUNT_TYPE;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {AccountManagerShadow.class})
public class OperatorAddAccountTest {
    private @Mock Observer<Account> observer;

    private Activity activity;
    private AccountManagerShadow testShadow;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.buildActivity(Activity.class).create().get();

        testShadow = Robolectric.shadowOf_(AccountManager.get(activity));
        testShadow.addAuthenticator(ACCOUNT_TYPE);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void failFastOnNullActivity() {
        Observable.create(new OnSubscribeAddAccount(null, ACCOUNT_TYPE)).subscribe(new TestObserver<Account>(observer));
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void failFastOnNullAccount() {
        Observable.create(new OnSubscribeAddAccount(activity, null)).subscribe(new TestObserver<Account>(observer));
    }

    @Test
    public void errorIfCancelledFromOutside() {
        final Observable<Account> observable = Observable.create(new OnSubscribeAddAccount(activity, ACCOUNT_TYPE));
        final Subscription subscription = observable.subscribe(new TestObserver<Account>(observer));

        testShadow.cancelAccountCreation();

        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        verify(observer).onError(Matchers.any(OperationCanceledException.class));
        verify(observer, never()).onNext(Matchers.any(Account.class));
        verify(observer, never()).onCompleted();

        Assert.assertTrue(subscription.isUnsubscribed());
    }

    @Test
    public void cancelCreationWhenUnsubscribedFrom() throws AuthenticatorException, OperationCanceledException, IOException {
        final Account someOtherAccount = new Account("test999", ACCOUNT_TYPE);
        try {
            final Observable<Account> observable = Observable.create(new OnSubscribeAddAccount(activity, ACCOUNT_TYPE));
            final Subscription subscription = observable.subscribe(new TestObserver<Account>(observer));
            subscription.unsubscribe();

            // pretend that something else created an Account
            testShadow.addAccount(someOtherAccount);

            Robolectric.runUiThreadTasksIncludingDelayedTasks();

            verify(observer, never()).onError(Matchers.any(Exception.class));
            verify(observer, never()).onNext(Matchers.any(Account.class));
            verify(observer, never()).onCompleted();

            Assert.assertTrue(subscription.isUnsubscribed());
        } finally {
            testShadow.removeAccount(someOtherAccount, null, null).getResult();
        }
    }
}
