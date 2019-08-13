<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
	<meta name="format-detection" content="telephone=no">
	<meta name="author" content="JDSQ Team">
	<meta name="copyright" content="JDSQ">
	<title>店铺统计 - Powered By JDSQ</title>
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
	<script src="${base}/resources/common/js/jquery.cookie.js"></script>
	<script src="${base}/resources/common/js/underscore.js"></script>
	<script src="${base}/resources/common/js/url.js"></script>
	<script src="${base}/resources/common/js/velocity.js"></script>
	<script src="${base}/resources/common/js/velocity.ui.js"></script>
	<script src="${base}/resources/common/js/g2.js"></script>
	<script src="${base}/resources/common/js/base.js"></script>
	<script src="${base}/resources/admin/js/base.js"></script>
	<style>
		/*#data-count-otable th, td {*/
		/*	text-align: center;*/
		/*}*/

	</style>
	[#noautoesc]
		[#escape x as x?js_string]
			<script>
			$().ready(function() {
				
				var $storeStatisticForm = $("#storeStatisticForm");
				var $type = $("[name='type']");
				var $beginDate = $("[name='beginDate']");
				var $endDate = $("[name='endDate']");
				var dateFormat = "YYYY-MM-DD";
				
				//
				$("#search").click(function(){

					$storeStatisticForm.submit();

				})

				loadData();

				// 图表
				var Frame = G2.Frame;
				var chart = new G2.Chart({
					id: "chart",
					height: 500,
					forceFit: true,
					plotCfg: {
						margin: [20, 50, 80, 150]
					}
				});

				chart.source([], {
					name: {
						type: "cat",
						alias: "店铺名称"
					},
					value: {}
				});

				chart.axis("name", {
					title: null
				});
				chart.coord("rect").transpose();
				chart.interval().position("name*value").color("#ffab66");
				chart.render();

				// 加载数据
				function loadTabData() {
					$storeStatisticForm.submit();
				};

				// 日期
				// $beginDate.add($endDate).on("dp.change", function () {
				// 	loadData();
				// 	loadTabData();
				// });


				function loadData() {

					$.ajax({
						url: "${base}/admin/store_statistic/count-top",
						data: {
							beginDate: $("#beginDate").val(),
							endDate: $("#endDate").val()
						},
						type: "POST",
						dataType: "json",
						cache: false,
						success: function(data) {
							var frame = new Frame(data);

							chart.col("value", {
								alias: "销售额"
							});
							frame = Frame.sort(frame, "value");
							chart.changeData(frame);
							chart.changeSize(1000, data.length * 40 + 100);
							chart.forceFit();
						}
					});

				};
			
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
				<li class="active">店铺统计</li>
			</ol>
			<form id="storeStatisticForm" action="${base}/admin/store_statistic/list" method="get">
				<input name="type" type="hidden" value="${type}">
				<div class="panel panel-default">
					<div class="panel-heading">
						<div class="row">

							<div class="col-xs-12 col-sm-4">
								<div class="input-group" data-provide="datetimerangepicker"  data-date-format="YYYY-MM-DD HH:mm:ss">
									<span class="input-group-addon">${message("common.dateRange")}</span>
									<input name="beginDate" style="min-width:160px;" class="form-control" type="text" value="${beginDate?string("yyyy-MM-dd HH:mm:ss")}">
									<span class="input-group-addon">-</span>
									<input name="endDate" style="min-width:160px;" class="form-control" type="text" value="${endDate?string("yyyy-MM-dd HH:mm:ss")}">

									<div class="input-group-btn">
										<button class="btn btn-default" type="submit" id="search">
											<i class="iconfont icon-search"></i>
											查询
										</button>
									</div>
								</div>

							</div>
						</div>

						<div class="row">

							<div class="col-xs-12" style="padding-top: 20px">
								<span style="color: red;font-weight: bolder"> 店铺销售TOP 10</span>
							</div>
						</div>
					</div>
					<div class="panel-body">
						<div class="table-responsive">
							<div id="chart"></div>

							<div class="col-xs-6" style="padding-top: 10px">
                            <span style="color: red">
                                *注释: GMV(总订单金额)：是指已支付订单金额+未支付订单金额+退单金额
                            </span>
							</div>

							<table class="table table-hover" id="data-count-table" style="padding-top: 10px">
								<thead>
								<tr>
									<th>
										<a href="javascript:void(0);" data-order-property="streoName">
											店铺名称
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
									<th>${message("common.action")}</th>

								</tr>
								</thead>
								[#if page.content?has_content]
									<tbody>
									[#list page.content as cstore]
										<tr>
											<td>${cstore.name}</td>
											<td style="color: red">${cstore.yxnum}</td>
											<td>${cstore.wxnum}</td>
											<td style="color: red">${cstore.tnums}</td>
											<td>${cstore.coprice}</td>
											<td>${cstore.gmv}</td>
											<td style="color: red">￥${cstore.tpaids}</td>
											<td>
												<a class="btn btn-default btn-xs btn-icon" href="${base}/admin/store_statistic/detail-list?type=4&storeId=${cstore.id}&beginDate=${beginDate?string("yyyy-MM-dd HH:mm:ss")}&endDate=${endDate?string("yyyy-MM-dd HH:mm:ss")}" title="明细" data-toggle="tooltip">
													<i class="iconfont icon-search"></i>
												</a>
											</td>
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
	</main>
</body>
</html>