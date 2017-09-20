package com.fsck.k9.ui.messageview;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.fsck.k9.R;
import com.fsck.k9.view.MessageCryptoDisplayStatus;
import com.fsck.k9.view.ThemeUtils;


public class SecurityInfoDialog extends DialogFragment {
    public static final String ARG_DISPLAY_STATUS = "display_status";
    public static final String ARG_HAS_SECURITY_WARNING = "has_security_warning";
    public static final int ICON_ANIM_DELAY = 400;
    public static final int ICON_ANIM_DURATION = 350;


    private View dialogView;

    private View authenticationIconFrame;
    private ImageView authenticationIcon_1;
    private ImageView authenticationIcon_2;
    private ImageView authenticationIcon_3;
    private TextView authenticationText;

    private View trustIconFrame;
    private ImageView trustIcon_1;
    private ImageView trustIcon_2;
    private TextView trustText;

    private View transportSecurityIconFrame;
    private ImageView transportSecurityIcon_1;
    private TextView transportSecurityText;


    public static SecurityInfoDialog newInstance(MessageCryptoDisplayStatus displayStatus, boolean hasSecurityWarning) {
        SecurityInfoDialog frag = new SecurityInfoDialog();

        Bundle args = new Bundle();
        args.putString(ARG_DISPLAY_STATUS, displayStatus.toString());
        args.putBoolean(ARG_HAS_SECURITY_WARNING, hasSecurityWarning);
        frag.setArguments(args);

        return frag;
    }

    @SuppressLint("InflateParams") // inflating without root element is fine for creating a dialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder b = new AlertDialog.Builder(getActivity());

        dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.message_security_info_dialog, null);

        authenticationIconFrame = dialogView.findViewById(R.id.security_info_authentication_frame);
        authenticationIcon_1 = (ImageView) authenticationIconFrame.findViewById(R.id.security_info_authentication_icon_1);
        authenticationIcon_2 = (ImageView) authenticationIconFrame.findViewById(R.id.security_info_authentication_icon_2);
        authenticationIcon_3 = (ImageView) authenticationIconFrame.findViewById(R.id.security_info_authentication_icon_3);
        authenticationText = (TextView) dialogView.findViewById(R.id.security_info_authentication_text);

        trustIconFrame = dialogView.findViewById(R.id.security_info_trust_frame);
        trustIcon_1 = (ImageView) trustIconFrame.findViewById(R.id.security_info_trust_icon_1);
        trustIcon_2 = (ImageView) trustIconFrame.findViewById(R.id.security_info_trust_icon_2);
        trustText = (TextView) dialogView.findViewById(R.id.security_info_trust_text);

        transportSecurityIconFrame = dialogView.findViewById(R.id.security_info_transport_security_frame);
        transportSecurityIcon_1 = (ImageView) trustIconFrame.findViewById(R.id.security_info_transport_security_icon_1);
        transportSecurityText = (TextView) dialogView.findViewById(R.id.security_info_transport_security_text);

        MessageCryptoDisplayStatus displayStatus =
                MessageCryptoDisplayStatus.valueOf(getArguments().getString(ARG_DISPLAY_STATUS));
        setMessageForDisplayStatus(displayStatus);

        b.setView(dialogView);
        b.setPositiveButton(R.string.crypto_info_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        boolean hasSecurityWarning = getArguments().getBoolean(ARG_HAS_SECURITY_WARNING);
        if (hasSecurityWarning) {
            b.setNeutralButton(R.string.crypto_info_view_security_warning, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Fragment frag = getTargetFragment();
                    if (! (frag instanceof OnClickShowCryptoKeyListener)) {
                        throw new AssertionError("Displaying activity must implement OnClickShowCryptoKeyListener!");
                    }
                    ((OnClickShowCryptoKeyListener) frag).onClickShowSecurityWarning();
                }
            });
        } else if (displayStatus.hasAssociatedKey()) {
            b.setNeutralButton(R.string.crypto_info_view_key, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Fragment frag = getTargetFragment();
                    if (! (frag instanceof OnClickShowCryptoKeyListener)) {
                        throw new AssertionError("Displaying activity must implement OnClickShowCryptoKeyListener!");
                    }
                    ((OnClickShowCryptoKeyListener) frag).onClickShowCryptoKey();
                }
            });
        }

        return b.create();
    }

    private void setMessageForDisplayStatus(MessageCryptoDisplayStatus cryptoDisplayStatus,
            MessageTransportSecurityDisplayStatus transportSecurityDisplayStatus) {
        if (cryptoDisplayStatus.textResTop == null) {
            throw new AssertionError("Crypto info dialog can only be displayed for items with text!");
        }

        if (cryptoDisplayStatus.textResBottom == null) {
            setMessageSingleLine(cryptoDisplayStatus.colorAttr,
                    cryptoDisplayStatus.textResTop, cryptoDisplayStatus.statusIconRes,
                    cryptoDisplayStatus.statusDotsRes);
        } else {
            if (cryptoDisplayStatus.statusDotsRes == null) {
                throw new AssertionError("second icon must be non-null if second text is non-null!");
            }
            setMessageWithAnimation(cryptoDisplayStatus.colorAttr,
                    cryptoDisplayStatus.textResTop, cryptoDisplayStatus.statusIconRes,
                    cryptoDisplayStatus.textResBottom, cryptoDisplayStatus.statusDotsRes);
        }

    }

    private void setMessageSingleLine(@AttrRes int colorAttr,
            @StringRes int topTextRes, @DrawableRes int statusIconRes,
            @DrawableRes Integer statusDotsRes) {
        @ColorInt int color = ThemeUtils.getStyledColor(getActivity(), colorAttr);

        authenticationIcon_1.setImageResource(statusIconRes);
        authenticationIcon_1.setColorFilter(color);
        authenticationText.setText(topTextRes);

        if (statusDotsRes != null) {
            authenticationIcon_3.setImageResource(statusDotsRes);
            authenticationIcon_3.setColorFilter(color);
            authenticationIcon_3.setVisibility(View.VISIBLE);
        } else {
            authenticationIcon_3.setVisibility(View.GONE);
        }

        trustText.setVisibility(View.GONE);
        trustIconFrame.setVisibility(View.GONE);
    }

    private void setMessageWithAnimation(@AttrRes int colorAttr,
            @StringRes int topTextRes, @DrawableRes int statusIconRes,
            @StringRes int bottomTextRes, @DrawableRes int statusDotsRes) {
        authenticationIcon_1.setImageResource(statusIconRes);
        authenticationIcon_2.setImageResource(statusDotsRes);
        authenticationIcon_3.setVisibility(View.GONE);
        authenticationText.setText(topTextRes);

        trustIcon_1.setImageResource(statusIconRes);
        trustIcon_2.setImageResource(statusDotsRes);
        trustText.setText(bottomTextRes);

        authenticationIcon_1.setColorFilter(ThemeUtils.getStyledColor(getActivity(), colorAttr));
        trustIcon_2.setColorFilter(ThemeUtils.getStyledColor(getActivity(), colorAttr));

        prepareIconAnimation();
    }

    private void prepareIconAnimation() {
        authenticationText.setAlpha(0.0f);
        trustText.setAlpha(0.0f);

        dialogView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                float halfVerticalPixelDifference = (trustIconFrame.getY() - authenticationIconFrame.getY()) / 2.0f;
                authenticationIconFrame.setTranslationY(halfVerticalPixelDifference);
                trustIconFrame.setTranslationY(-halfVerticalPixelDifference);

                authenticationIconFrame.animate().translationY(0)
                        .setStartDelay(ICON_ANIM_DELAY)
                        .setDuration(ICON_ANIM_DURATION)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
                trustIconFrame.animate().translationY(0)
                        .setStartDelay(ICON_ANIM_DELAY)
                        .setDuration(ICON_ANIM_DURATION)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
                authenticationText.animate().alpha(1.0f).setStartDelay(ICON_ANIM_DELAY + ICON_ANIM_DURATION).start();
                trustText.animate().alpha(1.0f).setStartDelay(ICON_ANIM_DELAY + ICON_ANIM_DURATION).start();

                view.removeOnLayoutChangeListener(this);
            }
        });
    }

    public interface OnClickShowCryptoKeyListener {
        void onClickShowCryptoKey();
        void onClickShowSecurityWarning();
    }
}
