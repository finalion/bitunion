package vleon.app.bitunion.fragment;

import java.util.ArrayList;

import vleon.app.bitunion.R;
import vleon.app.bitunion.api.BuForum;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class MenuFragment extends SherlockListFragment {

	ArrayList<BuForum> mForumList = null;
	OnForumSelectedListener mListener;
	MenuAdapter mAdapter;
	int mCurrentPos = 0;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.left_menu, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			mCurrentPos = savedInstanceState.getInt("pos", 0);
		}
		mForumList = new ArrayList<BuForum>();
		String[] forumNames = getSherlockActivity().getResources().getStringArray(R.array.forums);
		int[] forumFids =  getSherlockActivity().getResources().getIntArray(R.array.fids);
		int[] forumTypes =  getActivity().getResources().getIntArray(R.array.types);
		for (int i = 0; i < forumFids.length; i++) {
			mForumList.add(new BuForum(forumNames[i], forumFids[i],
					forumTypes[i]));
		}
		
		mAdapter = new MenuAdapter(getSherlockActivity());
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
		setListAdapter(mAdapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mListener = (OnForumSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFormSelectedListener interface");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putInt("pos", mCurrentPos);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Object obj = mAdapter.getItem(position);
		if (obj instanceof BuForum) {
			BuForum forum = (BuForum) obj;
			mListener.onForumSelected(forum.getFid(), forum.getName());
			mCurrentPos = position;
		}
	}

	public interface OnForumSelectedListener {
		public void onForumSelected(int fid, String name);
	}

	public class MenuAdapter extends ArrayAdapter<Object> {

		Context mContext;

		public MenuAdapter(Context context) {
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
