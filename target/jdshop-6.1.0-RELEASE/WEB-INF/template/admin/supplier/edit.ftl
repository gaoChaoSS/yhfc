<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
	<meta name="format-detection" content="telephone=no">
	<meta name="author" content="JDSQ Team">
	<meta name="copyright" content="JDSQ">
	<title>${message("business.product.add")} - Powered By JDSQ</title>
	<link href="${base}/favicon.ico" rel="icon">
	<link href="${base}/resources/common/css/bootstrap.css" rel="stylesheet">
	<link href="${base}/resources/common/css/iconfont.css" rel="stylesheet">
	<link href="${base}/resources/common/css/font-awesome.css" rel="stylesheet">
	<link href="${base}/resources/common/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
	<link href="${base}/resources/common/css/bootstrap-select.css" rel="stylesheet">
	<link href="${base}/resources/common/css/bootstrap-fileinput.css" rel="stylesheet">
	<link href="${base}/resources/common/css/summernote.css" rel="stylesheet">
	<link href="${base}/resources/common/css/base.css" rel="stylesheet">
	<link href="${base}/resources/business/css/base.css" rel="stylesheet">
	<!--[if lt IE 9]>
		<script src="${base}/resources/common/js/html5shiv.js"></script>
		<script src="${base}/resources/common/js/respond.js"></script>
	<![endif]-->
	<script src="${base}/resources/common/js/jquery.js"></script>
	<script src="${base}/resources/common/js/bootstrap.js"></script>
	<script src="${base}/resources/common/js/bootstrap-growl.js"></script>
	<script src="${base}/resources/common/js/bootbox.js"></script>
	<script src="${base}/resources/common/js/bootstrap-select.js"></script>
	<script src="${base}/resources/common/js/sortable.js"></script>
	<script src="${base}/resources/common/js/bootstrap-fileinput.js"></script>
	<script src="${base}/resources/common/js/summernote.js"></script>
	<script src="${base}/resources/common/js/jquery.nicescroll.js"></script>
	<script src="${base}/resources/common/js/jquery.validate.js"></script>
	<script src="${base}/resources/common/js/jquery.validate.additional.js"></script>
	<script src="${base}/resources/common/js/jquery.form.js"></script>
	<script src="${base}/resources/common/js/jquery.cookie.js"></script>
	<script src="${base}/resources/common/js/underscore.js"></script>
	<script src="${base}/resources/common/js/url.js"></script>
	<script src="${base}/resources/common/js/velocity.js"></script>
	<script src="${base}/resources/common/js/velocity.ui.js"></script>
	<script src="${base}/resources/common/js/base.js"></script>
	<script src="${base}/resources/business/js/base.js"></script>
	<style>
		.table tbody tr td {
			vertical-align: top;
		}
	</style>

	<script id="addParameterTemplate" type="text/template">
		<div class="form-group">
			<div class="col-xs-3 col-sm-1 col-sm-offset-1">
				<input name="parameterValues[<%-parameterIndex%>].entries[<%-parameterEntryIndex%>].name" class="parameter-entry-name form-control text-right" type="text" maxlength="200">
			</div>
			<div class="col-xs-6 col-sm-4">
				<input name="parameterValues[<%-parameterIndex%>].entries[<%-parameterEntryIndex%>].value" class="parameter-entry-value form-control" type="text" maxlength="200">
			</div>
			<div class="col-xs-3 col-sm-6">
				<p class="form-control-static">
					<a class="remove" href="javascript:;">[${message("common.delete")}]</a>
				</p>
			</div>
		</div>
	</script>

	[#noautoesc]
		[#escape x as x?js_string]
			<script>
			$().ready(function() {
			    //加载表单
                var $supplierForm = $("#supplierForm");


				// 判断是否为空
				function isEmpty($elements) {
					var isEmpty = true;

					$elements.each(function() {
						var $element = $(this);
						if ($.trim($element.val()) != "") {
							isEmpty = false;
							return false;
						}
					});
					return isEmpty;
				}

				
				// 表单验证
				$supplierForm.validate({
					rules: {
                        supplierName: {
							pattern: /^.{1,20}$/
						},
                        name: {
							pattern: /^.{1,20}$/
						},
                        tel: {
							pattern: /^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\d{8}$/
						},
                        position: {
							pattern:/^.{1,20}$/
						}

					},

					submitHandler: function(form) {
						$(form).ajaxSubmit({
							successRedirectUrl: "${base}/admin/supplier/list"
						});
					}
				});
			
			});
			</script>
		[/#escape]
	[/#noautoesc]
</head>
<body class="business">
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
				<li class="active">${message("business.supplier.add")}</li>
			</ol>
			<form id="supplierForm" class="form-horizontal" action="${base}/admin/supplier/update" method="post">
				<div class="panel panel-default">
					<div class="panel-body">

						[#--标题tab开始--]
						<ul class="nav nav-tabs">
							<li class="active">
								<a href="#base" data-toggle="tab">${message("business.supplier.base")}</a>
							</li>
						</ul>
						[#--标题tab结束--]


						<div class="tab-content">
							[#--属性开始--]
							<div id="base" class="tab-pane active">
								<input id="id" name="id" value="${id}" hidden>

								[#--供应商名--]
								<div class="form-group">
									<label class="col-xs-3 col-sm-2 control-label item-required" for="supplierName">${message("Supplier.supplierName")}:</label>
									<div class="col-xs-9 col-sm-4" title="供应商名，或者公司名" data-toggle="tooltip">
										<input id="supplierName" name="supplierName" value="${supplierName}" class="form-control" type="text" minlength="1" maxlength="200">
									</div>
								</div>
								[#--联系人名--]
								<div class="form-group">
									<label class="col-xs-3 col-sm-2 control-label item-required" for="name">${message("Supplier.name")}:</label>
									<div class="col-xs-9 col-sm-4" title="供应商联系人姓名" data-toggle="tooltip">
										<input id="name" name="name" value="${name}" class="form-control" type="text" minlength="1" maxlength="200">
									</div>
								</div>
								[#--联系人tel--]
								<div class="form-group">
									<label class="col-xs-3 col-sm-2 control-label item-required" for="tel">${message("Supplier.tel")}:</label>
									<div class="col-xs-9 col-sm-4" title="联系人电话" data-toggle="tooltip">
										<input id="tel" name="tel" class="form-control" value="${tel}" type="text" minlength="11" maxlength="16">
									</div>
								</div>
								[#--供应商地址--]
								<div class="form-group">
									<label class="col-xs-3 col-sm-2 control-label item-required" for="position">${message("Supplier.position")}:</label>
									<div class="col-xs-9 col-sm-4" title="供应商详细地址" data-toggle="tooltip">
										<input id="position" name="position" class="form-control" value="${position}" type="text" minlength="1" maxlength="200">
									</div>
								</div>

							</div>
							[#--属性结束--]


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