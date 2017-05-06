package com.tale.model;

import java.io.Serializable;

import com.blade.jdbc.annotation.Table;

@Table(name = "t_books")
public class Book implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String name;
	private String status;
	private Integer begin_time;
	private Integer end_time;

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

}
