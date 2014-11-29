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
import android.app.Activity;
import android.support.annotation.NonNull;
import rx.Observable;

public final class AccountObservable {
    private AccountObservable() {
        throw new AssertionError("No instances");
    }

    /**
     * Create an account if no accounts of specified type exist yet.
     *<p>
     * This convenience methods won't create an account, unless no accounts of required type
     * exist. If you want to force account creation (for example, for custom account selection dialog),
     * use {@link OnSubscribeAddAccount} directly.
     *
     * @param b Builder with {@link Account} parameters.
     * @return Observable emitting existing Accounts (if any) or newly created Account.
     */
    public static Observable<Account> requireAccount(OnSubscribeAddAccount.Builder b) {
        final AccountManager am = AccountManager.get(b.activity.getApplication());
        final Account[] account = am.getAccountsByType(b.accountType);
        return account.length == 0 ? Observable.create(b.build()) : Observable.from(account);
    }

    /**
     * Create an account if no accounts of specified type exist yet.
     *<p>
     * This convenience methods won't create an account, unless no accounts of required type
     * exist. If you want to force account creation (for example, for custom account selection dialog),
     * use {@link OnSubscribeAddAccount} directly.
     *
     * @param activity used by {@link AccountManager} to launch Account creation Activity, if new Account is needed.
     * @param accountType type of Account.
     * @return Observable emitting existing Accounts (if any) or newly created Account.
     */
    public static Observable<Account> requireAccount(@NonNull Activity activity, @NonNull String accountType) {
        return requireAccount(new OnSubscribeAddAccount.Builder(activity, accountType));
    }
}
