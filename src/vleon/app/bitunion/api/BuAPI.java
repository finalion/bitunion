package vleon.app.bitunion.api;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BuAPI {
	public static final int NETERROR = -1;
	public static final int SESSIONERROR = 0;
	public static final int NONE = 1;
	public static final int UNKNOWNERROR = -2;
	public static final int OUTNET = 1;
	public static final int BITNET = 0;
	// {"result":"fail","msg":"IP+logged"}
	String BASEURL;
	String REQUEST_LOGGING, REQUEST_FORUM, REQUEST_THREAD, REQUEST_POST,
			REQUEST_PROFILE, NEWPOST, NEWTHREAD;

	public enum Result {
		SUCCESS, // 返回数据成功，result字段为success
		FAILURE, // 返回数据失败，result字段为failure
		SUCCESS_EMPTY, // 返回数据成功，但字段没有数据
		SESSIONLOGIN, //
		NETWRONG, // 没有返回数据
		UNKNOWN;
		// 根据ordinal值获得枚举类型
		public static Result valueOf(int ordinal) {
			if (ordinal < 0 || ordinal >= values().length)
				return UNKNOWN;
			return values()[ordinal];
		}
	};

	String mUsername, mPassword;
	String mSession;
	boolean isLogined;

	HttpParams httpParams;
	DefaultHttpClient client;
	CookieStore cookieStore;
	Cookie mCookie;

	int flagCnt = 0;
	int mError = NONE;
	int mNetType;

	public BuAPI(String username, String password) {
		this(username, password, BITNET);
	}

	public BuAPI(String username, String password, int net) {
		this.mUsername = username;
		this.mPassword = password;
		this.isLogined = false;
		httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 2000);
		HttpConnectionParams.setSoTimeout(httpParams, 3000);
		client = new DefaultHttpClient(httpParams);
		switchToNet(net);
	}

	public BuAPI(String username, String password, String session) {
		this.mUsername = username;
		this.mPassword = password;
		this.mSession = session;
	}

	public void switchToNet(int net) {
		mNetType = net;
		if (net == BITNET) {
			BASEURL = "http://www.bitunion.org/open_api/";
		} else if (net == OUTNET) {
			BASEURL = "http://out.bitunion.org/open_api/";
		}
		REQUEST_LOGGING = BASEURL + "bu_logging.php";
		REQUEST_FORUM = BASEURL + "bu_forum.php";
		REQUEST_THREAD = BASEURL + "bu_thread.php";
		REQUEST_PROFILE = BASEURL + "bu_profile.php";
		REQUEST_POST = BASEURL + "bu_post.php";
		NEWPOST = BASEURL + "bu_newpost.php";
		NEWTHREAD = BASEURL + "bu_newpost.php";
	}

	public boolean available() {
		if (mSession == null)
			return false;
		return true;
	}

	public String getSession() {
		return this.mSession;
	}

	public Result refresh() {
		return login();
	}

	public Result login() {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("action", "login");
			jsonObj.put("username", this.mUsername);
			jsonObj.put("password", this.mPassword);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		JSONObject obj = post(REQUEST_LOGGING, jsonObj);
		if (obj == null) {
			isLogined = false;
			return Result.NETWRONG;
		}
		String result = getString(obj, "result");
		if (result.equals("success")) {
			isLogined = true;
			this.mSession = getString(obj, "session");
			this.cookieStore = client.getCookieStore();
			return Result.SUCCESS;
		} else {
			isLogined = false;
			return Result.FAILURE;
		}
	}

	public Result logout() {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("action", "logout");
			jsonObj.put("username", this.mUsername);
			jsonObj.put("password", this.mPassword);
			jsonObj.put("session", this.mSession);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		JSONObject obj = post(REQUEST_LOGGING, jsonObj);
		if (obj == null) {
			return Result.NETWRONG;
		}
		String result = getString(obj, "result");
		if (result.equals("success")) {
			isLogined = false; // 注销成功
			mSession = null;
			return Result.SUCCESS;
		}
		return Result.FAILURE;
	}

	/*
	 * 得到论坛列表
	 */
	public JSONObject getForumList() {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("action", "forum");
			jsonObj.put("username", this.mUsername);
			jsonObj.put("session", this.mSession);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		JSONObject obj = post(REQUEST_FORUM, jsonObj);
		return obj;
	}

	/*
	 * 查询用户信息
	 */
	public BuProfile getUserProfile(String username) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("action", "profile");
			jsonObj.put("username", username);
			jsonObj.put("session", this.mSession);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		JSONObject obj = post(REQUEST_PROFILE, jsonObj);
		if (obj == null) {
			return null;
		}
		String result = getString(obj, "result");
		if (result.equals("success")) {
			return new BuProfile(obj);
		}
		return null;
	}

	/*
	 * 得到指定论坛的帖子
	 */
	public Result getThreads(ArrayList<BuThread> threads, int fid, int from,
			int to) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("action", "thread");
			jsonObj.put("username", this.mUsername);
			jsonObj.put("session", this.mSession);
			jsonObj.put("fid", fid);
			jsonObj.put("from", from);
			jsonObj.put("to", to);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		JSONObject obj = post(REQUEST_THREAD, jsonObj);
		if (obj == null) {
			return Result.NETWRONG;
		}
		String result = getString(obj, "result");
		if (result.equals("success")) {
			JSONArray data = getData(obj, "threadlist");
			if (data == null)
				return Result.SUCCESS_EMPTY;
			for (int i = 0; i < data.length(); i++) {
				threads.add(new BuThread((JSONObject) data.opt(i)));
			}
			return Result.SUCCESS;
		}
		return Result.FAILURE;
	}

	/*
	 * 得到指定帖子的详细信息
	 */
	public Result getPosts(ArrayList<BuPost> posts, String id, int from, int to) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("action", "post");
			jsonObj.put("username", this.mUsername);
			jsonObj.put("session", this.mSession);
			jsonObj.put("tid", id);
			jsonObj.put("from", from + "");
			jsonObj.put("to", to + "");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		JSONObject obj = post(REQUEST_POST, jsonObj);
		if (obj == null) {
			return Result.NETWRONG;
		}
		String result = getString(obj, "result");
		if (result.equals("success")) {
			JSONArray data = getData(obj, "postlist");
			if (data == null)
				return Result.SUCCESS_EMPTY;
			for (int i = 0; i < data.length(); i++) {
				posts.add(new BuPost((JSONObject) data.opt(i)));
			}
			return Result.SUCCESS;
		}
		return Result.FAILURE;
	}

	/*
	 * 发布新帖
	 */
	public Result postThread(int fid, String subject, String message) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("action", "newthread");
			jsonObj.put("username", this.mUsername);
			jsonObj.put("session", this.mSession);
			jsonObj.put("fid", fid);
			jsonObj.put("subject", URLEncoder.encode(subject, "UTF-8"));
			jsonObj.put("message", URLEncoder.encode(message, "UTF-8"));
			jsonObj.put("attachment", 1);
		} catch (JSONException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return post2(NEWPOST, jsonObj);
	}

	/*
	 * 回复帖子
	 */
	public Result replyThread(String tid, String message) {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("action", "newreply");
			jsonObj.put("username", this.mUsername);
			jsonObj.put("session", this.mSession);
			jsonObj.put("tid", tid);
			jsonObj.put("message", URLEncoder.encode(message, "UTF-8"));
			jsonObj.put("attachment", 1);
		} catch (JSONException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return post2(NEWPOST, jsonObj);
	}

	/*
	 * post数据
	 */
	private JSONObject post(String url, JSONObject obj) {
		HttpPost httpPost = new HttpPost(url);
		String result = null;
		try {
			httpPost.setEntity(new StringEntity(obj.toString()));
			HttpResponse response = client.execute(httpPost);
			this.cookieStore = ((AbstractHttpClient) client).getCookieStore();
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity());

				return new JSONObject(result);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			result = e.getMessage().toString();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			result = e.getMessage().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			result = e.getMessage().toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private Result post2(String url, JSONObject obj) {
		HttpURLConnection conn = null;
		final String BOUNDARY = "----BitunionAndroidKit";
		String lineEnd = System.getProperty("line.separator");
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(120000);
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);
			conn.connect();
			DataOutputStream stream = new DataOutputStream(
					conn.getOutputStream());
			StringBuffer resSB = new StringBuffer();
			// resSB.append("--" + BOUNDARY + lineEnd);
			// resSB.append("Content-Disposition: form-data; name=\"content\""
			// + lineEnd);
			// resSB.append(lineEnd);
			// resSB.append("test Bitunion Android Version" + lineEnd);

			resSB.append("--" + BOUNDARY + lineEnd);
			resSB.append("Content-Disposition: form-data; name=\"json\""
					+ lineEnd);
			resSB.append(lineEnd);
			resSB.append(obj.toString() + lineEnd);

			// resSB.append("--" + BOUNDARY + lineEnd);
			// resSB.append("Content-Disposition: form-data; name=\"attach\""
			// + lineEnd);
			// resSB.append(lineEnd);
			// resSB.append(lineEnd);

			resSB.append("--" + BOUNDARY + "--" + lineEnd);
			stream.writeBytes(resSB.toString());
			stream.flush();

			int code = conn.getResponseCode();

			if (code == 200) {
				InputStream in = conn.getInputStream();
				int ch;
				StringBuilder sb2 = new StringBuilder();
				while ((ch = in.read()) != -1) {
					sb2.append((char) ch);
				}
				JSONObject tempobj = new JSONObject(sb2.toString());
				String result = getString(tempobj, "result");
				if (result.equals("success")) {
					return Result.SUCCESS;
				} else {
					return Result.FAILURE;
				}
			}
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return Result.NETWRONG;
	}

	private JSONArray getData(JSONObject jsonObject, String dataName) {
		try {
			return jsonObject.getJSONArray(dataName);
		} catch (JSONException e) {
			return null;
		}
	}

	public static String getString(JSONObject jsonObject, String name) {
		try {
			return URLDecoder.decode(jsonObject.getString(name));
		} catch (JSONException e) {
			return "";
		}
	}

	public static int getInt(JSONObject jsonObject, String name) {
		try {
			return jsonObject.getInt(name);
		} catch (JSONException e) {
			return 0;
		}
	}

	public static String getTimeStr(JSONObject jsonObject, String name,
			String format) {
		try {
			String t = URLDecoder.decode(jsonObject.getString(name));
			return formatTime(t, format);
		} catch (JSONException e) {
			return "";
		}

	}

	private static String formatTime(String t, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.CHINA);
		return dateFormat.format(new Date(Long.valueOf(t) * 1000L));
	}

	// 下载图片
	public InputStream getImageStream(String url) {
		if (mNetType == OUTNET) {
			url = url.replace("www.bitunion.org", "out.bitunion.org");
		}
		url = url.replace("//bitunion.org", "//www.bitunion.org");
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response;
		try {
			httpGet.setHeader("Referer", "http://www.bitunion.org");
			DefaultHttpClient client2 = new DefaultHttpClient();
			// client2.setCookieStore(this.cookieStore);
			response = client.execute(httpGet);
			client.setCookieStore(this.cookieStore);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity.getContentLength() > 0){
					InputStream stream = entity.getContent();
					int a=1;
					return stream;
				}
					
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Cookie getCookie() {
		return mCookie;
	}

	public void setCookie(Cookie mCookie) {
		this.mCookie = mCookie;
	}

	public int getError() {
		return mError;
	}

	public int getNetType() {
		return mNetType;
	}

	public void setNetType(int net) {
		mNetType = net;
	}
}
//
//
// class RequestResult {
// String result, msg;
//
// public RequestResult(JSONObject obj) {
// result = BuAPI.getString(obj, "result");
// msg = BuAPI.getString(obj, "msg");
// }
//
// public String getResult() {
// return result;
// }
//
// public String getMessage() {
// return msg;
// }
// }