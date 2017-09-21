package com.fsck.k9.mail.internet;

import com.fsck.k9.R;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.DKIMState.DKIMError;
import com.fsck.k9.mail.internet.SPFState.SPFError;
import com.fsck.k9.mail.internet.SecureTransportState.SecureTransportError;
import timber.log.Timber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes ReceivedHeaders to extract information
 */
public class ReceivedHeaders {
    public static final String RECEIVED = "Received";
    public static Pattern fromPattern = Pattern.compile("from ([A-Za-z0-9.]*?) ");
    public static Pattern byPattern = Pattern.compile("by ([A-Za-z0-9.]*?) ");
    public static Pattern usingPattern = Pattern.compile("using (.*?) with cipher (.*?) \\(([0-9]*/[0-9]*?) bits\\)");
    public static final String AUTHENTICATION_RESULTS = "Authentication-Results";
    public static Pattern spfPattern = Pattern.compile("spf=([A-Za-z]*)");
    public static Pattern dkimPattern = Pattern.compile("dkim=([A-Za-z]*)");


    public static SecureTransportState wasMessageTransmittedSecurely(Message message) {
        String[] headers = message.getHeader(RECEIVED);
        Timber.d("Received headers: " + headers.length);

        for(String header: headers) {
            String fromAddress = "", toAddress = "", sslVersion = null, cipher, bits;
            header = header.trim();

            Matcher matcher = fromPattern.matcher(header);
            if(matcher.find())
                fromAddress = matcher.group(1);

            matcher = byPattern.matcher(header);
            if(matcher.find())
                toAddress = matcher.group(1);

            matcher = usingPattern.matcher(header);
            if(matcher.find()) {
                sslVersion = matcher.group(1);
                cipher = matcher.group(2);
                bits = matcher.group(3);
            }

            if (fromAddress.equals("localhost") || fromAddress.equals("127.0.0.1") ||
                    toAddress.equals("localhost") || toAddress.equals("127.0.0.1")) {
                //Loopback is considered secure
                continue;
            }

            if (sslVersion == null || sslVersion.startsWith("SSL")) {
                //SSLv1, v2, v3 considered broken
                return new SecureTransportState(SecureTransportError.INSECURELY_ENCRYPTED,
                        R.string.transport_crypto_insecure_ssl_version);
            }

            //TODO: Blacklisted ciphers and key lengths

            return new SecureTransportState(SecureTransportError.SECURELY_ENCRYPTED);
        }
        return new SecureTransportState(SecureTransportError.UNKNOWN);
    }

    public static SPFState isEmailPotentialSpoof(Message message) {
        String[] headers = message.getHeader(AUTHENTICATION_RESULTS);
        Timber.d("Authentication-Results headers: " + headers.length);
        boolean hasPass = false;
        for(String header: headers) {
            Matcher matcher = spfPattern.matcher(header);
            if(matcher.find()) {
                String passOrFail = matcher.group(1);
                if (passOrFail.equalsIgnoreCase("fail")) {
                    return new SPFState(SPFError.FAIL);
                } else if (passOrFail.equalsIgnoreCase("none")) {
                    return new SPFState(SPFError.NONE);
                } else {
                    hasPass = true;
                }
            }
        }

        if (hasPass) {
            return new SPFState(SPFError.PASS);
        } else {
            return new SPFState(SPFError.UNKNOWN);
        }
    }

    public static DKIMState isEmailIntegrityValid(Message message) {
        String[] headers = message.getHeader(AUTHENTICATION_RESULTS);
        Timber.d("Authentication-Results headers: " + headers.length);
        boolean hasPass = false;
        for(String header: headers) {
            Matcher matcher = dkimPattern.matcher(header);
            if(matcher.find()) {
                String passOrFail = matcher.group(1);
                if (passOrFail.equalsIgnoreCase("fail")) {
                    return new DKIMState(DKIMError.FAIL);
                } else if (passOrFail.equalsIgnoreCase("none")) {
                    return new DKIMState(DKIMError.NONE);
                } else {
                    hasPass = true;
                }
            }
        }

        if (hasPass) {
            return new DKIMState(DKIMError.PASS);
        } else {
            return new DKIMState(DKIMError.UNKNOWN);
        }
    }
}
