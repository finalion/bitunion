package vleon.app.bitunion.fragment;

import java.util.ArrayList;

import vleon.app.bitunion.MainActivity;
import vleon.app.bitunion.MainAdapter;
import vleon.app.bitunion.PostActivity;
import vleon.app.bitunion.R;
import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuThread;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ThreadFragment extends SherlockListFragment {
	private ArrayList<BuThread> mData;
	private ThreadsAdapter mAdapter;
	private static int mFrom;
	final int STEP = 20;
	ActionMode mActionMode;
	int mActionItemPosition = -1;
	MenuItem mRefreshItem;

	public static ThreadFragment newInstance(int fid) {
		ThreadFragment fragment = new ThreadFragment();
		mFrom = 0;
		Bundle args = new Bundle();
		args.putInt("fid", fid);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.thread_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		/*
		 * 长按事件触发时，如果不返回true，onListItemClick单击事件也会触发, 否则会一直分发事件直到触发单击
		 */
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mActionItemPosition = arg2;
				if (mActionMode != null) {
					// if already in action mode - do nothing
					return false;
				}
				mAdapter.beginSelected();
				mAdapter.addSelects(arg2);
				mAdapter.notifyDataSetChanged();
				mActionMode = getSherlockActivity().startActionMode(
						new ActionModeCallback());
				mActionMode.invalidate();
				mActionMode.setTitle("已选择" + mAdapter.getSelectedCnt() + "帖");
				return true;
			}
		});

		mData = new ArrayList<BuThread>();
		mAdapter = new ThreadsAdapter(getActivity(), mData);
		setListAdapter(mAdapter);
		fetchThreads();
	}

	class ActionModeCallback implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.thread_context_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// mode.getMenuInflater().inflate(R.menu.thread_context_menu, menu);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_hide:
				break;
			case R.id.menu_top:
				break;
			case R.id.menu_profile:
				BuThread t = (BuThread) mAdapter.getItem(Integer
						.valueOf(mAdapter.getSelected().get(0)));
				ProfileFragment fragment = ProfileFragment.newInstance(
						t.authorid, t.author);
				FragmentTransaction ft = getSherlockActivity()
						.getSupportFragmentManager().beginTransaction();
				fragment.show(ft, "作者信息");
				break;
			default:
				break;
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			mAdapter.endSelected();
		}

		public void disableProfileItem() {

		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.thread, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_post:
			final View view = LayoutInflater.from(getSherlockActivity())
					.inflate(R.layout.newthread_dialog, null);
			new AlertDialog.Builder(getSherlockActivity()).setView(view)
					.setTitle("发表新帖")
					.setNegativeButton("取消", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).setPositiveButton("发表", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							new NewThreadTask().execute(((EditText) view
									.findViewById(R.id.newSubjectText))
									.getText().toString(), ((EditText) view
									.findViewById(R.id.newContentText))
									.getText().toString());
						}
					}).show();
		case R.id.menu_refresh:
			mRefreshItem = item;
			mRefreshItem.setActionView(R.layout.progress);
			fetchThreads();
			break;
		case R.id.menu_next:
			fetchNextPage();
			break;
		case R.id.menu_prev:
			fetchPrevPage();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		if (mActionMode != null) {
			mAdapter.toggleSelected(position);
			// mAdapter.notifyDataSetChanged();
			mActionMode.setTitle("已选择" + mAdapter.getSelectedCnt() + "帖");
			if (mAdapter.getSelectedCnt() > 1) {
				mActionMode.getMenu().findItem(R.id.menu_profile)
						.setVisible(false);
			} else {
				mActionMode.getMenu().findItem(R.id.menu_profile)
						.setVisible(true);
			}
			if (mAdapter.getSelectedCnt() == 0) {
				mActionMode.finish();
			}
		} else {
			Intent intent = new Intent(getActivity(), PostActivity.class);
			BuThread thread = mData.get(position);
			intent.putExtra("id", thread.tid);
			intent.putExtra("subject", thread.subject);
			startActivity(intent);
		}

	}

	public class NewThreadTask extends AsyncTask<String, Void, Result> {

		@Override
		protected Result doInBackground(String... arg0) {
			return MainActivity.api.postThread(getArguments().getInt("fid"),
					arg0[0], arg0[1]);
		}

		@Override
		protected void onPostExecute(Result result) {
			fetchThreads();
		}
	}

	public class FetchThreadsTask extends AsyncTask<Void, Void, Result> {
		ArrayList<BuThread> threads = new ArrayList<BuThread>();

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			threads.clear();
		}

		@Override
		protected Result doInBackground(Void... params) {
			int fid = ThreadFragment.this.getArguments().getInt("fid");
			return MainActivity.api.getThreads(threads, fid, mFrom, mFrom
					+ STEP);
		}

		@Override
		protected void onPostExecute(Result result) {
			if (mRefreshItem!=null && mRefreshItem.getActionView() != null) {
				mRefreshItem.setActionView(null);
			}
			switch (result) {
			case SUCCESS:
				mData.clear();
				for (int i = 0; i < threads.size(); i++) {
					mData.add(threads.get(i));
				}
				mAdapter.notifyDataSetChanged();
				// 自动滚动到顶端显示
				ThreadFragment.this.setSelection(0);
				break;
			case SUCCESS_EMPTY:
				mFrom -= STEP;
				showToast("没有数据");
				break;
			case FAILURE:
				showToast("获取session失败");
				break;
			case NETWRONG:
				showToast("网络错误");
				break;
			default:
				showToast("未知错误");
				break;
			}
		}
	}

	class ThreadsAdapter extends MainAdapter {
		ArrayList<BuThread> threads;

		public ThreadsAdapter(Context context, ArrayList<BuThread> threads) {
			super(context);
			this.threads = threads;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return threads.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return threads.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		class ViewHolder {
			TextView flagView, subjectView, authorView, countsView,
					lastpostView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			// @SuppressWarnings("unchecked")
			BuThread item = (BuThread) getItem(position);
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.thread_item, null);
				holder = new ViewHolder();
				holder.flagView = (TextView) convertView
						.findViewById(R.id.flagText);
				holder.subjectView = (TextView) convertView
						.findViewById(R.id.subjectText);
				holder.authorView = (TextView) convertView
						.findViewById(R.id.authorText);
				holder.countsView = (TextView) convertView
						.findViewById(R.id.countText);
				holder.lastpostView = (TextView) convertView
						.findViewById(R.id.timeText);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.subjectView.setText(Html.fromHtml(item.subject));
			holder.authorView.setText(item.author);
			holder.countsView.setText(item.replies + "/" + item.views);
			holder.lastpostView.setText(item.lastpost);
			if (item.topFlag) {
				holder.flagView.setVisibility(View.VISIBLE);
			} else {
				holder.flagView.setVisibility(View.GONE);
			}
			// if (position % 2 == 0) {
			// convertView.setBackgroundResource(R.drawable.odd_item);
			// } else {
			// convertView.setBackgroundResource(R.drawable.even_item);
			// }
			if (mSelected
					&& mSelectedIndexs.contains(Integer.valueOf(position))) {
				convertView.setBackgroundColor(getResources().getColor(
						R.color.item_selected));
			} else {
				convertView.setBackgroundResource(R.drawable.even_item);
			}
			return convertView;
		}
	}

	void fetchNextPage() {
		mFrom += STEP;
		fetchThreads();
	}

	void fetchPrevPage() {
		mFrom -= STEP;
		if (mFrom <= 0)
			mFrom = 0;
		fetchThreads();
	}

	void fetchThreads() {
		new FetchThreadsTask().execute();
	}

	void showToast(String str) {
		Toast.makeText(getSherlockActivity(), str, Toast.LENGTH_SHORT).show();
	}
}