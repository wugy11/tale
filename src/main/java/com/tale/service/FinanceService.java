package com.tale.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.blade.ioc.annotation.Inject;
import com.blade.ioc.annotation.Service;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.core.Take;
import com.blade.kit.CollectionKit;
import com.blade.kit.DateKit;
import com.blade.kit.StringKit;
import com.tale.model.Finance;

@Service
public class FinanceService {

	@Inject
	private ActiveRecord activeRecord;

	public void saveFinance(Finance finance) {
		Integer id = finance.getId();
		Date expenseTime = finance.getExpenseTime();
		if (null != expenseTime)
			finance.setExpense_time(DateKit.getUnixTimeByDate(finance.getExpenseTime()));
		if (null == id) {
			finance.setCreate_time(DateKit.getCurrentUnixTime());
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

	public List<Finance> selectFinanceList(Take take) {
		return activeRecord.list(take);
	}

	public Map<String, Object> statisticByDay(String day) {
		if (StringKit.isEmpty(day))
			day = DateKit.getToday("yyyy-MM-dd");
		StringBuilder sql = new StringBuilder();
		sql.append(
				"select strftime('%Y-%m-%d', datetime(expense_time, 'unixepoch', 'localtime') ) date_str, type, sum(money) sum_money ")
				.append("from t_finance where date_str = ? ").append("group by date_str,type order by date_str desc");
		List<Map<String, Object>> listMap = activeRecord.listMap(sql.toString(), day);
		List<String> types = CollectionKit.createLinkedList();
		List<String> datas = CollectionKit.createLinkedList();
		listMap.forEach(map -> {
			types.add(String.valueOf(map.get("type")));
			datas.add(String.valueOf(map.get("sum_money")));
		});
		Map<String, Object> resMap = CollectionKit.newHashMap(2);
		resMap.put("types", types);
		resMap.put("datas", datas);
		return resMap;
	}
}
