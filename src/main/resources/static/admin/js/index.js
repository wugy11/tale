$(function() {
	
	$("#publishDate").val(new Date().Format("yyyy-MM"));
	// 异步加载指定图表的配置项和数据
	var $pieChart = echarts.init(document.getElementById("articlePieChart"));
	var articleChart = new ArticleChart();
	articleChart.queryPieChart($pieChart);
	
	$("#selectPublishBtn").click(function() {
		articleChart.queryPieChart($pieChart);
	});
	
});

var ArticleChart = function() {
	
	this.queryPieChart = function($pieChart) {
		var month = $("#publishDate").val();
		$.post({
			url: '/admin/article/statisticPieData',
			data: {"month": month},
		}).done(function(data) {
			var heatmapData = [], lunarData = [], dateList = data.datas;
	        for (var i = 0; i < dateList.length; i++) {
	            heatmapData.push([
	                dateList[i][0],
	                dateList[i][1],
	            ]);
	            lunarData.push([
	                dateList[i][0],
	                1,
	                dateList[i][1]
	            ]);
	        }

	        var option = {
        		tooltip: {
        	        formatter: function (params) {
        	            return '文章发布数: ' + params.value[1];
        	        }
        	    },
	    		title : {
		            text: '文章发布情况',
		            subtext: month,
		            x:'center'
		        },
	            visualMap: {
	                show: false,
	                min: 0,
	                max: 300,
	                calculable: true,
	                seriesIndex: [2],
	                orient: 'horizontal',
	                left: 'center',
	                bottom: 20,
	                inRange: {
	                    color: ['#e0ffff', '#006edd'],
	                    opacity: 0.3
	                },
	                controller: {
	                    inRange: {
	                        opacity: 0.5
	                    }
	                }
	            },
	            calendar: [{
	                left: 'center',
	                top: 'middle',
	                cellSize: [80, 80],
	                yearLabel: {show: false},
	                orient: 'vertical',
	                dayLabel: {
	                    firstDay: 1,
	                    nameMap: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
	                },
	                monthLabel: {
	                    show: false
	                },
	                range: month
	            }],
	            series: [{
	                type: 'scatter',
	                coordinateSystem: 'calendar',
	                symbolSize: 1,
	                label: {
	                    normal: {
	                        show: true,
	                        formatter: function (params) {
	                        	var date = new Date(Date.parse(params.value[0])).Format("MM-dd");
	                            // var date = echarts.number.parseDate(params.value[0]).getDate();
	                            return date + '\n\n' + params.value[2] + '\n\n';
	                        },
	                        textStyle: {
	                        	fontSize: 14,
	                            color: '#000'
	                        }
	                    }
	                },
	                data: lunarData
	            }, {
	                type: 'scatter',
	                coordinateSystem: 'calendar',
	                symbolSize: 1,
	                label: {
	                    normal: {
	                        show: true,
	                        textStyle: {
	                            fontSize: 14,
	                            fontWeight: 700,
	                            color: '#FF0000'
	                        }
	                    }
	                },
	                data: lunarData
	            }, {
	                name: '文章发布数',
	                type: 'heatmap',
	                coordinateSystem: 'calendar',
	                data: heatmapData
	            }, ]
	        };
	        $pieChart.setOption(option);
		});
	}
	
}
