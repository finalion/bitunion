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
		subject = BuAPI.getString(obj, "subject");
		tid = BuAPI.getString(obj, "tid");
		author = BuAPI.getString(obj, "author");
		authorid = BuAPI.getString(obj, "authorid");
		dateline = BuAPI.getString(obj, "dateline");
		lastpost = BuAPI.getTimeStr(obj, "lastpost", "yyyy-MM-dd HH:mm");
		lastposter = BuAPI.getString(obj, "lastposter");
		views = BuAPI.getString(obj, "views");
		replies = BuAPI.getString(obj, "replies");
	}
}