package net.shopxx.controller.api.store;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import net.shopxx.entity.Store;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.annotation.JsonView;

import net.shopxx.Filter;
import net.shopxx.Filter.Operator;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.Order;
import net.shopxx.entity.OrderCountByMember.orderCountByMember;
import net.shopxx.service.OrderService;
import net.shopxx.service.StoreService;
import net.shopxx.util.DateUtils;

/**
 * 店铺订单统计
 * @author yangli
 *
 */
@Controller("storeOrderCount")
@RequestMapping("/api/store/storeOrder")
public class StoreOrderCountApiController extends BaseController{
	
	@Inject
	private OrderService orderService;
	@Inject
	private StoreService storeService;
	
	@GetMapping(path = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(orderCountByMember.class)
	public ResponseEntity<?> count(Long storeId)
	{
		try {
			Store store=storeService.find(storeId);
//			Filter store = new Filter();
//			store.setProperty("store");
//			store.setValue(storeService.find(storeId));
//			store.setOperator(Operator.EQ);
//
//			Filter orderStatus = new Filter();
//			orderStatus.setProperty("status");
//			orderStatus.setValue(Order.Status.SHIPPED);
//			orderStatus.setOperator(Operator.EQ);
//
//
//			Long unReceived = orderService.count(store, orderStatus);//待取
//
//			Filter orderCompleted = new Filter();
//			orderCompleted.setProperty("status");
//			orderCompleted.setValue(Order.Status.COMPLETED);
//			orderCompleted.setOperator(Operator.EQ);
//
//
//			Filter startTime = new Filter();
//			startTime.setProperty("createdDate");
//			startTime.setValue(DateUtils.getMonthFirstDay());
//			startTime.setOperator(Operator.GE);
//
//
//			Filter endTime = new Filter();
//			endTime.setProperty("createdDate");
//			endTime.setValue(DateUtils.getMonthLastDay());
//			endTime.setOperator(Operator.LE);
//
//
//			Filter today = new Filter();
//			today.setProperty("lastModifiedDate");
//			DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
//			today.setValue(format1.parse(DateUtils.getToday()));
//			today.setOperator(Operator.EQ);
//
//			Long completed = orderService.count(store, orderCompleted, today);//已取
//			Long totalMonth = orderService.count(store, startTime, endTime);//当月总单


			Map<String, Object> map = new HashMap<String, Object>();
			map.put("unReceived", orderService.findStoreDayNoTakeCount(store));
			map.put("completed", orderService.findStoreDayTakeCount(store));
			map.put("totalMonth", orderService.findStoreMonthCount(store));

			return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", map));
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
		}
	}
	

}
