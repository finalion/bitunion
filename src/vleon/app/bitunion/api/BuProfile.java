package vleon.app.bitunion.api;
import org.json.JSONException;
import org.json.JSONObject;
public class BuProfile {
	String uid, status, username, avatar, credit, regdate, lastvisit, bday, signature,
	      postnum, threadnum, email, site, icq, oicq, yahoo, msn;
	public BuProfile(JSONObject obj){
		try {
			obj = obj.getJSONObject("meminfo");
			uid = BuAPI.getString(obj, "uid");
			status = BuAPI.getString(obj, "status");
			username = BuAPI.getString(obj, "username");
			avatar = BuAPI.getString(obj, "avatar");
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
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}