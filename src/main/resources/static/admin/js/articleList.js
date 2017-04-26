var tale = new $.tale();
$(document).ready(function () {
	$("#orderBy").bind('change', function() {
		var orderBy = $(this).children('option:selected').val();
		tale.post({
            url : '/admin/article/orderByList',
            data : {orderBy : orderBy},
            success: function (result) {
                if(result && result.success){
                	var articles = result.payload;
                	var tbody = $("table tbody");
                	tbody.html('');
                	$.each(articles, function(i, article) {
                		var cid = article.cid;
                		var html = new Array();
                		html.push('<tr><td><a href="/admin/article/'+cid+'">'+article.title+'</a></td>');
                		var date = new Date(article.created * 1000);
                		html.push('<td>'+date.Format('yyyy-MM-dd HH:mm:ss')+'</td>');
                		html.push('<td>'+article.hits+'</td>');
                		html.push('<td>'+article.categories+'</td>');
                		if (null != article.tags)
                			html.push('<td>'+article.tags+'</td>');
                		else 
                			html.push('<td></td>');
                		if (article.status == 'publish')
                			html.push('<td><span class="label label-success">已发布</span></td>');
                		else
                			html.push('<td><span class="label label-default">草稿</span></td>');
                		html.push('<td><a href="/admin/article/${cid}" class="btn btn-primary btn-sm waves-effect waves-light m-b-5"><i class="fa fa-edit"></i> <span>编辑</span></a>');
                		html.push('<a href="javascript:void(0)" onclick="delPost(${cid});" class="btn btn-danger btn-sm waves-effect waves-light m-b-5"><i class="fa fa-trash-o"></i> <span>删除</span></a>');
                		if (article.status == 'publish')
                			html.push('<a class="btn btn-warning btn-sm waves-effect waves-light m-b-5" href="${permalink(post)}"target="_blank"><i class="fa fa-rocket"></i> <span>预览</span></a>');
                		html.push('</td></tr>');
                		tbody.append(html.join(""));
                	});
                }
            }
        });
	});
});
    
function delPost(cid) {
    tale.alertConfirm({
        title:'确定删除该文章吗?',
        then: function () {
           tale.post({
               url : '/admin/article/delete',
               data: {cid: cid},
               success: function (result) {
                   if(result && result.success){
                       tale.alertOkAndReload('文章删除成功');
                   } else {
                       tale.alertError(result.msg || '文章删除失败');
                   }
               }
           });
       }
    });
}