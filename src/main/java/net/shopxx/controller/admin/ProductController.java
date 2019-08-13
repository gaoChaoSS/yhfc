/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: bxul9H0O1V+Qz0+3rGnadeLUAuJmYw9G
 */
package net.shopxx.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import net.shopxx.entity.*;
import net.shopxx.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.shopxx.FileType;
import net.shopxx.Pageable;
import net.shopxx.Results;
import net.shopxx.controller.business.ProductController.SkuForm;
import net.shopxx.controller.business.ProductController.SkuListForm;
import net.shopxx.exception.UnauthorizedException;
import net.shopxx.security.CurrentStore;

/**
 * Controller - 商品
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Controller("adminProductController")
@RequestMapping("/admin/product")
public class ProductController extends BaseController {

	@Inject
	private ProductService productService;
	@Inject
	private ProductCategoryService productCategoryService;
	@Inject
	private BrandService brandService;
	@Inject
	private ProductTagService productTagService;
	@Inject
	private StoreService storeService;
	@Inject
	private StoreProductCategoryService storeProductCategoryService;
	@Inject
	private PromotionService promotionService;
	@Inject
	private StoreProductTagService storeProductTagService;
	@Inject
	private SpecificationService specificationService;
	@Inject
	private ProductImageService productImageService;
	@Inject
	private ParameterValueService parameterValueService;
	@Inject
	private SpecificationItemService specificationItemService;
	@Inject
	private AttributeService attributeService;
	@Inject
	private FileService fileService;
	@Inject
	private SkuService skuService;

	@Inject
	private SupplierService supplierService ;
	
	@Inject
	private OrderService orderService;
	
	/**
	 * 添加属性
	 */
	@ModelAttribute
	public void populateModel(Long productId, Long productCategoryId, Long storeId, ModelMap model) {
		
		
		Product product = productService.find(productId);
		
//		Store currentStore = this.storeService.find(storeId);
		
//		if(null == storeId && product !=null)
//		{
//			
//			currentStore = product.getStore();
//		}
		
		
//		if (product != null && !currentStore.equals(product.getStore())) {
//			throw new UnauthorizedException();
//		}
		ProductCategory productCategory = productCategoryService.find(productCategoryId);
//		if (productCategory != null && !storeService.productCategoryExists(currentStore, productCategory)) {
//			throw new UnauthorizedException();
//		}

		model.addAttribute("product", product);
		
		model.addAttribute("productCategory", productCategory);
	}
	

	/**
	 * 添加
	 */
	@GetMapping("/add")
	public String add(@CurrentStore Store currentStore, ModelMap model) {
		model.addAttribute("maxProductImageSize", Product.MAX_PRODUCT_IMAGE_SIZE);
		model.addAttribute("maxParameterValueSize", Product.MAX_PARAMETER_VALUE_SIZE);
		model.addAttribute("maxParameterValueEntrySize", ParameterValue.MAX_ENTRY_SIZE);
		model.addAttribute("maxSpecificationItemSize", Product.MAX_SPECIFICATION_ITEM_SIZE);
		model.addAttribute("maxSpecificationItemEntrySize", SpecificationItem.MAX_ENTRY_SIZE);
		model.addAttribute("types", Product.Type.values());
		model.addAttribute("productCategoryTree", productCategoryService.findTree());
		model.addAttribute("allowedProductCategories", productCategoryService.findList(currentStore, null, null, null));
//		model.addAttribute("allowedProductCategoryParents", getAllowedProductCategoryParents(currentStore));
		model.addAttribute("allowedProductCategoryParents",productCategoryService.findTree());
//		model.addAttribute("storeProductCategoryTree", storeProductCategoryService.findTree(currentStore));//页面上没用，屏蔽
		model.addAttribute("brands", brandService.findAll());
//		model.addAttribute("promotions", promotionService.findList(null, currentStore, true));
		model.addAttribute("promotions", null);
		model.addAttribute("productTags", productTagService.findAll());
//		model.addAttribute("storeProductTags", storeProductTagService.findList(currentStore, true));
		model.addAttribute("storeProductTags", null);
		model.addAttribute("specifications", specificationService.findAll());
		model.addAttribute("stores", storeService.findAll());
		model.addAttribute("suppliers",supplierService.findAll());
		return "admin/product/add";
	}
	
	/**
	 * 保存
	 */
	@PostMapping("/save")
	public ResponseEntity<?> save(@ModelAttribute(name = "productForm") Product productForm, @ModelAttribute(binding = false) ProductCategory productCategory, SkuForm skuForm, SkuListForm skuListForm, Long brandId, Long[] promotionIds, Long[] productTagIds, Long[] storeProductTagIds,
			Long storeProductCategoryId, HttpServletRequest request, Long[] storeIds,Long supplierId) {

		List<Store> storeList = null;
		
		if(null == storeIds)
		{
			return Results.UNPROCESSABLE_ENTITY; 
		}
		if(storeIds.length>0&&storeIds[0]==0) //所有店铺
		{
			storeList = storeService.findList(null, Store.Status.SUCCESS, true, false, 0, 999);
			productForm.setIsAllStore(true);
		}
		else
		{
			storeList = storeService.findList(storeIds);
			productForm.setIsAllStore(false);
		}
		
		if(null ==storeList || storeList.size()<=0)
		{
			return Results.UNPROCESSABLE_ENTITY; 
		}



		productImageService.filter(productForm.getProductImages());
		parameterValueService.filter(productForm.getParameterValues());
		specificationItemService.filter(productForm.getSpecificationItems());
		skuService.filter(skuListForm.getSkuList());
		
		

		/**
		 *   跟店铺类型相关的判断暂时不用，后期针对加盟店有特殊需求的时候再做修改
		 *   by yangli 2019-07-20
		Long productCount = productService.count(null, currentStore, null, null, null, null, null, null);
		if (currentStore.getStoreRank() != null && currentStore.getStoreRank().getQuantity() != null && productCount >= currentStore.getStoreRank().getQuantity()) {
			return Results.unprocessableEntity("business.product.addCountNotAllowed", currentStore.getStoreRank().getQuantity());
		}
		if (productCategory == null) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (storeProductCategoryId != null) {
			StoreProductCategory storeProductCategory = storeProductCategoryService.find(storeProductCategoryId);
			if (storeProductCategory == null || !currentStore.equals(storeProductCategory.getStore())) {
				return Results.UNPROCESSABLE_ENTITY;
			}
			productForm.setStoreProductCategory(storeProductCategory);
		}
		**/
		
		
		Set<Store> set = new HashSet<Store>();
		set.addAll(storeList);
		
		productForm.setStores(set);
		productForm.setProductCategory(productCategory);
		productForm.setBrand(brandService.find(brandId));
		productForm.setPromotions(new HashSet<>(promotionService.findList(promotionIds)));
		productForm.setProductTags(new HashSet<>(productTagService.findList(productTagIds)));
		productForm.setStoreProductTags(new HashSet<>(storeProductTagService.findList(storeProductTagIds)));
		productForm.setMaxBuy(productForm.getMaxBuy()==null?0:productForm.getMaxBuy());
		productForm.setSupplier(supplierService.find(supplierId));

		productForm.removeAttributeValue();
		for (Attribute attribute : productForm.getProductCategory().getAttributes()) {
			String value = request.getParameter("attribute_" + attribute.getId());
			String attributeValue = attributeService.toAttributeValue(attribute, value);
			productForm.setAttributeValue(attribute, attributeValue);
		}

		if (!isValid(productForm, BaseEntity.Save.class)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (StringUtils.isNotEmpty(productForm.getSn()) && productService.snExists(productForm.getSn())) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (productForm.hasSpecification()) {
			List<Sku> skus = skuListForm.getSkuList();
			if (CollectionUtils.isEmpty(skus) || !isValid(skus, getValidationGroup(productForm.getType()), BaseEntity.Save.class)) {
				return Results.UNPROCESSABLE_ENTITY;
			}
			productService.create(productForm, skus);
		} else {
			Sku sku = skuForm.getSku();
			if (sku == null || !isValid(sku, getValidationGroup(productForm.getType()), BaseEntity.Save.class)) {
				return Results.UNPROCESSABLE_ENTITY;
			}
			productService.create(productForm, sku);
		}

		return Results.OK;
	}
	
	/**
	 * 编辑
	 */
	@GetMapping("/edit")
	public String edit(@ModelAttribute(binding = false) Product product, @CurrentStore Store currentStore, ModelMap model) {
		if (product == null) {
			return UNPROCESSABLE_ENTITY_VIEW;
		}

		model.addAttribute("maxProductImageSize", Product.MAX_PRODUCT_IMAGE_SIZE);
		model.addAttribute("maxParameterValueSize", Product.MAX_PARAMETER_VALUE_SIZE);
		model.addAttribute("maxParameterValueEntrySize", ParameterValue.MAX_ENTRY_SIZE);
		model.addAttribute("maxSpecificationItemSize", Product.MAX_SPECIFICATION_ITEM_SIZE);
		model.addAttribute("maxSpecificationItemEntrySize", SpecificationItem.MAX_ENTRY_SIZE);
		model.addAttribute("types", Product.Type.values());
		model.addAttribute("productCategoryTree", productCategoryService.findTree());
		model.addAttribute("allowedProductCategories", productCategoryService.findList(currentStore, null, null, null));
//		model.addAttribute("allowedProductCategoryParents", getAllowedProductCategoryParents(currentStore));
		model.addAttribute("allowedProductCategoryParents",productCategoryService.findTree());
//		model.addAttribute("storeProductCategoryTree", storeProductCategoryService.findTree(currentStore));
		model.addAttribute("brands", brandService.findAll());
//		model.addAttribute("promotions", promotionService.findList(null, currentStore, true));
		model.addAttribute("promotions", null);
		model.addAttribute("productTags", productTagService.findAll());
//		model.addAttribute("storeProductTags", storeProductTagService.findList(currentStore, true));
		model.addAttribute("storeProductTags", null);
		model.addAttribute("specifications", specificationService.findAll());
		model.addAttribute("product", product);
		model.addAttribute("stores", storeService.findAll());
		model.addAttribute("suppliers",supplierService.findAll());
		
		return "admin/product/edit";
	}
	
	/**
	 * 更新
	 */
	@PostMapping("/update")
	public ResponseEntity<?> update(@ModelAttribute("productForm") Product productForm, @ModelAttribute(binding = false) Product product, @ModelAttribute(binding = false) ProductCategory productCategory, SkuForm skuForm, SkuListForm skuListForm, Long brandId, Long[] promotionIds,
			Long[] productTagIds, Long[] storeProductTagIds, Long storeProductCategoryId, HttpServletRequest request,Long[] storeIds,Long supplierId) {
		
		List<Store> storeList = null;
		productImageService.filter(productForm.getProductImages());
		parameterValueService.filter(productForm.getParameterValues());
		specificationItemService.filter(productForm.getSpecificationItems());
		skuService.filter(skuListForm.getSkuList());
		if (product == null) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (productCategory == null) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		List<Promotion> promotions = promotionService.findList(promotionIds);
		
//		if (CollectionUtils.isNotEmpty(promotions)) {
//			if (currentStore.getPromotions() == null || !currentStore.getPromotions().containsAll(promotions)) {
//				return Results.UNPROCESSABLE_ENTITY;
//			}
//		}
		if (storeProductCategoryId != null) {
			StoreProductCategory storeProductCategory = storeProductCategoryService.find(storeProductCategoryId);
//			if (storeProductCategory == null || !currentStore.equals(storeProductCategory.getStore())) {
//				return Results.UNPROCESSABLE_ENTITY;
//			}
			productForm.setStoreProductCategory(storeProductCategory);
		}
		
		if(storeIds.length>0&&storeIds[0]==0) //所有店铺
		{
			storeList = storeService.findList(null, Store.Status.SUCCESS, true, false, 0, 999);
			productForm.setIsAllStore(true);
		}
		else
		{
			storeList = storeService.findList(storeIds);
			productForm.setIsAllStore(false);
		}
		
		if(null ==storeList || storeList.size()<=0)
		{
			return Results.UNPROCESSABLE_ENTITY; 
		}
		Set<Store> set = new HashSet<Store>();
		set.addAll(storeList);
		
		productForm.setStores(set);
		
		productForm.setId(product.getId());
		productForm.setType(product.getType());
		productForm.setIsActive(true);
		productForm.setProductCategory(productCategory);
		productForm.setBrand(brandService.find(brandId));
		productForm.setPromotions(new HashSet<>(promotions));
		productForm.setProductTags(new HashSet<>(productTagService.findList(productTagIds)));
		productForm.setStoreProductTags(new HashSet<>(storeProductTagService.findList(storeProductTagIds)));
		productForm.setSupplier(supplierService.find(supplierId));
		productForm.removeAttributeValue();
		for (Attribute attribute : productForm.getProductCategory().getAttributes()) {
			String value = request.getParameter("attribute_" + attribute.getId());
			String attributeValue = attributeService.toAttributeValue(attribute, value);
			productForm.setAttributeValue(attribute, attributeValue);
		}

		if (!isValid(productForm, BaseEntity.Update.class)) {
			return Results.UNPROCESSABLE_ENTITY;
		}

		if (productForm.hasSpecification()) {
			List<Sku> skus = skuListForm.getSkuList();
			if (CollectionUtils.isEmpty(skus) || !isValid(skus, getValidationGroup(productForm.getType()), BaseEntity.Update.class)) {
				return Results.UNPROCESSABLE_ENTITY;
			}
			productService.modify(productForm, skus);
		} else {
			Sku sku = skuForm.getSku();
			if (sku == null || !isValid(sku, getValidationGroup(productForm.getType()), BaseEntity.Update.class)) {
				return Results.UNPROCESSABLE_ENTITY;
			}
			productService.modify(productForm, sku);
		}

		return Results.OK;
	}
	
	/**
	 * 上传商品图片
	 */
	@PostMapping("/upload_product_image")
	public ResponseEntity<?> uploadProductImage(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (!fileService.isValid(FileType.IMAGE, file)) {
			return Results.unprocessableEntity("common.upload.invalid");
		}
		ProductImage productImage = productImageService.generate(file);
		if (productImage == null) {
			return Results.unprocessableEntity("common.upload.error");
		}
		return ResponseEntity.ok(productImage);
	}
	

	/**
	 * 删除商品图片
	 */
	@PostMapping("/delete_product_image")
	public ResponseEntity<?> deleteProductImage() {
		return Results.OK;
	}
	
	/**
	 * 获取参数
	 */
	@GetMapping("/parameters")
	public @ResponseBody List<Map<String, Object>> parameters(@ModelAttribute(binding = false) ProductCategory productCategory) {
		List<Map<String, Object>> data = new ArrayList<>();
		if (productCategory == null || CollectionUtils.isEmpty(productCategory.getParameters())) {
			return data;
		}
		for (Parameter parameter : productCategory.getParameters()) {
			Map<String, Object> item = new HashMap<>();
			item.put("group", parameter.getGroup());
			item.put("names", parameter.getNames());
			data.add(item);
		}
		return data;
	}
	

	/**
	 * 获取规格
	 */
	@GetMapping("/specifications")
	public @ResponseBody List<Map<String, Object>> specifications(@ModelAttribute(binding = false) ProductCategory productCategory) {
		List<Map<String, Object>> data = new ArrayList<>();
		if (productCategory == null || CollectionUtils.isEmpty(productCategory.getSpecifications())) {
			return data;
		}
		for (Specification specification : productCategory.getSpecifications()) {
			Map<String, Object> item = new HashMap<>();
			item.put("name", specification.getName());
			item.put("options", specification.getOptions());
			data.add(item);
		}
		return data;
	}
	
	/**
	 * 检查编号是否存在
	 */
	@GetMapping("/check_sn")
	public @ResponseBody boolean checkSn(String sn) {
		return StringUtils.isNotEmpty(sn) && !productService.snExists(sn);
	}

	/**
	 * 获取属性
	 */
	@GetMapping("/attributes")
	public @ResponseBody List<Map<String, Object>> attributes(@ModelAttribute(binding = false) ProductCategory productCategory) {
		List<Map<String, Object>> data = new ArrayList<>();
		if (productCategory == null || CollectionUtils.isEmpty(productCategory.getAttributes())) {
			return data;
		}
		for (Attribute attribute : productCategory.getAttributes()) {
			Map<String, Object> item = new HashMap<>();
			item.put("id", attribute.getId());
			item.put("name", attribute.getName());
			item.put("options", attribute.getOptions());
			data.add(item);
		}
		return data;
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(Product.Type type, Long productCategoryId, Long brandId, Long productTagId, Boolean isActive, Boolean isMarketable, Boolean isList, Boolean isTop, Boolean isOutOfStock, Boolean isStockAlert, Pageable pageable, ModelMap model) {
		ProductCategory productCategory = productCategoryService.find(productCategoryId);
		Brand brand = brandService.find(brandId);
		ProductTag productTag = productTagService.find(productTagId);

		model.addAttribute("types", Product.Type.values());
		model.addAttribute("productCategoryTree", productCategoryService.findTree());
		model.addAttribute("brands", brandService.findAll());
		model.addAttribute("productTags", productTagService.findAll());
		model.addAttribute("type", type);
		model.addAttribute("productCategoryId", productCategoryId);
		model.addAttribute("brandId", brandId);
		model.addAttribute("productTagId", productTagId);
		model.addAttribute("isMarketable", isMarketable);
		model.addAttribute("isList", isList);
		model.addAttribute("isTop", isTop);
		model.addAttribute("isActive", isActive);
		model.addAttribute("isOutOfStock", isOutOfStock);
		model.addAttribute("isStockAlert", isStockAlert);
		model.addAttribute("page", productService.findPage(type, null, null, productCategory, null, brand, null, productTag, null, null, null, null, isMarketable, isList, isTop, isActive, isOutOfStock, isStockAlert, null, null, pageable));
		return "admin/product/list";
	}

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	public ResponseEntity<?> delete(Long[] ids) {
		for (Long id : ids) {
			Product product = productService.find(id);
			Long count = orderService.count(null, null, null, null, product, null, null, null, null, null, null);
			if(count<=0)
			{
				productService.delete(id);
			}
		}
		
		return Results.OK;
	}

	/**
	 * 上架商品
	 */
	@PostMapping("/shelves")
	public ResponseEntity<?> shelves(Long[] ids) {
		if (ids != null) {
			for (Long id : ids) {
				Product product = productService.find(id);
				if (product == null) {
					return Results.UNPROCESSABLE_ENTITY;
				}
//				if (!storeService.productCategoryExists(product.getStore(), product.getProductCategory())) {
//					return Results.unprocessableEntity("admin.product.marketableNotExistCategoryNotAllowed", product.getName());
//				}
			}
			productService.shelves(ids);
		}
		return Results.OK;
	}

	/**
	 * 下架商品
	 */
	@PostMapping("/shelf")
	public ResponseEntity<?> shelf(Long[] ids) {
		productService.shelf(ids);
		return Results.OK;
	}
	
	/**
	 * 获取允许发布商品分类上级分类
	 * 
	 * @param store
	 *            店铺
	 * @return 允许发布商品分类上级分类
	 */
	private Set<ProductCategory> getAllowedProductCategoryParents(Store store) {
		Assert.notNull(store, "[Assertion failed] - store is required; it must not be null");

		Set<ProductCategory> result = new HashSet<>();
//		List<ProductCategory> allowedProductCategories = productCategoryService.findList(store, null, null, null);
		List<ProductCategory> allowedProductCategories = productCategoryService.findTree();
		for (ProductCategory allowedProductCategory : allowedProductCategories) {
			result.addAll(allowedProductCategory.getParents());
		}
		return result;
	}
	
	/**
	 * 根据类型获取验证组
	 * 
	 * @param type
	 *            类型
	 * @return 验证组
	 */
	private Class<?> getValidationGroup(Product.Type type) {
		Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");

		switch (type) {
		case GENERAL:
			return Sku.General.class;
		case EXCHANGE:
			return Sku.Exchange.class;
		case GIFT:
			return Sku.Gift.class;
		}
		return null;
	}



	/**
	 * 更新排序sort字段
	 */
	@GetMapping("/updateSort")
	@ResponseBody
	public JSONObject updateSort(String sn , Integer sort){
		System.err.println("sn:"+sn);
		System.err.println("sort:"+sort);
		JSONObject jsonObject = new JSONObject();
		try {
			if(sort == null){
				sort = 0 ;
			}
			if(sort>50000){
				sort = 50000 ;
			}
			productService.updateSort(sn,sort);
			jsonObject.put("bool","ok");

			return jsonObject ;
		}catch (Exception e){
			System.err.println("更新sort失败");
			jsonObject.put("bool","false");
			return jsonObject ;
		}

	}

}