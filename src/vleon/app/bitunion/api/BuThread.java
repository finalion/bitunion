package vleon.app.bitunion.api;

import org.json.JSONObject;

public class BuThread {

	public String tid;
	public String author;
	String authorid;
	public String subject;
	String dateline;
	public String lastpost;
	String lastposter;
	public String views;
	public String replies;
	public boolean topFlag;

	public BuThread(JSONObject obj) {
		topFlag = false;
		subject = BitunionAPI.getString(obj, "subject");
		tid = BitunionAPI.getString(obj, "tid");
		author = BitunionAPI.getString(obj, "author");
		dateline = BitunionAPI.getString(obj, "dateline");
		lastpost = BitunionAPI.getTimeStr(obj, "lastpost", "yyyy-MM-dd HH:mm");
		lastposter = BitunionAPI.getString(obj, "lastposter");
		views = BitunionAPI.getString(obj, "views");
		replies = BitunionAPI.getString(obj, "replies");
	}
}