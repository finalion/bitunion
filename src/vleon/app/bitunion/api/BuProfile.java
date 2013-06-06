package vleon.app.bitunion.api;
import org.json.JSONException;
import org.json.JSONObject;
public class BuProfile {
	String uid, status, username, avatar, credit, regdate, lastvisit, bday, signature,
	      postnum, threadnum, email, site, icq, oicq, yahoo, msn;
	public BuProfile(JSONObject obj){
		try {
			obj = obj.getJSONObject("meminfo");
			uid = BitunionAPI.getString(obj, "uid");
			status = BitunionAPI.getString(obj, "status");
			username = BitunionAPI.getString(obj, "username");
			avatar = BitunionAPI.getString(obj, "avatar");
			credit = BitunionAPI.getString(obj, "credit");
			regdate = BitunionAPI.getTimeStr(obj, "regdate", "yyyy-MM-dd HH:mm:ss");
			lastvisit = BitunionAPI.getTimeStr(obj, "lastvisit", "yyyy-MM-dd HH:mm:ss");
			bday = BitunionAPI.getString(obj, "bday");
			signature = BitunionAPI.getString(obj, "signature");
			postnum = BitunionAPI.getString(obj, "postnum");
			threadnum = BitunionAPI.getString(obj, "threadnum");
			email = BitunionAPI.getString(obj, "email");
			site = BitunionAPI.getString(obj, "site");
			icq = BitunionAPI.getString(obj, "icq");
			oicq = BitunionAPI.getString(obj, "oicq");
			yahoo = BitunionAPI.getString(obj, "yahoo");
			msn = BitunionAPI.getString(obj, "msn");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}