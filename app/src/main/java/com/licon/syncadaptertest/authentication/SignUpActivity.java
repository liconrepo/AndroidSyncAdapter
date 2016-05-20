package com.licon.syncadaptertest.authentication;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.licon.syncadaptertest.R;

import static com.licon.syncadaptertest.authentication.AccountGeneral.sServerAuthenticate;
import static com.licon.syncadaptertest.authentication.SignInAuthenticatorActivity.ARG_ACCOUNT_TYPE;
import static com.licon.syncadaptertest.authentication.SignInAuthenticatorActivity.KEY_ERROR_MESSAGE;
import static com.licon.syncadaptertest.authentication.SignInAuthenticatorActivity.PARAM_USER_PASS;

public class SignUpActivity extends Activity implements View.OnClickListener {

    private String TAG = getClass().getSimpleName();
    private String mAccountType;

    private TextView mTextAlreadyMember;
    private Button mButtonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
        setContentView(R.layout.activity_register);

        mTextAlreadyMember = (TextView) findViewById(R.id.alreadyMember);
        mButtonSubmit = (Button) findViewById(R.id.submit);

        mTextAlreadyMember.setOnClickListener(this);
        mButtonSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alreadyMember:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.submit:
                createAccount();
                break;
        }
    }

    private void createAccount() {

        // Validation!
        new AsyncTask<String, Void, Intent>() {

            String name = ((TextView) findViewById(R.id.name)).getText().toString().trim();
            String accountName = ((TextView) findViewById(R.id.accountName)).getText().toString().trim();
            String accountPassword = ((TextView) findViewById(R.id.accountPassword)).getText().toString().trim();

            @Override
            protected Intent doInBackground(String... params) {

                Log.d(TAG, " > Started authenticating");

                String authtoken = null;
                Bundle data = new Bundle();
                try {
                    authtoken = sServerAuthenticate.userSignUp(name, accountName, accountPassword,
                            AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);

                    data.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                    data.putString(PARAM_USER_PASS, accountPassword);
                } catch (Exception e) {
                    data.putString(KEY_ERROR_MESSAGE, e.getMessage());
                }

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                    Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE),
                            Toast.LENGTH_SHORT).show();
                } else {
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}