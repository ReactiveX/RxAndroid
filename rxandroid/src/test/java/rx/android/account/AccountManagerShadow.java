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
import android.accounts.AccountManagerFuture;
import android.accounts.AccountManagerCallback;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowAccountManager;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Shadow implementation for the Android {@code AccountManager } class,
 * copied from Robolectric 2.4 sources (a1d4fed841b9984178a1be2c3a977a74d718994d)
 * with couple of changes to allow testing RxAndroid
 */
@Implements(AccountManager.class)
public class AccountManagerShadow extends ShadowAccountManager {
    private static final Object lock = new Object();
    private static AccountManager instance;
    private final List<Account> accounts = new ArrayList<Account>();
    private final Map<Account, Map<String, String>> authTokens = new HashMap<Account, Map<String,String>>();
    private final Map<String, AuthenticatorDescription> authenticators = new LinkedHashMap<String, AuthenticatorDescription>();
    private final List<OnAccountsUpdateListener> listeners = new ArrayList<OnAccountsUpdateListener>();
    private final Map<Account, Map<String, String>> userData = new HashMap<Account, Map<String,String>>();
    private final Map<Account, String> passwords = new HashMap<Account, String>();
    private volatile Handler callbackHandler;
    private volatile AccountManagerCallback<Bundle> pendingAddCallback;
    private volatile RoboAccountManagerFuture pendingAddFuture;

    public static void reset() {
        synchronized (lock) {
            instance = null;
        }
    }

