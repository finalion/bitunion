package vleon.app.bitunion;

import java.util.ArrayList;

import vleon.app.bitunion.api.BuPost;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MainAdapter extends BaseAdapter {

	public Context mContext;
	public ArrayList<Integer> mSelectedIndexs;
	public boolean mSelected;
	public MainAdapter(Context context) {
		this.mContext = context;
		mSelectedIndexs = new ArrayList<Integer>();
	}
	public void addSelects(int position) {
		mSelectedIndexs.add(Integer.valueOf(position));
	}

	public void toggleSelected(int position) {
		Integer p = Integer.valueOf(position);
		if (mSelectedIndexs.contains(p)) {
			mSelectedIndexs.remove(p);
		} else {
			mSelectedIndexs.add(p);
		}
	}

	public int getSelectedCnt() {
		return mSelectedIndexs.size();
	}

	public void beginSelected() {
		mSelected = true;
		notifyDataSetChanged();
	}

	public void endSelected() {
		mSelected = false;
		mSelectedIndexs.clear();
		notifyDataSetChanged();
	}
	
	public ArrayList<Integer> getSelected(){
		return mSelectedIndexs;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
