package com.fsck.k9.activity.setup;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.fsck.k9.Preferences;
import com.fsck.k9.QMail;
import com.fsck.k9.R;
import com.fsck.k9.preferences.StorageEditor;
import org.openintents.smime.util.SMimeApi;


public class SMimeAppSelectDialog extends Activity {
    private static final String OPENSMIME_PACKAGE = "com.whiuk.philip.opensmime";

    public static final String FRAG_SMIME_SELECT = "smime_select";

    private static final String MARKET_INTENT_URI_BASE = "market://details?id=%s";
    private static final Intent MARKET_INTENT = new Intent(Intent.ACTION_VIEW, Uri.parse(
            String.format(MARKET_INTENT_URI_BASE, OPENSMIME_PACKAGE)));

    private static final ArrayList<String> PROVIDER_BLACKLIST = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(QMail.getK9Theme() == QMail.Theme.LIGHT ?
                R.style.Theme_K9_Dialog_Translucent_Light : R.style.Theme_K9_Dialog_Translucent_Dark);

        if (savedInstanceState == null) {
            showSMimeSelectDialogFragment();
        }
    }

    private void showSMimeSelectDialogFragment() {
        SMimeAppSelectFragment fragment = new SMimeAppSelectFragment();
        fragment.show(getFragmentManager(), FRAG_SMIME_SELECT);
    }

    public static class SMimeAppSelectFragment extends DialogFragment {
        private ArrayList<SMimeProviderEntry> sMimeProviderList = new ArrayList<>();
        private String selectedPackage;

        private void populateAppList() {
            sMimeProviderList.clear();

            Context context = getActivity();

            SMimeProviderEntry noneEntry = new SMimeProviderEntry("",
                    context.getString(R.string.smime_list_preference_none),
                    getResources().getDrawable(R.drawable.ic_action_cancel_launchersize));
            sMimeProviderList.add(0, noneEntry);

            // search for SMIME providers...
            Intent intent = new Intent(SMimeApi.SERVICE_INTENT_2);
            List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentServices(intent, 0);
            boolean hasNonBlacklistedChoices = false;
            if (resInfo != null) {
                for (ResolveInfo resolveInfo : resInfo) {
                    if (resolveInfo.serviceInfo == null) {
                        continue;
                    }

                    String packageName = resolveInfo.serviceInfo.packageName;
                    String simpleName = String.valueOf(resolveInfo.serviceInfo.loadLabel(context.getPackageManager()));
                    Drawable icon = resolveInfo.serviceInfo.loadIcon(context.getPackageManager());

                    if (!PROVIDER_BLACKLIST.contains(packageName)) {
                        sMimeProviderList.add(new SMimeProviderEntry(packageName, simpleName, icon));
                        hasNonBlacklistedChoices = true;
                    }
                }
            }

            if (!hasNonBlacklistedChoices) {
                // add install links if provider list is empty
                resInfo = context.getPackageManager().queryIntentActivities(MARKET_INTENT, 0);
                for (ResolveInfo resolveInfo : resInfo) {
                    Intent marketIntent = new Intent(MARKET_INTENT);
                    marketIntent.setPackage(resolveInfo.activityInfo.packageName);
                    Drawable icon = resolveInfo.activityInfo.loadIcon(context.getPackageManager());
                    String marketName = String.valueOf(resolveInfo.activityInfo.applicationInfo
                            .loadLabel(context.getPackageManager()));
                    String simpleName = context.getString(R.string
                            .smime_install_opensmime_via, marketName);
                    sMimeProviderList.add(new SMimeProviderEntry(OPENSMIME_PACKAGE, simpleName,
                            icon, marketIntent));
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            selectedPackage = QMail.getSMimeProvider();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.account_settings_smime_app);

            // do again, maybe an app has now been installed
            populateAppList();

            // Init ArrayAdapter with S/MIME Providers
            ListAdapter adapter = new ArrayAdapter<SMimeProviderEntry>(getActivity(),
                    android.R.layout.select_dialog_singlechoice, android.R.id.text1, sMimeProviderList) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    // User super class to create the View
                    View v = super.getView(position, convertView, parent);
                    TextView tv = (TextView) v.findViewById(android.R.id.text1);

                    // Put the image on the TextView
                    tv.setCompoundDrawablesWithIntrinsicBounds(sMimeProviderList.get(position).icon, null,
                            null, null);

                    // Add margin between image and text (support various screen densities)
                    int dp10 = (int) (10 * getContext().getResources().getDisplayMetrics().density + 0.5f);
                    tv.setCompoundDrawablePadding(dp10);

                    return v;
                }
            };

            builder.setSingleChoiceItems(adapter, getIndexOfProviderList(selectedPackage),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SMimeProviderEntry entry = sMimeProviderList.get(which);

                            if (entry.intent != null) {
                                /*
                                 * Intents are called as activity
                                 *
                                 * Current approach is to assume the user installed the app.
                                 * If he does not, the selected package is not valid.
                                 *
                                 * However  applications should always consider this could happen,
                                 * as the user might remove the currently used S/MIME app.
                                 */
                                getActivity().startActivity(entry.intent);
                                return;
                            }

                            selectedPackage = entry.packageName;

                            dialog.dismiss();
                        }
                    });

            return builder.create();
        }

        private int getIndexOfProviderList(String packageName) {
            for (SMimeProviderEntry app : sMimeProviderList) {
                if (app.packageName.equals(packageName)) {
                    return sMimeProviderList.indexOf(app);
                }
            }
            // default is "none"
            return 0;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);

            ((SMimeAppSelectDialog) getActivity()).onSelectProvider(selectedPackage);
        }
    }

    public void onSelectProvider(String selectedPackage) {
        persistSMimeProviderSetting(selectedPackage);
        finish();
    }

    private void persistSMimeProviderSetting(String selectedPackage) {
        QMail.setSMimeProvider(selectedPackage);

        StorageEditor editor = Preferences.getPreferences(this).getStorage().edit();
        QMail.save(editor);
        editor.commit();
    }

    public void onDismissApgDialog() {
        showSMimeSelectDialogFragment();
    }

    private static class SMimeProviderEntry {
        private String packageName;
        private String simpleName;
        private Drawable icon;
        private Intent intent;

        SMimeProviderEntry(String packageName, String simpleName, Drawable icon) {
            this.packageName = packageName;
            this.simpleName = simpleName;
            this.icon = icon;
        }

        SMimeProviderEntry(String packageName, String simpleName, Drawable icon, Intent intent) {
            this(packageName, simpleName, icon);
            this.intent = intent;
        }

        @Override
        public String toString() {
            return simpleName;
        }
    }
}