    @Implementation
    public static AccountManager get(Context context) {
        synchronized (lock) {
            if (instance == null) {
                instance = Robolectric.newInstanceOf(AccountManager.class);
            }
            return instance;
        }
    }
    @Implementation
    public Account[] getAccounts() {
        return accounts.toArray(new Account[accounts.size()]);
    }
    @Implementation
    public Account[] getAccountsByType(String type) {
        List<Account> accountsByType = new ArrayList<Account>();
        for (Account a : accounts) {
            if (type.equals(a.type)) {
                accountsByType.add(a);
            }
        }
        return accountsByType.toArray(new Account[accountsByType.size()]);
    }
    @Implementation
    public synchronized void setAuthToken(Account account, String tokenType, String authToken) {
        if(accounts.contains(account)) {
            Map<String, String> tokenMap = authTokens.get(account);
            if(tokenMap == null) {
                tokenMap = new HashMap<String, String>();
                authTokens.put(account, tokenMap);
            }
            tokenMap.put(tokenType, authToken);
        }
    }
    @Implementation
    public String peekAuthToken(Account account, String tokenType) {
        Map<String, String> tokenMap = authTokens.get(account);
        if(tokenMap != null) {
            return tokenMap.get(tokenType);
        }
        return null;
    }
    @Implementation
    public boolean addAccountExplicitly(Account account, String password, Bundle userdata) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        for (Account a: getAccountsByType(account.type)) {
            if (a.name.equals(account.name)) {
                return false;
            }
        }
        if (!accounts.add(account)) {
            return false;
        }
        setPassword(account, password);
        if(userdata != null) {
            for (String key : userdata.keySet()) {
                setUserData(account, key, userdata.get(key).toString());
            }
        }
        return true;
    }
    @Implementation
    public String blockingGetAuthToken(Account account, String authTokenType,
                                       boolean notifyAuthFailure) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        }
        Map<String, String> tokensForAccount = authTokens.get(account);
        if (tokensForAccount == null) {
            return null;
        }
        return tokensForAccount.get(authTokenType);
    }
    @Implementation
    public AccountManagerFuture<Boolean> removeAccount (final Account account,
                                                        AccountManagerCallback<Boolean> callback,
                                                        Handler handler) {
        if (account == null) throw new IllegalArgumentException("account is null");
        final boolean accountRemoved = accounts.remove(account);
        passwords.remove(account);
        userData.remove(account);
        return new AccountManagerFuture<Boolean>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }
            @Override
            public boolean isCancelled() {
                return false;
            }
            @Override
            public boolean isDone() {
                return false;
            }
            @Override
            public Boolean getResult() throws OperationCanceledException, IOException,
                    AuthenticatorException {
                return accountRemoved;
            }
            @Override
            public Boolean getResult(long timeout, TimeUnit unit) throws OperationCanceledException,
                    IOException, AuthenticatorException {
                return accountRemoved;
            }
        };
    }
    @Implementation
    public AuthenticatorDescription[] getAuthenticatorTypes() {
        return authenticators.values().toArray(new AuthenticatorDescription[authenticators.size()]);
    }
    @Implementation
    public void addOnAccountsUpdatedListener(final OnAccountsUpdateListener listener,
                                             Handler handler, boolean updateImmediately) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        if (updateImmediately) {
            listener.onAccountsUpdated(getAccounts());
        }
    }
    @Implementation
    public String getUserData(Account account, String key) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        if (!userData.containsKey(account)) {
            return null;
        }
        Map<String, String> userDataMap = userData.get(account);
        if (userDataMap.containsKey(key)) {
            return userDataMap.get(key);
        }
        return null;
    }
    @Implementation
    public void setUserData(Account account, String key, String value) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        if (!userData.containsKey(account)) {
            userData.put(account, new HashMap<String, String>());
        }
        Map<String, String> userDataMap = userData.get(account);
        if (value == null) {
            userDataMap.remove(key);
        } else {
            userDataMap.put(key, value);
        }
    }
    @Implementation
    public void setPassword (Account account, String password) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        if (password == null) {
            passwords.remove(account);
        } else {
            passwords.put(account, password);
        }
    }
    @Implementation
    public String getPassword (Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        if (passwords.containsKey(account)) {
            return passwords.get(account);
        } else {
            return null;
        }
    }
    @Implementation
    public void invalidateAuthToken(final String accountType, final String authToken) {
        Account[] accountsByType = getAccountsByType(accountType);
        for (Account account : accountsByType) {
            Map<String, String> tokenMap = authTokens.get(account);
            if (tokenMap != null) {
                Iterator<Map.Entry<String, String>> it = tokenMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> map = it.next();
                    if (map.getValue().equals(authToken)) {
                        it.remove();
                    }
                }
                authTokens.put(account, tokenMap);
            }
        }
    }
    private void notifyListeners() {
        Account[] accounts = getAccounts();
        Iterator<OnAccountsUpdateListener> iter = listeners.iterator();
        OnAccountsUpdateListener listener;
        while (iter.hasNext()) {
            listener = iter.next();
            listener.onAccountsUpdated(accounts);
        }
    }

    /**
     * Non-android accessor. Allows the test case to cancel account creation,
     * as if {@link android.accounts.AccountAuthenticatorActivity} was finished by user.
     */
    public void cancelAccountCreation() {
        if (pendingAddCallback != null) {
            pendingAddFuture.resultBundle.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_CANCELED);
            pendingAddFuture.resultBundle.putString(AccountManager.KEY_ERROR_MESSAGE, "cancel");
            pendingAddFuture.cancel(true);
            callbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    pendingAddCallback.run(pendingAddFuture);
                }
            });
        }
    }

    /**
     * Non-android accessor. Allows the test case to populate the
     * list of active accounts.
     *
     * @param account
     */
    public void addAccount(Account account) {
        accounts.add(account);
        if (pendingAddCallback != null) {
            pendingAddFuture.resultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            callbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    pendingAddCallback.run(pendingAddFuture);
                }
            });
        }
        notifyListeners();
    }
    private class RoboAccountManagerFuture implements AccountManagerFuture<Bundle> {
        private final String accountType;
        final Bundle resultBundle;
        public RoboAccountManagerFuture(String accountType, Bundle resultBundle) {
            this.accountType = accountType;
            this.resultBundle = resultBundle;
        }
        @Override
        public boolean cancel(boolean b) {
            return false;
        }
        @Override
        public boolean isCancelled() {
            return false;
        }
        @Override
        public boolean isDone() {
            return resultBundle.containsKey(AccountManager.KEY_ACCOUNT_NAME);
        }
        @Override
        public Bundle getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            if (!authenticators.containsKey(accountType)) {
                throw new AuthenticatorException("No authenticator specified for " + accountType);
            }
            resultBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
            return resultBundle;
        }
        @Override
        public Bundle getResult(long l, TimeUnit timeUnit) throws OperationCanceledException, IOException, AuthenticatorException {
            if (!authenticators.containsKey(accountType)) {
                throw new AuthenticatorException("No authenticator specified for " + accountType);
            }
            return resultBundle;
        }
    }
    @Implementation
    public AccountManagerFuture<Bundle> addAccount(final String accountType, String authTokenType, String[] requiredFeatures, Bundle addAccountOptions, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        final Bundle resultBundle = new Bundle();
        if (activity == null) {
            Intent resultIntent = new Intent();
            resultBundle.putParcelable(AccountManager.KEY_INTENT, resultIntent);
        } else if (callback == null) {
            resultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, "some_user@gmail.com");
        }
        pendingAddCallback = callback;
        callbackHandler = handler == null ? new Handler(Looper.getMainLooper()) : handler;
        pendingAddFuture = new RoboAccountManagerFuture(accountType, resultBundle);
        return pendingAddFuture;
    }
    /**
     * Non-android accessor. Allows the test case to populate the
     * list of active authenticators.
     *
     * @param authenticator
     */
    public void addAuthenticator(AuthenticatorDescription authenticator) {
        authenticators.put(authenticator.type, authenticator);
    }
    /**
     * @see #addAuthenticator(AuthenticatorDescription)
     */
    public void addAuthenticator(String type) {
        addAuthenticator(AuthenticatorDescription.newKey(type));
    }
}
