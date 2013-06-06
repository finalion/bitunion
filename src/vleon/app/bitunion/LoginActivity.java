package vleon.app.bitunion;

import vleon.app.bitunion.api.BuAPI;
import vleon.app.bitunion.api.BuAPI.Result;
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

import com.actionbarsherlock.app.SherlockActivity;

public class LoginActivity extends SherlockActivity {
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
		readConfig();
		// 如果读取保存的账户信息成功
		if (mUsername != null && mPassword != null) {
			// 如果打开了自动登录功能，则用保存的账号登录，直接跳转到主界面
			if (mAutoLogin) {
				api = new BuAPI(mUsername, mPassword);
				api.switchToNet(mNetType);
				new LoginTask().execute();
				return;
			}
		}
		// 显示登录界面
		setContentView(R.layout.activity_login);
		usernameText = (EditText) findViewById(R.id.usernameText);
		passwordText = (EditText) findViewById(R.id.passwordText);
		loginButton = (Button) findViewById(R.id.loginButton);
		autologinCheck = (CheckBox) findViewById(R.id.autologinCheck);
		netGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		usernameText.setText("vleon");
		passwordText.setText("fengliang20701159");
		netGroup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.innerNetButton) {
					netType = BuAPI.BITNET;
				} else if (v.getId() == R.id.outerNetButton) {
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
				api.switchToNet(netType);
				new LoginTask().execute();
				// Intent intent = new Intent(LoginActivity.this,
				// MainActivity.class);
				// intent.putExtra("autologin", autologinCheck.isChecked());
				// intent.putExtra("username",
				// usernameText.getText().toString());
				// intent.putExtra("password",
				// passwordText.getText().toString());
				// intent.putExtra("nettype", netType);
				// setResult(RESULT_OK, intent);
				// finish();
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
			// Toast.makeText(MainActivity.this,
			// "Login Result: " + result.toString(), Toast.LENGTH_SHORT)
			// .show();
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			intent.putExtra("autologin", mAutoLogin);
			intent.putExtra("username", mUsername);
			intent.putExtra("password", mPassword);
			intent.putExtra("nettype", netType);
			// setResult(RESULT_OK, intent);
			startActivity(intent);
			finish();
			overridePendingTransition(0, 0);
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

	public void readConfig() {
		SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
		mNetType = config.getInt("nettype", BuAPI.BITNET);
		mStartFid = config.getInt("startfid", 14);
		mUsername = config.getString("username", null);
		mPassword = config.getString("password", null);
		mAutoLogin = config.getBoolean("autologin", false);
	}

}
