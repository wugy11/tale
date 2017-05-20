package com.tale.utils;

import static com.blade.Blade.$;

import java.awt.Image;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.blade.context.WebContextHolder;
import com.blade.kit.Assert;
import com.blade.kit.CollectionKit;
import com.blade.kit.DateKit;
import com.blade.kit.FileKit;
import com.blade.kit.StringKit;
import com.blade.kit.Tools;
import com.blade.kit.base.Config;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;
import com.blade.mvc.http.wrapper.Session;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.tale.constants.TaleConst;
import com.tale.ext.Commons;
import com.tale.init.TaleLoader;
import com.tale.model.Users;

/**
 * Tale工具类
 * <p>
 * Created by biezhi on 2017/2/21.
 */
abstract public class TaleUtils {

	public static final String accessKey;
	public static final String secretKey;
	public static final String domain;
	public static final String bucket;

	static {
		Config config = TaleUtils.getCfg();
		accessKey = config.get("qiniu.accessKey");
		secretKey = config.get("qiniu.secretKey");
		domain = config.get("qiniu.domain");
		bucket = config.get("qiniu.bucket");
	}

	/**
	 * 上传图片到七牛云
	 * 
	 * @return 返回图片链接
	 */
	public static String uploadFile(File file, String key) {
		Auth auth = Auth.create(accessKey, secretKey);
		UploadManager uploadManager = new UploadManager();
		com.qiniu.http.Response res = null;
		try {
			res = uploadManager.put(file, key, auth.uploadToken(bucket));
			if (res.isOK())
				return String.format("http://%s/%s", domain, key);
		} catch (QiniuException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取统计饼图日期数据
	 */
	public static List<List<Object>> getPieScatterData(String month) {
		List<List<Object>> scatterData = CollectionKit.createLinkedList();

		Assert.notBlank(month);

		Calendar calendar = Calendar.getInstance();
		Date dateStart = DateKit.dateFormat(month + "-01", "yyyy-MM-dd");
		calendar.setTime(dateStart);
		int m = calendar.get(Calendar.MONTH);
		calendar.set(Calendar.MONTH, m + 1);
		Date dateEnd = calendar.getTime();

		int start = DateKit.getUnixTimeByDate(dateStart);
		int end = DateKit.getUnixTimeByDate(dateEnd);

		int dayTime = 3600 * 24;
		for (int time = start; time < end; time += dayTime) {
			List<Object> dateList = CollectionKit.newArrayList(1);
			dateList.add(DateKit.dateFormat(DateKit.getDateByUnixTime(time), "yyyy-MM-dd"));
			dateList.add(Math.floor(Math.random() * 10000));
			scatterData.add(dateList);
		}
		return scatterData;
	}

	/**
	 * 一个月
	 */
	private static final int one_month = 30 * 24 * 60 * 60;

	/**
	 * 匹配邮箱正则
	 */
	private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern
			.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	private static final Pattern SLUG_REGEX = Pattern.compile("^[A-Za-z0-9_-]{5,100}$", Pattern.CASE_INSENSITIVE);

	/**
	 * 设置记住密码cookie
	 */
	public static void setCookie(Response response, Integer uid) {
		try {
			String val = Tools.enAes(uid.toString(), TaleConst.AES_SALT);
			boolean isSSL = Commons.site_url().startsWith("https");
			response.cookie("/", TaleConst.USER_IN_COOKIE, val, one_month, isSSL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回当前登录用户
	 */
	public static Users getLoginUser() {
		Session session = WebContextHolder.session();
		if (null == session) {
			return null;
		}
		Users user = session.attribute(TaleConst.LOGIN_SESSION_KEY);
		return user;
	}

	/**
	 * 退出登录状态
	 */
	public static void logout(Session session, Response response) {
		session.removeAttribute(TaleConst.LOGIN_SESSION_KEY);
		response.removeCookie(TaleConst.USER_IN_COOKIE);
		response.redirect(Commons.site_url());
	}

	/**
	 * 获取cookie中的用户id
	 */
	public static Integer getCookieUid(Request request) {
		if (null != request) {
			String value = request.cookie(TaleConst.USER_IN_COOKIE);
			if (StringKit.isNotBlank(value)) {
				try {
					String uid = Tools.deAes(value, TaleConst.AES_SALT);
					return StringKit.isNotBlank(uid) && StringKit.isNumber(uid) ? Integer.valueOf(uid) : null;
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * markdown转换为html
	 */
	public static String mdToHtml(String markdown) {
		if (StringKit.isBlank(markdown)) {
			return "";
		}

		List<Extension> extensions = Arrays.asList(TablesExtension.create());
		Parser parser = Parser.builder().extensions(extensions).build();
		Node document = parser.parse(markdown);
		HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
		String content = renderer.render(document);
		content = Commons.emoji(content);

		// 支持网易云音乐输出
		if (TaleConst.BCONF.getBoolean("app.support_163_music", true) && content.contains("[mp3:")) {
			content = content.replaceAll("\\[mp3:(\\d+)\\]",
					"<iframe frameborder=\"no\" border=\"0\" marginwidth=\"0\" marginheight=\"0\" width=350 height=106 src=\"//music.163.com/outchain/player?type=2&id=$1&auto=0&height=88\"></iframe>");
		}
		// 支持gist代码输出
		if (TaleConst.BCONF.getBoolean("app.support_gist", true) && content.contains("https://gist.github.com/")) {
			content = content.replaceAll("&lt;script src=\"https://gist.github.com/(\\w+)/(\\w+)\\.js\">&lt;/script>",
					"<script src=\"https://gist.github.com/$1/$2\\.js\"></script>");
		}
		return content;
	}

	/**
	 * 提取html中的文字
	 */
	public static String htmlToText(String html) {
		if (StringKit.isNotBlank(html)) {
			return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");
		}
		return "";
	}

	/**
	 * 判断文件是否是图片类型
	 */
	public static boolean isImage(File imageFile) {
		if (!imageFile.exists()) {
			return false;
		}
		try {
			Image img = ImageIO.read(imageFile);
			if (img == null || img.getWidth(null) <= 0 || img.getHeight(null) <= 0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断是否是邮箱
	 */
	public static boolean isEmail(String emailStr) {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
		return matcher.find();
	}

	/**
	 * 判断是否是合法路径
	 */
	public static boolean isPath(String slug) {
		if (StringKit.isNotBlank(slug)) {
			if (slug.contains("/") || slug.contains(" ") || slug.contains(".")) {
				return false;
			}
			Matcher matcher = SLUG_REGEX.matcher(slug);
			return matcher.find();
		}
		return false;
	}

	/**
	 * 替换HTML脚本
	 */
	public static String cleanXSS(String value) {
		// You'll need to remove the spaces from the html entities below
		value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
		value = value.replaceAll("'", "&#39;");
		value = value.replaceAll("eval\\((.*)\\)", "");
		value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
		value = value.replaceAll("script", "");
		return value;
	}

	/**
	 * 将list转为 (1, 2, 4) 这样的sql输出
	 * 
	 * @param list
	 * @param <T>
	 * @return
	 */
	public static <T> String listToInSql(java.util.List<T> list) {
		StringBuffer sbuf = new StringBuffer();
		list.forEach(item -> sbuf.append(',').append(item.toString()));
		sbuf.append(')');
		return '(' + sbuf.substring(1);
	}

	public static final String upDir = TaleLoader.CLASSPATH.substring(0, TaleLoader.CLASSPATH.length() - 1);

	public static String getFileKey(String name) {
		String prefix = "/upload/" + DateKit.dateFormat(new Date(), "yyyy/MM");
		String dir = upDir + prefix;
		if (!FileKit.exist(dir)) {
			new File(dir).mkdirs();
		}
		return prefix + "/" + com.blade.kit.UUID.UU32() + "." + FileKit.getExtension(name);
	}

	public static Config getCfg() {
		return $().bConfig().config();
	}
}
