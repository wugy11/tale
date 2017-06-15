package com.tale.constants;

import java.util.Arrays;
import java.util.List;

public enum Constant {

    // 首页特殊链接
    resume("简历"), book("书单"), thinking("思绪"), travel("游记"), network("网摘"), summary("总结"),

    // 书单状态
    reading("在读"), readed("已读"), readable("想读"),

    // 财务类型
    salary("工资"), shopping("购物"), breakfast("早餐"), lunch("午餐"), dinner("晚餐"), traffic("交通"), donation("捐款"), inverst(
            "投资出账"), earnings("投资入账"), other("其它"),
    // 资金变动分类
    income("收入"), expense("支出"),

    // 评论状态
    unread("未读"), /* readed("已读"), */ replyed("已回复"),

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
        return Arrays.asList(reading, readed, readable);
    }

    public static List<Constant> financeTypes() {
        return Arrays.asList(salary, shopping, breakfast, lunch, dinner, traffic, donation, inverst,
                earnings, other);
    }

    public static String getFinanceCategory(String financeTypeDesc) {
        if (salary.getDesc().equals(financeTypeDesc) || earnings.getDesc().equals(financeTypeDesc))
            return income.getDesc();
        return expense.getDesc();
    }
}
