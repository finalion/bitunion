package vleon.app.bitunion.fragment;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import vleon.app.bitunion.MainActivity;
import vleon.app.bitunion.MainAdapter;
import vleon.app.bitunion.R;
import vleon.app.bitunion.api.BuAPI;
import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuPost;
import vleon.app.bitunion.api.Quote;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;

public class PostFragment extends ContentFragment {
	HashMap<String, SoftReference<Drawable>> mDrawableCache;
	private ArrayList<BuPost> mPosts;
	TextView titleView;

	public static PostFragment newInstance(String tid, String subject) {
		PostFragment fragment = new PostFragment();
		Bundle args = new Bundle();
		args.putString("tid", tid);
		args.putString("subject", subject);
		args.putInt("tag", POST);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.post_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		titleView = (TextView) getSherlockActivity().findViewById(
				R.id.titleView);
		titleView.setText(getArguments().getString("subject"));
		mDrawableCache = new HashMap<String, SoftReference<Drawable>>();
		mPosts = new ArrayList<BuPost>();
		mAdapter = new PostsAdapter(getSherlockActivity(), mPosts);
		setListAdapter(mAdapter);
		fetchContents();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.menu_post).setIcon(R.drawable.social_reply);
		menu.findItem(R.id.menu_post).setTitle("回复");
	}

	@Override
	public void reply() {
		showReplyDialog("");
	}

	@Override
	public void replyOthers() {
		String replyQuote = "";
		BuPost tmpPost;
		// 构造引用文本
		for (Integer index : mAdapter.getSelected()) {
			tmpPost = (BuPost) mAdapter.getItem(Integer.valueOf(index));
			replyQuote += "[quote][b]" + tmpPost.author + "[/b] "
					+ tmpPost.lastedit + "\n" + tmpPost.content + "[/quote]\n";
		}
		showReplyDialog(replyQuote);
	}

	public void showReplyDialog(String content) {
		View view = LayoutInflater.from(getSherlockActivity()).inflate(
				R.layout.reply_dialog, null);
		final EditText contentText = (EditText) view
				.findViewById(R.id.replyText);
		contentText.setText(content);
		contentText.setSelection(content.length()); // 定位光标到文本框末尾
		new AlertDialog.Builder(getSherlockActivity()).setView(view)
				.setTitle(getArguments().getString("subject"))
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

	public class ReplyTask extends AsyncTask<String, Void, Result> {

		@Override
		protected Result doInBackground(String... arg0) {
			return MainActivity.api.replyThread(
					getArguments().getString("tid"), arg0[0]);
		}

		@Override
		protected void onPostExecute(Result result) {
			fetchContents();
		}
	}

	@Override
	public Result fetchContentTask() {
		String tid = PostFragment.this.getArguments().getString("tid");
		return MainActivity.api.getPosts(mPosts, tid + "", mCurrentPageCnt,
				mCurrentPageCnt + mPageStep);
	}

	class PostsAdapter extends MainAdapter {
		ArrayList<BuPost> mData;
		ViewHolder holder;

		public PostsAdapter(Context context, ArrayList<BuPost> data) {
			super(context);
			this.mData = data;
		}

		@Override
		public void clear() {
			mData.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public String getAuthor(int position) {
			return ((BuPost) getItem(position)).author;
		}

		@Override
		public String getAuthorID(int position) {
			return ((BuPost) getItem(position)).authorid;
		}

		class ViewHolder {
			TextView titleView, messageView, authorView, lasteditView,
					quotesView;
			ImageView attachmentView;
			RelativeLayout quoteLayout;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
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
			String quoteString = "";
			Quote tmpQuote;
			for (int i = 0; i < item.quotes.size(); i++) {
				tmpQuote = item.quotes.get(i);
				if (i > 0)
					quoteString += "<br/><br/>";
				quoteString += "&nbsp;&nbsp;&nbsp;" + tmpQuote.quoteAuthor
						+ ":&nbsp;" + tmpQuote.quoteContent;
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

			// 设置控件状态
			if (item.quotes.size() > 0) {
				holder.quotesView.setVisibility(View.VISIBLE);
				if (item.content.equals("")) {
					holder.messageView.setVisibility(View.GONE);
				} else {
					holder.messageView.setVisibility(View.VISIBLE);
				}
			} else {
				holder.quotesView.setVisibility(View.GONE);
				holder.messageView.setVisibility(View.VISIBLE);
			}
			if (mSelected
					&& mSelectedIndexs.contains(Integer.valueOf(position))) {
				convertView.setBackgroundColor(getResources().getColor(
						R.color.item_selected));
			} else {
				convertView.setBackgroundResource(R.drawable.even_item);
			}
			// mListView.invalidateViews();
			return convertView;
		}
	}

	/*
	 * Textview中异步显示图片
	 */
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
			InputStream stream = BuAPI.getImageStream(mImageDownloadData
					.getImageSource());
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
