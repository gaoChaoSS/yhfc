<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="format-detection" content="telephone=no">
    <meta name="author" content="JDSQ Team">
    <meta name="copyright" content="JDSQ">
    <title>${message("admin.registerStatistic.list")} - Powered By JDSQ</title>
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
    <script src="${base}/resources/common/js/moment.js"></script>
    <script src="${base}/resources/common/js/bootstrap-datetimepicker.js"></script>
    <script src="${base}/resources/common/js/jquery.nicescroll.js"></script>
    <script src="${base}/resources/common/js/jquery.cookie.js"></script>
    <script src="${base}/resources/common/js/jquery.form.js"></script>
    <script src="${base}/resources/common/js/velocity.js"></script>
    <script src="${base}/resources/common/js/velocity.ui.js"></script>
    <script src="${base}/resources/common/js/g2.js"></script>
    <script src="${base}/resources/common/js/bootstrap-table.js"></script>
    <script src="${base}/resources/common/js/bootstrap-table-zh-CN.js"></script>
    <script src="${base}/resources/common/js/base.js"></script>
    <script src="${base}/resources/admin/js/base.js"></script>
    <style>
        #data-count-mtable th {
            text-align: center;
        }

    </style>


    [#noautoesc]
        [#escape x as x?js_string]
            <script>
                $().ready(function () {

                    var $registerStatisticForm = $("#registerStatisticForm");
                    var $type = $("[name='type']");
                    var $period = $("[name='period']");
                    var $export = $("#export");
                    var $typeItem = $("[data-type]");
                    var $periodItem = $("[data-period]");
                    var $beginDate = $("[name='beginDate']");
                    var $endDate = $("[name='endDate']");
                    var dateFormat = "YYYY-MM-DD";

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
                                dateFormat = "YYYY";
                                break;
                            case "MONTH":
                                dateFormat = "YYYY-MM";
                                break;
                            default:
                                dateFormat = "YYYY-MM-DD";
                        }

                        $element.addClass("active").siblings().removeClass("active");
                        $period.val(period);
                        $beginDate.data("DateTimePicker").format(dateFormat);
                        $endDate.data("DateTimePicker").format(dateFormat);

                        loadData();
                    });

                    // 日期
                    $beginDate.add($endDate).on("dp.change", function () {
                        loadData();
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
                                return moment(value).format(dateFormat);
                            }
                        },
                        value: {}
                    });
                    chart.line().position("date*value").color("#66baff");
                    chart.render();

                    // 加载数据
                    function loadData() {
                        $registerStatisticForm.ajaxSubmit({
                            success: function (data, textStatus, xhr, $form) {
                                chart.col("value", {
                                    alias: $typeItem.filter(".active").data("type-name")
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


                    //列表
                    var url = '${base}/admin/register_statistic/data-count-list';


                    function reloaddata() {


                        var opt = {
                            url: url,
                            silent: true,
                            query: {
                                beginDate: $beginDate.val(),
                                endDate: $endDate.val(),
                                type: 1
                            }
                        };

                        $("#data-count-mtable").bootstrapTable('refresh', opt);


                    }


                    function loadtabcount() {
                        console.log("pppppp", "2")
                        $('#data-count-mtable').bootstrapTable({
                            url: url,
                            pagination: true,
                            method: 'POST',
                            contentType: "application/x-www-form-urlencoded",
                            queryParams: function queryParams(params) {
                                var temp2 = {
                                    rows: params.limit,                         //页面大小
                                    page: (params.offset / params.limit) + 1,   //页码
                                    sort: params.sort,
                                    sortOrder: params.order,
                                    type: $("#type").val(),
                                    beginDate: $("#beginDate").val(),
                                    endDate: $("#endDate").val()
                                };
                                return temp2;
                            },
                            columns: [{
                                field: 'cdate',
                                title: '日期',

                            }, {
                                field: 'num',
                                title: '会员注册数'
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


                                $("#data-count-mtable").bootstrapTable("load", ndata);
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
            <li class="active">${message("admin.registerStatistic.list")}</li>
        </ol>
        <form id="registerStatisticForm" action="${base}/admin/register_statistic/data" method="get">
            <input name="type" type="hidden" value="${type}">
            <input name="period" type="hidden" value="${period}">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <div class="row">
                        <div class="col-xs-12 col-sm-3">
[#--                            <div class="btn-group">--]
[#--                                <button id="export" class="btn btn-default" type="button">--]
[#--                                    <i class="iconfont icon-upload"></i>--]
[#--                                    ${message("admin.registerStatistic.export")}--]
[#--                                </button>--]
[#--                                <div class="btn-group">--]
[#--                                    <button class="btn btn-default dropdown-toggle" type="button"--]
[#--                                            data-toggle="dropdown">--]
[#--                                        ${message("admin.registerStatistic.type")}--]
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
[#--                                        ${message("admin.registerStatistic.period")}--]
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
[#--                            </div>--]
                        </div>
                        <div class="col-xs-12 col-sm-4">
                            <div class="input-group" data-provide="datetimerangepicker" data-date-format="YYYY-MM-DD">
                                <span class="input-group-addon">${message("common.dateRange")}</span>
                                <input name="beginDate" style="min-width:160px;"  class="form-control" type="text"
                                       value="${beginDate?string("yyyy-MM-dd HH:mm:ss")}">
                                <span class="input-group-addon">-</span>
                                <input name="endDate" style="min-width:160px;"  class="form-control" type="text"
                                       value="${endDate?string("yyyy-MM-dd HH:mm:ss")}">

                            </div>
                        </div>
                    </div>
                </div>
                <div class="panel-body">
                    <div id="chart"></div>
                </div>
            </div>
        </form>


        <form id="registerStatisticForm2" method="post">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <div class="row">
                        <div class="col-xs-12 col-sm-3">
                            <div class="btn-group">
                                <span>会员列表统计</span>
                            </div>
                        </div>

                    </div>
                </div>
                <div class="panel-body">
                    <div class="table-responsive">

                        <div class="col-xs-12" style="text-align: center">
                            <table align="center" class="table table-hover" id="data-count-mtable"
                                   style="padding-top: 10px;text-align: center">

                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </form>


    </div>
</main>
</body>
</html>