package com.example.googleoauth2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class GMailClient {

    private final String TAG = "GglServ";
    /* Scope was https://www.googleapis.com/auth/userinfo.profile but after changing it to GMail, I managed to connect to STMP successfully.*/
    public static final String SCOPE = "oauth2:https://mail.google.com/";

    private String mEmail;

    private Context mContext;
    private final int myREQUEST_CODE_PICK_ACCOUNT;

    private GoogleOAuth2Client mGoogleOAuth2Client;
    private String recvEmail;
    private String subjectEmail;
    private String body;

    private GMailOauthSender gMailOauthSender;
    public GMailClient(Context a_context, final int  a_REQUEST_CODE_PICK_ACCOUNT) {
        mEmail = null; /* TODO: Get it from saved preferences. */
        mContext = a_context;
        myREQUEST_CODE_PICK_ACCOUNT = a_REQUEST_CODE_PICK_ACCOUNT;
        mGoogleOAuth2Client = null;
        gMailOauthSender = new GMailOauthSender();
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    /** Attempt to get the user name. If the email address isn't known yet,
     * then call pickUserAccount() method so the user can pick an account.
     */
    public void getUserEmail() {
        if (mEmail == null) {
            pickUserAccount();
        } else {
            Log.d(TAG, "Email that will be used is: " + mEmail);
        }
    }

    /** Starts an activity in Google Play Services so the user can pick an account */
    private void pickUserAccount() {

        String[] allowableAccountTypes = new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
        boolean isPromptForAccount = false;
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                allowableAccountTypes, isPromptForAccount, null, null, null, null);
        ((Activity)mContext).startActivityForResult(intent, myREQUEST_CODE_PICK_ACCOUNT);
    }

    /** Checks whether the device currently has a network connection */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    public void obtainAccessToken() {

        if (mEmail == null) {
            /* Note that meanwhile, email be set using Account Picker class. */
            Log.d(TAG, "Do nothing since User Email is not set yet.");
        } else { /* email is set, so now obtain the relevant access token */

            /*if (mGoogleOAuth2Client.isTokenExpired()) {*/
                    if (isDeviceOnline()) {
                        mGoogleOAuth2Client =
                                (GoogleOAuth2Client) new GoogleOAuth2Client((MainActivity) mContext, mEmail, SCOPE).execute();
                    } else {
                        Toast.makeText(mContext, "No network connection available", Toast.LENGTH_SHORT).show();
                    }

           /* } else{
                return;
            }*/
        }

    }

    private void obtainAccessToken(GMailClient gMailClient) {
        if (isDeviceOnline()) {
            mGoogleOAuth2Client =
                    (GoogleOAuth2Client) new GoogleOAuth2Client((MainActivity) mContext, mEmail, SCOPE).execute(gMailClient);
        } else {
            Toast.makeText(mContext, "No network connection available", Toast.LENGTH_SHORT).show();
        }
    }

    /*private Session createSessionObject() throws MessagingException {

        Properties props = new Properties();
        props.put("mail.imap.ssl.enable", "true"); // required for Gmail
        props.put("mail.imap.sasl.enable", "true");
        props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
        props.put("mail.imap.auth.login.disable", "true");
        props.put("mail.imap.auth.plain.disable", "true");
        Session session = Session.getInstance(props);
        *//*Store store = session.getStore("imap");
        store.connect("imap.gmail.com", mEmail, mGoogleOAuth2Client.getToken());*//*

        return  session;
    }

    private Message createMessage(String email, String subject, String messageBody, Session session)
            throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mEmail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject(subject);
        message.setText(messageBody);
        return message;
    }*/

    public void sendEmailMsg(String email, String subject, String messageBody) throws Exception {

        if ( mGoogleOAuth2Client != null /*&&
                ! mGoogleOAuth2Client.isTokenExpired()*/) {

            new SendMailTask().execute();

            /*try {
                Session session = createSessionObject();
                Message message = createMessage(email, subject, messageBody, session);
                new SendMailTask().execute(message);
            } catch (AddressException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }*/

        }else{

            this.recvEmail = email;
            this.subjectEmail = subject;
            this.body = messageBody;

            /* Pass given information to be sent after getting access token. */
            obtainAccessToken(this);
        }

    }

    public void sendEmailMsg() throws Exception {
        sendEmailMsg(this.recvEmail, this.subjectEmail, this.body);
    }


    private class SendMailTask extends AsyncTask<Message, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(mContext, "Please wait", "Sending mail", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Message... messages) {
            try {

                /* Connection must run in background so you can't call it directly in onPostExecute after getting
                * access token*/

                /*gMailOauthSender.connectToSmtp("smtp.gmail.com",
                        587,
                        mEmail,
                        mGoogleOAuth2Client.getToken(),
                        true);*/
                gMailOauthSender.sendMail(subjectEmail,body,mEmail,mGoogleOAuth2Client.getToken(),recvEmail);
                Log.i(TAG, "Success email sending");
            } catch (Exception e) {
                Log.e(TAG, "Failed to Send to STMP");
                e.printStackTrace();
            }
            return null;
        }
    }
}
