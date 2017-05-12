package com.tale.model;

import java.io.Serializable;
import java.util.Date;

import com.blade.jdbc.annotation.Column;
import com.blade.jdbc.annotation.Table;
import com.blade.mvc.annotation.DateFormat;

@Table(name = "t_finance")
public class Finance implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String money;
	private String type;
	private Integer expense_time;
	private Integer create_time;

	@DateFormat
	@Column(ignore = true)
	private Date expenseTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getExpense_time() {
		return expense_time;
	}

	public void setExpense_time(Integer expense_time) {
		this.expense_time = expense_time;
	}

	public Integer getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Integer create_time) {
		this.create_time = create_time;
	}

	public Date getExpenseTime() {
		return expenseTime;
	}

	public void setExpenseTime(Date expenseTime) {
		this.expenseTime = expenseTime;
	}

}
