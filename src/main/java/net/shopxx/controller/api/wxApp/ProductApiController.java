package net.shopxx.controller.api.wxApp;

import com.fasterxml.jackson.annotation.JsonView;
import net.shopxx.Order;
import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.Product;
import net.shopxx.entity.Product.ProductView;
import net.shopxx.entity.ProductCategory;
import net.shopxx.entity.ProductCategory.ProductCategoryView;
import net.shopxx.entity.Sku;
import net.shopxx.entity.Store;
import net.shopxx.service.ProductCategoryService;
import net.shopxx.service.ProductService;
import net.shopxx.service.SkuService;
import net.shopxx.service.StoreService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 產品相關接口
 *
 * @author yangli
 */
@Controller("productApiController")
@RequestMapping("/api/product")
public class ProductApiController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(ProductApiController.class);


    @Inject
    private ProductService productService;

    @Inject
    private ProductCategoryService productCategoryService;

    @Inject
    private StoreService storeService;

    @Inject
    private SkuService skuService;

    /**
     * 产品分类接口
     *
     * @param id      類型id
     * @param request
     * @return
     */
    @GetMapping(path = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(ProductCategoryView.class)
    public ResponseEntity<?> category(String id, HttpServletRequest request, Integer storeId) {
        try {
            Store store = storeService.find(Long.parseLong(storeId + ""));
            if (!StringUtils.isNumeric(id)) {
                //	返回一級分類
                List<ProductCategory> productCategoryList = productCategoryService.findRoots();
                List<ProductCategory> result = new ArrayList<>();

                for (ProductCategory p : productCategoryList) {

                    Page<Product> pro = productService.findPage(null, null, store, p, null, null, null, null, null, null, null, null, true, true, null, true, null, null, null, null, null);
                    long total = pro.getTotal();
//					List<ProductCategory> ps= productCategoryService.findChildren(p.getId(),true,99,true);
//					List<Product> pros=productService.findByStoreAndCategory(store,ps);
                    if (total > 0) {
                        result.add(p);
                    }
                }
                return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", result));
            } else {

                List<ProductCategory> productCategoryList = productCategoryService.findChildren(Long.parseLong(id), true, 99, true);
                List<ProductCategory> result = new ArrayList<>();
                for (ProductCategory p : productCategoryList) {
                    Page<Product> pro = productService.findPage(null, null, store, p, null, null, null, null, null, null, null, null, true, true, null, true, null, null, null, null, null);
                    long total = pro.getTotal();
                    if (total > 0) {
                        result.add(p);
                    }
                }
                return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", result));

            }

        } catch (Exception e) {
            logger.error("获取产品分类接口失败, storeId,{},id,{}", storeId, id, e);
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        }
    }

    /**
     * 产品列表
     *
     * @param pageSize=20
     * @param searchProperty=name or   productCategory.id
     * @param searchValue=123
     * @param orderProperty=id
     * @param orderDirection=ASC  or  DESC
     * @param eg:                 pageSize=20&searchProperty=name&orderProperty=&orderDirection=ASC&searchValue=test1
     * @return
     */
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(ProductCategoryView.class)
    public ResponseEntity<?> list(Pageable pageable, Long category_id, Integer storeId, HttpServletRequest request) {
        try {
            ProductCategory productCategory = null;
//			System.out.println("商品搜索名称还未编码——————————————————————"+pageable.getSearchValue());
//			if(pageable.getSearchValue() != null){
//				byte[] a=pageable.getSearchValue().getBytes("ISO-8859-1");
//				String searchValue=new String(a,"UTF-8");
//				pageable.setSearchValue(searchValue);
//				System.out.println("商品搜索名称还已编码——————————————————————"+pageable.getSearchValue());
//			}
            Store store = storeService.find(Long.valueOf(storeId + ""));
            if (null != category_id)
                productCategory = productCategoryService.find(category_id);
            String pro = pageable.getOrderProperty();
            if (StringUtils.isEmpty(pro) || "sales".equals(pro)) {
                pageable.setOrderProperty("sort");
                pageable.setOrderDirection(Order.Direction.DESC);
            }
//			Page<Product> page = productService.findPage(pageable);
            Page<Product> page = productService.findPage(null, null, store, productCategory == null ? null : productCategory, null, null, null, null, null, null, null, null, true, null, null, true, null, null, null, null, pageable);
            List<Product> productList = page.getContent();
            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", productList));
        } catch (Exception e) {
            logger.error("获取产品列表失败, storeId,{},category_id,{}", storeId, category_id, e);
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        }
    }

    /**
     * 產品詳情
     *
     * @param pageable
     * @param id
     * @return
     */
    @GetMapping(path = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(ProductView.class)
    public ResponseEntity<?> detail(Pageable pageable, Long id, Long skuId) {
        try {
            Product product = null;
            if (null != id) {
                product = productService.find(id);
            }

            if (null != skuId) {
                Sku sku = skuService.find(skuId);
                product = sku.getProduct();
            }
            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", product));

        } catch (Exception e) {

            logger.error("获取产品详情失败, productId,{},skuId,{}", id, skuId, e);

            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));

        }
    }

}
