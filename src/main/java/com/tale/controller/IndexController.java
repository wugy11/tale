package com.tale.controller;

import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.core.Take;
import com.blade.jdbc.model.Paginator;
import com.blade.kit.DateKit;
import com.blade.kit.PatternKit;
import com.blade.kit.StringKit;
import com.blade.mvc.annotation.*;
import com.blade.mvc.http.HttpMethod;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;
import com.blade.mvc.http.Session;
import com.blade.mvc.ui.RestResponse;
import com.tale.constants.Constant;
import com.tale.constants.TaleConst;
import com.tale.dto.Archive;
import com.tale.dto.ErrorCode;
import com.tale.dto.MetaDto;
import com.tale.dto.Types;
import com.tale.exception.TipException;
import com.tale.ext.Commons;
import com.tale.model.*;
import com.tale.service.*;
import com.tale.utils.TaleUtils;
import com.vdurmont.emoji.EmojiParser;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Path
public class IndexController extends BaseController {

    @Inject
    private ContentsService contentsService;
    @Inject
    private MetasService metasService;
    @Inject
    private CommentsService commentsService;
    @Inject
    private SiteService siteService;
    @Inject
    private AttachService attachService;
    @Inject
    private BookService bookService;

    /**
     * 首页
     */
    @Route(values = "/", method = HttpMethod.GET)
    public String index(Request request, @QueryParam(defaultValue = "12") int limit) {
        return this.index(request, 1, limit);
    }

