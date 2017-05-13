!function ($) {
    "use strict";
    //$.MoltranApp.init();
}(window.jQuery);

$(function() {
	var dayChart = echarts.init(document.getElementById('dayChart'));
	// 指定图表的配置项和数据
	var option = {
	    title: {
	        text: 'ECharts 入门示例'
	    },
	    tooltip: {},
	    legend: {
	        data:['销量']
	    },
	    xAxis: {
	        data: ["衬衫","羊毛衫","雪纺衫","裤子","高跟鞋","袜子"]
	    },
	    yAxis: {},
	    series: [{
	        name: '销量',
	        type: 'bar',
	        data: [5, 20, 36, 10, 10, 20]
	    }]
	};

	// 使用刚指定的配置项和数据显示图表。
	dayChart.setOption(option);
	
});
var monthChart = echarts.init(document.getElementById("monthChart"));
var app = {};
option = null;
var cellSize = [140, 140];
var pieRadius = 60;

function getVirtulData() {
    var date = +echarts.number.parseDate('2017-02-01');
    var end = +echarts.number.parseDate('2017-03-01');
    var dayTime = 3600 * 24 * 1000;
    var data = [];
    for (var time = date; time < end; time += dayTime) {
        data.push([
            echarts.format.formatTime('yyyy-MM-dd', time),
            Math.floor(Math.random() * 10000)
        ]);
    }
    return data;
}

function getPieSeries(scatterData, chart) {
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
            radius: pieRadius,
            data: [
                {name: '工作', value: Math.round(Math.random() * 24)},
                {name: '娱乐', value: Math.round(Math.random() * 24)},
                {name: '睡觉', value: Math.round(Math.random() * 24)}
            ]
        };
    });
}

function getPieSeriesUpdate(scatterData, chart) {
    return echarts.util.map(scatterData, function (item, index) {
        var center = chart.convertToPixel('calendar', item);
        return {
            id: index + 'pie',
            center: center
        };
    });
}

var scatterData = getVirtulData();

option = {
    tooltip : {},
    legend: {
        data: ['工作', '娱乐', '睡觉'],
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
        range: ['2017-02']
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

if (!app.inNode) {
    var pieInitialized;
    setTimeout(function () {
        pieInitialized = true;
        monthChart.setOption({
            series: getPieSeries(scatterData, monthChart)
        });
    }, 10);

    app.onresize = function () {
        if (pieInitialized) {
            monthChart.setOption({
                series: getPieSeriesUpdate(scatterData, monthChart)
            });
        }
    };
};
if (option && typeof option === "object") {
    monthChart.setOption(option, true);
}