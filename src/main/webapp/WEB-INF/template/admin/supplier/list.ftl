<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="format-detection" content="telephone=no">
    <meta name="author" content="JDSQ Team">
    <meta name="copyright" content="JDSQ">
    <title>${message("admin.order.list")} - Powered By JDSQ</title>
    <link href="${base}/favicon.ico" rel="icon">
    <link href="${base}/resources/common/css/bootstrap.css" rel="stylesheet">
    <link href="${base}/resources/common/css/iconfont.css" rel="stylesheet">
    <link href="${base}/resources/common/css/font-awesome.css" rel="stylesheet">
    <link href="${base}/resources/common/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
    <link href="${base}/resources/common/css/bootstrap-select.css" rel="stylesheet">
    <link href="${base}/resources/common/css/bootstrap-datetimepicker.css" rel="stylesheet">
    <link href="${base}/resources/common/css/bootstrap-fileinput.css" rel="stylesheet">
    <link href="${base}/resources/common/css/summernote.css" rel="stylesheet">
    <link href="${base}/resources/common/css/base.css" rel="stylesheet">
    <link href="${base}/resources/admin/css/base.css" rel="stylesheet">
    <!--[if lt IE 9]>
    <script src="${base}/resources/common/js/html5shiv.js"></script>
    <script src="${base}/resources/common/js/respond.js"></script>
    <![endif]-->
    <script src="${base}/resources/common/js/jquery.js"></script>
    <script src="${base}/resources/common/js/bootstrap.js"></script>
    <script src="${base}/resources/common/js/bootstrap-growl.js"></script>
    <script src="${base}/resources/common/js/bootstrap-select.js"></script>
    <script src="${base}/resources/common/js/moment.js"></script>
    <script src="${base}/resources/common/js/bootstrap-datetimepicker.js"></script>
    <script src="${base}/resources/common/js/bootstrap-fileinput.js"></script>
    <script src="${base}/resources/common/js/summernote.js"></script>
    <script src="${base}/resources/common/js/jquery.nicescroll.js"></script>
    <script src="${base}/resources/common/js/jquery.cookie.js"></script>
    <script src="${base}/resources/common/js/jquery.validate.js"></script>
    <script src="${base}/resources/common/js/jquery.validate.additional.js"></script>
    <script src="${base}/resources/common/js/jquery.form.js"></script>
    <script src="${base}/resources/common/js/underscore.js"></script>
    <script src="${base}/resources/common/js/url.js"></script>
    <script src="${base}/resources/common/js/velocity.js"></script>
    <script src="${base}/resources/common/js/velocity.ui.js"></script>
    <script src="${base}/resources/common/js/base.js"></script>
    <script src="${base}/resources/admin/js/base.js"></script>
