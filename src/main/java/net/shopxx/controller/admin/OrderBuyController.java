package net.shopxx.controller.admin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import net.shopxx.ExcelView;
import net.shopxx.entity.Order;
import net.shopxx.entity.OrderBuy;
import net.shopxx.entity.OrderBuySheet;
import net.shopxx.entity.Product;
import net.shopxx.entity.ProductCategory;
import net.shopxx.entity.Sku;
import net.shopxx.service.OrderItemService;
import net.shopxx.service.OrderService;
import net.shopxx.service.ProductCategoryService;
import net.shopxx.service.SkuService;
import net.shopxx.service.StatisticService;

/**
 * 	订单统计导出-供应商
 * @author yangli
 *
 */
@Controller("orderBuyController")
@RequestMapping("/admin/order_buy")
public class OrderBuyController  extends BaseController{

	   @Inject
	    private OrderService orderService;
	   
	   @Inject
	    private OrderItemService orderItemService;
	   
	   @Inject
	    private StatisticService statisticService;
	   
	   @Inject
	    private ProductCategoryService productCategoryService;
	   
	   @Inject
	   private SkuService skuService;
	   
	   
	   @GetMapping("/exportExcel")
	    public ModelAndView exportExcel(ModelMap model, Date beginDate, Date endDate) {
		   
		   String filename = "订单采购表" + DateFormatUtils.format(beginDate, "yyyyMMdd") + "_" + DateFormatUtils.format(endDate, "yyyyMMdd") + ".xls";

	        String beginDateFormat = DateFormatUtils.format(beginDate, "yyyy-MM-dd")+ " " + this.order_limit_time;
	        String endDateFormat = DateFormatUtils.format(endDate, "yyyy-MM-dd")+ " " + this.order_limit_time;
	        model.addAttribute("beginDate", beginDateFormat);
	        model.addAttribute("endDate", endDateFormat);
	       
	        
	        List<OrderBuySheet> list = new ArrayList<OrderBuySheet>();
	        OrderBuySheet sheet = null;
	        List<ProductCategory> rootCategory = productCategoryService.findRoots();
	        List<OrderBuy> buyListAll = statisticService.orderBuyList(beginDateFormat, endDateFormat);
	        List<String> sheetName = new ArrayList<String>();
	        for(ProductCategory root:rootCategory)
	        {
	        	sheet = new OrderBuySheet();
	        	
	        	sheet.setRootCategory_id(root.getId());
	        	sheet.setRootCategory(root.getName());
	        	
	        	sheet.setList(this.splitOrderBuy(root, buyListAll));
	        	
	        	list.add(sheet);
	        	
	        	sheetName.add(root.getName());
	        	
//	        	System.out.println(root.getName()+" 数量："+sheet.getList().size());
	        }
		   
	        model.addAttribute("pages", list);
	        model.addAttribute("sheetNames", sheetName);
		   
		   return new ModelAndView(new ExcelView("/admin/order_statistic/orderBuy.xls", filename), model);
	   }
	   
	   private List<OrderBuy> splitOrderBuy(ProductCategory root,List<OrderBuy> list)
	   {
		   List<OrderBuy> buyList = new ArrayList<OrderBuy>(); 
		   Long rootId = root.getId();
		   for(OrderBuy buy:list)
		   { 
			   String treepath = buy.getTreePath();
			   if(StringUtils.isEmpty(treepath))
			   {
				   Sku sku = this.skuService.findBySn(buy.getSn());
				   if(null==sku)
				   {
					   buy.setProductCategory("SKU已被删除");
					   buyList.add(buy);
				   }
				   else
				   {
				   ProductCategory cat = sku.getProduct().getProductCategory();
				   treepath = cat.getTreePath();
				   buy.setProductCategory(cat.getName());
				   buy.setProductCategory_id(BigInteger.valueOf(cat.getId()));
				   }
			   }
			   if(StringUtils.isEmpty(treepath))
			   {
				   continue;
			   }
			   String[] trees = treepath.split(",");
			   for(int i=0;i<trees.length;i++)
			   {
				   String t = trees[i];
				   if(rootId.toString().equals(t))
				   {
//					   System.out.println("是父类的商品"+rootId+" vs "+buy.getTreePath());
					   Integer status = buy.getStatus();
					   if(status==2)
					   {
						   buy.setStatusDesc("已付款(待发货)");
					   }
					   else if(status==3)
					   {
						   buy.setStatusDesc("已发货(待收获)");
					   }
					   else if(status==5)
					   {
						   buy.setStatusDesc("已取货(已完成)");
					   }
					   else
					   {
						   buy.setStatusDesc("未知状态");
					   }
					   buyList.add(buy);
					   
				   }
			   }
		   }
		   return buyList;
	   }
	
}
