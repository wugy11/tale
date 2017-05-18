$(function() {
	
	$("#expenseMonth").val(new Date().Format("yyyy-MM"));
	// 异步加载指定图表的配置项和数据
	var $pieChart = echarts.init(document.getElementById("pieChart"));
	var financeChart = new FinanceChart();
	financeChart.queryPieChart($pieChart);
	
	$("#selectMonthBtn").click(function() {
		financeChart.queryPieChart($pieChart);
	});
	
	var $lineChart = echarts.init(document.getElementById("lineChart"));
	financeChart.loadLineChart($lineChart);
	
});

var FinanceChart = function() {
	
	this.queryPieChart = function($pieChart) {
		
		var cellSize = [100, 100];
		var month = $("#expenseMonth").val();
		var expenseMonth = month + "-01";
		
		var scatterData = [];
		var date = +echarts.number.parseDate(expenseMonth);
	    var end = +echarts.number.parseDate(getNextMonth(expenseMonth));
	    var dayTime = 3600 * 24 * 1000;
	    for (var time = date; time < end; time += dayTime) {
	    	scatterData.push([
	            echarts.format.formatTime('yyyy-MM-dd', time),
	            Math.floor(Math.random() * 10000)
	        ]);
	    }
	    $.post({
	    	url: '/admin/finance/statisticPieData',
	    	data: {"month": month},
	    }).done(function (data) {
	    	var pieChartOption = {
    			title : {
		            text: '资金变动饼图',
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
	
	this.loadLineChart = function ($lineChart) {
		$.post({
			url: '/admin/finance/statisticLineData'
		}).done(function(data) {
			var lineChartOption = {
				title: {
			        text: '资金变动折线图',
			    },
			    tooltip: {
			        trigger: 'axis'
			    },
			    legend: {
			        data: data.legendData
			    },
			    grid: {
			        left: '25%',
			        right: '25%',
			        bottom: '3%',
			        containLabel: true
			    },
			    toolbox: {
			        feature: {
			            saveAsImage: {}
			        }
			    },
			    xAxis: {
			        type: 'category',
			        boundaryGap: false,
			        data: data.xAxisData
			    },
			    yAxis: {
			        type: 'value'
			    },
			    series: data.seriesData	
			};
			$lineChart.setOption(lineChartOption);
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
	            radius: 40,
	            data: pieDatas[item[0]]
	        };
	    });
	}
}
