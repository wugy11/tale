package com.tale.ext;

import java.util.List;

import com.blade.kit.CollectionKit;
import com.blade.kit.StringKit;
import com.tale.constants.Constant;
import com.tale.model.Book;
import com.tale.model.Metas;

/**
 * 后台公共函数
 * <p>
 * Created by biezhi on 2017/2/21.
 */
public final class AdminCommons {

	/**
	 * 判断category和cat的交集
	 *
	 * @param cats
	 * @return
	 */
	public static boolean exist_cat(Metas category, String cats) {
		String[] arr = cats.split(",");
		if (!CollectionKit.isEmpty(arr)) {
			for (String c : arr) {
				if (c.trim().equals(category.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean existBook(Book book, Integer bookId) {
		if (bookId == book.getId())
			return true;
		return false;
	}

	private static final String[] COLORS = { "default", "primary", "success", "info", "warning", "danger", "inverse",
			"purple", "pink" };

	public static String rand_color() {
		int r = StringKit.rand(0, COLORS.length - 1);
		return COLORS[r];
	}

	public static List<String> bookStatus() {
		List<String> bookStatus = CollectionKit.newLinkedList();
		Constant.bookStatus().forEach(status -> bookStatus.add(status.getDesc()));
		return bookStatus;
	}

	public static List<String> financeType() {
		List<String> financeTypes = CollectionKit.newLinkedList();
		Constant.financeTypes().forEach(type -> financeTypes.add(type.getDesc()));
		return financeTypes;
	}

}
