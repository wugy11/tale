package com.tale.controller.admin;

import java.util.List;
import java.util.Map;

import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.core.Take;
import com.blade.jdbc.model.Paginator;
import com.blade.kit.StringKit;
import com.blade.mvc.annotation.Controller;
import com.blade.mvc.annotation.EntityObj;
import com.blade.mvc.annotation.JSON;
import com.blade.mvc.annotation.PathParam;
import com.blade.mvc.annotation.QueryParam;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.HttpMethod;
import com.blade.mvc.http.Request;
import com.blade.mvc.multipart.FileItem;
import com.blade.mvc.view.RestResponse;
import com.tale.controller.BaseController;
import com.tale.dto.LogActions;
import com.tale.dto.Types;
import com.tale.exception.TipException;
import com.tale.ext.Commons;
import com.tale.init.TaleConst;
import com.tale.model.Contents;
import com.tale.model.Metas;
import com.tale.model.Users;
import com.tale.service.ContentsService;
import com.tale.service.LogService;
import com.tale.service.MetasService;
import com.tale.service.SiteService;
import com.tale.utils.QiniuUtils;

/**
 * 文章管理控制器 Created by biezhi on 2017/2/21.
 */
@Controller("admin/article")
public class ArticleController extends BaseController {

	@Inject
	private ContentsService contentsService;

	@Inject
	private MetasService metasService;

	@Inject
	private LogService logService;

	@Inject
	private SiteService siteService;

	/**
	 * 文章管理首页
	 */
	@Route(value = "", method = HttpMethod.GET)
	public String index(@QueryParam(value = "page", defaultValue = "1") int page,
			@QueryParam(value = "limit", defaultValue = "15") int limit, Request request) {

		Paginator<Contents> contentsPaginator = contentsService
				.getArticles(new Take(Contents.class).eq("type", Types.ARTICLE).page(page, limit, "created desc"));
		request.attribute("articles", contentsPaginator);
		return "admin/article_list";
	}

	/**
	 * 文章发布页面
	 */
	@Route(value = "publish", method = HttpMethod.GET)
	public String newArticle(Request request) {
		List<Metas> categories = metasService.getMetas(Types.CATEGORY);
		request.attribute("categories", categories);
		request.attribute(Types.ATTACH_URL, Commons.site_option(Types.ATTACH_URL, Commons.site_url()));
		return "admin/article_edit";
	}

	/**
	 * 文章编辑页面
	 */
	@Route(value = "/:cid", method = HttpMethod.GET)
	public String editArticle(@PathParam String cid, Request request) {
		Contents contents = contentsService.getContents(cid);
		request.attribute("contents", contents);
		List<Metas> categories = metasService.getMetas(Types.CATEGORY);
		request.attribute("categories", categories);
		request.attribute("active", "article");
		request.attribute(Types.ATTACH_URL, Commons.site_option(Types.ATTACH_URL, Commons.site_url()));
		return "admin/article_edit";
	}

	/**
	 * 图片上传到七牛服务器
	 */
	@Route(value = "uploadToQiniu", method = HttpMethod.POST)
	@JSON
	public String uploadImg(Request request) {
		Map<String, FileItem> fileItemMap = request.fileItems();
		for (Map.Entry<String, FileItem> entry : fileItemMap.entrySet()) {
			FileItem f = entry.getValue();
			String fname = f.fileName();
			if (f.file().length() / 1024 <= TaleConst.MAX_FILE_SIZE) {
				return QiniuUtils.uploadFile(f.file(), fname);
			}
		}
		return "";
	}

	/**
	 * 发布文章操作
	 */
	@Route(value = "publish", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> publishArticle(@EntityObj Contents contents) {

		Users users = this.user();

		contents.setType(Types.ARTICLE);
		contents.setAuthor_id(users.getUid());
		String categories = contents.getCategories();
		if (StringKit.isBlank(categories)) {
			categories = "默认分类";
		}
		contents.setCategories(categories);

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
	@Route(value = "modify", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> modifyArticle(@EntityObj Contents contents) {

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
	 *
	 * @param cid
	 * @param request
	 * @return
	 */
	@Route(value = "delete")
	@JSON
	public RestResponse<?> delete(@QueryParam int cid, Request request) {
		try {
			contentsService.delete(cid);
			siteService.cleanCache(Types.C_STATISTICS);
			logService.save(LogActions.DEL_ARTICLE, cid + "", request.address(), this.getUid());
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
}
