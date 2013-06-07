package vleon.app.bitunion;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuPost;
import vleon.app.bitunion.api.Quote;
import vleon.app.bitunion.fragment.ThreadFragment.FetchThreadsTask;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PostActivity extends SherlockActivity {
	private String mTid;
	private String mSubject;
	ArrayList<BuPost> data = new ArrayList<BuPost>();
	ListAdapter adapter = null;
	ListView mListView;
	TextView mTitleView;
	int mFrom;
	final int STEP = 20;
	HashMap<String, SoftReference<Drawable>> mDrawableCache;
	ActionMode mActionMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_posts);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mTid = getIntent().getStringExtra("id");
		mSubject = getIntent().getStringExtra("subject");
		mListView = (ListView) findViewById(R.id.post_list);
		mTitleView = (TextView) findViewById(R.id.title_text);
		mTitleView.setText(Html.fromHtml(mSubject));
		adapter = new ListAdapter(this, data);
		mListView.setAdapter(adapter);
		mFrom = 0;
		mDrawableCache = new HashMap<String, SoftReference<Drawable>>();
		fetchPosts();

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (mActionMode != null) {
					// if already in action mode - do nothing
					return false;
				}
				adapter.beginSelected();
				adapter.addSelects(arg2);
				adapter.notifyDataSetChanged();
				mActionMode = PostActivity.this
						.startActionMode(new ActionModeCallback());
				mActionMode.invalidate();
				mActionMode.setTitle("已引用" + adapter.getSelectedCnt() + "个回复");
				return true;
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (mActionMode != null) {
					adapter.toggleSelected(arg2);
					adapter.notifyDataSetChanged();
					mActionMode.setTitle("已引用" + adapter.getSelectedCnt() + "个回复");
					if(adapter.getSelectedCnt()==0){
						mActionMode.finish();
					}
						
				} else {

				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.thread, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_post).setIcon(R.drawable.social_reply);
		menu.findItem(R.id.menu_post).setTitle("回复");
		return super.onPrepareOptionsMenu(menu);
	}

	class ActionModeCallback implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.post_context_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			adapter.endSelected();
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_quotereply:
				Toast.makeText(PostActivity.this, "引用: ", Toast.LENGTH_SHORT)
						.show();
				String replyQuote = "";
				BuPost tmpPost;
				// 构造引用文本
				for (Integer index : adapter.getSelected()) {
					tmpPost = (BuPost) adapter.getItem(Integer.valueOf(index));
					replyQuote += "[quote][b]" + tmpPost.author + "[/b] "
							+ tmpPost.lastedit + "\n" + tmpPost.content
							+ "[/quote]\n";
				}
				showReplyDialog(replyQuote);
				mode.finish();
				return true;
			default:
				return false;
			}
		}

	}

	public void showReplyDialog(String content) {
		View view = LayoutInflater.from(this).inflate(R.layout.reply_dialog,
				null);
		final EditText contentText = (EditText) view
				.findViewById(R.id.replyText);
		contentText.setText(content);
		contentText.setSelection(content.length()); // 定位光标到文本框末尾
		new AlertDialog.Builder(this).setView(view).setTitle(mSubject)
				.setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).setPositiveButton("回复", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new ReplyTask().execute(contentText.getText()
								.toString());

					}
				}).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case R.id.menu_refresh:
			fetchPosts();
			break;
		case R.id.menu_next:
			mFrom += STEP;
			fetchPosts();
			break;
		case R.id.menu_prev:
			mFrom -= STEP;
			if (mFrom <= 0)
				mFrom = 0;
			fetchPosts();
			break;
		case R.id.menu_post:
			showReplyDialog("");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void fetchPosts() {
		new FetchPostsTask().execute();
	}

	public class LoginTask extends AsyncTask<Void, Void, Result> {

		@Override
		protected Result doInBackground(Void... arg0) {
			return MainActivity.api.refresh();
		}

		@Override
		protected void onPostExecute(Result result) {
			switch (result) {
			case SUCCESS:
				new FetchPostsTask().execute();
				break;
			case SUCCESS_EMPTY:
				break;
			case FAILURE:
				break;
			case NETWRONG:
				Toast.makeText(PostActivity.this, "网络错误", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(PostActivity.this, "未知错误", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	}
	
	class FetchPostsTask extends AsyncTask<Void, Void, Result> {
		ArrayList<BuPost> posts = new ArrayList<BuPost>();

		@Override
		protected void onPreExecute() {
			// pBar.setVisibility(View.VISIBLE);
			super.onPreExecute();
			posts.clear();
		}

		@Override
		protected Result doInBackground(Void... params) {
			return MainActivity.api.getPosts(posts, mTid, mFrom, mFrom + STEP);
		}

		@Override
		protected void onPostExecute(Result result) {
			switch (result) {
			case SUCCESS:
				data.clear();
				for (int i = 0; i < posts.size(); i++) {
					data.add(posts.get(i));
				}
				adapter.notifyDataSetChanged();
				// 自动滚动到顶端显示
				mListView.setSelection(0);
				break;
			case SUCCESS_EMPTY:
				Toast.makeText(PostActivity.this, "没有数据", Toast.LENGTH_SHORT)
						.show();
				break;
			case FAILURE:
				// 返回数据result字段为failure，刷新api，重新获取session，一般情况下第二次会获得正确数据
				// 但如果有其他原因一直得不到数据，这个任务会一直进行，解决方法是设置重试次数
				new LoginTask().execute();
				Toast.makeText(PostActivity.this, "重新获取SESSION成功",
						Toast.LENGTH_SHORT).show();
				break;
			case NETWRONG:
				Toast.makeText(PostActivity.this, "网络错误", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(PostActivity.this, "未知错误", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	}

	public class ReplyTask extends AsyncTask<String, Void, Result> {

		@Override
		protected Result doInBackground(String... arg0) {
			return MainActivity.api.replyThread(mTid, arg0[0]);
		}

		@Override
		protected void onPostExecute(Result result) {
			new FetchPostsTask().execute();
		}
	}

	class ListAdapter extends MainAdapter {
		ArrayList<BuPost> mData;
		ViewHolder holder;

		public ListAdapter(Context context, ArrayList<BuPost> data) {
			super(context);
			this.mData = data;
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

		class ViewHolder {
			TextView titleView, messageView, authorView, lasteditView,
					quotesView;
			ImageView attachmentView;
			RelativeLayout quoteLayout;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// @SuppressWarnings("unchecked")
			BuPost item = (BuPost) getItem(position);
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.post_item, null);
				holder = new ViewHolder();
				holder.attachmentView = (ImageView) convertView
						.findViewById(R.id.attachmentView);
				holder.messageView = (TextView) convertView
						.findViewById(R.id.messageView);
				holder.authorView = (TextView) convertView
						.findViewById(R.id.authorText2);
				holder.lasteditView = (TextView) convertView
						.findViewById(R.id.timeText2);
				holder.quotesView = (TextView) convertView
						.findViewById(R.id.quotesView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 显示引用信息部分
			holder.quotesView.setVisibility(View.GONE);
			String quoteString = "";
			Quote tmpQuote;
			for (int i = 0; i < item.quotes.size(); i++) {
				tmpQuote = item.quotes.get(i);
				if(i>0)
					quoteString += "<br/><br/>";
				quoteString += "&nbsp;&nbsp;&nbsp;"+tmpQuote.quoteAuthor + ":&nbsp;"+ tmpQuote.quoteContent ;
				holder.quotesView.setVisibility(View.VISIBLE);
			}
			
			holder.quotesView
					.setText(Html.fromHtml(quoteString, new ImageGetterFirst(
							holder.quotesView, quoteString), null));
			// 显示发帖人信息部分
			holder.messageView.setText(Html.fromHtml(item.content,
					new ImageGetterFirst(holder.messageView, item.content),
					null));
			holder.authorView.setText(item.author);
			holder.lasteditView.setText(item.lastedit);
			// 如果只引用了回复没有发表自己的message，那么messageView不显示（如果显示的话，
			// 引用段落下面有一个大的空白，不美观；但如果没有message，也没有引用回复的话，如果不显示
			// messageView，条目变成只含有作者和时间的一窄条，也不美观）
			// if (item.quoteAuthor != null && item.content == "") {
			// holder.messageView.setVisibility(View.GONE);
			// } else {
			// holder.messageView.setVisibility(View.VISIBLE);
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

	class ImageGetterFirst implements Html.ImageGetter {

		Drawable defaultDrawable = getResources().getDrawable(
				R.drawable.content_picture);
		private TextView mTextView;
		private String mContent;

		public ImageGetterFirst(TextView textView, String content) {
			mTextView = textView;
			mContent = content;
		}

		@Override
		public Drawable getDrawable(String source) {
			Drawable drawable = null;
			if (mDrawableCache.containsKey(source)) {
				drawable = mDrawableCache.get(source).get();
			} else {
				ImageDownloadData data = new ImageDownloadData(source,
						mContent, mTextView);
				new GetImageTask(data).execute();
				drawable = defaultDrawable;
			}
			if (drawable != null)
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight());
			return drawable;
		}

	}

	class ImageGetterSecond implements Html.ImageGetter {

		@Override
		public Drawable getDrawable(String source) {
			if (mDrawableCache.containsKey(source)) {
				return mDrawableCache.get(source).get();
			}
			return null;
		}

	}

	class ImageDownloadData {
		private String imageSource;
		private String message;
		private TextView textView;

		public ImageDownloadData(String imageSource, String message,
				TextView textView) {
			this.imageSource = imageSource;
			this.message = message;
			this.textView = textView;
		}

		public String getImageSource() {
			return imageSource;
		}

		public String getMessage() {
			return message;
		}

		public TextView getTextView() {
			return textView;
		}
	}

	class GetImageTask extends AsyncTask<String, Void, Drawable> {
		ImageDownloadData mImageDownloadData;

		public GetImageTask(ImageDownloadData data) {
			this.mImageDownloadData = data;
		}

		@Override
		protected Drawable doInBackground(String... params) {
			Drawable drawable;
			InputStream stream = MainActivity.api
					.getImageStream(mImageDownloadData.getImageSource());
			if (stream != null) {
				drawable = Drawable.createFromStream(stream, "src");
				// 如果图片为不能访问的外部图片，此时返回的drawable为null
				if (drawable == null) {
					drawable = getResources().getDrawable(
							R.drawable.content_picture);
				}
			} else {
				drawable = getResources().getDrawable(
						R.drawable.content_picture);
			}
			drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(),
					0 + drawable.getIntrinsicHeight());
			return drawable;
		}

		@Override
		protected void onPostExecute(Drawable drawable) {
			mDrawableCache.put(mImageDownloadData.getImageSource(),
					new SoftReference<Drawable>(drawable));
			mImageDownloadData.getTextView().setText(
					Html.fromHtml(mImageDownloadData.getMessage(),
							new ImageGetterSecond(), null));
		}
	}
}
