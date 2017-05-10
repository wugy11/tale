package com.tale.service;

import java.util.Arrays;
import java.util.List;

import com.blade.ioc.annotation.Inject;
import com.blade.ioc.annotation.Service;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.core.Take;
import com.blade.kit.DateKit;
import com.tale.constants.Constant;
import com.tale.model.Book;

@Service
public class BookService {

	@Inject
	private ActiveRecord activeRecord;

	public void saveBook(Book book) {
		Integer id = book.getId();
		if (null == id) {
			book.setBegin_time(DateKit.getUnixTimeByDate(book.getBeginTime()));
			activeRecord.insert(book);
		} else {
			if (Constant.readed.getDesc().equals(book.getStatus())) {
				book.setEnd_time(DateKit.getUnixTimeByDate(book.getEndTime()));
			}
			activeRecord.update(book);
		}
	}

	public void deleteBook(String ids) {
		Take take = new Take(Book.class).in("id", Arrays.asList(ids.split(",")));
		activeRecord.delete(take);
	}

	public List<Book> selectBookList() {
		Take take = Take.create(Book.class);
		return activeRecord.list(take);
	}

	public List<Book> selectBookList(Take take) {
		return activeRecord.list(take);
	}
}
