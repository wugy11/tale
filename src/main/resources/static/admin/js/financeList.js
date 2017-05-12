var tale = new $.tale();
$(function() {
	var financeListTable = new FinanceListTable();
	financeListTable.init();
	
	$("#updateFinanceBtn").click(function() {
		var selectList = financeListTable.getSelections();
		var len = selectList.length;
		if (len == 0 || len > 1) {
			tale.alertWarn({text : '请选择一条数据',});
			return;
		}
		$("#financeModal").modal();
		var data = selectList[0];
		tale.autoFillForm('financeForm', data);
		if (data.expense_time)
			$("#expenseTime").val(new Date(data.expense_time * 1000).Format('yyyy-MM-dd HH:mm:ss'));
	});
	$("#deleteFinanceBtn").click(function() {
		var selectList = financeListTable.getSelections();
		var len = selectList.length;
		if (len == 0) {
			tale.alertWarn({text : '请至少选择一条数据',});
			return;
		}
		var params = [];
		$.each(selectList, function(i, select) {
			params.push(select.id);
		})
		tale.post({
			url : '/admin/finance/deleteFinance',
			data : {ids : params.join(",")},
			success : function(result) {
				if (!result.success) {
					tale.alertError(result.msg);
					return;
				}
				financeListTable.refresh();
			}
		});
	});
	$("#saveBtn").click(function() {
		var params = tale.getParams("financeForm");
		tale.post({
			url : '/admin/finance/saveFinance',
			data : params,
			success : function(result) {
				if (!result.success) {
					tale.alertError(result.msg);
					return;
				}
				financeListTable.refresh();
			}
		});
	});
	
});
var FinanceListTable = function() {
	var table = new Object();
	table.init = function() {
		$("#financeListTable").bootstrapTable({
			url : '/admin/finance/selectFinanceList', 
			method : 'post', 
			dataType : 'json',
			striped : true, // 是否显示行间隔色
			pagination : true, 
			queryParams : table.queryParams,
			pageNumber : 1, 
			pageSize : 10, 
			pageList : [ 10, 20, 50], 
			strictSearch : true,
			search : true,
			showRefresh : true,
			clickToSelect : true, // 是否启用点击选中行
			idField : "id", // 每一行的唯一标识，一般为主键列
			columns : [ {
				field : 'id', visible : false,
			}, {
				title : '序号', checkbox : true
			}, {
				field : 'name', title : '金额', width : '15%', 
				formatter : function(value, row, index) {
					return '<a target="#">'+value+'</a>';
				}
			}, {
				field : 'type', title : '类型', width : '15%',
				formatter : function(value, row, index) {
					return new Date(value * 1000).Format('yyyy-MM-dd HH:mm:ss');
				}
			}, {
				field : 'expense_time', title : '时间', width : '25%',
				formatter : function(value, row, index) {
					if (value)
						return new Date(value * 1000).Format('yyyy-MM-dd HH:mm:ss');
					return '';
				}
			}, {
				field : 'create_time', title : '创建时间', width : '25%',
				formatter : function(value, row, index) {
					if (value)
						return new Date(value * 1000).Format('yyyy-MM-dd HH:mm:ss');
					return '';
				}
			}, {
				field : 'remark', title : '备注',
			}, ],
			toolbar : '#operateToolbar',
		});
	};
	
	table.loadTable = function(data) {
		$("#financeListTable").bootstrapTable('load', data);
	} 
	table.refresh = function() {
		$("#financeListTable").bootstrapTable('refresh');
	}
	table.getSelections = function() {
		return $("#financeListTable").bootstrapTable('getSelections');
	}
	table.queryParams = function (params) {
		return {};
	}
	return table;
}
