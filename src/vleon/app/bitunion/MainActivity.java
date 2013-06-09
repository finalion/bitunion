package vleon.app.bitunion;

import java.util.ArrayList;
import vleon.app.bitunion.api.BuAPI;
import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuForum;
import vleon.app.bitunion.api.BuThread;
import vleon.app.bitunion.fragment.ContentFragment.OnContentItemClickListener;
import vleon.app.bitunion.fragment.MenuFragment;
import vleon.app.bitunion.fragment.MenuFragment.OnForumSelectedListener;
import vleon.app.bitunion.fragment.ContentFragment;
import vleon.app.bitunion.fragment.PostFragment;
import vleon.app.bitunion.fragment.ThreadFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity implements
		OnForumSelectedListener, OnContentItemClickListener {
	FragmentManager mFragManager = getSupportFragmentManager();
	ArrayList<BuForum> mForumList = new ArrayList<BuForum>();
	ArrayList<ThreadFragment> mFragmentList = new ArrayList<ThreadFragment>();
	ThreadFragment mCurrentFragment = null;

	private int mStartFid = 14;
	public static BuAPI api;

	String mUsername, mPassword;
	boolean mAutoLogin;
	int mNetType;

	final int LOGIN_REQUEST_CODE = 11111;

	public void setSideMenu() {
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
		sm.setBehindWidth(280);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSlidingActionBarEnabled(true);
		// sm.setMode(SlidingMenu.LEFT_RIGHT);
		// sm.setSecondaryMenu(R.layout.menu_frame_right);
		// getSupportFragmentManager().beginTransaction()
		// .replace(R.id.menu_frame_right, new RightMenuFragment()).commit();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSideMenu();
		Intent intent = getIntent();
		mUsername = intent.getStringExtra("username");
		mPassword = intent.getStringExtra("password");
		mNetType = intent.getIntExtra("nettype", BuAPI.BITNET);
		mAutoLogin = intent.getBooleanExtra("autologin", true);
		api = new BuAPI(mUsername, mPassword, mNetType);
		new LoginTask().execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// saveConfig();
	}

	public void showForum(int fid, String tag) {
		
		FragmentTransaction transaction = mFragManager.beginTransaction();
		if (mCurrentFragment != null) {
			transaction.hide(mCurrentFragment);
		}
		ThreadFragment toShowFragment = (ThreadFragment) mFragManager
				.findFragmentByTag(tag);
		if (toShowFragment != null) {
			transaction.show(toShowFragment);
		} else {
			toShowFragment = ThreadFragment.newInstance(fid);
			transaction.add(R.id.contentFragment, toShowFragment, tag);
		}
		transaction.commit();
		setTitle(tag); // getText(R.string.app_name) + "" +
		mCurrentFragment = toShowFragment;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		if(android.os.Build.VERSION.SDK_INT<11)
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		case R.id.menu_switchnet:
			if (api.getNetType() == BuAPI.BITNET) {
				api.setNetType(BuAPI.OUTNET);
			} else if (api.getNetType() == BuAPI.OUTNET) {
				api.setNetType(BuAPI.BITNET);
			}
			break;
		case R.id.menu_logout:
			api.logout();
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem switchItem = menu.findItem(R.id.menu_switchnet);
		if (api.getNetType() == BuAPI.BITNET) {
			switchItem.setTitle("切换网络至外网");
		} else if (api.getNetType() == BuAPI.OUTNET) {
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
				showForum(mStartFid, "灌水乐园");
				break;
			case FAILURE:
				break;
			case NETWRONG:
				break;
			case UNKNOWN:
			default:
				break;
			}
			Toast.makeText(MainActivity.this, "登录结果: " + result.toString(),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onForumSelected(int fid, String name) {
		getSlidingMenu().showContent();
		showForum(fid, name);
	}

	@Override
	public void onItemClicked(int position) {
		BuThread thread = (BuThread) mCurrentFragment.mAdapter.getItem(position);
		PostFragment fragment = PostFragment.newInstance(thread.tid,thread.subject);
		FragmentTransaction transaction = mFragManager.beginTransaction();
		transaction.hide(mCurrentFragment);
		transaction.add(R.id.contentFragment, fragment, null);
		transaction.addToBackStack(null);
		transaction.commit();

	}

	
}
