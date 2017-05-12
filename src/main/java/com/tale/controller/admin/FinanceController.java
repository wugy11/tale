package com.tale.controller.admin;

import com.blade.mvc.annotation.Controller;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.HttpMethod;
import com.tale.controller.BaseController;

/**
 * 财务管理
 * 
 * @author wugy 2017-5-12 07:29:50
 */
@Controller("admin/finance")
public class FinanceController extends BaseController {

	@Route(value = "", method = HttpMethod.GET)
	public String index() {
		return "admin/financeList";
	}

}
