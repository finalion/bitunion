package vleon.app.bitunion.fragment;

import vleon.app.bitunion.MainActivity;
import vleon.app.bitunion.R;
import vleon.app.bitunion.api.BuProfile;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ProfileFragment extends SherlockDialogFragment {
	String uid, username;

	public static ProfileFragment newInstance(String uid, String username) {
		ProfileFragment fragment = new ProfileFragment();
		Bundle bundle = new Bundle();
		bundle.putString("uid", uid);
		bundle.putString("username", username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		uid = getArguments().getString("uid");
		username = getArguments().getString("username");
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.profile_fragment, container,
				false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		new FetchProfileTask().execute();
	}

	void setProfileView(int resId, String str) {
		TextView view = (TextView) getView().findViewById(resId);
		view.setText(Html.fromHtml(str));
	}

	void setAvatarView(int resId, Bitmap bm) {
		QuickContactBadge badge = (QuickContactBadge) getView().findViewById(
				resId);
		if (bm != null)
			badge.setImageBitmap(bm);
	}

	class FetchProfileTask extends AsyncTask<Void, Void, BuProfile> {
		BuProfile profile;

		@Override
		protected BuProfile doInBackground(Void... params) {

			return MainActivity.api.getUserProfile(uid);
		}

		@Override
		protected void onPostExecute(BuProfile profile) {
			super.onPostExecute(profile);
			setProfileView(R.id.usernameView, username);
			setProfileView(R.id.uidView, uid);
			setProfileView(R.id.creditView, profile.credit);
			setProfileView(R.id.regdateView, profile.regdate);
			setProfileView(R.id.lastvisitView, profile.lastvisit);
			setProfileView(R.id.postcntView, profile.threadnum + "/"
					+ profile.postnum);
			setProfileView(R.id.bdayView, profile.bday);
			setProfileView(R.id.msnView, profile.msn);
			setProfileView(R.id.qqView, profile.oicq);
			setProfileView(R.id.emailView, profile.email);
			setProfileView(R.id.signView, profile.signature);
			setAvatarView(R.id.quickContactBadge1, profile.avatar);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
	}
}
