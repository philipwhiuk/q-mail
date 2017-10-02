package com.fsck.k9.activity;


import java.util.List;

import com.fsck.k9.Account;
import com.fsck.k9.Preferences;


public class Utils {

    public static void deleteExistingAccounts(Preferences preferences) {
        List<Account> accounts = preferences.getAccounts();
        for (Account account: accounts)
            preferences.deleteAccount(account);
    }
}
