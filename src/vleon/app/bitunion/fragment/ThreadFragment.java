package vleon.app.bitunion.fragment;

import java.util.ArrayList;

import vleon.app.bitunion.MainActivity;
import vleon.app.bitunion.MainAdapter;
import vleon.app.bitunion.PostActivity;
import vleon.app.bitunion.R;
import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuThread;
import android.R.integer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
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
	ProgressBar progressBar = null;

//	Handler handler = new Handler(){
//
//		@Override
//		public void handleMessage(Message msg) {
//			switch(msg.what){
//			case 
//			}
//		}
//		
//	};
	
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
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 在fragment中使能选项菜单
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.thread_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		progressBar = (ProgressBar) getView().findViewById(R.id.progressBar1);

		/*
		 * 长按事件触发时，如果不反悔true，onListItemClick单击事件也会触发, 否则会一直分发事件直到触发单击
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
		new FetchThreadsTask().execute();
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
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			String string = (String) mode.getTitle();
			switch (item.getItemId()) {
			case R.id.menu_hide:
				Toast.makeText(getActivity(),
						"隐藏: " + mData.get(mActionItemPosition).subject,
						Toast.LENGTH_SHORT).show();
				mode.finish();
				return true;
			case R.id.menu_top:
				Toast.makeText(
						getActivity(),
						"置顶: " + mData.get(mActionItemPosition).subject
								+ string, Toast.LENGTH_SHORT).show();
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			mAdapter.endSelected();
		}

	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			MenuInflater inflater) {
		inflater.inflate(R.menu.thread, menu);
		super.onCreateOptionsMenu(menu, inflater);
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
			new FetchThreadsTask().execute();
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
			mAdapter.notifyDataSetChanged();
			mActionMode.setTitle("已选择" + mAdapter.getSelectedCnt() + "帖");
			if(mAdapter.getSelectedCnt()==0){
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
	


//	Runnable runnable = new Runnable() {
//		
//		@Override
//		public void run() {
//			if(MainActivity.api.refresh()== Result.SUCCESS){
//				handler.sendEmptyMessage(0);
//			}
//		}
//	};

	public class LoginTask extends AsyncTask<Void, Void, Result> {

		@Override
		protected Result doInBackground(Void... arg0) {
			return MainActivity.api.refresh();
		}

		@Override
		protected void onPostExecute(Result result) {
			switch (result) {
			case SUCCESS:
				new FetchThreadsTask().execute();
				break;
			case SUCCESS_EMPTY:
				break;
			case FAILURE:
				break;
			case NETWRONG:
				Toast.makeText(getSherlockActivity(), "网络错误", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(getSherlockActivity(), "未知错误", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	}
	
	public class NewThreadTask extends AsyncTask<String, Void, Result> {

		@Override
		protected Result doInBackground(String... arg0) {
			return MainActivity.api.postThread(getArguments().getInt("fid"),arg0[0],arg0[1]);
		}

		@Override
		protected void onPostExecute(Result result) {
			new FetchThreadsTask().execute();
		}
	}

	public class FetchThreadsTask extends AsyncTask<Void, Void, Result> {
		ArrayList<BuThread> threads = new ArrayList<BuThread>();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
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
			progressBar.setVisibility(View.GONE);
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
				Toast.makeText(getActivity(), "没有数据", Toast.LENGTH_SHORT)
						.show();
				break;
			case FAILURE:
				// 返回数据result字段为failure，刷新api，重新获取session，一般情况下第二次会获得正确数据
				// 但如果有其他原因一直得不到数据，这个任务会一直进行，解决方法是设置重试次数
				new LoginTask().execute();
				new FetchThreadsTask().execute();
				break;
			case NETWRONG:
				Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(getActivity(), "未知错误", Toast.LENGTH_SHORT)
						.show();
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

	public void fetchNextPage() {
		mFrom += STEP;
		new FetchThreadsTask().execute();
	}

	public void fetchPrevPage() {
		mFrom -= STEP;
		if (mFrom <= 0)
			mFrom = 0;
		new FetchThreadsTask().execute();
	}

}