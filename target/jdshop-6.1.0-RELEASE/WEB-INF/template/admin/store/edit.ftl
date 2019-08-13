<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
	<meta name="format-detection" content="telephone=no">
	<meta name="author" content="JDSQ Team">
	<meta name="copyright" content="JDSQ">
	<title>${message("admin.store.edit")} - Powered By JDSQ</title>
	<link href="${base}/favicon.ico" rel="icon">
	<link href="${base}/resources/common/css/bootstrap.css" rel="stylesheet">
	<link href="${base}/resources/common/css/iconfont.css" rel="stylesheet">
	<link href="${base}/resources/common/css/font-awesome.css" rel="stylesheet">
	<link href="${base}/resources/common/css/bootstrap-select.css" rel="stylesheet">
	<link href="${base}/resources/common/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
	<link href="${base}/resources/common/css/bootstrap-datetimepicker.css" rel="stylesheet">
	<link href="${base}/resources/common/css/bootstrap-fileinput.css" rel="stylesheet">
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
	<script src="${base}/resources/common/js/jquery.lSelect.js"></script>
	<script src="${base}/resources/common/js/bootstrap-datetimepicker.js"></script>
	<script src="${base}/resources/common/js/bootstrap-fileinput.js"></script>
	<script src="${base}/resources/common/js/jquery.nicescroll.js"></script>
	<script src="${base}/resources/common/js/jquery.validate.js"></script>
	<script src="${base}/resources/common/js/jquery.validate.additional.js"></script>
	<script src="${base}/resources/common/js/jquery.form.js"></script>
	<script src="${base}/resources/common/js/jquery.cookie.js"></script>
	<script src="${base}/resources/common/js/underscore.js"></script>
	<script src="${base}/resources/common/js/url.js"></script>
	<script src="${base}/resources/common/js/base.js"></script>
	<script src="${base}/resources/admin/js/base.js"></script>
	[#noautoesc]
		[#escape x as x?js_string]
			<script>
			$().ready(function() {
			
				var $storeForm = $("#storeForm");
				var $areaId = $("input[name='areaId']");
				
				// 地区选择
				$areaId.lSelect({
					url: "${base}/common/area"
				});
				
				// 表单验证
				$storeForm.validate({
					rules: {
						name: {
							required: true,
							remote: {
								url: "${base}/admin/store/check_name?id=${store.id}",
								cache: false
							}
						},
						email: {
							required: true,
							email: true
						},
						mobile: {
							required: true,
							mobile: true
						},
						zipCode: "zipCode",
						address :"required",
						areaId : "required",
						phone: "phone",
						introduction: {
							maxlength: 200
						},
						endDate: "required"
					},
					messages: {
						name: {
							remote: "${message("common.validator.exist")}"
						}
					}
				});
			
			});
			$('#limitTime1').datetimepicker({
    			//minView: "month", 
    			language:  'zh-CN',
    			format: 'HH:mm:ss',
    			todayBtn:  1,
    			autoclose: 1,
			});


			// $('#specSendTime').datetimepicker({
			// 	minView: 0,
			// 	format: 'yyyy-mm-dd HH:mm:ss',
			// 	autoclose: 1,
			// 	startDate:new Date()
			// });
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
				<li class="active">${message("admin.store.edit")}</li>
			</ol>
			<form id="storeForm" class="ajax-form form-horizontal" action="${base}/admin/store/update" method="post">
				<input name="id" type="hidden" value="${store.id}">
				<div class="panel panel-default">
					<div class="panel-heading">${message("admin.store.edit")}</div>
					<div class="panel-body">
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label item-required" for="name">${message("Store.name")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="name" name="name" class="form-control" type="text" value="${store.name}" maxlength="200">
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label">${message("Store.logo")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input name="logo" type="hidden" value="${store.logo}" data-provide="fileinput" data-file-type="IMAGE">
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label">${message("Store.type")}:</label>
							<div class="col-xs-9 col-sm-4">
								<p class="form-control-static">${message("Store.Type." + store.type)}</p>
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label item-required" for="email">${message("Store.email")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="email" name="email" class="form-control" type="text" value="${store.email}" maxlength="200">
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label item-required" for="mobile">${message("Store.mobile")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="mobile" name="mobile" class="form-control" type="text" value="${store.mobile}" maxlength="200">
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label" for="phone">${message("Store.phone")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="phone" name="phone" class="form-control" type="text" value="${store.phone}" maxlength="200">
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label item-required">${message("Store.area")}:</label>
							<div class="col-xs-9 col-sm-10">
								<input name="areaId" type="hidden" value="${(store.area.id)!}" treePath="${(store.area.treePath)!}">
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label" for="address">${message("Store.address")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="address" name="address" class="form-control" type="text" value="${store.address}" maxlength="200">
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label" for="zipCode">${message("Store.zipCode")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="zipCode" name="zipCode" class="form-control" type="text" value="${store.zipCode}" maxlength="200">
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label" for="introduction">${message("Store.introduction")}:</label>
							<div class="col-xs-9 col-sm-4">
								<textarea id="introduction" name="introduction" class="form-control" rows="5">${store.introduction}</textarea>
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label" for="keyword">${message("Store.keyword")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="keyword" name="keyword" class="form-control" type="text" value="${store.keyword}" maxlength="200">
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label">${message("Store.storeRank")}:</label>
							<div class="col-xs-9 col-sm-4">
								<select name="storeRankId" class="selectpicker form-control" data-size="10">
									[#list storeRanks as storeRank]
										<option value="${storeRank.id}"[#if storeRank == store.storeRank] selected[/#if]>${storeRank.name}</option>
									[/#list]
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label">${message("Store.storeCategory")}:</label>
							<div class="col-xs-9 col-sm-4">
								<select name="storeCategoryId" class="selectpicker form-control" data-size="10">
									[#list storeCategories as storeCategory]
										<option value="${storeCategory.id}"[#if storeCategory == store.storeCategory] selected[/#if]>${storeCategory.name}</option>
									[/#list]
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label">${message("Store.productCategories")}:</label>
							<div class="col-xs-9 col-sm-4">
								<select name="productCategoryIds" class="selectpicker form-control" title="${message("common.choose")}" data-size="10" multiple>
									[#list productCategoryTree as productCategory]
										<option value="${productCategory.id}" title="${productCategory.name}"[#if store.productCategories?seq_contains(productCategory)] selected[/#if]>
											[#if productCategory.grade != 0]
												[#list 1..productCategory.grade as i]
													&nbsp;&nbsp;
												[/#list]
											[/#if]
											${productCategory.name}
										</option>
									[/#list]
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label item-required" for="endDate">${message("Store.endDate")}:</label>
							<div class="col-xs-9 col-sm-4">
								<div class="input-group">
									<input id="endDate" name="endDate" class="form-control" type="text" value="${store.endDate?string("yyyy-MM-dd HH:mm:ss")}" data-provide="datetimepicker">
									<span class="input-group-addon">
										<i class="iconfont icon-calendar"></i>
									</span>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label">${message("common.setting")}:</label>
							<div class="col-xs-9 col-sm-4">
								<div class="checkbox">
									<input name="_isEnabled" type="hidden" value="false">
									<input id="isEnabled" name="isEnabled" type="checkbox" value="true"[#if store.isEnabled] checked[/#if]>
									<label for="isEnabled">${message("User.isEnabled")}</label>
								</div>
							</div>
						</div>
						<!--
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label item-required" for="limitTime1">${message("Store.limitTime")}:</label>
							<div class="col-xs-9 col-sm-4">
								<div class="input-group">
									<input id="limitTime1" name="limitTime1" class="form-control" type="text" value="${store.limitTime}" data-provide="datetimepicker">
									<span class="input-group-addon">
										<i class="iconfont icon-calendar"></i>
									</span>
								</div>
							</div>
						</div>
						-->
						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label" for="limitTime">${message("Store.limitTime")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="limitTimeStr" name="limitTimeStr" class="form-control" type="text" value="${store.limitTime}" maxlength="200">
							</div>
						</div>


						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label" for="keyword">${message("Store.sendDesc")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="sendDescDef" name="sendDescDef" class="form-control" value="${store.sendDescDef}" type="text" maxlength="200">
							</div>
						</div>

						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label" for="specSendTime">${message("Store.sendTime")}:</label>
							<div class="col-xs-9 col-sm-4">
								<div class="input-group" >
									<input id="specSendTime" name="specSendTime"  class="form-control" type="text" value="${(store.specSendTime?string("yyyy-MM-dd HH:mm:ss"))!""}" data-provide="datetimepicker" data-date-format="YYYY-MM-DD HH:mm:ss">
									<span class="input-group-addon">
										<i class="iconfont icon-calendar"></i>
									</span>
								</div>
								<span style="color: red;">指定了发货时间，将显示指定的发货说明</span>
							</div>
						</div>

						<div class="form-group">
							<label class="col-xs-3 col-sm-2 control-label" for="keyword">${message("Store.specsendDesc")}:</label>
							<div class="col-xs-9 col-sm-4">
								<input id="specSendDesc" name="specSendDesc" class="form-control" value="${store.specSendDesc}" type="text" maxlength="200">
								<span style="color: red;display: block">请将选择的指定发货时间写入指定发货说明里</span>
							</div>

						</div>

					</div>
					<div class="panel-footer">
						<div class="row">
							<div class="col-xs-9 col-sm-10 col-xs-offset-3 col-sm-offset-2">
								<button class="btn btn-primary" type="submit">${message("common.submit")}</button>
								<button class="btn btn-default" type="button" data-action="back">${message("common.back")}</button>
							</div>
						</div>
					</div>
				</div>
			</form>
		</div>
	</main>
</body>
</html>