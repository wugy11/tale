/**
 * Tale全局函数对象 var tale = new $.tale();
 */
$.extend({
    tale: function () {
    },
    constant:function(){ // 常量池
        return{
            // /-------文件常量----------
            MAX_FILES:10,// 一次队列最大文件数
        }
    }

});

/**
 * 成功弹框
 * 
 * @param options
 */
$.tale.prototype.alertOk = function (options) {
    options = options.length ? {text:options} : ( options || {} );
    options.title = options.title || '操作成功';
    options.text = options.text;
    options.showCancelButton = false;
    options.showCloseButton = false;
    options.type = 'success';
    this.alertBox(options);
};

/**
 * 弹出成功，并在500毫秒后刷新页面
 * 
 * @param text
 */
$.tale.prototype.alertOkAndReload = function (text) {
    this.alertOk({text:text, then:function () {
        setTimeout(function () {
            window.location.reload();
        }, 700);
    }});
};

/**
 * 警告弹框
 * 
 * @param options
 */
$.tale.prototype.alertWarn = function (options) {
    options = options.length ? {text:options} : ( options || {} );
    options.title = options.title || '警告信息';
    options.text = options.text;
    options.timer = 3000;
    options.type = 'warning';
    this.alertBox(options);
};

/**
 * 询问确认弹框，这里会传入then函数进来
 * 
 * @param options
 */
$.tale.prototype.alertConfirm = function (options) {
    options = options || {};
    options.title = options.title || '确定要删除吗？';
    options.text = options.text;
    options.showCancelButton = true;
    options.type = 'question';
    this.alertBox(options);
};

/**
 * 错误提示
 * 
 * @param options
 */
$.tale.prototype.alertError = function (options) {
    options = options.length ? {text:options} : ( options || {} );
    options.title = options.title || '错误信息';
    options.text = options.text;
    options.type = 'error';
    this.alertBox(options);
};

/**
 * 公共弹框
 * 
 * @param options
 */
$.tale.prototype.alertBox = function (options) {
    swal({
        title: options.title,
        text: options.text,
        type: options.type,
        timer: options.timer || 9999,
        showCloseButton: options.showCloseButton,
        showCancelButton: options.showCancelButton,
        showLoaderOnConfirm: options.showLoaderOnConfirm || false,
        confirmButtonColor: options.confirmButtonColor || '#3085d6',
        cancelButtonColor: options.cancelButtonColor || '#d33',
        confirmButtonText: options.confirmButtonText || '确定',
        cancelButtonText: options.cancelButtonText || '取消'
    }).then(function (e) {
        options.then && options.then(e);
    }).catch(swal.noop);
};

/**
 * 全局post函数
 * 
 * @param options
 *            参数
 */
$.tale.prototype.post = function (options) {
    var self = this;
    $.ajax({
        type: 'POST',
        url: options.url,
        data: options.data || {},
        async: options.async || false,
        dataType: 'json',
        success: function (result) {
            self.hideLoading();
            options.success && options.success(result);
        },
        error: function () {
            //
        }
    });
};
/**
 * 处理form请求参数
 */
$.tale.prototype.getParams =  function(formId) {
	var params = $("#" + formId).serializeArray();
	var data = {};
	$.each(params, function (i, param) {
		data[param.name] = param.value;
	});
	return data;
}
/**
 * 自动填充表单
 */
$.tale.prototype.autoFillForm = function(formId, data) {
	var form = $("#" + formId)[0];
	$.each(form, function(i, elem) {
		var name = elem.name;
		var value = data[name];
		if (elem.tagName == 'INPUT') {
			$(this).val(value);
		} else if (elem.tagName == 'SELECT') {
			$(this).val(value);
		}
	});
}
/**
 * 清空表单
 */
$.tale.prototype.clearForm = function (formId) {
	var form = $("#" + formId)[0];
	$.each(form, function(i, elem) {
		var tagName = this.tagName.toLowerCase(), type = this.type;
		if (type == 'text' || type == 'password' || tagName == 'textarea')
            $(this).val("");
		else if (tagName == 'select')
			this.selectedIndex = 0;
	});
}
/**
 * 显示动画
 */
$.tale.prototype.showLoading = function () {
    if ($('#tale-loading').length == 0) {
        $('body').append('<div id="tale-loading"></div>');
    }
    $('#tale-loading').show();
};

/**
 * 隐藏动画
 */
$.tale.prototype.hideLoading = function () {
    $('#tale-loading') && $('#tale-loading').hide();
};
// 日期格式化
Date.prototype.Format = function(fmt) {   
	var o = {   
		"M+" : this.getMonth()+1,                 // 月份
		"d+" : this.getDate(),                    // 日
		"H+" : this.getHours(), 				  // 24小时制
		"h+" : this.getHours()%12 == 0 ? 12 : this.getHours()%12,   // 小时
		"m+" : this.getMinutes(),                 // 分
		"s+" : this.getSeconds(),                 // 秒
		"q+" : Math.floor((this.getMonth()+3)/3), // 季度
		"S"  : this.getMilliseconds()             // 毫秒
	};   
	if(/(y+)/.test(fmt))   
		fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));   
	for(var k in o)   
		if(new RegExp("("+ k +")").test(fmt))   
	fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));   
	return fmt;   
}  
