package com.fsck.k9.activity.setup;


import android.content.Intent;

import com.fsck.k9.K9RobolectricTestRunner;
import com.fsck.k9.R;
import com.fsck.k9.activity.Accounts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(K9RobolectricTestRunner.class)
public class WelcomeMessageTest {

    private ActivityController<WelcomeMessage> controller;
    private ShadowApplication shadowApplication = ShadowApplication.getInstance();

    @Before
    public void setup() {
        controller = Robolectric.buildActivity( WelcomeMessage.class ).create().resume();
    }

    @Test
    public void showsNextButton() throws Exception {
        WelcomeMessage activity = controller.get();

        assertNotNull(activity.findViewById(R.id.next));
        assertTrue(activity.findViewById(R.id.next).isEnabled());
    }

    @Test
    public void showsImportSettingsButton() throws Exception {
        WelcomeMessage activity = controller.get();

        assertNotNull(activity.findViewById(R.id.import_settings));
        assertTrue(activity.findViewById(R.id.import_settings).isEnabled());
    }

    @Test
    public void clickingNextStartsAccountCreation() throws Exception {
        WelcomeMessage activity = controller.get();

        activity.findViewById(R.id.next).performClick();

        Intent expectedIntent = new Intent(activity, AccountSetupBasics.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void clickingImportSettingsStartsAccounts() throws Exception {
        WelcomeMessage activity = controller.get();

        activity.findViewById(R.id.import_settings).performClick();

        Intent expectedIntent = new Intent(activity, Accounts.class);
        Intent actual = shadowApplication.getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }
}
