$(function() {
	
	// 异步加载指定图表的配置项和数据
	var $pieChart = echarts.init(document.getElementById("pieChart"));
	var articleChart = new ArticleChart();
	articleChart.queryPieChart($pieChart);
	
	$("#selectMonthBtn").click(function() {
		articleChart.queryPieChart($pieChart);
	});
	
});

var ArticleChart = function() {
	
	this.queryPieChart = function($pieChart) {
		
		var cellSize = [120, 120];
		var month = $("#publishMonth").val();
		var publishMonth = month + "-01";
		
		var scatterData = [];
		var date = +echarts.number.parseDate(publishMonth);
	    var end = +echarts.number.parseDate(getNextMonth(publishMonth));
	    var dayTime = 3600 * 24 * 1000;
	    for (var time = date; time < end; time += dayTime) {
	    	scatterData.push([
	            echarts.format.formatTime('yyyy-MM-dd', time),
	            Math.floor(Math.random() * 10000)
	        ]);
	    }
	    $.post({
	    	url: '/selectArticleStatistic',
	    	data: {"month": month},
	    }).done(function (data) {
	    	var pieChartOption = {
    			title : {
		            text: '文章饼图',
		            subtext: month,
		            x:'center'
		        },
		        tooltip : {},
		        legend: {
		            data: data.legendDatas,
		            bottom: 20
		        },
		        calendar: {
		            top: 'middle',
		            left: 'center',
		            orient: 'vertical',
		            cellSize: cellSize,
		            yearLabel: {
		                show: false,
		                textStyle: {
		                    fontSize: 30
		                }
		            },
		            dayLabel: {
		                margin: 20,
		                firstDay: 1,
		                nameMap: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
		            },
		            monthLabel: {
		                show: false
		            },
		            range: [month]
		        },
		        series: [{
		            id: 'label',
		            type: 'scatter',
		            coordinateSystem: 'calendar',
		            symbolSize: 1,
		            label: {
		                normal: {
		                    show: true,
		                    formatter: function (params) {
		                        return echarts.format.formatTime('dd', params.value[0]);
		                    },
		                    offset: [-cellSize[0] / 2 + 10, -cellSize[1] / 2 + 10],
		                    textStyle: {
		                        color: '#000',
		                        fontSize: 14
		                    }
		                }
		            },
		            data: scatterData
		        }]
	    	};
	    	$pieChart.setOption(pieChartOption);
		    $pieChart.setOption({
	            series: getPieSeries(scatterData, $pieChart, data.pieDatas)
	        });
	    });
	}
	
	getPieSeries = function(scatterData, chart, pieDatas) {
		return echarts.util.map(scatterData, function (item, index) {
	        var center = chart.convertToPixel('calendar', item);
	    	return {
	            id: index + 'pie',
	            type: 'pie',
	            center: center,
	            label: {
	                normal: {
	                    formatter: '{c}',
	                    position: 'inside'
	                }
	            },
	            radius: 50,
	            data: pieDatas[item[0]]
	        };
	    });
	}
	
	getNextMonth = function (date) {
	    var arr = date.split('-');
	    var year = arr[0]; //获取当前日期的年份
	    var month = arr[1]; //获取当前日期的月份
	    var day = arr[2]; //获取当前日期的日
	    var days = new Date(year, month, 0);
	    days = days.getDate(); //获取当前日期中的月的天数
	    var year2 = year;
	    var month2 = parseInt(month) + 1;
	    if (month2 == 13) {
	        year2 = parseInt(year2) + 1;
	        month2 = 1;
	    }
	    var day2 = day;
	    var days2 = new Date(year2, month2, 0);
	    days2 = days2.getDate();
	    if (day2 > days2) {
	        day2 = days2;
	    }
	    if (month2 < 10) {
	        month2 = '0' + month2;
	    }
	    var t2 = year2 + '-' + month2 + '-' + day2;
	    return t2;
	}
}
