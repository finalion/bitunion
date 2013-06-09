package vleon.app.bitunion.api;

import java.io.InputStream;

import org.json.JSONObject;

import vleon.app.bitunion.MainActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BuProfile {
	public String uid, status, username, credit, regdate, lastvisit, bday,
			signature, postnum, threadnum, email, site, icq, oicq, yahoo, msn;
	public Bitmap avatar;

	public BuProfile(JSONObject obj) {
		uid = BuAPI.getString(obj, "uid");
		status = BuAPI.getString(obj, "status");
		username = BuAPI.getString(obj, "username");
		String avatarUri = BuAPI.getString(obj, "avatar");
		avatar = getAvatar(avatarUri);
		credit = BuAPI.getString(obj, "credit");
		regdate = BuAPI.getTimeStr(obj, "regdate", "yyyy-MM-dd HH:mm:ss");
		lastvisit = BuAPI.getTimeStr(obj, "lastvisit", "yyyy-MM-dd HH:mm:ss");
		bday = BuAPI.getString(obj, "bday");
		signature = BuAPI.getString(obj, "signature");
		postnum = BuAPI.getString(obj, "postnum");
		threadnum = BuAPI.getString(obj, "threadnum");

		email = BuAPI.getString(obj, "email");
		site = BuAPI.getString(obj, "site");
		icq = BuAPI.getString(obj, "icq");
		oicq = BuAPI.getString(obj, "oicq");
		yahoo = BuAPI.getString(obj, "yahoo");
		msn = BuAPI.getString(obj, "msn");
	}

	Bitmap getAvatar(String uri) {
		if (!uri.startsWith("http://"))
			uri = BuAPI.ROOTURL + "/" + uri;
		InputStream is =BuAPI.getImageStream(uri);
		if (is != null)
			avatar = BitmapFactory.decodeStream(is);
		else
			avatar = null;
		return avatar;
	}

}