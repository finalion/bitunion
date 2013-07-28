package vleon.app.bitunion;

import java.util.ArrayList;

import vleon.app.bitunion.api.BuAPI;
import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuForum;
import vleon.app.bitunion.api.BuThread;
import vleon.app.bitunion.fragment.ContentFragment;
import vleon.app.bitunion.fragment.ContentFragment.OnContentItemClickListener;
import vleon.app.bitunion.fragment.MenuFragment;
import vleon.app.bitunion.fragment.MenuFragment.OnForumSelectedListener;
import vleon.app.bitunion.fragment.PostFragment;
import vleon.app.bitunion.fragment.ThreadFragment;
import android.app.AlertDialog;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity implements
		OnForumSelectedListener, OnContentItemClickListener {
	FragmentManager mFragManager = getSupportFragmentManager();
	ArrayList<BuForum> mForumList = new ArrayList<BuForum>();
	ArrayList<ThreadFragment> mFragmentList = new ArrayList<ThreadFragment>();
	ContentFragment mCurrentThreadFragment = null;
	private MenuItem mRefreshItem;
	long lastExitTime = 0;
	private int mStartFid = 14;
	public static BuAPI api;

	String mUsername, mPassword;
	boolean mAutoLogin;
	int mNetType;

	DrawerLayout mDrawerLayout;
	ListView mDrawerList;
	ActionBarDrawerToggle mDrawerToggle;
	LeftDrawerAdapter mAdapter;

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
	 // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	
	 setSlidingActionBarEnabled(true);
	 sm.setMode(SlidingMenu.RIGHT);
	
	 }

	public void setLeftDrawer() {
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
//				getActionBar().setTitle(mTitle);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
//				getActionBar().setTitle(mDrawerTitle);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		String[] forumNames = getResources().getStringArray(R.array.forums);
		int[] forumFids = getResources().getIntArray(R.array.fids);
		int[] forumTypes = getResources().getIntArray(R.array.types);
		for (int i = 0; i < forumFids.length; i++) {
			mForumList.add(new BuForum(forumNames[i], forumFids[i],
					forumTypes[i]));
		}
		ArrayList<Integer> typesAdded = new ArrayList<Integer>();
		SparseArray<String> types = new SparseArray<String>();
		types.put(0, "系统管理区");
		types.put(1, "直通理工区");
		types.put(3, "苦中作乐区");
		types.put(2, "技术讨论区");
		for (BuForum forum : mForumList) {
			if (!typesAdded.contains(forum.getType())) {
				typesAdded.add(forum.getType());
				mAdapter.add(types.get(forum.getType()));
			}
			mAdapter.add(forum);
		}
		mDrawerList.setAdapter(mAdapter);
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mAdapter = new LeftDrawerAdapter(this);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		setLeftDrawer();
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
	public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();   //必须有这个，否则ActionBarDrawerTogger不能改变图标
    }
	@Override
	protected void onPause() {
		super.onPause();
		saveNetType();
	}

	public void showForum(int fid, String tag) {
		FragmentTransaction transaction = mFragManager.beginTransaction();
		ThreadFragment toShowFragment = (ThreadFragment) mFragManager
				.findFragmentByTag(tag);
		if (mCurrentThreadFragment != null) {
			transaction.hide(mCurrentThreadFragment);
			// if (mCurrentThreadFragment.getTag().equals("post")) {
			// transaction.remove(mCurrentThreadFragment);
			// }
		}
		if (showingPostFragment()) {
			transaction.remove(mFragManager.findFragmentByTag("post"));
		}
		if (toShowFragment != null) {
			transaction.show(toShowFragment);
		} else {
			toShowFragment = ThreadFragment.newInstance(fid);
			transaction.add(R.id.contentFragment, toShowFragment, tag);
		}
		transaction.commit();
		setTitle(tag); // getText(R.string.app_name) + "" +
		mCurrentThreadFragment = toShowFragment;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// if(android.os.Build.VERSION.SDK_INT<11)
		switch (item.getItemId()) {
		case android.R.id.home:
			Fragment fragment = mFragManager.findFragmentByTag("post");
			if (fragment == null) {
				 toggle();
			} else {
				onBackPressed();
			}
			break;
		case R.id.menu_refresh:
			((ContentFragment) getCurrentFragment()).refresh();
			break;
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
		case R.id.menu_about:
			new AlertDialog.Builder(this)
					.setMessage(
							"项目地址: https://github.com/finalion/bitunion \n\n开发者: vleon, somebody ")
					.setTitle("关于").show();
			break;
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		mRefreshItem = menu.findItem(R.id.menu_refresh);
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
		// getSupportMenuInflater().inflate(R.menu.main, menu); 6.21
		getMenuInflater().inflate(R.menu.main, menu);
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
		// getSlidingMenu().showContent();
		showForum(fid, name);
	}

	@Override
	public void onItemClicked(int position) {
		BuThread thread = (BuThread) mCurrentThreadFragment.mAdapter
				.getItem(position);
		PostFragment fragment = PostFragment.newInstance(thread.tid,
				thread.subject);
		FragmentTransaction transaction = mFragManager.beginTransaction();
		transaction.hide(mCurrentThreadFragment);
		transaction.add(R.id.contentFragment, fragment, "post");
		transaction.addToBackStack(thread.tid + "");
		transaction.commit();
	}

	public Fragment getBackStackTopFragment() {
		int backStackCount = mFragManager.getBackStackEntryCount();
		if (backStackCount == 0) {
			return null;
		}
		BackStackEntry backStackEntry = mFragManager
				.getBackStackEntryAt(backStackCount - 1);
		String fragName = backStackEntry.getName();
		return mFragManager.findFragmentByTag(fragName);
	}

	public boolean showingPostFragment() {
		Fragment fragment = mFragManager.findFragmentByTag("post");
		return (fragment == null) ? false : true;
	}

	public Fragment getCurrentFragment() {
		Fragment fragment = mFragManager.findFragmentByTag("post");
		if (fragment != null) {
			return fragment;
		}
		return mCurrentThreadFragment;
	}

	/*
	 * 按两次返回键退出程序
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!showingPostFragment() && keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if (System.currentTimeMillis() - lastExitTime > 2000) {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				lastExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * 保存网络登录类型，下次启动使用
	 */
	public void saveNetType() {
		SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
		SharedPreferences.Editor editor = config.edit();
		editor.putInt("nettype", api.getNetType());
		editor.commit();
	}

	public MenuItem getRefreshItem() {
		return mRefreshItem;
	}

	public class LeftDrawerAdapter extends ArrayAdapter<Object> {

		Context mContext;

		public LeftDrawerAdapter(Context context) {
			super(context, 0);
			mContext = context;
		}

		/*
		 * 屏蔽标题栏的点击事件
		 */
		@Override
		public boolean areAllItemsEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			return getItem(position) instanceof BuForum;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.menu_row, null);
			}
			TextView title = (TextView) convertView
					.findViewById(R.id.row_title);
			TextView categroy = (TextView) convertView
					.findViewById(R.id.category_title);
			Object item = getItem(position);
			if (item instanceof String) {
				title.setVisibility(View.GONE);
				categroy.setVisibility(View.VISIBLE);
				categroy.setText((String) item);

			} else {
				title.setVisibility(View.VISIBLE);
				categroy.setVisibility(View.GONE);
				title.setText(((BuForum) item).getName());
			}
			return convertView;
		}
	}
}
