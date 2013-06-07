package vleon.app.bitunion.api;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import vleon.app.bitunion.Constants;

public class BuPost {

	public String pid, fid, tid, aid, icon;
	public String content;
	public ArrayList<Quote> quotes;
//	public String all;
	public String author;
	public String authorid;
	public String subject;
	public String dataline;
	public String usesig;
	public String uid;
	public String username;
	public String avatar;
	public String attachmentImg;
	public String lastedit;

	public BuPost(JSONObject obj) {
		String message = BuAPI.getString(obj, "message");
		quotes = new ArrayList<Quote>();
		parseMessage(message);
		// "attachimg":1,"attachment":"attachments%2Fforumid_14%2FX%2Fo%2FXoEp_MjAxMjA3MDk%3D.jpg"
		if (BuAPI.getInt(obj, "attachimg") == 1) {
			attachmentImg = Constants.ROOT_URL
					+ BuAPI.getString(obj, "attachment");
			content = content + "<br/><img src='" + attachmentImg + "'>";
		}
		aid = BuAPI.getString(obj, "aid");
		pid = BuAPI.getString(obj, "pid");
		subject = BuAPI.getString(obj, "subject");
		author = BuAPI.getString(obj, "author");
		lastedit = BuAPI.getTimeStr(obj, "lastedit", "yyyy-MM-dd HH:mm");
	}

	public void parseMessage(String message) {
		message = parseEmbbedImages2(message);
		message = parseQuotes(message);
		this.content = parseParagraph(message);
	}

	// 去掉嵌在文本中的图片和表情
	public String parseEmbbedImages(String message) {
		Pattern pattern = Pattern.compile("<img src=\"([^>]+)\"[^>]*>");
		// <img src="../images/smilies/25.gif"
		Matcher matcher = pattern.matcher(message);
		while (matcher.find()) {
			String src = matcher.group(0);
			message = message.replace(src, "...");
		}
		return message;
	}

	// 将嵌在文本中的图片和表情转为绝对路径
	public String parseEmbbedImages2(String content) {
		// Pattern pattern = Pattern.compile("<img src=\"(\\.\\./[^\"]*)\" ");
		// // <img src="../images/smilies/25.gif"
		// Matcher matcher = pattern.matcher(content);
		// while (matcher.find()) {
		// String src = matcher.group(1);
		// content = content.replace("../", Constants.ROOT_URL);
		// }
		content = content.replace("../", Constants.ROOT_URL);
		return content;
	}

	// 去除段前的换行符
	public String parseParagraph(String content) {
		content = content.trim();
		while (content.startsWith("<br>")) {
			content = content.substring(4).trim();
		}
		while (content.startsWith("<br />")) {
			content = content.substring(6).trim();
		}
		while (content.endsWith("<br />")) {
			content = content.substring(0, content.length() - 6).trim();
		}
		return content;
	}

	// 解析帖子的引用部分
	public String parseQuotes(String message) {
		quotes.clear();
		Pattern p = Pattern
				.compile(
						"<br><br><center><table border=\"0\" width=\"90%\".*?bgcolor=\"ALTBG2\"><b>(.*?)</b> (\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2})<br />(.*?)</td></tr></table></td></tr></table></center><br>",
						Pattern.DOTALL);
		Matcher m = p.matcher(message);
		while (m.find()) {
			// 1: author; 2:time; 3:content
			quotes.add(new Quote(m.group(1),m.group(2),parseParagraph(m.group(3))));
			message = message.replace(m.group(0), "");
		}
		return message;
	}

}
