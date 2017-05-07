var tale = new $.tale();
$(function() {
	$(".select2").select2({
		width : '60%'
	});
	
	var articleTable = new ArticleTable();
	articleTable.init();
	
	$("#selectArticleBtn").click(function() {
		var categorys = $("#categorySelect").val();
		if (categorys)
			categorys = categorys.join(',');
		var data = {
			title : $("#articleTitle").val(),
			categorys : categorys
		};
		tale.post({
			url : '/admin/article/selectArticleList',
			data : data,
			success : function(result) {
				articleTable.loadTable(result);
			}
		});
	});
});
var ArticleTable = function() {
	var table = new Object();
	table.init = function() {
		$("#articleTable").bootstrapTable({
			url : '/admin/article/selectArticleList', 
			method : 'post', 
			dataType : 'json',
			striped : true, // 是否显示行间隔色
			pagination : true, 
//			queryParams : table.queryParams,
			pageNumber : 1, 
			pageSize : 10, 
			pageList : [ 10, 20, 50], 
			strictSearch : true,
			clickToSelect : true, // 是否启用点击选中行
			idField : "cid", // 每一行的唯一标识，一般为主键列
			columns : [ {
				field : 'cid', visible : false
			}, {
				field : 'title', title : '文章标题', width : '25%', 
				formatter : function(value, row, index) {
					return '<a target="_blank" href="/article/'+row.cid+'">'+value+'</a>';
				}
			}, {
				field : 'created', title : '发布时间', width : '15%',
				formatter : function(value, row, index) {
					return new Date(value * 1000).Format('yyyy-MM-dd HH:mm:ss');
				}
			}, {
				field : 'hits', title : '浏览量'
			}, {
				field : 'categories', title : '所属分类'
			}, {
				field : 'tags', title : '所属标签'
			}, {
				field : 'status', title : '发布状态', width : '8%',
				formatter : function(value, row, index) {
					if (value == 'publish')
						return '<span class="label label-success">已发布</span>';
					return '<span class="label label-default">草稿</span>';
				}
			}, {
				field : 'oper', title : '操作',
				formatter : function(value, row, index) {
					var oper = new Array(), cid = row.cid;
					oper.push('<a href="/admin/article/'+cid+'" class="btn btn-primary btn-sm waves-effect waves-light m-b-5"><i class="fa fa-edit"></i> <span>编辑</span></a>');
					oper.push('<a id="delete" onclick="delArticle('+cid+')" class="btn btn-danger btn-sm waves-effect waves-light m-b-5"><i class="fa fa-trash-o"></i> <span>删除</span></a>');
	                /*if(row.status == 'publish') {
	                	oper.push('<a class="btn btn-warning btn-sm waves-effect waves-light m-b-5" href="/article/'+cid+'" target="_blank"><i class="fa fa-rocket"></i> <span>查看</span></a>');
	                }*/
	                return oper.join(" ");     
				}
			}, ],
		});
	};
	
	table.loadTable = function(data) {
		$("#articleTable").bootstrapTable('load', data);
	} 
	
	delArticle = function(articleId) {
		tale.alertConfirm({
			title : '确定删除该文章吗?',
			then : function() {
				tale.post({
					url : '/admin/article/delete',
					data : {cid : articleId },
					success : function(result) {
						if (result && result.success) {
							tale.alertOkAndReload('文章删除成功');
						} else {
							tale.alertError(result.msg || '文章删除失败');
						}
					}
				});
			}
		});
	}
	return table;
}
