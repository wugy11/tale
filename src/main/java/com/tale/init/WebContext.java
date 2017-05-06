package com.tale.init;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import com.blade.Const;
import com.blade.config.BConfig;
import com.blade.context.WebContextListener;
import com.blade.ioc.BeanProcessor;
import com.blade.ioc.Ioc;
import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.ar.SampleActiveRecord;
import com.blade.kit.FileKit;
import com.blade.kit.StringKit;
import com.blade.kit.base.Config;
import com.blade.mvc.view.ViewSettings;
import com.blade.mvc.view.template.JetbrickTemplateEngine;
import com.tale.constants.TaleConst;
import com.tale.dto.Types;
import com.tale.ext.AdminCommons;
import com.tale.ext.Commons;
import com.tale.ext.JetTag;
import com.tale.ext.Theme;
import com.tale.model.ExtSql2o;
import com.tale.service.OptionsService;
import com.tale.service.SiteService;
import com.tale.utils.RewriteUtils;

import jetbrick.template.JetGlobalContext;
import jetbrick.template.resolver.GlobalResolver;

/**
 * Tale初始化进程
 *
 * @author biezhi
 */
public class WebContext implements BeanProcessor, WebContextListener {

	@Inject
	private OptionsService optionsService;

	@Override
	public void init(BConfig bConfig, ServletContext sec) {
		JetbrickTemplateEngine templateEngine = new JetbrickTemplateEngine();

		List<String> macros = new ArrayList<>(8);
		macros.add("/comm/macros.html");
		// 扫描主题下面的所有自定义宏
		String themeDir = TaleLoader.CLASSPATH + "templates/themes";
		try {
			themeDir = new URI(themeDir).getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		File[] dir = new File(themeDir).listFiles();
		for (File f : dir) {
			if (f.isDirectory() && FileKit.exist(f.getPath() + "/macros.html")) {
				String macroName = "/themes/" + f.getName() + "/macros.html";
				macros.add(macroName);
			}
		}
		StringBuffer sbuf = new StringBuffer();
		macros.forEach(s -> sbuf.append(',').append(s));
		templateEngine.addConfig("jetx.import.macros", sbuf.substring(1));

		GlobalResolver resolver = templateEngine.getGlobalResolver();
		resolver.registerFunctions(Commons.class);
		resolver.registerFunctions(Theme.class);
		resolver.registerFunctions(AdminCommons.class);
		resolver.registerTags(JetTag.class);

		JetGlobalContext context = templateEngine.getGlobalContext();
		Config config = bConfig.config();
		context.set("version", config.get("app.version", "v1.0"));

		ViewSettings.$().templateEngine(templateEngine);

		TaleConst.MAX_FILE_SIZE = config.getInt("app.max-file-size", 20480);

		TaleConst.AES_SALT = config.get("app.salt", "012c456789abcdef");
		TaleConst.OPTIONS.addAll(optionsService.getOptions());
		String ips = TaleConst.OPTIONS.get(Types.BLOCK_IPS, "");
		if (StringKit.isNotBlank(ips)) {
			TaleConst.BLOCK_IPS.addAll(Arrays.asList(StringKit.split(ips, ",")));
		}
		if (config.getBoolean("app.installed", false) || FileKit.exist(TaleLoader.CLASSPATH + Const.INSTALLED)) {
			TaleConst.INSTALL = Boolean.TRUE;
		}

		String db_rewrite = TaleConst.OPTIONS.get("rewrite_url", "");
		if (StringKit.isNotEmpty(db_rewrite)) {
			RewriteUtils.rewrite(db_rewrite);
		}

		Theme.THEME = "themes/" + Commons.site_option("site_theme");

		TaleConst.BCONF = config;
	}

	@Override
	public void register(Ioc ioc) {
		SqliteJdbc.importSql();
		ExtSql2o sql2o = new ExtSql2o(SqliteJdbc.DB_SRC);
		ActiveRecord activeRecord = new SampleActiveRecord(sql2o);
		ioc.addBean(activeRecord);
		Commons.setSiteService(ioc.getBean(SiteService.class));
	}

}
