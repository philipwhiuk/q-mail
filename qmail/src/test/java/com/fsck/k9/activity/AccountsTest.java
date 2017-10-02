package com.fsck.k9.activity;


import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

import com.fsck.k9.Account;
import com.fsck.k9.K9RobolectricTestRunner;
import com.fsck.k9.Preferences;
import com.fsck.k9.QMail;
import com.fsck.k9.activity.setup.WelcomeMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


@RunWith(K9RobolectricTestRunner.class)
public class AccountsTest {

    private ActivityController<Accounts> controller;
    private ShadowApplication shadowApplication = ShadowApplication.getInstance();
    private Preferences preferences = Preferences.getPreferences(shadowApplication.getApplicationContext());

    @Before
    public void setUp() throws Exception {
        setChangeLogSeen();
        Utils.deleteExistingAccounts(preferences);
    }

    private void setChangeLogSeen() throws NameNotFoundException {
        Context context = shadowApplication.getApplicationContext();
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                context.getPackageName(), 0);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(shadowApplication.getApplicationContext())
                .edit();
        editor.putInt("ckChangeLog_last_version_code", packageInfo.versionCode);
        editor.apply();
    }

    @After
    public void cleanUp() {
        Utils.deleteExistingAccounts(preferences);
    }

    @Test
    public void withNoAccounts_showWelcomeMessage() throws Exception {
        controller = Robolectric.buildActivity( Accounts.class ).create();

        Accounts activity = controller.get();
        Intent expectedIntent = new Intent(activity, WelcomeMessage.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void withDatabaseOutOfDate_showUpgradingDatabases() throws Exception {
        preferences.newAccount();
        QMail.sDatabasesUpToDate = false;

        controller = Robolectric.buildActivity( Accounts.class ).create();

        Accounts activity = controller.get();
        Intent expectedIntent = new Intent(activity, UpgradeDatabases.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void with1Account_showsMessageList() throws Exception {
        preferences.newAccount();
        QMail.setDatabasesUpToDate(true);

        controller = Robolectric.buildActivity( Accounts.class ).create();

        Accounts activity = controller.get();
        Intent expectedIntent = new Intent(activity, MessageList.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void with2Accounts_startsNoActivity() throws Exception {
        preferences.newAccount();
        preferences.newAccount();

        QMail.setDatabasesUpToDate(true);

        controller = Robolectric.buildActivity( Accounts.class ).create();

        Intent nextStartedActivity = shadowApplication.getNextStartedActivity();
        assertNull(nextStartedActivity);
    }
}
