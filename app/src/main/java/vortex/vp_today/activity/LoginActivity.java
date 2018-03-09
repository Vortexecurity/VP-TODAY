package vortex.vp_today.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import vortex.vp_today.R;
import vortex.vp_today.util.Tuple;

/**
 * @author Simon Dräger
 * @version 6.3.18
 */

public class LoginActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null;
    private SharedPreferences prefs;

    private AutoCompleteTextView mUsrView;
    private EditText mPasswordView;
    private CheckBox mRememberLogin;
    private Button mSignInButton;
    private View mProgressView;
    private View mLoginFormView;

    // TODO: Server URL und die Credentials von dem
    private static final String SERVER_URL = "";
    private static final Tuple<String, String> SERVER_CREDENTIALS = new Tuple<>("vp", "god9201");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

        mUsrView = findViewById(R.id.usr);
        mRememberLogin = findViewById(R.id.saveLogin);
        mPasswordView = findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mSignInButton = findViewById(R.id.usr_sign_in_button);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        if (prefs.getBoolean(getString(R.string.settingSaveLogin), false)) {
            mUsrView.setText(prefs.getString(getString(R.string.settingUsrname), ""));
            mPasswordView.setText(prefs.getString(getString(R.string.settingPwd), ""));
            mRememberLogin.setChecked(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mUsrView.setError(null);
        mPasswordView.setError(null);

        String usr = mUsrView.getText().toString();
        String password = mPasswordView.getText().toString();

        Tuple<String, String> creds = /*getCredentials()*/ SERVER_CREDENTIALS;

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password)) {
            if (!mPasswordView.getText().toString().equals(creds.y)) {
                Log.i("attemptLogin", "pwd was " + mPasswordView.getText().toString());
                mPasswordView.setError(getString(R.string.error_incorrect_creds));
                cancel = true;
            }
            focusView = mPasswordView;
        }

        if (TextUtils.isEmpty(usr)) {
            mUsrView.setError(getString(R.string.error_field_required));
            focusView = mUsrView;
            cancel = true;
        } else if (!usr.equals(creds.x)) {
            mUsrView.setError(getString(R.string.error_incorrect_creds));
            focusView = mUsrView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(usr, password);
            mAuthTask.execute((Void) null);
        }
    }

    @Nullable
    private Tuple<String, String> getCredentials() {
        try {
            URL url = new URL(SERVER_URL);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + SERVER_CREDENTIALS);
            // TODO
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mUsrname;
        private final String mPassword;

        UserLoginTask(String usrname, String password) {
            mUsrname = usrname;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: Login vom Server holen

            // TODO: hier Server abfragen

            if (mUsrname.equals(SERVER_CREDENTIALS.x)) {
                if (mPassword.equals(SERVER_CREDENTIALS.y)) {
                    SharedPreferences.Editor e = prefs.edit();

                    e.putBoolean(getString(R.string.settingSaveLogin), mRememberLogin.isChecked());

                    // TODO: VERSCHLÜSSELN !!!
                    e.putString(getString(R.string.settingUsrname), mUsrView.getText().toString());
                    e.putString(getString(R.string.settingPwd), mPasswordView.getText().toString());

                    e.apply();

                    MainActivity.show(getApplicationContext());

                    finish();

                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_creds));
                mUsrView.setError(getString(R.string.error_incorrect_creds));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
