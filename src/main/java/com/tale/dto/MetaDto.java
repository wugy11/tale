package com.tale.dto;

import com.tale.model.Metas;

/**
 * Created by biezhi on 2017/2/22.
 */
public class MetaDto extends Metas {

	private static final long serialVersionUID = 1L;
	private int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
