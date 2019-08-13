<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="format-detection" content="telephone=no">
    <meta name="author" content="JDSQ Team">
    <meta name="copyright" content="JDSQ">
    <title>${message("admin.orderStatistic.list")} - Powered By JDSQ</title>
    <link href="${base}/favicon.ico" rel="icon">
    <link href="${base}/resources/common/css/bootstrap.css" rel="stylesheet">
    <link href="${base}/resources/common/css/iconfont.css" rel="stylesheet">
    <link href="${base}/resources/common/css/font-awesome.css" rel="stylesheet">
    <link href="${base}/resources/common/css/bootstrap-datetimepicker.css" rel="stylesheet">
    <link href="${base}/resources/common/css/bootstrap-table.css" rel="stylesheet">
    <link href="${base}/resources/common/css/base.css" rel="stylesheet">
    <link href="${base}/resources/admin/css/base.css" rel="stylesheet">
    <!--[if lt IE 9]>
    <script src="${base}/resources/common/js/html5shiv.js"></script>
    <script src="${base}/resources/common/js/respond.js"></script>
    <![endif]-->
    <script src="${base}/resources/common/js/jquery.js"></script>
    <script src="${base}/resources/common/js/bootstrap.js"></script>
    <script src="${base}/resources/common/js/bootstrap-growl.js"></script>
    <script src="${base}/resources/common/js/bootbox.js"></script>
    <script src="${base}/resources/common/js/moment.js"></script>
    <script src="${base}/resources/common/js/bootstrap-datetimepicker.js"></script>
    <script src="${base}/resources/common/js/jquery.nicescroll.js"></script>
    <script src="${base}/resources/common/js/jquery.form.js"></script>
    <script src="${base}/resources/common/js/jquery.cookie.js"></script>
    <script src="${base}/resources/common/js/underscore.js"></script>
    <script src="${base}/resources/common/js/url.js"></script>
    <script src="${base}/resources/common/js/velocity.js"></script>
    <script src="${base}/resources/common/js/velocity.ui.js"></script>
    <script src="${base}/resources/common/js/g2.js"></script>
    <script src="${base}/resources/common/js/bootstrap-table.js"></script>
    <script src="${base}/resources/common/js/bootstrap-table-zh-CN.js"></script>
    <script src="${base}/resources/common/js/base.js"></script>
    <script src="${base}/resources/admin/js/base.js?t=2019"></script>
    <style>
        #data-count-otable th, td {
            text-align: center;
        }

    </style>

    [#noautoesc]
        [#escape x as x?js_string]
            <script>
                $().ready(function () {

                    var $orderStatisticForm = $("#orderStatisticForm");
                    var $type = $("[name='type']");
                    var $period = $("[name='period']");
                    var $export = $("#export");
                    var $exportToExcelCount = $("#exportToExcelCount");
                    var $exportToExcelBuy = $("#exportToExcelBuy");
                    var $exportToExcelDetail = $("#exportToExcelDetail");
                    var $typeItem = $("[data-type]");
                    var $periodItem = $("[data-period]");
                    var $beginDate = $("[name='beginDate']");
                    var $endDate = $("[name='endDate']");
                    var dateFormat = "YYYY-MM-DD HH:mm:ss";
                    var dateFormatDay = "YYYY-MM-DD";

                    $beginDate.data("DateTimePicker").format(dateFormat);
                    $endDate.data("DateTimePicker").format(dateFormat);

                    // 类型
                    $typeItem.click(function () {
                        var $element = $(this);
                        var type = $element.data("type");

                        $element.addClass("active").siblings().removeClass("active");
                        $type.val(type);

                        loadData();
                    });

                    // 周期
                    $periodItem.click(function () {
                        var $element = $(this);
                        var period = $element.data("period");

                        switch (period) {
                            case "YEAR":
                                dateFormatDay = "YYYY";
                                break;
                            case "MONTH":
                                dateFormatDay = "YYYY-MM";
                                break;
                            default:
                                dateFormatDay = "YYYY-MM-DD";
                        }

                        $element.addClass("active").siblings().removeClass("active");
                        $period.val(period);
                        $beginDate.data("DateTimePicker").format(dateFormatDay);
                        $endDate.data("DateTimePicker").format(dateFormatDay);

                        loadData();
                    });

                    // 日期
                    $beginDate.add($endDate).on("dp.change", function () {
                        loadData();
                        loadToTalData();
                        reloaddata();
                    });

                    // 图表
                    var chart = new G2.Chart({
                        id: "chart",
                        height: 400,
                        forceFit: true
                    });

                    chart.source([], {
                        date: {
                            type: "time",
                            alias: "${message("Statistic.date")}",
                            tickCount: 10,
                            formatter: function (value) {
                                return moment(value).format(dateFormatDay);
                            }
                        },
                        value: {}
                    });
                    chart.line().position("date*value").color("#66baff");
                    chart.render();

                    // 加载数据
                    function loadData() {
                        $orderStatisticForm.ajaxSubmit({
                            success: function (data, textStatus, xhr, $form) {
                                chart.col("value", {
                                    alias: "已支付订单数" //$typeItem.filter(".active").data("type-name")
                                });
                                chart.changeData(data);
                            }
                        });
                    };

                    loadData();

                    // 导出
                    $export.click(function () {
                        setTimeout(function () {
                            chart.downloadImage();
                        }, 1000);
                    });

                    //采购汇总
                    $exportToExcelBuy.click(function () {
                        var begin = $("#beginDate").val()
                        var end = $("#endDate").val()
                        var limit = $("#order_limit_time").val()
                        if (begin.split(/[\s\n]/)[1] != limit) {
                            bootbox.alert("不能修改系统截单时间：" + limit)
                            return;
                        }
                        bootbox.confirm("导出之前确认下时间哈，整错了打屎！！", function (result) {
                            if (result == false) {
                                return;
                            }
                            window.location.href = "${base}/admin/order_buy/exportExcel?beginDate=" + begin + "&endDate=" + end
                        });
                    });

                    // 导出汇总
                    $exportToExcelCount.click(function () {
                        var begin = $("#beginDate").val()
                        var end = $("#endDate").val()
                        var limit = $("#order_limit_time").val()
                        if (begin.split(/[\s\n]/)[1] != limit) {
                            bootbox.alert("不能修改系统截单时间：" + limit)
                            return;
                        }
                        bootbox.confirm("导出之前确认下时间哈，整错了打屎！！", function (result) {
                            if (result == false) {
                                return;
                            }
                            //console.log(begin)
                            //console.log(end)
                            window.location.href = "${base}/admin/order_statistic/exportExcel?beginDate=" + begin + "&endDate=" + end
                        });
                    });

                    // 导出明细
                    $exportToExcelDetail.click(function () {
                        var begin = $("#beginDate").val()
                        var end = $("#endDate").val()
                        var limit = $("#order_limit_time").val()
                        if (begin.split(/[\s\n]/)[1] != limit) {
                            bootbox.alert("不能修改系统截单时间：" + limit)
                            return;
                        }
                        bootbox.confirm("导出之前确认下时间哈，整错了打屎！！", function (result) {
                            if (result == false) {
                                return;
                            }

                            window.location.href = "${base}/admin/order_statistic/exportDetails?beginDate=" + begin + "&endDate=" + end
                        });
                    });


                    //汇总统计
                    function loadToTalData() {

                        $.ajax({
                            url: "${base}/admin/order_statistic/count-total",
                            data: {
                                beginDate: $("#beginDate").val(),
                                endDate: $("#endDate").val()
                            },
                            type: "POST",
                            dataType: "json",
                            cache: false,
                            success: function (data) {
                                if (data != "") {
                                    $("#yxnum").html(data.totalyxnum);
                                    $("#wxnum").html(data.totalwxnum);
                                    $("#ttotalnum").html(data.tnums);
                                }
                            }
                        });

                    };

                    loadToTalData();


                    //列表统计
                    var url = '${base}/admin/order_statistic/data-count';

                    function reloaddata() {


                        var opt = {
                            url: url,
                            silent: true,
                            contentType:"application/x-www-form-urlencoded",
                            query: {
                                beginDate: $("#beginDate").val(),
                                endDate: $("#endDate").val(),
                                type: 1
                            }
                        };

                        $("#data-count-otable").bootstrapTable('refresh', opt);


                    }


                    function loadtabcount() {
                        console.log("pppppp", "2")
                        $('#data-count-otable').bootstrapTable({
                            url: url,
                            pagination: true,
                            method: 'POST',
                            contentType:"application/x-www-form-urlencoded",
                            queryParams: function queryParams(params) {
                                var temp = {
                                    rows: params.limit,                         //页面大小
                                    page: (params.offset / params.limit) + 1,   //页码
                                    sort: params.sort,      //排序列名
                                    sortOrder: params.order, //排位命令（desc，asc）
                                    type: $("#type").val(),
                                    beginDate: $("#beginDate").val(),
                                    endDate: $("#endDate").val()
                                };
                                return temp;
                            },
                            columns: [{
                                field: 'cdate',
                                title: '日期'
                            }, {
                                field: 'yxnum',
                                title: '已支付订单'
                            }, {
                                field: 'wxnum',
                                title: '未支付订单(含退单)'
                            }, {
                                field: 'tnums',
                                title: '总订单'
                            }, {
                                field: 'coprice',
                                title: '客单价'
                            }, {
                                field: 'gmv',
                                title: 'GMV(总订单金额)'
                            }, {
                                field: 'tpaids',
                                title: '销售额'
                            }, {
                                field: '',
                                title: '操作',
                                formatter: function (cell, row, index) {

                                    var cbdate =new Date($("#beginDate").val()).Format("yyyy-MM-dd");

                                    var cedate =new Date($("#endDate").val()).Format("yyyy-MM-dd");


                                    var bdate= row.cdate+ " 00:00:00";
                                    var edate =row.cdate+ " 23:59:59";

                                    if(row.cdate == cbdate){
                                        bdate = $("#beginDate").val();
                                    }

                                    if(row.cdate == cedate){
                                        edate =$("#endDate").val();
                                    }

                                    var html = "<a class=\"btn btn-default btn-xs btn-icon\" href=\"${base}/admin/order_statistic/detaillist?type=2&beginDate="+bdate+"&endDate="+edate+"\" title=\"明细\" data-toggle=\"tooltip\">" +
                                        "<i class=\"iconfont icon-search\"></i>明细</a>";
                                    return html;
                                },

                            }],
                            onLoadSuccess: function (data) {
                                //通过对data判断
                                var ndata = {};
                                if (data != "") {
                                    ndata.total = data.total;
                                    ndata.rows = data.content;
                                    ndata.pageNumber = data.pageable.pageNumber;
                                    ndata.pageSize = data.pageable.pageSize;
                                    ndata.totalRows = data.totalPages;
                                }


                                $("#data-count-otable").bootstrapTable("load", ndata);
                            },

                        })

                    }

                    loadtabcount();


                });
            </script>
        [/#escape]
    [/#noautoesc]
</head>
<body class="admin">
[#include "/admin/include/main_header.ftl" /]
[#include "/admin/include/main_sidebar.ftl" /]
<main>
    <div class="container-fluid">
        <ol class="breadcrumb">
            <li>
                <a href="${base}/admin/index">
                    <i class="iconfont icon-homefill"></i>
                    ${message("common.breadcrumb.index")}
                </a>
            </li>
            <li class="active">${message("admin.orderStatistic.list")}</li>
        </ol>
        <form id="orderStatisticForm" action="${base}/admin/order_statistic/data" method="get">
            <input name="type" type="hidden" value="${type}">
            <input name="period" type="hidden" value="${period}">
            <input name="order_limit_time" id="order_limit_time" type="hidden" value="${order_limit_time}">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <div class="row">
                        <div class="col-xs-12 col-sm-4">
                            <div class="btn-group">
                                [#--<button id="export" class="btn btn-default" type="button">--]
                                [#--<i class="iconfont icon-upload"></i>--]
                                [#--导出图片--]
                                [#--</button>--]
                                <button id="exportToExcelBuy" class="btn btn-default" type="button">
                                    <i class="iconfont icon-upload"></i>
                                    已支付采购单明细
                                </button>
                                <button id="exportToExcelCount" class="btn btn-default" type="button">
                                    <i class="iconfont icon-upload"></i>
                                    已支付仓储汇总单
                                </button>
                                <button id="exportToExcelDetail" class="btn btn-default" type="button">
                                    <i class="iconfont icon-upload"></i>
                                    已支付门店明细单
                                </button>
[#--                                <div class="btn-group">--]
[#--                                    <button class="btn btn-default dropdown-toggle" type="button"--]
[#--                                            data-toggle="dropdown">--]
[#--                                        ${message("admin.orderStatistic.type")}--]
[#--                                        <span class="caret"></span>--]
[#--                                    </button>--]
[#--                                    <ul class="dropdown-menu">--]
[#--                                        [#list types as value]--]
[#--                                            <li[#if value == type] class="active"[/#if] data-type="${value}"--]
[#--                                                                                        data-type-name="${message("Statistic.Type." + value)}">--]
[#--                                                <a href="javascript:;">${message("Statistic.Type." + value)}</a>--]
[#--                                            </li>--]
[#--                                        [/#list]--]
[#--                                    </ul>--]
[#--                                </div>--]
[#--                                <div class="btn-group">--]
[#--                                    <button class="btn btn-default dropdown-toggle" type="button"--]
[#--                                            data-toggle="dropdown">--]
[#--                                        ${message("admin.orderStatistic.period")}--]
[#--                                        <span class="caret"></span>--]
[#--                                    </button>--]
[#--                                    <ul class="dropdown-menu" role="menu">--]
[#--                                        [#list periods as value]--]
[#--                                            <li[#if value == period] class="active"[/#if] data-period="${value}">--]
[#--                                                <a href="javascript:;">${message("Statistic.Period." + value)}</a>--]
[#--                                            </li>--]
[#--                                        [/#list]--]
[#--                                    </ul>--]
[#--                                </div>--]
                            </div>
                        </div>
                        <div class="col-xs-12 col-sm-4">
                            <div class="input-group" data-provide="datetimerangepicker"
                                 data-date-format="YYYY-MM-DD HH:mm:ss">
                                <span class="input-group-addon">${message("common.dateRange")}</span>
                                <input name="beginDate" style="min-width:160px;"  id="beginDate" class="form-control" type="text"
                                       value="${beginDate?string('yyyy-MM-dd HH:mm:ss')}">
                                <span class="input-group-addon">-</span>
                                <input name="endDate" style="min-width:160px;"  id="endDate" class="form-control" type="text"
                                       value="${endDate?string('yyyy-MM-dd HH:mm:ss')}">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="panel-body">
                    <div id="chart"></div>

                </div>
            </div>
        </form>


        <!-- table list  count-->

        <form id="statisticForm2" method="post">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <div class="row">
                        <div class="col-xs-12 col-sm-3">
                            <div class="btn-group">
                                <span>订单统计</span>
                            </div>
                        </div>

                    </div>
                </div>
                <div class="panel-body">
                    <div class="table-responsive">

                        <div class="col-xs-10" style="text-align: center">
                            <table class="table table-hover" style="padding-top: 10px">
                                <thead>
                                <tr>

                                    <th style="text-align: center">
                                        <a href="javascript:void(0);" data-order-property="num">
                                            已支付订单数量
                                        </a>
                                    </th>
                                    <th style="text-align: center">
                                        <a href="javascript:void(0);" data-order-property="num">
                                            未支付订单数量
                                        </a>
                                    </th>
                                    <th style="text-align: center">
                                            总订单数量
                                        </a>
                                    </th>

                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td id="yxnum" style="text-align: center;color: red">&nbsp;</td>
                                    <td id="wxnum" style="text-align: center;color: red">&nbsp;</td>
                                    <td id="ttotalnum" style="text-align: center;color:red">&nbsp;</td>

                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="col-xs-6" style="padding-top: 10px">
                            <span style="color: red">
                                *注释: GMV(总订单金额)：是指已支付订单金额+未支付订单金额+退单金额
                            </span>
                        </div>
                        <table class="table table-hover" id="data-count-otable" style="padding-top: 10px">

                        </table>
                    </div>
                </div>
            </div>
        </form>

    </div>
</main>
</body>
</html>