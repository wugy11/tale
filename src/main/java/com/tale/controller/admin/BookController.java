package com.tale.controller.admin;

import java.util.List;

import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.core.Take;
import com.blade.kit.StringKit;
import com.blade.mvc.annotation.Controller;
import com.blade.mvc.annotation.EntityObj;
import com.blade.mvc.annotation.JSON;
import com.blade.mvc.annotation.QueryParam;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.HttpMethod;
import com.blade.mvc.http.Request;
import com.blade.mvc.view.RestResponse;
import com.tale.controller.BaseController;
import com.tale.model.Book;
import com.tale.service.BookService;

/**
 * 书单管理
 * 
 * @author wugy 2017-5-6 10:47:39
 */
@Controller("admin/book")
public class BookController extends BaseController {

	@Inject
	private BookService bookService;

	/**
	 * 书单首页管理
	 */
	@Route(value = "")
	public String index() {
		return "admin/bookList";
	}

	@Route(value = "/saveBook", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> saveBook(@EntityObj Book book) {
		try {
			bookService.saveBook(book);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("保存书单失败:" + e);
			return RestResponse.fail("保存书单失败");
		}
		return RestResponse.ok();
	}
	
	@Route(value = "/deleteBook", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> deleteBook(@QueryParam String ids) {
		try {
			bookService.deleteBook(ids);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("删除书单失败:" + e);
			return RestResponse.fail("删除书单失败");
		}
		return RestResponse.ok();
	}

	@Route(value = "/selectBookList", method = HttpMethod.POST)
	@JSON
	public List<Book> selectBookList(@QueryParam(value = "page", defaultValue = "1") int page,
			@QueryParam(value = "limit", defaultValue = "10") int limit, @QueryParam(value = "name") String name,
			Request request) {
		Take take = new Take(Book.class).page(page, limit, "id desc");
		if (!StringKit.isEmpty(name))
			take.like("name", name);
		List<Book> bookList = bookService.selectBookList(take);
		return bookList;
	}
}
