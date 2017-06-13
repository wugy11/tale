package com.tale.init;

import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import com.blade.Blade;
import com.blade.mvc.WebContextHolder;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;
import com.blade.mvc.http.Session;
import com.blade.mvc.ui.ModelAndView;
import com.blade.mvc.ui.template.TemplateEngine;

import jetbrick.template.JetConfig;
import jetbrick.template.JetContext;
import jetbrick.template.JetEngine;
import jetbrick.template.JetGlobalContext;
import jetbrick.template.JetTemplate;
import jetbrick.template.TemplateException;
import jetbrick.template.resolver.GlobalResolver;

/**
 * JetbrickTemplateEngine
 * 
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.0
 */
public class JetbrickTemplateEngine implements TemplateEngine {

	private JetEngine jetEngine;
	private Properties config = new Properties();
	private String suffix = ".html";

	public JetbrickTemplateEngine() {
		config.put(JetConfig.TEMPLATE_SUFFIX, suffix);
		Class<?> bootClass = Blade.me().bootClass();
		if (null != bootClass) {
			config.put(JetConfig.AUTOSCAN_PACKAGES, bootClass.getPackage().getName());
		}

		String $classpathLoader = "jetbrick.template.loader.ClasspathResourceLoader";
		config.put(JetConfig.TEMPLATE_LOADERS, "$classpathLoader");
		config.put("$classpathLoader", $classpathLoader);
		config.put("$classpathLoader.root", "/templates/");
		config.put("$classpathLoader.reloadable", "true");
	}

	public JetbrickTemplateEngine(Properties config) {
		this.config = config;
		jetEngine = JetEngine.create(config);
	}

	public JetbrickTemplateEngine(String conf) {
		jetEngine = JetEngine.create(conf);
	}

	public JetbrickTemplateEngine(JetEngine jetEngine) {
		if (null == jetEngine) {
			throw new IllegalArgumentException("jetEngine must not be null");
		}
		this.jetEngine = jetEngine;
	}

	@Override
	public void render(ModelAndView modelAndView, Writer writer) throws TemplateException {
		if (null == jetEngine) {
			this.jetEngine = JetEngine.create(config);
		}
		Map<String, Object> modelMap = modelAndView.getModel();

		Request request = WebContextHolder.request();
		modelMap.putAll(request.attributes());

		Response response = WebContextHolder.response();
		response.contentType("text/html; charset=UTF-8");

		Session session = request.session();
		if (null != session) {
			modelMap.putAll(session.attributes());
		}

		JetContext context = new JetContext(modelMap.size());
		context.putAll(modelMap);

		String view = modelAndView.getView();
		String templateName = view.endsWith(suffix) ? view : view + suffix;
		JetTemplate template = jetEngine.getTemplate(templateName);
		template.render(context, writer);
	}

	public JetEngine getJetEngine() {
		return jetEngine;
	}

	public void setJetEngine(JetEngine jetEngine) {
		this.jetEngine = jetEngine;
	}

	public JetGlobalContext getGlobalContext() {
		if (null == jetEngine) {
			this.jetEngine = JetEngine.create(config);
		}
		return jetEngine.getGlobalContext();
	}

	public GlobalResolver getGlobalResolver() {
		if (null == jetEngine) {
			this.jetEngine = JetEngine.create(config);
		}
		return jetEngine.getGlobalResolver();
	}

	public Properties getConfig() {
		return config;
	}

	public TemplateEngine addConfig(String key, String value) {
		config.put(key, value);
		return this;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
}