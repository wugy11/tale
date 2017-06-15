package com.tale.utils;

import com.blade.Blade;
import com.blade.Environment;
import com.blade.kit.*;
import com.blade.mvc.WebContextHolder;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;
import com.blade.mvc.http.Session;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.tale.constants.TaleConst;
import com.tale.ext.Commons;
import com.tale.model.Users;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tale工具类
 * <p>
 * Created by biezhi on 2017/2/21.
 */
abstract public class TaleUtils {

    private static final String accessKey;
    private static final String secretKey;
    private static final String domain;
    private static final String bucket;

    static {
        Environment env = getEnv();
        accessKey = env.getString("qiniu.accessKey");
        secretKey = env.getString("qiniu.secretKey");
        domain = env.getString("qiniu.domain");
        bucket = env.getString("qiniu.bucket");
    }

    /**
     * 上传图片到七牛云
     *
     * @return 返回图片链接
     */
    public static String uploadFile(String filePath, String key) {
        Auth auth = Auth.create(accessKey, secretKey);
        UploadManager uploadManager = new UploadManager();
        com.qiniu.http.Response res;
        try {
            res = uploadManager.put(filePath, key, auth.uploadToken(bucket));
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
        List<List<Object>> scatterData = CollectionKit.newLinkedList();

        Assert.notBlank(month);

        Calendar calendar = Calendar.getInstance();
        Date dateStart = DateKit.toDate(month + "-01", "yyyy-MM-dd");
        calendar.setTime(dateStart);
        int m = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, m + 1);
        Date dateEnd = calendar.getTime();

        int start = DateKit.toUnix(dateStart);
        int end = DateKit.toUnix(dateEnd);

        int dayTime = 3600 * 24;
        for (int time = start; time < end; time += dayTime) {
            List<Object> dateList = CollectionKit.newArrayList(1);
            dateList.add(DateKit.toString(time, "yyyy-MM-dd"));
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
            String val = new String(EncrypKit.encryptAES(uid.toString().getBytes(), TaleConst.AES_SALT.getBytes()));
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
        Session session = WebContextHolder.request().session();
        if (null == session) {
            return null;
        }
        return session.attribute(TaleConst.LOGIN_SESSION_KEY);
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
            Optional<String> value = request.cookie(TaleConst.USER_IN_COOKIE);
            if (value.isPresent()) {
                String uid = new String(
                        EncrypKit.decryptAES(value.get().getBytes(), TaleConst.AES_SALT.getBytes()));
                return StringKit.isNotBlank(uid) && StringKit.isNumber(uid) ? Integer.valueOf(uid) : null;
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
     */
    public static <T> String listToInSql(java.util.List<T> list) {
        StringBuffer sb = new StringBuffer();
        list.forEach(item -> sb.append(',').append(item.toString()));
        sb.append(')');
        return '(' + sb.substring(1);
    }

    public static final String upDir = TaleConst.CLASSPATH.substring(0, TaleConst.CLASSPATH.length() - 1);

    public static String getFileKey(String name) {
        String prefix = "/upload/" + DateKit.toString("yyyy/MM");
        String dir = upDir + prefix;
        if (!Files.exists(Paths.get(dir))) {
            new File(dir).mkdirs();
        }
        return prefix + "/" + com.blade.kit.UUID.UU32() + "." + StringKit.fileExt(name);
    }

    public static Environment getEnv() {
        return Blade.me().environment();
    }
}
