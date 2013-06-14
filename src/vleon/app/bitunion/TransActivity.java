package vleon.app.bitunion;

import vleon.app.bitunion.api.BuAPI;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class TransActivity extends SherlockActivity {

	String mUsername, mPassword;
	int mNetType;
	boolean mAutoLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		readConfig();
		// 读取保存的用户信息成功，用户并且设置了自动登录，跳转到主界面
		// 否则跳转到登录界面
//		Toast.makeText(this, "过渡界面", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.putExtra("username", mUsername);
		intent.putExtra("password", mPassword);
		intent.putExtra("nettype", mNetType);
		intent.putExtra("autologin", mAutoLogin);
		if (mUsername != null && mPassword != null && mAutoLogin) {
//			Toast.makeText(this, "跳转到主界面", Toast.LENGTH_SHORT).show();
			intent.setClass(this, MainActivity.class);
		} else {
//			Toast.makeText(this, "跳转到登录界面", Toast.LENGTH_SHORT).show();
			intent.setClass(this, LoginActivity.class);
		}
		startActivity(intent);
		finish();
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
		// mStartFid = config.getInt("startfid", 14);
		mUsername = config.getString("username", null);
		mPassword = config.getString("password", null);
		mAutoLogin = config.getBoolean("autologin", false);
	}
	

}
