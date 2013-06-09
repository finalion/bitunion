package vleon.app.bitunion.fragment;

import java.util.ArrayList;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import com.actionbarsherlock.view.Menu;

import vleon.app.bitunion.MainActivity;
import vleon.app.bitunion.MainAdapter;
import vleon.app.bitunion.R;
import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuContent;
import vleon.app.bitunion.api.BuPost;
import vleon.app.bitunion.api.BuThread;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;

public class ThreadFragment extends ContentFragment {
	// private ArrayList<BuThread> mData;
	int mActionItemPosition = -1;
	ArrayList<BuThread> mThreads;

	public static ThreadFragment newInstance(int fid) {
		ThreadFragment fragment = new ThreadFragment();
		Bundle args = new Bundle();
		args.putInt("fid", fid);
		args.putInt("tag", THREAD);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.thread_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mThreads = new ArrayList<BuThread>();
		mAdapter = new ThreadsAdapter(getSherlockActivity(), mThreads);
		setListAdapter(mAdapter);
		fetchContents();
	}

	@Override
	public void reply() {
		final View view = LayoutInflater.from(getSherlockActivity()).inflate(
				R.layout.newthread_dialog, null);
		new AlertDialog.Builder(getSherlockActivity()).setView(view)
				.setTitle("发表新帖")
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setPositiveButton("发表", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new NewThreadTask().execute(((EditText) view
								.findViewById(R.id.newSubjectText)).getText()
								.toString(), ((EditText) view
								.findViewById(R.id.newContentText)).getText()
								.toString());
					}
				}).show();
	}

	public class NewThreadTask extends AsyncTask<String, Void, Result> {

		@Override
		protected Result doInBackground(String... arg0) {
			return MainActivity.api.postThread(getArguments().getInt("fid"),
					arg0[0], arg0[1]);
		}
		@Override
		protected void onPostExecute(Result result) {
			fetchContents();
		}
	}

	@Override
	public Result fetchContentTask() {
		int fid = ThreadFragment.this.getArguments().getInt("fid");
		return MainActivity.api.getThreads(mThreads, fid, mCurrentPageCnt,
				mCurrentPageCnt + mPageStep);
	}

	class ThreadsAdapter extends MainAdapter {
		public ArrayList<BuThread> mData;

		ViewHolder holder;

		public ThreadsAdapter(Context context, ArrayList<BuThread> threads) {
			super(context);
			this.mData = threads;
			holder = new ViewHolder();
		}

		class ViewHolder {
			TextView flagView, subjectView, authorView, countsView,
					lastpostView;
		}

		@Override
		public void clear() {
			mData.clear();
			notifyDataSetChanged();
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public String getAuthor(int position) {
			return ((BuThread) getItem(position)).author;
		}

		@Override
		public String getAuthorID(int position) {
			return ((BuThread) getItem(position)).authorid;
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

}