[#noautoesc]
    [#escape x as x?js_string]
        <script>
            $().ready(function () {

                var $printModal = $("#printModal");
                var $printSelect = $("#printSelect");
                var $printButton = $("#printButton");
                var $print = $("a.print");
                var orderId;

                // 打印
                $printButton.click(function () {
                    if ($printSelect.val() == "order") {
                        window.open("${base}/admin/print/order?id=" + orderId);
                    }
                });

                // 打印
                $print.click(function () {
                    var $element = $(this);

                    orderId = $element.data("order-id");
                    $printModal.modal();
                });

            });

            /**
             * 删除供应商
             * @param id
             */


            function deleteSupplier(id) {
                $.ajax({
                    type:"GET",
                    url:"${base}/admin/supplier/delete?id="+id,
                    success:function(data){
                       alert("删除成功！")
                    }
                });
                window.location.href="${base}/admin/supplier/list"
            }




        </script>
    [/#escape]
[/#noautoesc]
</head>
<body class="admin">
[#include "/admin/include/main_header.ftl" /]
[#include "/admin/include/main_sidebar.ftl" /]
<main>
    <div class="container-fluid">


    [#--打印模态框开始--]
        <div id="printModal" class="modal fade" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                [#--打印头开始--]
                    <div class="modal-header">
                        <button class="close" type="button" data-dismiss="modal">&times;</button>
                        <h5 class="modal-title">${message("admin.supplier.print")}</h5>[#--打印--]
                    </div>
                [#--打印头结束--]

                [#--打印选择开始--]
                    <div class="modal-body text-center">
                        <select id="printSelect" class="selectpicker">
                            <option value="order">${message("admin.supplier.supplierPrint")}</option>[#--供应商--]
                        </select>
                    </div>
                [#--打印选择结束--]

                [#--打印按钮开始--]
                    <div class="modal-footer">
                        <button id="printButton" class="btn btn-primary"
                                type="button">${message("common.ok")}</button>[#--确定--]
                        <button class="btn btn-default" type="button"
                                data-dismiss="modal">${message("common.cancel")}</button>[#--取消--]
                    </div>
                [#--打印按钮结束--]
                </div>
            </div>
        </div>
    [#--打印模态框结束--]


        <ol class="breadcrumb">
            <li>
                <a href="${base}/admin/index">
                    <i class="iconfont icon-homefill"></i>
                ${message("common.breadcrumb.index")}[#--首页--]
                </a>
            </li>
            <li class="active">${message("admin.supplier.list")}</li>[#--供应商列表--]
        </ol>

    [#--获取供应商数据--]
        <form action="${base}/admin/supplier/list" method="get">
            <input name="pageSize" type="hidden" value="${page.pageSize}">
            <input name="searchProperty" type="hidden" value="${page.searchProperty}">
            <input name="orderProperty" type="hidden" value="${page.orderProperty}">
            <input name="orderDirection" type="hidden" value="${page.orderDirection}">


        [#--更多选项开始--]
           [#-- <div id="filterModal" class="modal fade" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button class="close" type="button" data-dismiss="modal">&times;</button>
                            <h5 class="modal-title">${message("admin.supplier.moreOption")}</h5>--][#--更多选项--][#--
                        </div>
                    --][#--<div class="modal-body form-horizontal">
                        <div class="form-group">
                            <label class="col-xs-3 control-label">${message("Order.type")}:</label>
                            <div class="col-xs-9 col-sm-7">
                                <select name="type" class="selectpicker form-control" data-size="5">
                                    <option value="">${message("common.choose")}</option>
                                    [#list types as value]
                                        <option value="${value}"[#if value == type] selected[/#if]>${message("Order.Type." + value)}</option>
                                    [/#list]
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-xs-3 control-label">${message("Order.status")}:</label>
                            <div class="col-xs-9 col-sm-7">
                                <select name="status" class="selectpicker form-control" data-size="5">
                                    <option value="">${message("common.choose")}</option>
                                    [#list statuses as value]
                                        <option value="${value}"[#if value == status] selected[/#if]>${message("Order.Status." + value)}</option>
                                    [/#list]
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-xs-3 control-label" for="memberUsername">${message("admin.order.memberUsername")}:</label>
                            <div class="col-xs-9 col-sm-7">
                                <input id="memberUsername" name="memberUsername" class="form-control" type="text" value="${memberUsername}" maxlength="200">
                            </div>
                        </div>
                    </div>--][#--
                        <div class="modal-footer">
                            <button class="btn btn-primary" type="submit">${message("common.ok")}</button>
                            <button class="btn btn-default" type="button"
                                    data-dismiss="modal">${message("common.cancel")}</button>
                        </div>
                    </div>
                </div>
            </div>--]
        [#--更多选项结束--]

            <div class="panel panel-default">
                <div class="panel-heading">
                    <div class="row">
                        <div class="col-xs-12 col-sm-9">
                            <div class="btn-group">

                            [#--刷新开始--]
                                <button class="btn btn-default" type="button" data-action="refresh">
                                    <i class="iconfont icon-refresh"></i>
                                ${message("common.refresh")}
                                </button>
                            [#--刷新结束--]

                            [#--添加供应商开始--]
                                <a class="btn btn-default" href="${base}/admin/supplier/add"
                                   data-redirect-url="${base}/admin/supplier/list">
                                    <i class="iconfont icon-add"></i>
                                ${message("common.add")}
                                </a>
                            [#--添加供应商结束--]


                            [#--筛选开始--]
                                <div class="btn-group">
                                    [#--<button class="btn btn-default dropdown-toggle" type="button"
                                            data-toggle="dropdown">
                                    ${message("admin.supplier.filter")}--][#--供应商筛选--][#--
                                        <span class="caret"></span>
                                    </button>--]
                                [#--<ul class="dropdown-menu">
                                    <li[#if isPendingReceive?? && isPendingReceive] class="active"[/#if] data-filter-property="isPendingReceive" data-filter-value="true">
                                        <a href="javascript:;">${message("admin.order.pendingReceive")}</a>
                                    </li>
                                    <li[#if isPendingReceive?? && !isPendingReceive] class="active"[/#if] data-filter-property="isPendingReceive" data-filter-value="false">
                                        <a href="javascript:;">${message("admin.order.unPendingReceive")}</a>
                                    </li>
                                    <li class="divider"></li>
                                    <li[#if isPendingRefunds?? && isPendingRefunds] class="active"[/#if] data-filter-property="isPendingRefunds" data-filter-value="true">
                                        <a href="javascript:;">${message("admin.order.pendingRefunds")}</a>
                                    </li>
                                    <li[#if isPendingRefunds?? && !isPendingRefunds] class="active"[/#if] data-filter-property="isPendingRefunds" data-filter-value="false">
                                        <a href="javascript:;">${message("admin.order.unPendingRefunds")}</a>
                                    </li>
                                    <li class="divider"></li>
                                    <li[#if isAllocatedStock?? && isAllocatedStock] class="active"[/#if] data-filter-property="isAllocatedStock" data-filter-value="true">
                                        <a href="javascript:;">${message("admin.order.allocatedStock")}</a>
                                    </li>
                                    <li[#if isAllocatedStock?? && !isAllocatedStock] class="active"[/#if] data-filter-property="isAllocatedStock" data-filter-value="false">
                                        <a href="javascript:;">${message("admin.order.unAllocatedStock")}</a>
                                    </li>
                                    <li class="divider"></li>
                                    <li[#if hasExpired?? && hasExpired] class="active"[/#if] data-filter-property="hasExpired" data-filter-value="true">
                                        <a href="javascript:;">${message("admin.order.hasExpired")}</a>
                                    </li>
                                    <li[#if hasExpired?? && !hasExpired] class="active"[/#if] data-filter-property="hasExpired" data-filter-value="false">
                                        <a href="javascript:;">${message("admin.order.unexpired")}</a>
                                    </li>
                                </ul>--]
                                </div>
                            [#--筛选结束--]

                            [#--更多选项开始--]
                                [#--<button class="btn btn-default" type="button" data-toggle="modal"
                                        data-target="#filterModal">${message("common.moreOption")}</button>--]
                            [#--更多筛选结束--]

                            [#--每页显示开始--]
                                <div class="btn-group">
                                    <button class="btn btn-default dropdown-toggle" type="button"
                                            data-toggle="dropdown">
                                    ${message("common.pageSize")}
                                        <span class="caret"></span>
                                    </button>
                                    <ul class="dropdown-menu">
                                        <li[#if page.pageSize == 10] class="active"[/#if] data-page-size="10">
                                            <a href="javascript:;">10</a>
                                        </li>
                                        <li[#if page.pageSize == 20] class="active"[/#if] data-page-size="20">
                                            <a href="javascript:;">20</a>
                                        </li>
                                        <li[#if page.pageSize == 50] class="active"[/#if] data-page-size="50">
                                            <a href="javascript:;">50</a>
                                        </li>
                                        <li[#if page.pageSize == 100] class="active"[/#if] data-page-size="100">
                                            <a href="javascript:;">100</a>
                                        </li>
                                    </ul>
                                </div>
                            [#--每页开始结束--]

                            </div>
                        </div>

                    [#--搜索区开始--]
                        <div class="col-xs-12 col-sm-3">
                            <div id="search" class="input-group">
                                <div class="input-group-btn">

                                [#--搜索选项--]
										<button class="btn btn-default" type="button" data-toggle="dropdown">
											[#switch page.searchProperty]
												[#case "supplierName"]
													<span>${message("供应商")}</span>
													[#break /]
												[#case "name"]
													<span>${message("联系人")}</span>
													[#break /]
                                                [#case "tel"]
													<span>${message("电话")}</span>
													[#break /]
												[#default]
													<span>${message("编号")}</span>
											[/#switch]
											<span class="caret"></span>
										</button>
										[#--搜索下拉菜单--]
										<ul class="dropdown-menu">
											<li[#if !page.searchProperty?? || page.searchProperty == "supplierName"] class="active"[/#if] data-search-property="supplierName">
												<a href="javascript:;">${message("供应商")}</a>
											</li>
											<li[#if page.searchProperty == "name"] class="active"[/#if] data-search-property="name">
                                                <a href="javascript:;">${message("联系人")}</a>
											</li>
											<li[#if page.searchProperty == "tel"] class="active"[/#if] data-search-property="tel">
												<a href="javascript:;">${message("电话")}</a>
											</li>
                                            <li[#if page.searchProperty == "id"] class="active"[/#if] data-search-property="id">
												<a href="javascript:;">${message("编号")}</a>
											</li>
										</ul>
                                </div>
                            [#--搜索值--]
                                <input name="searchValue" class="form-control" type="text" value="${page.searchValue}"
                                       placeholder="${message("common.search")}" x-webkit-speech="x-webkit-speech"
                                       x-webkit-grammar="builtin:search">
                                <div class="input-group-btn">
                                    <button class="btn btn-default" type="submit">
                                        <i class="iconfont icon-search"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    [#--搜索区结束--]

                    </div>
                </div>
                <div class="panel-body">
                    <div class="table-responsive">

                    [#--表格区开始--]
                        <table class="table table-hover">
                            <thead>
                            <tr>
                                <th>
                                    <a href="javascript:;" data-order-property="id">
                                    ${message("Supplier.id")}
                                        <i class="iconfont icon-biaotou-kepaixu"></i>
                                    </a>
                                </th>
                                <th>
                                    <a href="javascript:;" data-order-property="supplierName">
                                    ${message("Supplier.supplierName")}
                                        <i class="iconfont icon-biaotou-kepaixu"></i>
                                    </a>
                                </th>
                                <th>
                                    <a href="javascript:;" data-order-property="name">
                                    ${message("Supplier.name")}
                                        <i class="iconfont icon-biaotou-kepaixu"></i>
                                    </a>
                                </th>
                                <th>
                                    <a href="javascript:;" data-order-property="tel">
                                    ${message("Supplier.tel")}
                                        <i class="iconfont icon-biaotou-kepaixu"></i>
                                    </a>
                                </th>
                                <th>
                                    <a href="javascript:;" data-order-property="position">
                                    ${message("Supplier.position")}
                                        <i class="iconfont icon-biaotou-kepaixu"></i>
                                    </a>
                                </th>
                                <th>
                                    <a href="javascript:;" data-order-property="createdDate">
                                    ${message("common.createdDate")}
                                        <i class="iconfont icon-biaotou-kepaixu"></i>
                                    </a>
                                </th>
                                <th>${message("common.action")}</th>
                            </tr>
                            </thead>
                        [#if page.content?has_content]
                            <tbody>
                                [#list page.content as supplier]
                                <tr>
                                    <td>${supplier.id}</td>
                                    <td>${supplier.supplierName}</td>
                                    <td>${supplier.name}</td>
                                    <td>${supplier.tel}</td>
                                    <td>${supplier.position}</td>
                                    <td>${supplier.createdDate}</td>
                                [#--操作--]
                                    <td>
                                    [#--编辑--]
                                        <a class="btn btn-default btn-xs btn-icon"
                                           href="${base}/admin/supplier/edit?id=${supplier.id}"
                                           title="${message("编辑")}" data-toggle="tooltip">
                                            <i class="iconfont icon-edit"></i>
                                        </a>
                                    [#--删除--]
                                        <a class="btn btn-default btn-xs btn-icon" onclick="deleteSupplier(${supplier.id})"
                                           title="${message("删除")}" data-toggle="tooltip">
                                            <i class="iconfont icon-delete"></i>
                                        </a>

                                    </td>
                                </tr>
                                [/#list]
                            </tbody>
                        [/#if]
                        </table>
                    [#--表格区结束--]

                    [#--暂无信息开始--]
                    [#if !page.content?has_content]
                        <p class="no-result">${message("common.noResult")}</p>
                    [/#if]
                    [#--暂无信息结束--]


                    </div>
                </div>
            [@pagination pageNumber = page.pageNumber totalPages = page.totalPages]
                [#if totalPages > 1]
                    <div class="panel-footer text-right">
                        [#include "/admin/include/pagination.ftl" /]
                    </div>
                [/#if]
            [/@pagination]
            </div>
        </form>
    </div>
</main>
</body>
</html>