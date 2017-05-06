package com.tale.constants;

public enum SocialTypes {

	github("github", "https://github.com/"),
	weibo("weibo", "http://weibo.com/"),
	oschina("oschina", "http://git.oschina.net/"),
	zhihu("zhihu", "https://www.zhihu.com/people/"),
	;

	private String type;
	private String link;

	private SocialTypes(String type, String link) {
		this.type = type;
		this.link = link;
	}

	public String getType() {
		return type;
	}

	public String getLink() {
		return link;
	}

	public static String getLink(String type) {
		for (SocialTypes socialType : SocialTypes.values()) {
			if (socialType.getType().equalsIgnoreCase(type))
				return socialType.getLink();
		}
		return "";
	}

}
