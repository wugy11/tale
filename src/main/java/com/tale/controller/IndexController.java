package com.tale.controller;

import java.net.URLEncoder;
import java.util.List;

import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.core.Take;
import com.blade.jdbc.model.Paginator;
import com.blade.kit.IPKit;
import com.blade.kit.PatternKit;
import com.blade.kit.StringKit;
import com.blade.mvc.annotation.Controller;
import com.blade.mvc.annotation.JSON;
import com.blade.mvc.annotation.PathParam;
import com.blade.mvc.annotation.QueryParam;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.HttpMethod;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;
import com.blade.mvc.http.wrapper.Session;
import com.blade.mvc.view.RestResponse;
import com.tale.dto.Archive;
import com.tale.dto.ErrorCode;
import com.tale.dto.MetaDto;
import com.tale.dto.Types;
import com.tale.exception.TipException;
import com.tale.ext.Commons;
import com.tale.init.TaleConst;
import com.tale.model.Comments;
import com.tale.model.Contents;
import com.tale.model.Metas;
import com.tale.service.CommentsService;
import com.tale.service.ContentsService;
import com.tale.service.MetasService;
import com.tale.service.SiteService;
import com.tale.utils.TaleUtils;
import com.vdurmont.emoji.EmojiParser;

@Controller
public class IndexController extends BaseController {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(IndexController.class);

	@Inject
	private ContentsService contentsService;

	@Inject
	private MetasService metasService;

	@Inject
	private CommentsService commentsService;

	@Inject
	private SiteService siteService;

	/**
	 * 首页
	 */
	@Route(value = "/", method = HttpMethod.GET)
	public String index(Request request, @QueryParam(value = "limit", defaultValue = "12") int limit) {
		return this.index(request, 1, limit);
	}

	/**
	 * 自定义页面
	 */
	@Route(values = { "/:cid", "/:cid.html" }, method = HttpMethod.GET)
	public String page(@PathParam String cid, Request request) {
		Contents contents = contentsService.getContents(cid);
		if (null == contents) {
			return this.render_404();
		}
		if (contents.getAllow_comment()) {
			int cp = request.queryInt("cp", 1);
			request.attribute("cp", cp);
		}
		request.attribute("article", contents);
		updateArticleHit(contents.getCid(), contents.getHits());
		if (Types.ARTICLE.equals(contents.getType())) {
			return this.render("post");
		}
		if (Types.PAGE.equals(contents.getType())) {
			return this.render("page");
		}
		return this.render_404();
	}

	/**
	 * 首页分页
	 */
	@Route(values = { "page/:page", "page/:page.html" }, method = HttpMethod.GET)
	public String index(Request request, @PathParam int page,
			@QueryParam(value = "limit", defaultValue = "12") int limit) {
		page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;
		// allow_feed代表隐藏/显示(这里主要用于首页是否展示文章)：1表示显示，0表示隐藏，隐藏的文章只能作者本人看到
		// PageRow pageRow = new PageRow(page, limit, "created desc");
		// StringBuilder sql = new StringBuilder("select * from t_contents where
		// type = ? and status = ?");
		// Users loginUser = TaleUtils.getLoginUser();
		// if (null == loginUser) {
		// sql.append(" and allow_feed = 1");
		// sql = "select * from t_contents where type = ? and status = ? and
		// allow_feed = 1";
		// } else {
		// sql = "select * from t_contents where type = ? and status = ? and
		// (allow_feed = 0 and author_id="
		// + loginUser.getUid() + " or allow_feed = 1)";
		// }
		// Object[] args = new Object[] { "post", "publish" };
		Take take = new Take(Contents.class).eq("type", Types.ARTICLE).eq("status", Types.PUBLISH).page(page, limit,
				"created desc");
		Paginator<Contents> articles = contentsService.getArticles(take);
		List<Contents> viewableList = TaleUtils.getViewableList(articles.getList());
		articles.setList(viewableList);
		articles.setTotal(viewableList.size());
		request.attribute("articles", articles);
		if (page > 1) {
			this.title(request, "第" + page + "页");
		}
		request.attribute("is_home", true);
		request.attribute("page_prefix", "/page");
		return this.render("index");
	}

	/**
	 * 文章页
	 */
	@Route(values = { "article/:cid", "article/:cid.html" }, method = HttpMethod.GET)
	public String post(Request request, @PathParam String cid) {
		Contents contents = contentsService.getContents(cid);
		if (null == contents || Types.DRAFT.equals(contents.getStatus())) {
			return this.render_404();
		}
		request.attribute("article", contents);
		request.attribute("is_post", true);
		if (contents.getAllow_comment()) {
			int cp = request.queryInt("cp", 1);
			request.attribute("cp", cp);
		}
		updateArticleHit(contents.getCid(), contents.getHits());
		return this.render("post");
	}

