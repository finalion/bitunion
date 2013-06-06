package vleon.app.bitunion.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import vleon.app.bitunion.Constants;

public class BuPost {

	String pid, fid, tid, aid, icon;
	public String content;
	public String quoteAuthor;
	public String quoteTime;
	public String quoteContent;
	String all;
	public String author;
	String authorid;
	String subject;
	String dataline;
	String usesig;
	String uid;
	String username;
	String avatar;
	String attachmentImg;
	public String lastedit;

	public BuPost(JSONObject obj) {
		String message = BitunionAPI.getString(obj, "message");
		parseMessage(message);
		// "attachimg":1,"attachment":"attachments%2Fforumid_14%2FX%2Fo%2FXoEp_MjAxMjA3MDk%3D.jpg"
		if (BitunionAPI.getInt(obj, "attachimg") == 1) {
			attachmentImg = Constants.ROOT_URL
					+ BitunionAPI.getString(obj, "attachment");
			content = content + "<br/><img src='" + attachmentImg + "'>";
		}
		aid = BitunionAPI.getString(obj, "aid");
		subject = BitunionAPI.getString(obj, "subject");
		author = BitunionAPI.getString(obj, "author");
		lastedit = BitunionAPI.getTimeStr(obj, "lastedit", "yyyy-MM-dd HH:mm");
	}

	public void parseMessage(String message) {
		message = parseEmbbedImages2(message);
		getQuotes(message);
		if (quoteAuthor != null) {
			content = message.replace(all, "");
			content = parseParagraph(content);
		} else {
			content = parseParagraph(message);
		}

	}

	// 去掉嵌在文本中的图片和表情
	public String parseEmbbedImages(String content) {
		Pattern pattern = Pattern.compile("<img src=\"([^>]+)\"[^>]*>");
		// <img src="../images/smilies/25.gif"
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			String src = matcher.group(0);
			content = content.replace(src, "...");
		}
		return content;
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

	// 得到帖子的引用部分
	public void getQuotes(String content) {
		/*
		 * Pattern p = Pattern.compile(
		 * "<table border=\"0\" width=\"100%\" cellspacing=\"1\" cellpadding=\"10\" bgcolor=\"BORDERCOLOR\"><tr><td width=\"100%\" bgcolor=\"ALTBG2\"><b>(.*)</b> (\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2})<br />(.*)</td></tr></table></td></tr></table></center>"
		 * , Pattern.DOTALL);
		 */
		Pattern p = Pattern
				.compile(
						"<br><br><center><table border=\"0\" width=\"90%\".*bgcolor=\"ALTBG2\"><b>(.*)</b> (\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2})<br />(.*)</td></tr></table></td></tr></table></center><br>",
						Pattern.DOTALL);
		Matcher m = p.matcher(content);
		while (m.find()) {
			all = m.group(0); //
			quoteAuthor = m.group(1);
			quoteTime = m.group(2);
			quoteContent = parseParagraph(m.group(3));
			break;
		}
	}

}