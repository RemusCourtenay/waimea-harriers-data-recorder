package com.example.waimeaHarriersDataInput;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String SPREADSHEET_ID = "1xQy1WRit5LhKJAQF-bMWw2SvkWHoNo11oSheV3-R-d8";
    private static final String TAB_NAME = "Recorder";
    private static final int TAB_ID = 338712919;

    private static final String CLIENT_ID = "324949108524-qbsct65rum0d14i517g23ifog74hktcg.apps.googleusercontent.com";

    private static final String SHEET_RANGE = TAB_NAME + "!B:B";

    private static final String TAG = "IMPORTANT";

    // Scope for reading user's contacts
    private static final String SHEETS_SCOPE = "https://www.googleapis.com/auth/drive";

    // Bundle key for account object
    private static final String KEY_ACCOUNT = "key_account";

    // Request codes
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_RECOVERABLE = 9002;

    // Global instance of the HTTP transport
    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    // Global instance of the JSON factory
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // ------------------------------------

    private GoogleSignInClient mGoogleSignInClient;

    private Account mAccount;

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restore instance state
        if (savedInstanceState != null) {
            mAccount = savedInstanceState.getParcelable(KEY_ACCOUNT);
        }

        // Configure sign-in to request all Sheets permissions
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(SHEETS_SCOPE))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // For this example we don't need the disconnect button
        findViewById(R.id.disconnect_button).setVisibility(View.GONE);

        // Show a standard Google Sign In button. If your application does not rely on Google Sign
        // In for authentication you could replace this with a "Get Google Contacts" button
        // or similar.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if the user is already signed in and all required scopes are granted
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, new Scope(SHEETS_SCOPE))) {
            updateUI(account);
        } else {
            updateUI(null);
        }
    }


    // Don't know what this does
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ACCOUNT, mAccount);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        // Handling a user-recoverable auth exception
        if (requestCode == RC_RECOVERABLE) {
            if (resultCode == RESULT_OK) {
                getIndex();
            } else {
                Toast.makeText(this, "User recoverable auth exception - RESULT NOT OK", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Signing out clears the current authentication state and resets the default user,
        // this should be used to "switch users" without fully un-linking the user's google
        // account from your application.
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
    }

    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleSignInResult:" + completedTask.isSuccessful());

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);

            // Store the account from the result
            mAccount = account.getAccount();

            // Asynchronously access the People API for the account
            getIndex();
        } catch (ApiException e) {
            Log.w(TAG, "handleSignInResult:error", e);

            // Clear the local account
            mAccount = null;

            // Signed out, show unauthenticated UI.
            updateUI(null);
        }
    }

    private void getIndex() {
        if (mAccount == null) {
            Log.w(TAG, "getIndex: null account");
            return;
        }

        showProgressDialog();
        new GetContactsTask(this).execute(mAccount);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }

    protected void onConnectionsLoadFinished(Integer index) {
        hideProgressDialog();

        if (index == null) {
            Log.d(TAG, "getIndex:integer: null");
            mDetailTextView.setText(getString(R.string.connections_fmt, "None"));
            return;
        }

        Log.d(TAG, "getIndex:integer=" + index);
        mDetailTextView.setText("next free grid num is: " + index);
    }

    protected void onRecoverableAuthException(UserRecoverableAuthIOException recoverableException) {
        Log.w(TAG, "onRecoverableAuthException", recoverableException);
        startActivityForResult(recoverableException.getIntent(), RC_RECOVERABLE);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText("Signed out");
            mDetailTextView.setText(null);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }


    // Actual request
    private static class GetContactsTask extends AsyncTask<Account, Void, ValueRange> {

        private WeakReference<LoginActivity> mActivityRef;

        public GetContactsTask(LoginActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        @Override
        protected ValueRange doInBackground(Account... accounts) {
            if (mActivityRef.get() == null) {
                return null;
            }

            Context context = mActivityRef.get().getApplicationContext();
            try {
                // using static scopes
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        context,
                        Collections.singleton(SHEETS_SCOPE));
                // choosing our single account
                credential.setSelectedAccount(accounts[0]);

                Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName("Waimea Harriers Data Test")
                        .build();

                return service.spreadsheets().values()
                        .get(SPREADSHEET_ID,SHEET_RANGE)
                        .execute();

            } catch (UserRecoverableAuthIOException recoverableException) {
                if (mActivityRef.get() != null) {
                    mActivityRef.get().onRecoverableAuthException(recoverableException);
                }
            } catch (IOException e) {
                Log.w(TAG, "getContacts:exception", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ValueRange values) {
            super.onPostExecute(values);
            if (mActivityRef.get() != null) {
                mActivityRef.get().onConnectionsLoadFinished(values.getValues().size());
            }
        }
    }
}




