package com.tale.service;

import com.blade.ioc.annotation.Bean;
import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.core.Take;
import com.blade.jdbc.model.Paginator;
import com.blade.kit.DateKit;
import com.tale.model.Attach;

/**
 * Created by biezhi on 2017/2/23.
 */
@Bean
public class AttachService {

	@Inject
	private ActiveRecord activeRecord;

	public Attach save(String fname, String fkey, String ftype, Integer author) {
		Attach attach = new Attach();
		attach.setFname(fname);
		attach.setAuthor_id(author);
		attach.setFkey(fkey);
		attach.setFtype(ftype);
		attach.setCreated(DateKit.nowUnix());
		activeRecord.insert(attach);
		return attach;
	}

	public Attach byId(Integer id) {
		if (null != id) {
			return activeRecord.byId(Attach.class, id);
		}
		return null;
	}

	public void delete(Integer id) {
		if (null != id) {
			activeRecord.delete(Attach.class, id);
		}
	}

	public Paginator<Attach> getAttachs(Take take) {
		if (null != take) {
			return activeRecord.page(take);
		}
		return null;
	}
}