	private void updateArticleHit(Integer cid, Integer chits) {
		Integer hits = cache.hget(Types.C_ARTICLE_HITS, cid.toString());
		hits = null == hits ? 1 : hits + 1;
		if (hits >= TaleConst.HIT_EXCEED) {
			Contents temp = new Contents();
			temp.setCid(cid);
			temp.setHits(chits + hits);
			contentsService.update(temp);
			cache.hset(Types.C_ARTICLE_HITS, cid.toString(), 1);
		} else {
			cache.hset(Types.C_ARTICLE_HITS, cid.toString(), hits);
		}
	}

	/**
	 * 分类页
	 */
	@Route(values = { "category/:keyword", "category/:keyword.html" }, method = HttpMethod.GET)
	public String categories(Request request, @PathParam String keyword,
			@QueryParam(value = "limit", defaultValue = "12") int limit) {
		return this.categories(request, keyword, 1, limit);
	}

	@Route(values = { "category/:keyword/:page", "category/:keyword/:page.html" }, method = HttpMethod.GET)
	public String categories(Request request, @PathParam String keyword, @PathParam int page,
			@QueryParam(value = "limit", defaultValue = "12") int limit) {
		return renderByType(Types.CATEGORY, request, keyword, page, limit);
	}

	/**
	 * 标签页
	 */
	@Route(values = { "tag/:name", "tag/:name.html" }, method = HttpMethod.GET)
	public String tags(Request request, @PathParam String name,
			@QueryParam(value = "limit", defaultValue = "12") int limit) {
		return this.tags(request, name, 1, limit);
	}

	/**
	 * 标签分页
	 */
	@Route(values = { "tag/:name/:page", "tag/:name/:page.html" }, method = HttpMethod.GET)
	public String tags(Request request, @PathParam String name, @PathParam int page,
			@QueryParam(value = "limit", defaultValue = "12") int limit) {
		return renderByType(Types.TAG, request, name, page, limit);
	}

	private String renderByType(String type, Request request, String name, int page, int limit) {
		page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;
		MetaDto metaDto = metasService.getMeta(type, name);
		if (null == metaDto) {
			return this.render_404();
		}

		Paginator<Contents> contentsPaginator = contentsService.getArticles(metaDto.getMid(), page, limit);
		request.attribute("articles", contentsPaginator);
		request.attribute("meta", metaDto);
		request.attribute("type", Types.TAG.equalsIgnoreCase(type) ? "标签" : "分类");
		request.attribute("keyword", name);
		request.attribute("name", name + "(" + contentsPaginator.getList().size() + ")");
		// request.attribute("is_tag", true);
		request.attribute("page_prefix", "/" + type + "/" + name);
		return this.render("page-category");
	}

	/**
	 * 搜索页
	 *
	 * @param keyword
	 * @return
	 */
	@Route(values = { "search/:keyword", "search/:keyword.html" }, method = HttpMethod.GET)
	public String search(Request request, @PathParam String keyword,
			@QueryParam(value = "limit", defaultValue = "12") int limit) {
		return this.search(request, keyword, 1, limit);
	}

	@Route(values = { "search", "search.html" })
	public String search(Request request, @QueryParam(value = "limit", defaultValue = "12") int limit) {
		String keyword = request.query("s");
		return this.search(request, keyword, 1, limit);
	}

	@Route(values = { "search/:keyword/:page", "search/:keyword/:page.html" }, method = HttpMethod.GET)
	public String search(Request request, @PathParam String keyword, @PathParam int page,
			@QueryParam(value = "limit", defaultValue = "12") int limit) {

		page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;
		Take take = new Take(Contents.class).eq("type", Types.ARTICLE).eq("status", Types.PUBLISH)
				.like("title", "%" + keyword + "%").page(page, limit, "created desc");

		Paginator<Contents> articles = contentsService.getArticles(take);
		request.attribute("articles", articles);

		request.attribute("type", "搜索");
		request.attribute("keyword", keyword);
		request.attribute("page_prefix", "/search/" + keyword);
		return this.render("page-category");
	}

	/**
	 * 归档页
	 */
	@Route(values = { "archives", "archives.html" }, method = HttpMethod.GET)
	public String archives(Request request) {
		List<Archive> archives = siteService.getArchives();
		request.attribute("archives", archives);
		request.attribute("is_archive", true);
		return this.render("archives");
	}

	/**
	 * 总分类页
	 */
	@Route(values = { "categories", "categories.html" }, method = HttpMethod.GET)
	public String categories(Request request) throws Exception {
		List<MetaDto> categories = getMetas(Types.CATEGORY);
		request.attribute("categories_tags", categories);
		request.attribute("type", "文章分类");
		return this.render("categories-tags");
	}

	/**
	 * 总标签页
	 */
	@Route(values = { "tags", "tags.html" }, method = HttpMethod.GET)
	public String tags(Request request) throws Exception {
		List<MetaDto> tags = getMetas(Types.TAG);
		request.attribute("categories_tags", tags);
		request.attribute("type", "标签");
		return this.render("categories-tags");
	}

