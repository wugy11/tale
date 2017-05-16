package com.tale.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blade.ioc.annotation.Inject;
import com.blade.ioc.annotation.Service;
import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.core.Take;
import com.blade.kit.CollectionKit;
import com.blade.kit.DateKit;
import com.blade.kit.StringKit;
import com.tale.constants.Constant;
import com.tale.model.Finance;

@Service
public class FinanceService {

	@Inject
	private ActiveRecord activeRecord;

	public void saveFinance(Finance finance) {
		Integer id = finance.getId();
		Date expenseTime = finance.getExpenseTime();
		finance.setCategory(Constant.getFinanceCategory(finance.getType()));
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

	public Map<String, Object> statisticPieData(String month) {
		if (StringKit.isEmpty(month))
			month = DateKit.getToday("yyyy-MM");
		StringBuilder sql = new StringBuilder();
		sql.append("select strftime('%Y-%m', datetime(expense_time, 'unixepoch', 'localtime')) month_str,")
				.append("strftime('%Y-%m-%d', datetime(expense_time, 'unixepoch', 'localtime')) date_str, type, sum(money) sum_money ")
				.append("from t_finance where month_str = ? ").append("group by date_str,type order by date_str desc");
		List<Map<String, Object>> listMap = activeRecord.listMap(sql.toString(), month);

		Set<String> dateSet = CollectionKit.newHashSet();
		listMap.forEach(map -> {
			dateSet.add(String.valueOf(map.get("date_str")));
		});

		Map<String, List<Map<String, Object>>> datas = CollectionKit.newHashMap();
		dateSet.forEach(date -> {
			List<Map<String, Object>> dayMapData = CollectionKit.newArrayList();
			listMap.forEach(map -> {
				if (date.equals(map.get("date_str"))) {
					Map<String, Object> dataMap = CollectionKit.newHashMap();
					dataMap.put("name", map.get("type"));
					dataMap.put("value", map.get("sum_money"));
					dayMapData.add(dataMap);
				}
			});
			datas.put(date, dayMapData);
		});

		Map<String, Object> resMap = CollectionKit.newHashMap();
		List<String> financeAllTypes = CollectionKit.newArrayList();
		Constant.financeTypes().forEach(financeType -> {
			financeAllTypes.add(financeType.getDesc());
		});
		resMap.put("legendDatas", financeAllTypes);
		resMap.put("pieDatas", datas);
		return resMap;
	}

	public Map<String, Object> statisticLineData() {
		StringBuilder sql = new StringBuilder();
		sql.append("select strftime('%Y-%m', datetime(expense_time,'unixepoch','localtime')) month_str, ")
				.append("sum(money) sum_money, category ").append("from t_finance ")
				.append("group by category order by month_str");
		List<Map<String, Object>> listMap = activeRecord.listMap(sql.toString());

		Set<String> xAxisDataList = CollectionKit.newHashSet();
		listMap.forEach(map -> {
			xAxisDataList.add(String.valueOf(map.get("month_str")));
		});

		Map<String, Object> resMap = CollectionKit.newHashMap();
		resMap.put("xAxisData", CollectionKit.createArrayList(xAxisDataList));

		List<String> legendData = CollectionKit.newArrayList();
		legendData.add(Constant.income.getDesc());
		legendData.add(Constant.expense.getDesc());
		resMap.put("legendData", legendData);

		List<Map<String, Object>> seriesDataList = CollectionKit.newArrayList();
		legendData.forEach(legend -> {
			Map<String, Object> dataMap = CollectionKit.newHashMap();
			dataMap.put("type", "line");
			dataMap.put("name", legend);
			List<Object> lineData = CollectionKit.newArrayList();
			listMap.forEach(map -> {
				if (legend.equals(map.get("category"))) {
					lineData.add(map.get("sum_money"));
				}
			});
			dataMap.put("data", lineData);
			seriesDataList.add(dataMap);
		});
		resMap.put("seriesData", seriesDataList);
		return resMap;
	}
}
