package com.tale.controller.admin;

import java.util.List;
import java.util.Map;

import com.blade.ioc.annotation.Inject;
import com.blade.jdbc.core.Take;
import com.blade.mvc.annotation.Controller;
import com.blade.mvc.annotation.EntityObj;
import com.blade.mvc.annotation.JSON;
import com.blade.mvc.annotation.QueryParam;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.HttpMethod;
import com.blade.mvc.http.Request;
import com.blade.mvc.view.RestResponse;
import com.tale.controller.BaseController;
import com.tale.model.Finance;
import com.tale.model.Users;
import com.tale.service.FinanceService;

/**
 * 财务管理
 * 
 * @author wugy 2017-5-12 07:29:50
 */
@Controller("admin/finance")
public class FinanceController extends BaseController {

	@Inject
	private FinanceService financeService;

	@Route(value = "", method = HttpMethod.GET)
	public String index() {
		return "admin/financeList";
	}

	@Route(value = "/saveFinance", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> saveFinance(@EntityObj Finance finance) {
		try {
			Users user = user();
			if (null != user) {
				finance.setUid(user.getUid());
			}
			financeService.saveFinance(finance);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("保存失败:" + e);
			return RestResponse.fail("保存失败");
		}
		return RestResponse.ok();
	}

	@Route(value = "/deleteFinance", method = HttpMethod.POST)
	@JSON
	public RestResponse<?> deleteFinance(@QueryParam String ids) {
		try {
			financeService.deleteFinance(ids);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("删除失败:" + e);
			return RestResponse.fail("删除失败");
		}
		return RestResponse.ok();
	}

	@Route(value = "/selectFinanceList", method = HttpMethod.POST)
	@JSON
	public List<Finance> selectFinanceList(@QueryParam(value = "page", defaultValue = "1") int page,
			@QueryParam(value = "limit", defaultValue = "10") int limit, Request request) {
		Take take = new Take(Finance.class).page(page, limit).desc("expense_time").desc("id");
		List<Finance> financeList = financeService.selectFinanceList(take);
		return financeList;
	}

	@Route(value = "/statistics", method = HttpMethod.GET)
	public String statistics() {
		return "admin/financeStatistics";
	}

	@Route(value = "/statisticByDay", method = HttpMethod.POST)
	@JSON
	public Map<String, Object> statisticByDay(@QueryParam String day) {
		return financeService.statisticByDay(day);
	}

}
