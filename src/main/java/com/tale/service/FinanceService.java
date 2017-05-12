package com.tale.service;

import java.util.Arrays;
import java.util.List;

import com.blade.ioc.annotation.Inject;
import com.blade.ioc.annotation.Service;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.core.Take;
import com.blade.kit.DateKit;
import com.tale.model.Finance;

@Service
public class FinanceService {

	@Inject
	private ActiveRecord activeRecord;

	public void saveFinance(Finance finance) {
		Integer id = finance.getId();
		if (null == id) {
			finance.setCreate_time(DateKit.getCurrentUnixTime());
			finance.setExpense_time(DateKit.getUnixTimeByDate(finance.getExpenseTime()));
			activeRecord.insert(finance);
		} else {
			activeRecord.update(finance);
		}
	}

	public Finance byId(Integer id) {
		return activeRecord.byId(Finance.class, id);
	}

	public void deleteFinance(String ids) {
		Take take = new Take(Finance.class).in("id", Arrays.asList(ids.split(",")));
		activeRecord.delete(take);
	}

	public List<Finance> selectFinanceList() {
		Take take = Take.create(Finance.class);
		return activeRecord.list(take);
	}

	public List<Finance> selectFinanceList(Take take) {
		return activeRecord.list(take);
	}
}
