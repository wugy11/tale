package com.tale.init;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.blade.Blade;
import com.blade.Environment;
import com.blade.event.BeanProcessor;
import com.blade.ioc.Ioc;
import com.blade.ioc.annotation.Bean;
import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.ar.SampleActiveRecord;
import com.blade.jdbc.model.ExtSql2o;
import com.blade.kit.CollectionKit;
import com.blade.kit.StringKit;
import com.tale.constants.TaleConst;
import com.tale.dto.Types;
import com.tale.ext.AdminCommons;
import com.tale.ext.Commons;
import com.tale.ext.JetTag;
import com.tale.ext.Theme;
import com.tale.service.OptionsService;
import com.tale.service.SiteService;

import jetbrick.template.JetGlobalContext;
import jetbrick.template.resolver.GlobalResolver;

/**
 * Tale初始化进程
 *
 * @author biezhi
 */
@Bean
public class WebContext implements BeanProcessor {

	@Inject
	private OptionsService optionsService;
	@Inject
	private Environment environment;

	@Override
	public void processor(Blade blade) {
		JetbrickTemplateEngine templateEngine = new JetbrickTemplateEngine(blade);

		List<String> macros = CollectionKit.newArrayList(8);
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
			if (f.isDirectory() && Files.exists(Paths.get(f.getPath() + "/macros.html"))) {
				String macroName = "/themes/" + f.getName() + "/macros.html";
				macros.add(macroName);
			}
		}
		StringBuffer sb = new StringBuffer();
		macros.forEach(s -> sb.append(',').append(s));
		templateEngine.addConfig("jetx.import.macros", sb.substring(1));

		GlobalResolver resolver = templateEngine.getGlobalResolver();
		resolver.registerFunctions(Commons.class);
		resolver.registerFunctions(Theme.class);
		resolver.registerFunctions(AdminCommons.class);
		resolver.registerTags(JetTag.class);

		JetGlobalContext context = templateEngine.getGlobalContext();
		context.set("version", environment.get("app.version", "v1.0"));

		blade.templateEngine(templateEngine);

		TaleConst.MAX_FILE_SIZE = environment.getInt("app.max-file-size", 20480);

		TaleConst.AES_SALT = environment.get("app.salt", "012c456789abcdef");
		TaleConst.OPTIONS.addAll(optionsService.getOptions());
		String ips = TaleConst.OPTIONS.get(Types.BLOCK_IPS, "");
		if (StringKit.isNotBlank(ips)) {
			TaleConst.BLOCK_IPS.addAll(Arrays.asList(ips.split(",")));
		}
			
		Theme.THEME = "themes/" + Commons.site_option("site_theme");

		TaleConst.INSTALL = environment.getBoolean("app.installed", false);
		TaleConst.BCONF = environment;
	}

	@Override
	public void preHandle(Blade blade) {
		SqliteJdbc.importSql(blade.devMode());
		ExtSql2o sql2o = new ExtSql2o(SqliteJdbc.DB_SRC);
		ActiveRecord activeRecord = new SampleActiveRecord(sql2o);
		Ioc ioc = blade.ioc();
		ioc.addBean(activeRecord);
		Commons.setSiteService(ioc.getBean(SiteService.class));
	}
}
