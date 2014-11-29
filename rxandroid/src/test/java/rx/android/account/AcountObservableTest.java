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
import android.accounts.NetworkErrorException;
import android.app.Activity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Matchers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observer;
import rx.observers.TestObserver;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {AccountManagerShadow.class})
public class AcountObservableTest {
    static final String ACCOUNT_TYPE = "io.reactivex.test.account";

    private @Mock Observer<Account> observer;

    private AccountManagerShadow testShadow;
    private Activity activity;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.buildActivity(Activity.class).create().get();

        testShadow = Robolectric.shadowOf_(AccountManager.get(activity));
        testShadow.addAuthenticator(ACCOUNT_TYPE);
    }

    @Test
    public void existingAccountsReturned() throws NetworkErrorException, AuthenticatorException, OperationCanceledException, IOException {
        final Account existingAccount1 = new Account("test1", ACCOUNT_TYPE);
        final Account existingAccount2 = new Account("test2", ACCOUNT_TYPE);
        try {
            testShadow.addAccount(existingAccount1);
            testShadow.addAccount(existingAccount2);

            AccountObservable.requireAccount(activity, ACCOUNT_TYPE).subscribe(new TestObserver<Account>(observer));

            Robolectric.runUiThreadTasksIncludingDelayedTasks();

            verify(observer, never()).onError(Matchers.any(Throwable.class));
            verify(observer).onNext(existingAccount1);
            verify(observer).onNext(existingAccount2);
            verify(observer).onCompleted();
        } finally {
            testShadow.removeAccount(existingAccount1, null, null).getResult();
            testShadow.removeAccount(existingAccount2, null, null).getResult();
        }
    }

    @Test
    public void accountCreated() throws NetworkErrorException, AuthenticatorException, OperationCanceledException, IOException {
        final Account createdAccount = new Account("test3", ACCOUNT_TYPE);
        try {
            AccountObservable.requireAccount(activity, ACCOUNT_TYPE).subscribe(new TestObserver<Account>(observer));

            // pretend, that an Account was added by authenticator
            testShadow.addAccount(createdAccount);

            Robolectric.runUiThreadTasksIncludingDelayedTasks();

            verify(observer, never()).onError(Matchers.any(Throwable.class));
            verify(observer).onNext(createdAccount);
            verify(observer).onCompleted();
        } finally {
            testShadow.removeAccount(createdAccount, null, null).getResult();
        }
    }
}
