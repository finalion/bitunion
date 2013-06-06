package vleon.app.bitunion;

import java.util.ArrayList;

import vleon.app.bitunion.api.BitunionAPI;
import vleon.app.bitunion.api.BitunionAPI.LoginResult;
import vleon.app.bitunion.api.BuForum;
import vleon.app.bitunion.fragment.MenuFragment;
import vleon.app.bitunion.fragment.MenuFragment.OnForumSelectedListener;
import vleon.app.bitunion.fragment.ThreadFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity implements
		OnForumSelectedListener {

	ArrayList<BuForum> mForumList = new ArrayList<BuForum>();
	ArrayList<ThreadFragment> mFragmentList = new ArrayList<ThreadFragment>();
	ThreadFragment mCurrentFragment = null;
	int mNetType = 0;
	private int mStartFid = 14;
	public static BitunionAPI api;
	boolean mAutoLogin = false;
	String mUsername, mPassword;

	final int LOGIN_REQUEST_CODE = 11111;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		readConfig();
		if (mAutoLogin) {
			api = new BitunionAPI(mUsername, mPassword);
			api.switchToNet(mNetType);
			new LoginTask().execute();
		} else {
			startActivityForResult(new Intent(MainActivity.this,
					LoginActivity.class), LOGIN_REQUEST_CODE);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
			// Bundle tents = data.getExtras();
			mAutoLogin = data.getBooleanExtra("autologin", true);
			mUsername = data.getStringExtra("username");
			mPassword = data.getStringExtra("password");
			mNetType = data.getIntExtra("nettype", BitunionAPI.INNET);
			new LoginTask().execute();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveConfig();
	}

	public void init() {
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new MenuFragment()).commit();

		SlidingMenu sm = getSlidingMenu();

		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindWidth(400);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSlidingActionBarEnabled(true);

		// sm.setMode(SlidingMenu.LEFT_RIGHT);
		// sm.setSecondaryMenu(R.layout.menu_frame_right);
		// getSupportFragmentManager().beginTransaction()
		// .replace(R.id.menu_frame_right, new RightMenuFragment()).commit();
	}

	public void showForum(int fid, String tag) {
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		if (mCurrentFragment != null) {
			transaction.hide(mCurrentFragment);
		}
		ThreadFragment toShowFragment = (ThreadFragment) manager
				.findFragmentByTag(tag);
		if (toShowFragment != null) {
			transaction.show(toShowFragment);
		} else {
			toShowFragment = ThreadFragment.newInstance(fid);
			transaction.add(R.id.threadsFragment, toShowFragment, tag);
		}
		transaction.commit();
		setTitle(tag); // getText(R.string.app_name) + "" +
		mCurrentFragment = toShowFragment;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		case R.id.menu_switchnet:
			if (mNetType == BitunionAPI.INNET) {
				api.switchToNet(BitunionAPI.OUTNET);
				mNetType = BitunionAPI.OUTNET;
			} else if (mNetType == BitunionAPI.OUTNET) {
				api.switchToNet(BitunionAPI.INNET);
				mNetType = BitunionAPI.INNET;
			}
			break;
		case R.id.menu_logout:
			api.apiLogout();
			startActivityForResult(new Intent(MainActivity.this,
					LoginActivity.class), LOGIN_REQUEST_CODE);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem switchItem = menu.findItem(R.id.menu_switchnet);
		if (mNetType == BitunionAPI.INNET) {
			switchItem.setTitle("切换网络至外网");
		} else if (mNetType == BitunionAPI.OUTNET) {
			switchItem.setTitle("切换网络至内网");
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.main, menu);
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class LoginTask extends AsyncTask<Void, Void, LoginResult> {

		@Override
		protected LoginResult doInBackground(Void... params) {
			return api.apiLogin();
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

			// 显示默认论坛版块
			showForum(mStartFid, "灌水乐园");
		}
	}

	@Override
	public void onForumSelected(int fid, String name) {
		getSlidingMenu().showContent();
		showForum(fid, name);
	}

	/*
	 * 读取和保存用户配�?
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
		mNetType = config.getInt("nettype", BitunionAPI.INNET);
		mStartFid = config.getInt("startfid", 14);
		mUsername = config.getString("username", null);
		mPassword = config.getString("password", null);
		mAutoLogin = config.getBoolean("autologin", false);
	}
}
