/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.googleoauth2;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;

/**
 * This example shows how to fetch tokens if you are creating a foreground task/activity and handle
 * auth exceptions.
 */
public class GoogleOAuth2Client extends AsyncTask<GMailClient, Void, GMailClient> {

    private static final String TAG = "GglServ";
    protected MainActivity mActivity;

    protected String mScope;
    protected String mEmail;

    private boolean isTokenExpired ;
    public String getToken() {
        return mToken;
    }

    private String mToken;

    public GoogleOAuth2Client(MainActivity activity, String email, String scope) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = email;

        this.isTokenExpired = true;
        mToken = null;
    }

    /**
     * Display personalized greeting. This class contains boilerplate code to consume the token but
     * isn't integral to getting the tokens.
     */
    @Override
    protected GMailClient doInBackground(GMailClient... params) {
        try {
            mToken = fetchToken();
            if (mToken == null) {
                // error has already been handled in fetchToken()
                return null;
            } else{
                Log.d(TAG, "access token is: " + mToken);
                isTokenExpired = false;
            }
        } catch (IOException ex) {
            onError("Following Error occurred, please try again. " + ex.getMessage(), ex);
        }
        return params[0];
    }

    @Override
    protected void onPostExecute(GMailClient gMailClient) {
        if(gMailClient == null){
            Log.d(TAG, "mail client object is null, Do nothing");
        }else {
            try {
                gMailClient.sendEmailMsg();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void onError(String msg, Exception e) {
        if (e != null) {
            Log.e(TAG, "Exception: ", e);
        }
        /*mActivity.show(msg);*/  // will be run in UI thread
    }


    /**
     * Get a authentication token if one is not available. If the error is not recoverable then
     * it displays the error message on parent activity right away.
     */
    private String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present, which is
            // recoverable, so we need to show the user some UI through the activity.
            mActivity.handleException(userRecoverableException);
        } catch (GoogleAuthException fatalException) {
            onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
        }
        return null;
    }

    public boolean isTokenExpired (){
        /* TODO: To be implemented.
        * https://developers.google.com/accounts/docs/OAuth2UserAgent#handlingtheresponse */
        return isTokenExpired;
     }
}


