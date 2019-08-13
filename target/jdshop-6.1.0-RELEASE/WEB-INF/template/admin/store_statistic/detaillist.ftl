<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
	<meta name="format-detection" content="telephone=no">
	<meta name="author" content="JDSQ Team">
	<meta name="copyright" content="JDSQ">
	<title>店铺明细统计 - Powered By JDSQ</title>
	<link href="${base}/favicon.ico" rel="icon">
	<link href="${base}/resources/common/css/bootstrap.css" rel="stylesheet">
	<link href="${base}/resources/common/css/iconfont.css" rel="stylesheet">
	<link href="${base}/resources/common/css/font-awesome.css" rel="stylesheet">
	<link href="${base}/resources/common/css/bootstrap-datetimepicker.css" rel="stylesheet">
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
	<script src="${base}/resources/common/js/jquery.form.js"></script>
	<script src="${base}/resources/common/js/velocity.js"></script>
	<script src="${base}/resources/common/js/velocity.ui.js"></script>
	<script src="${base}/resources/common/js/g2.js"></script>
	<script src="${base}/resources/common/js/base.js"></script>
	<script src="${base}/resources/admin/js/base.js"></script>
	[#noautoesc]
		[#escape x as x?js_string]
			<script>
			$().ready(function() {
				
				var $storeStatisticForm = $("#storeStatisticForm");
				var $type = $("[name='type']");
				var $beginDate = $("[name='beginDate']");
				var $endDate = $("[name='endDate']");
				var dateFormat = "YYYY-MM-DD";
				

				$("#sback").click(function(){

					window.location.href="${base}/admin/store_statistic/list?beginDate=${beginDate?string('yyyy-MM-dd')}&endDate=${endDate?string('yyyy-MM-dd')}";

				})
			
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
				<li class="active">店铺明细统计</li>
			</ol>
			<form id="storeStatisticForm" action="${base}/admin/store_statistic/detail-list" method="get">
				<input name="type" type="hidden" value="${type}">
				<div class="panel panel-default">
					<div class="panel-heading">
						<div class="row">

							<div class="col-xs-12">
								<div class="input-group" data-provide="datetimerangepicker">
									<span class="input-group-addon">
										<em style="font-weight: bold">店铺：</em><label style="color: red">${store.name}</label>
										<em style="font-weight: bold;padding-left: 50px">&nbsp;&nbsp;时间：</em>
										${beginDate?string("yyyy-MM-dd")}&nbsp;&nbsp;至&nbsp;&nbsp;${endDate?string("yyyy-MM-dd")}

									</span>


								</div>

							</div>
							<div class="col-xs-6" style="padding-top: 10px">
                            <span style="color: red">
                                *注释: GMV(总订单金额)：是指已支付订单金额+未支付订单金额+退单金额
                            </span>
							</div>
						</div>
					</div>
					<div class="panel-body">
						<div class="table-responsive">
							<table class="table table-hover" id="data-count-table">
								<thead>
								<tr>
									<th>
										<a href="javascript:void(0);" data-order-property="streoName">
											时间
										</a>
									</th>
									<th>
										<a href="javascript:void(0);" data-order-property="num">
											已支付订单
										</a>
									</th>
									<th>
										<a href="javascript:void(0);" data-order-property="num">
											未支付订单(含退单)
										</a>
									</th>
									<th>
										<a href="javascript:void(0);" data-order-property="num">
											总订单
										</a>
									</th>
									<th>
										<a href="javascript:void(0);" data-order-property="num">
											客单价
										</a>
									</th>
									<th>
										<a href="javascript:void(0);" data-order-property="num">
											GMV(总订单金额)
										</a>
									</th>
									<th>
										<a href="javascript:void(0);" data-order-property="num">
											销售额
										</a>
									</th>
[#--									<th>${message("common.action")}</th>--]

								</tr>
								</thead>
								[#if page.content?has_content]
									<tbody>
									[#list page.content as cstore]
										<tr>
											<td>${cstore.cdate}</td>
											<td style="color: red">${cstore.yxnum}</td>
											<td>${cstore.wxnum}</td>
											<td style="color: red">${cstore.tnums}</td>
											<td>${cstore.coprice}</td>
											<td>${cstore.gmv}</td>
											<td style="color: red">￥${cstore.tpaids}</td>
[#--											<td>--]
[#--												--]
[#--											</td>--]
										</tr>
									[/#list]
									</tbody>
								[/#if]
							</table>
							[#if !page.content?has_content]
								<p class="no-result">${message("common.noResult")}</p>
							[/#if]
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
		<div class="panel-footer">
			<div class="row" style="text-align: center">
				<div class="col-xs-12">
					<button class="btn btn-default" type="button" id="sback"  style="color: #00b6f5">${message("common.back")}</button>
				</div>
			</div>
		</div>
	</main>
</body>
</html>