	private List<MetaDto> getMetas(String type) throws Exception {
		List<MetaDto> metas = siteService.getMetas(Types.RECENT_META, type, TaleConst.MAX_POSTS);
		for (MetaDto metaDto : metas) {
			int count = metaDto.getCount();
			if (count > 0) {
				String name = metaDto.getName();
				StringBuilder label = new StringBuilder().append("<a href=\"/" + type + "/")
						.append(URLEncoder.encode(name, "UTF-8")).append("\">").append(name).append("(").append(count)
						.append(")</a>");
				metaDto.setName(label.toString());
			}
		}
		return metas;
	}

	/**
	 * 友链页
	 */
	@Route(values = { "links", "links.html" }, method = HttpMethod.GET)
	public String links(Request request) {
		List<Metas> links = metasService.getMetas(Types.LINK);
		request.attribute("links", links);
		return this.render("links");
	}

	/**
	 * feed页
	 */
	@Route(values = { "feed", "feed.xml" }, method = HttpMethod.GET)
	public void feed(Response response) {
		Paginator<Contents> contentsPaginator = contentsService
				.getArticles(new Take(Contents.class).eq("type", Types.ARTICLE).eq("status", Types.PUBLISH)
						.eq("allow_feed", true).page(1, TaleConst.MAX_POSTS, "created desc"));
		try {
			String xml = TaleUtils.getRssXml(contentsPaginator.getList());
			response.xml(xml);
		} catch (Exception e) {
			LOGGER.error("生成RSS失败", e);
		}
	}

	/**
	 * 注销
	 */
	@Route("logout")
	public void logout(Session session, Response response) {
		TaleUtils.logout(session, response);
	}

	/**
	 * 评论操作
	 */
	@Route(value = "comment", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> comment(Request request, Response response, @QueryParam Integer cid,
			@QueryParam Integer coid, @QueryParam String author, @QueryParam String mail, @QueryParam String url,
			@QueryParam String text, @QueryParam String _csrf_token) {

		String ref = request.header("Referer");
		if (StringKit.isBlank(ref) || StringKit.isBlank(_csrf_token)) {
			return RestResponse.fail(ErrorCode.BAD_REQUEST);
		}

		if (!ref.startsWith(Commons.site_url())) {
			return RestResponse.fail("非法评论来源");
		}

		String token = cache.hget(Types.CSRF_TOKEN, _csrf_token);
		if (StringKit.isBlank(token)) {
			return RestResponse.fail(ErrorCode.BAD_REQUEST);
		}

		if (null == cid || StringKit.isBlank(author) || StringKit.isBlank(mail) || StringKit.isBlank(text)) {
			return RestResponse.fail("请输入完整后评论");
		}

		if (author.length() > 50) {
			return RestResponse.fail("姓名过长");
		}

		if (!TaleUtils.isEmail(mail)) {
			return RestResponse.fail("请输入正确的邮箱格式");
		}

		if (StringKit.isNotBlank(url) && !PatternKit.isURL(url)) {
			return RestResponse.fail("请输入正确的URL格式");
		}

		if (text.length() > 200) {
			return RestResponse.fail("请输入200个字符以内的评论");
		}

		String val = IPKit.getIpAddrByRequest(request.raw()) + ":" + cid;
		Integer count = cache.hget(Types.COMMENTS_FREQUENCY, val);
		if (null != count && count > 0) {
			return RestResponse.fail("您发表评论太快了，请过会再试");
		}

		author = TaleUtils.cleanXSS(author);
		text = TaleUtils.cleanXSS(text);

		author = EmojiParser.parseToAliases(author);
		text = EmojiParser.parseToAliases(text);

		Comments comments = new Comments();
		comments.setAuthor(author);
		comments.setCid(cid);
		comments.setIp(request.address());
		comments.setUrl(url);
		comments.setContent(text);
		comments.setMail(mail);
		comments.setParent(coid);
		try {
			commentsService.saveComment(comments);
			response.cookie("tale_remember_author", URLEncoder.encode(author, "UTF-8"), 7 * 24 * 60 * 60);
			response.cookie("tale_remember_mail", URLEncoder.encode(mail, "UTF-8"), 7 * 24 * 60 * 60);
			if (StringKit.isNotBlank(url)) {
				response.cookie("tale_remember_url", URLEncoder.encode(url, "UTF-8"), 7 * 24 * 60 * 60);
			}
			// 设置对每个文章30秒可以评论一次
			cache.hset(Types.COMMENTS_FREQUENCY, val, 1, 30);
			siteService.cleanCache(Types.C_STATISTICS);
			request.attribute("del_csrf_token", token);
			return RestResponse.ok();
		} catch (Exception e) {
			String msg = "评论发布失败";
			if (e instanceof TipException) {
				msg = e.getMessage();
			} else {
				LOGGER.error(msg, e);
			}
			return RestResponse.fail(msg);
		}
	}
}
