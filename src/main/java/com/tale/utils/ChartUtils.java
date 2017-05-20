package com.tale.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.blade.kit.Assert;
import com.blade.kit.CollectionKit;
import com.blade.kit.DateKit;

abstract public class ChartUtils {

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

}
