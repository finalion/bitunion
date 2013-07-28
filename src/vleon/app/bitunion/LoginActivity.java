package vleon.app.bitunion;

import vleon.app.bitunion.api.BuAPI;
import vleon.app.bitunion.api.BuAPI.Result;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class LoginActivity extends Activity {
	public static BuAPI api;
	EditText usernameText, passwordText;
	Button loginButton, registerButton, forgetPassButton;
	RadioGroup netGroup;
	CheckBox autologinCheck;
	int netType;
	String mUsername, mPassword;
	boolean mAutoLogin = false;
	int mNetType = 0;
	int mStartFid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		usernameText = (EditText) findViewById(R.id.usernameText);
		passwordText = (EditText) findViewById(R.id.passwordText);
		loginButton = (Button) findViewById(R.id.loginButton);
		autologinCheck = (CheckBox) findViewById(R.id.autologinCheck);
		netGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		netGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				if (arg1 == R.id.innerNetButton) {
					netType = BuAPI.BITNET;
				} else if (arg1 == R.id.outerNetButton) {
					netType = BuAPI.OUTNET;
				}
			}
		});
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mUsername = usernameText.getText().toString();
				mPassword = passwordText.getText().toString();
				mAutoLogin = autologinCheck.isChecked();
				api = new BuAPI(usernameText.getText().toString().trim(),
						passwordText.getText().toString());
				api.setNetType(netType);
				new LoginTask().execute();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveConfig();
	}

	class LoginTask extends AsyncTask<Void, Void, Result> {

		@Override
		protected Result doInBackground(Void... params) {
			return api.login();
		}

		@Override
		protected void onPostExecute(Result result) {
			switch (result) {
			case SUCCESS:
			case SESSIONLOGIN:
				break;
			case FAILURE:
				break;
			case NETWRONG:
				break;
			case UNKNOWN:
				break;
			default:
				break;
			}
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			intent.putExtra("autologin", mAutoLogin);
			intent.putExtra("username", mUsername);
			intent.putExtra("password", mPassword);
			intent.putExtra("nettype", netType);
			startActivity(intent);
			finish();
		}
	}

	/*
	 * 读取和保存用户配置
	 */
	public void saveConfig() {
		SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
		SharedPreferences.Editor editor = config.edit();
		editor.putInt("nettype", mNetType);
		editor.putString("username", mUsername);
		editor.putString("password", mPassword);
		editor.putBoolean("autologin", mAutoLogin);
		editor.commit();
	}
}
