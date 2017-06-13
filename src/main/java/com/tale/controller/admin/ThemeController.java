package com.tale.controller.admin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.blade.Blade;
import com.blade.Environment;
import com.blade.ioc.annotation.Inject;
import com.blade.kit.CollectionKit;
import com.blade.mvc.annotation.JSON;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.QueryParam;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.HttpMethod;
import com.blade.mvc.http.Request;
import com.blade.mvc.ui.RestResponse;
import com.tale.constants.TaleConst;
import com.tale.controller.BaseController;
import com.tale.dto.ThemeDto;
import com.tale.exception.TipException;
import com.tale.ext.Commons;
import com.tale.ext.Theme;
import com.tale.init.TaleLoader;
import com.tale.service.OptionsService;

/**
 * 主题控制器
 */
@Path("admin/themes")
public class ThemeController extends BaseController {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(ThemeController.class);

	@Inject
	private OptionsService optionsService;

	// @Inject
	// private LogService logService;

	@Route(values = "", method = HttpMethod.GET)
	public String index(Request request) {
		// 读取主题
		String themesDir = TaleLoader.CLASSPATH + "templates/themes";
		File[] themesFile = new File(themesDir).listFiles();
		List<ThemeDto> themes = CollectionKit.newArrayList(themesFile.length);
		for (File f : themesFile) {
			if (f.isDirectory()) {
				ThemeDto themeDto = new ThemeDto(f.getName());
				if (Files.exists(Paths.get(f.getPath() + "/setting.html"))) {
					themeDto.setHasSetting(true);
				}
				themes.add(themeDto);
				try {
					Blade.me().addStatics("/templates/themes/" + f.getName() + "/screenshot.png");
				} catch (Exception e) {
				}
			}
		}
		request.attribute("current_theme", Commons.site_theme());
		request.attribute("themes", themes);
		return "admin/themes";
	}

	/**
	 * 主题设置页面
	 * 
	 * @param request
	 * @return
	 */
	@Route(values = "setting", method = HttpMethod.GET)
	public String setting(Request request) {
		Map<String, String> themeOptions = optionsService.getOptions("theme_option_");
		request.attribute("theme_options", themeOptions);
		return this.render("setting");
	}

	/**
	 * 保存主题配置项
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@Route(values = "setting", method = HttpMethod.POST)
	@JSON
	public RestResponse saveSetting(Request request) {
		try {
			Map<String, List<String>> querys = request.parameters();
			optionsService.saveOptions(querys);

			TaleConst.OPTIONS = Environment.of(optionsService.getOptions());

			// logService.save(LogActions.THEME_SETTING,
			// JSONKit.toJSONString(querys), request.address(), this.getUid());
			return RestResponse.ok();
		} catch (Exception e) {
			String msg = "主题设置失败";
			if (e instanceof TipException) {
				msg = e.getMessage();
			} else {
				LOGGER.error(msg, e);
			}
			return RestResponse.fail(msg);
		}
	}

	/**
	 * 激活主题
	 * 
	 * @param request
	 * @param site_theme
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@Route(values = "active", method = HttpMethod.POST)
	@JSON
	public RestResponse activeTheme(Request request, @QueryParam String site_theme) {
		try {
			optionsService.saveOption("site_theme", site_theme);
			optionsService.deleteOption("theme_option_");

			TaleConst.OPTIONS.set("site_theme", site_theme);
			Theme.THEME = "themes/" + site_theme;

			String themePath = "/templates/themes/" + site_theme;
			try {
				TaleLoader.loadTheme(themePath);
			} catch (Exception e) {
			}
			// logService.save(LogActions.THEME_SETTING, site_theme,
			// request.address(), this.getUid());
			return RestResponse.ok();
		} catch (Exception e) {
			String msg = "主题启用失败";
			if (e instanceof TipException) {
				msg = e.getMessage();
			} else {
				LOGGER.error(msg, e);
			}
			return RestResponse.fail(msg);
		}
	}

}
