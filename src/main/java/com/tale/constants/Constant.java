package com.tale.constants;

import java.util.Arrays;
import java.util.List;

public enum Constant {

	resume("简历"), book("书单"), thinking("所想"), travel("游记"), network("网摘"),

	// 书单状态
	reading("在读"), readed("已读"), readable("想读"),

	// 财务类型
	salary("工资"), shopping("购物"), breakfast("早餐"), lunch("午餐"), dinner("晚餐"), traffic("交通"), donation("捐款"),

	//
	;

	private String desc;

	private Constant(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

	public static List<Constant> bookStatus() {
		return Arrays.asList(new Constant[] { reading, readed, readable });
	}

	public static List<Constant> financeTypes() {
		return Arrays.asList(new Constant[] { salary, shopping, breakfast, lunch, dinner, traffic, donation });
	}
}
