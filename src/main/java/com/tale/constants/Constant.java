package com.tale.constants;

public enum Constant {

	resume("简历"), book("书单"), thinking("所想"), travel("游记"), network("网摘"),

	// 书单状态
	reading("在读"), readed("已读"), readable("想读"),;

	private String desc;

	private Constant(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}
