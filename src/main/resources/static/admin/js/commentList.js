var tale = new $.tale();
$(function() {
	$(".select2").select2({
		width : '90%'
	});
	
	var commentTable = new CommentTable();
	commentTable.init();
	
	$("#selectCommentBtn").click(function() {
		var statuss = $("#statusSelect").val();
		if (statuss)
			statuss = statuss.join(',');
		var data = {
			status : statuss
		};
		tale.post({
			url : '/admin/comments/selectCommentList',
			data : data,
			success : function(result) {
				commentTable.loadTable(result);
			}
		});
	});
});
var CommentTable = function() {
	var table = new Object();
	table.init = function() {
		$("#commentTable").bootstrapTable({
			url : '/admin/comments/selectCommentList', 
			method : 'post', 
			dataType : 'json',
			striped : true, // 是否显示行间隔色
			pagination : true, 
			pageNumber : 1, 
			pageSize : 10, 
			pageList : [ 10, 20, 50], 
			strictSearch : true,
			clickToSelect : true, // 是否启用点击选中行
			idField : "coid", // 每一行的唯一标识，一般为主键列
			columns : [ {
				field : 'coid', visible : false
			}, {
				field : 'cid', visible : false
			}, {
				field : 'content', title : '评论内容', width : '25%', 
				formatter : function(value, row, index) {
					return '<a href="/article/'+row.cid+'/#comments" target="_blank">'+value+'</a>';
				}
			}, {
				field : 'created', title : '评论时间', width : '15%',
				formatter : function(value, row, index) {
					return new Date(value * 1000).Format('yyyy-MM-dd HH:mm:ss');
				}
			}, {
				field : 'author', title : '评论人'
			}, {
				field : 'mail', title : '评论人邮箱'
			}, {
				field : 'url', title : '评论人网址',
				formatter : function(value, row, index) {
					return '<a href="'+value+'" target="_blank">'+value+'</a>';
				}
			}, {
				field : 'status', title : '评论状态', width : '8%',
				formatter : function(value, row, index) {
					if (value == '未读')
						return '<span class="label label-success">未读</span>';
					return '<span class="label label-default">'+value+'</span>';
				}
			}, {
				field : 'oper', title : '操作',
				formatter : function(value, row, index) {
					var oper = new Array(), coid = row.coid;
					oper.push('<a onclick="reply('+coid+');" class="btn btn-primary btn-sm waves-effect waves-light m-b-5"><i class="fa fa-edit"></i> <span>回复</span></a>');
					oper.push('<a onclick="delComment('+coid+')" class="btn btn-danger btn-sm waves-effect waves-light m-b-5"><i class="fa fa-trash-o"></i> <span>删除</span></a>');
	                return oper.join(" ");     
				}
			}, ],
		});
	};
	
	table.loadTable = function(data) {
		$("#commentTable").bootstrapTable('load', data);
	} 
	
	reply = function (coid) {
        swal({
            title: "回复评论",
            text: "请输入你要回复的内容:",
            input: 'text',
            showCancelButton: true,
            confirmButtonText: '回复',
            cancelButtonText: '取消',
            showLoaderOnConfirm: true,
            preConfirm: function (comment) {
                return new Promise(function (resolve, reject) {
                    tale.post({
                        url : '/admin/comments',
                        data: {coid: coid, content: comment},
                        success: function (result) {
                            if(result && result.success){
                                tale.alertOk('已回复');
                            } else {
                                tale.alertError(result.msg || '回复失败');
                            }
                        }
                    });
                })
            },
            allowOutsideClick: false
        });
    }

	delComment = function(coid) {
        tale.alertConfirm({
            title:'确定删除该评论吗?',
            then: function () {
                tale.post({
                    url : '/admin/comments/delete',
                    data: {coid: coid},
                    success: function (result) {
                        if(result && result.success){
                            tale.alertOkAndReload('评论删除成功');
                        } else {
                            tale.alertError(result.msg || '评论删除失败');
                        }
                    }
                });
            }
        });
    }

	updateStatus = function (coid, status) {
        tale.post({
            url : '/admin/comments/status',
            data: {coid: coid, status: status},
            success: function (result) {
                if(result && result.success){
                    tale.alertOkAndReload('评论状态设置成功');
                } else {
                    tale.alertError(result.msg || '评论设置失败');
                }
            }
        });
    }
	return table;
}
