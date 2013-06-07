package vleon.app.bitunion.api;

public class Quote {
	public String quoteAuthor;
	public String quoteTime;
	public String quoteContent;
	public Quote(String author,String time, String content){
		quoteAuthor = author;
		quoteTime = time;
		quoteContent = content;
	}
}