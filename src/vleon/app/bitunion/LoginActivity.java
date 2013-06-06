package vleon.app.bitunion;

import vleon.app.bitunion.api.BitunionAPI;
import vleon.app.bitunion.api.BitunionAPI.LoginResult;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.actionbarsherlock.app.SherlockActivity;

public class LoginActivity extends SherlockActivity {

	EditText usernameText, passwordText;
	Button loginButton, registerButton, forgetPassButton;
//	RadioButton innerNetButton, outerNetButton;
    RadioGroup netGroup;
    CheckBox autologinCheck;
    int netType;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		usernameText = (EditText) findViewById(R.id.usernameText);
		passwordText = (EditText) findViewById(R.id.passwordText);
		loginButton = (Button) findViewById(R.id.loginButton);
//		innerNetButton = (RadioButton)findViewById(R.id.innerNetButton);
//		outerNetButton = (RadioButton)findViewById(R.id.outerNetButton);
		autologinCheck = (CheckBox) findViewById(R.id.autologinCheck);
		netGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		netGroup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v.getId()==R.id.innerNetButton){
					netType = BitunionAPI.INNET;
				} else if (v.getId()==R.id.outerNetButton) {
					netType = BitunionAPI.OUTNET;
				}
			}
		});
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity.api = new BitunionAPI(usernameText.getText()
						.toString().trim(), passwordText.getText().toString());
				MainActivity.api.switchToNet(netType);
//			    new LoginTask().execute();
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				intent.putExtra("autologin", autologinCheck.isChecked());
				intent.putExtra("username", usernameText.getText().toString());
				intent.putExtra("password", passwordText.getText().toString());
				intent.putExtra("nettype", netType);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
	}
	
	class LoginTask extends AsyncTask<Void, Void, LoginResult> {

		@Override
		protected LoginResult doInBackground(Void... params) {
			return MainActivity.api.apiLogin();
		}

		@Override
		protected void onPostExecute(LoginResult result) {
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
			intent.putExtra("autologin", autologinCheck.isChecked());
			intent.putExtra("username", usernameText.getText().toString());
			intent.putExtra("password", passwordText.getText().toString());
			intent.putExtra("nettype", netType);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	

}
