package com.tale.controller.admin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.core.Take;
import com.blade.kit.StringKit;
import com.blade.mvc.annotation.JSON;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PathParam;
import com.blade.mvc.annotation.QueryParam;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.HttpMethod;
import com.blade.mvc.http.Request;
import com.blade.mvc.multipart.FileItem;
import com.blade.mvc.ui.RestResponse;
import com.tale.constants.TaleConst;
import com.tale.controller.BaseController;
import com.tale.dto.Types;
import com.tale.exception.TipException;
import com.tale.ext.Commons;
import com.tale.model.Book;
import com.tale.model.Contents;
import com.tale.model.Metas;
import com.tale.model.Users;
import com.tale.service.BookService;
import com.tale.service.ContentsService;
import com.tale.service.MetasService;
import com.tale.service.SiteService;
import com.tale.utils.TaleUtils;

/**
 * 文章管理控制器 Created by biezhi on 2017/2/21.
 */
@Path("admin/article")
public class ArticleController extends BaseController {

	@Inject
	private ContentsService contentsService;
	@Inject
	private MetasService metasService;
	// @Inject
	// private LogService logService;
	@Inject
	private SiteService siteService;
	@Inject
	private BookService bookService;

	/**
	 * 文章管理首页
	 */
	@Route(values = "", method = HttpMethod.GET)
	public String index(Request request) {
		List<Metas> tags = metasService.getMetas(Types.TAG);
		request.attribute("tags", tags);
		return "admin/articleList";
	}

	@Route(values = "/selectArticleList", method = HttpMethod.POST)
	@JSON
	public List<Contents> articleList(@QueryParam(name = "page", defaultValue = "1") int page,
			@QueryParam(name = "limit", defaultValue = "10") int limit, @QueryParam String tags,
			@QueryParam String title, Request request) {
		Take take = new Take(Contents.class).eq("type", Types.ARTICLE).page(page, limit, "created desc");
		if (StringKit.isNotBlank(tags))
			take.in("tags", Arrays.asList(tags.split(",")));
		if (StringKit.isNotBlank(title))
			take.like("title", title);
		List<Contents> articleList = contentsService.getArticlesList(take);
		return articleList;
	}

	/**
	 * 文章发布页面
	 */
	@Route(values = "publish", method = HttpMethod.GET)
	public String newArticle(Request request) {
		List<Metas> tags = metasService.getMetas(Types.TAG);
		request.attribute("tags", tags);

		List<Book> books = bookService.selectBookList();
		request.attribute("books", books);
		request.attribute(Types.ATTACH_URL, Commons.site_option(Types.ATTACH_URL, Commons.site_url()));
		return "admin/articleEdit";
	}

	/**
	 * 文章编辑页面
	 */
	@Route(values = "/:cid", method = HttpMethod.GET)
	public String editArticle(@PathParam String cid, Request request) {
		Contents contents = contentsService.getContents(cid);
		request.attribute("contents", contents);

		List<Metas> tags = metasService.getMetas(Types.TAG);
		request.attribute("tags", tags);
		List<Book> books = bookService.selectBookList();
		request.attribute("books", books);

		request.attribute("active", "article");
		request.attribute(Types.ATTACH_URL, Commons.site_option(Types.ATTACH_URL, Commons.site_url()));
		return "admin/articleEdit";
	}

	/**
	 * 图片上传到七牛服务器
	 */
	@Route(values = "uploadToQiniu", method = HttpMethod.POST)
	@JSON
	public String uploadImg(Request request) {
		Map<String, FileItem> fileItemMap = request.fileItems();
		for (Map.Entry<String, FileItem> entry : fileItemMap.entrySet()) {
			FileItem f = entry.getValue();
			if (f.length() / 1024 <= TaleConst.MAX_FILE_SIZE) {
				return TaleUtils.uploadFile(f.fileName(), f.name());
			}
		}
		return "";
	}

	/**
	 * 发布文章操作
	 */
	@Route(values = "publish", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> publishArticle(@QueryParam Contents contents) {

		Users users = this.user();

		contents.setType(Types.ARTICLE);
		contents.setAuthor_id(users.getUid());

		try {
			Integer cid = contentsService.publish(contents);
			siteService.cleanCache(Types.C_STATISTICS);
			return RestResponse.ok(cid);
		} catch (Exception e) {
			String msg = "文章发布失败";
			if (e instanceof TipException) {
				msg = e.getMessage();
			} else {
				LOGGER.error(msg, e);
			}
			return RestResponse.fail(msg);
		}
	}

	/**
	 * 修改文章操作
	 */
	@Route(values = "modify", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> modifyArticle(@QueryParam Contents contents) {

		Users users = this.user();
		contents.setAuthor_id(users.getUid());
		try {
			contentsService.updateArticle(contents);
			return RestResponse.ok(contents.getCid());
		} catch (Exception e) {
			String msg = "文章编辑失败";
			if (e instanceof TipException) {
				msg = e.getMessage();
			} else {
				LOGGER.error(msg, e);
			}
			return RestResponse.fail(msg);
		}
	}

	/**
	 * 删除文章操作
	 */
	@Route(values = "delete")
	@JSON
	public RestResponse<?> delete(@QueryParam int cid, Request request) {
		try {
			contentsService.delete(cid);
			siteService.cleanCache(Types.C_STATISTICS);
			// logService.save(LogActions.DEL_ARTICLE, cid + "",
			// request.address(), this.getUid());
		} catch (Exception e) {
			String msg = "文章删除失败";
			if (e instanceof TipException) {
				msg = e.getMessage();
			} else {
				LOGGER.error(msg, e);
			}
			return RestResponse.fail(msg);
		}
		return RestResponse.ok();
	}

	@Route(values = "/statisticPieData", method = HttpMethod.POST)
	@JSON
	public Map<String, Object> statisticPieData(@QueryParam String month) {
		return contentsService.statisticPieData(month);
	}
}
