package com.tale.model;

import java.io.Serializable;
import java.util.Date;

import com.blade.jdbc.annotation.Column;
import com.blade.jdbc.annotation.Table;
import com.blade.mvc.annotation.DateFormat;

@Table(name = "t_books")
public class Book implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String name;
	private String status;
	private Integer begin_time;
	private Integer end_time;

	@DateFormat
	@Column(ignore = true)
	private Date beginTime;
	@DateFormat
	@Column(ignore = true)
	private Date endTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getBegin_time() {
		return begin_time;
	}

	public void setBegin_time(Integer begin_time) {
		this.begin_time = begin_time;
	}

	public Integer getEnd_time() {
		return end_time;
	}

	public void setEnd_time(Integer end_time) {
		this.end_time = end_time;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}
