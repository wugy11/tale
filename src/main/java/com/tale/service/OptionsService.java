package com.tale.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blade.ioc.annotation.Bean;
import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.core.Take;
import com.blade.kit.CollectionKit;
import com.blade.kit.StringKit;
import com.tale.model.Options;

@Bean
public class OptionsService {

	@Inject
	private ActiveRecord activeRecord;

	public void saveOptions(Map<String, List<String>> options) {
		if (!CollectionKit.isEmpty(options)) {
			options.forEach((k, v) -> saveOption(k, v.get(0)));
		}
	}

	public void saveOption(String key, String value) {
		if (StringKit.isNotBlank(key) && StringKit.isNotBlank(value)) {
			Options options = new Options();
			options.setName(key);
			int count = activeRecord.count(options);
			if (count == 0) {
				options.setValue(value);
				activeRecord.insert(options);
			} else {
				options.setValue(value);
				activeRecord.update(options);
			}
		}
	}

	public Map<String, String> getOptions() {
		return getOptions(null);
	}

	public Map<String, String> getOptions(String key) {
		Map<String, String> options = new HashMap<>();
		Take take = new Take(Options.class);
		if (StringKit.isNotBlank(key)) {
			take.like("name", key + "%");
		}
		List<Options> optionsList = activeRecord.list(take);
		if (null != optionsList) {
			optionsList.forEach(option -> options.put(option.getName(), option.getValue()));
		}
		return options;
	}

	public void deleteOption(String key) {
		if (StringKit.isNotBlank(key)) {
			activeRecord.delete(new Take(Options.class).like("name", key + "%"));
		}
	}
}
