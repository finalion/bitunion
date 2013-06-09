package vleon.app.bitunion.api;

public class BuForum extends BuContent{
	private String name;
	private int fid;
	private int type;

	public BuForum(String name, int fid, int type) {
		this.name = name;
		this.fid = fid;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public int getFid() {
		return fid;
	}

	public int getType() {
		return type;
	}
}