    /**
     * 自定义页面
     */
    @Route(values = {"/:cid", "/:cid.html"}, method = HttpMethod.GET)
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
    @Route(values = {"page/:page", "page/:page.html"}, method = HttpMethod.GET)
    public String index(Request request, @PathParam int page, @QueryParam(defaultValue = "12") int limit) {
        page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;
        // allow_feed代表隐藏/显示(这里主要用于首页是否展示文章)：1表示显示，0表示隐藏，隐藏的文章只能作者本人看到
        Users loginUser = TaleUtils.getLoginUser();
        Integer uid = -1;
        if (null != loginUser) {
            uid = loginUser.getUid();
        }
        Take take = new Take(Contents.class).eq("type", Types.ARTICLE).eq("status", Types.PUBLISH).eq("allow_feed", 1)
                .or("allow_feed", 0).eq("author_id", uid).page(page, limit, "created desc");
        Paginator<Contents> articles = contentsService.getArticles(take);
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
    @Route(values = {"article/:cid", "article/:cid.html"}, method = HttpMethod.GET)
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
     * 标签页
     */
    @Route(values = {"tag/:name", "tag/:name.html"}, method = HttpMethod.GET)
    public String tags(Request request, @PathParam String name, @QueryParam(defaultValue = "12") int limit) {
        return this.tags(request, name, 1, limit);
    }

    /**
     * 标签分页
     */
    @Route(values = {"tag/:name/:page", "tag/:name/:page.html"}, method = HttpMethod.GET)
    public String tags(Request request, @PathParam String name, @PathParam int page,
                       @QueryParam(defaultValue = "12") int limit) {
        page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;
        MetaDto metaDto = metasService.getMeta(Types.TAG, name);
        if (null == metaDto) {
            return this.render_404();
        }

        Paginator<Contents> contentsPaginator = contentsService.getArticles(metaDto.getMid(), page, limit);
        request.attribute("articles", contentsPaginator);
        request.attribute("meta", metaDto);
        request.attribute("type", "标签");
        request.attribute("keyword", name);
        request.attribute("name", name + "(" + contentsPaginator.getList().size() + ")");
        request.attribute("page_prefix", "/" + Types.TAG + "/" + name);
        return this.render("pageTag");
    }

    /**
     * 搜索页
     */
    @Route(values = {"search/:keyword", "search/:keyword.html"}, method = HttpMethod.GET)
    public String search(Request request, @PathParam String keyword, @QueryParam(defaultValue = "12") int limit) {
        return this.search(request, keyword, 1, limit);
    }

    @Route(values = {"search", "search.html"})
    public String search(Request request, @QueryParam(defaultValue = "12") int limit) {
        String keyword = request.query("s").orElse("");
        return this.search(request, keyword, 1, limit);
    }

    @Route(values = {"search/:keyword/:page", "search/:keyword/:page.html"}, method = HttpMethod.GET)
    public String search(Request request, @PathParam String keyword, @PathParam int page,
                         @QueryParam(defaultValue = "12") int limit) {

        page = page < 0 || page > TaleConst.MAX_PAGE ? 1 : page;
        Take take = new Take(Contents.class).eq("type", Types.ARTICLE).eq("status", Types.PUBLISH)
                .like("title", "%" + keyword + "%").page(page, limit, "created desc");

        Paginator<Contents> articles = contentsService.getArticles(take);
        request.attribute("articles", articles);

        request.attribute("type", "搜索");
        request.attribute("keyword", keyword);
        request.attribute("page_prefix", "/search/" + keyword);
        return this.render("pageTag");
    }

    /**
     * 归档页
     */
    @Route(values = {"archives", "archives.html"}, method = HttpMethod.GET)
    public String archives(Request request) {
        List<Archive> archives = siteService.getArchives();
        request.attribute("archives", archives);
        request.attribute("is_archive", true);
        return this.render("archives");
    }

    /**
     * 总标签页
     */
    @Route(values = {"tags", "tags.html"}, method = HttpMethod.GET)
    public String tags(Request request) throws Exception {
        List<MetaDto> tags = getMetas(Types.TAG);
        request.attribute("tags", tags);
        request.attribute("type", "标签");
        return this.render("tags");
    }

    private List<MetaDto> getMetas(String type) throws Exception {
        List<MetaDto> metas = siteService.getMetas(Types.RECENT_META, type, TaleConst.MAX_POSTS);
        for (MetaDto metaDto : metas) {
            int count = metaDto.getCount();
            if (count > 0) {
                String name = metaDto.getName();
                StringBuilder label = new StringBuilder("<a href=\"/" + type + "/")
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
    @Route(values = {"links", "links.html"}, method = HttpMethod.GET)
    public String links(Request request) {
        List<Metas> links = metasService.getMetas(Types.LINK);
        request.attribute("links", links);
        return this.render("links");
    }

    /**
     * 注销
     */
    @Route(values = "logout")
    public void logout(Session session, Response response) {
        TaleUtils.logout(session, response);
    }

    /**
     * 评论操作
     */
    @Route(values = "comment", method = HttpMethod.POST)
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

        String val = request.address() + ":" + cid;
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
        comments.setStatus(Constant.unread.getDesc());
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

    /**
     * 简历预览
     */
    @Route(values = "/viewResume", method = HttpMethod.GET)
    public void viewResume(Request request, Response response) {
        // Take take = new Take(Attach.class).like("fname",
        // Constant.resume.getDesc()).page(1, 1);
        // Paginator<Attach> attachs = attachService.getAttachs(take);
        // if (null == attachs || CollectionKit.isEmpty(attachs.getList()))
        // return;
        // Attach resume = attachs.getList().get(0);
        //
        // HttpServletResponse raw = response.raw();
        // raw.setContentType("application/pdf");
        // try {
        // InputStream inStream = new FileInputStream(TaleUtils.upDir +
        // resume.getFkey()); // 读入原文件
        // BufferedInputStream bis = new BufferedInputStream(inStream);
        // ServletOutputStream outStream = raw.getOutputStream();
        // BufferedOutputStream bos = new BufferedOutputStream(outStream);
        // byte[] buffer = new byte[2048];
        // int byteread = 0;
        // while ((byteread = bis.read(buffer)) != -1) {
        // bos.write(buffer, 0, byteread);
        // }
        // bis.close();
        // bos.close();
        // inStream.close();
        // outStream.close();
        // } catch (Exception e) {
        // e.printStackTrace();
        // LOGGER.error("读取简历文件失败：" + e);
        // }
    }

    /**
     * 首页书单页
     */
    @Route(values = "/books", method = HttpMethod.GET)
    public String booksView(Request request) {
        List<Book> books = bookService.selectBookList();
        request.attribute("books", books);
        return render("books");
    }

    /**
     * 首页书单下对应文章页
     */
    @Route(values = "/books/:id", method = HttpMethod.GET)
    public String bookContentsView(Request request, @PathParam Integer id, @PathParam int page,
                                   @QueryParam(defaultValue = "12") int limit) {
        Take take = Take.create(Contents.class).eq("book_id", id).page(page, limit, "created desc");
        Paginator<Contents> articles = contentsService.getArticles(take);
        request.attribute("articles", articles);

        Book book = bookService.byId(id);
        request.attribute("book", book);
        return render("bookContents");
    }

    /**
     * 首页文章统计页面
     */
    @Route(values = "/articleStatistic", method = HttpMethod.GET)
    public String articleStatistic(Request request) {
        request.attribute("date", DateKit.toString(new Date(), "yyyy-MM"));
        return render("articleStatistic");
    }

    /**
     * 首页文章统计查询：按月份、标签查询
     */
    @Route(values = "/selectArticleStatistic", method = HttpMethod.POST)
    @JSON
    public Map<String, Object> selectArticleStatistic(@QueryParam String month, @QueryParam String tags) {
        return contentsService.articleStatistic(month, tags);
    }

}
