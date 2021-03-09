package com.example.waimeaHarriersDataInput;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class SheetReader  {

    private static final String SPREADSHEET_ID = "1xQy1WRit5LhKJAQF-bMWw2SvkWHoNo11oSheV3-R-d8";
    private static final String TAB_NAME = "Recorder";
    private static final String APPLICATION_NAME = "Waimea Harriers Data Recorder";
    private static final String TAG = "IMPORTANT!";

    private static final String SHEETS_SCOPE = "https://www.googleapis.com/auth/drive";


    // Global instance of the HTTP transport
    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    // Global instance of the JSON factory
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final Account loggedInAccount;

    public SheetReader(Account googleAccount) {
        this.loggedInAccount = googleAccount;
    }



    public void getDataRow(int rowNum, InputDataActivity activity) {
        new GetDataRowTask(rowNum, activity).execute(loggedInAccount);
    }

    private static class GetDataRowTask extends AsyncTask<Account, Void, ValueRange> {

        private int rowNum;
        private WeakReference<InputDataActivity> activityWeakReference;

        public GetDataRowTask(int rowNum, InputDataActivity activity) {
            this.rowNum = rowNum;
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected ValueRange doInBackground(Account... accounts) {

            if (activityWeakReference == null) {
                return null;
            }

            Context context = activityWeakReference.get().getApplicationContext();
            Sheets service;

            try {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singletonList(SHEETS_SCOPE));
                credential.setSelectedAccount(accounts[0]);
                service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();

                return service.spreadsheets().values().get(SPREADSHEET_ID,TAB_NAME + "!C" + rowNum + ":D" + rowNum).execute();
            } catch (IOException exception) {
                Log.w(TAG, "GetDataRowTask:IOException", exception);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ValueRange valueRange) {
            super.onPostExecute(valueRange);
            activityWeakReference.get().onSheetsReturnedData(valueRange);
        }
    }
}
