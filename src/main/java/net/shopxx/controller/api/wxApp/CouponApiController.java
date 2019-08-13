package net.shopxx.controller.api.wxApp;


import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.shopxx.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.annotation.JsonView;

import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.Coupon;
import net.shopxx.entity.Coupon.CouponView;
import net.shopxx.entity.CouponCode;
import net.shopxx.entity.Member;
import net.shopxx.entity.Store;
import net.shopxx.service.CouponService;
import net.shopxx.service.StoreService;

/**
 * 	优惠券接口
 * @author yangli
 *
 */
@Controller("couponApiController")
@RequestMapping("/api/coupon")
public class CouponApiController extends BaseController{
	
	@Inject
	private CouponService couponService;
	
	@Inject
	private StoreService storeService;

	@Inject
	private MemberService memberService;
	
	/**
	 * 	优惠券列表
	 * @param isExpired 是否有效
	 * @param isHaveUsed 是否包含已用
	 * @return
	 */
	@GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	@JsonView(CouponView.class)
	public ResponseEntity<?> list(Boolean isExpired,Boolean isHaveUsed,HttpServletRequest request)
	{
		try {
			//根据当前购物的店铺查询可用的优惠券
//			Member member=this.getCurrentWxInfo(request);
//			Member member=memberService.find(Long.parseLong("10352"));
			Member member=this.getCurrentWxInfo(request);
			member=memberService.find(member.getId());
//			Member member=memberService.find(Long.parseLong("10352"));
			Store store = member.getBuyStore();
//			Store store = storeService.find(9951L);
			if(null == isExpired || null == isHaveUsed)
			{
				return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, "参数不对！", null));
			}
			if(null == store)
			{
				return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, "用户未选择店铺！", null));
			}
			List<Coupon> list = couponService.findList(store, true, false, isExpired);
			Set<CouponCode> couponCodeList = member.getCouponCodes();//已用的
			if(isHaveUsed)
			{//不可用的包含了已用的
				
				for(CouponCode code:couponCodeList)
				{
					list.add(code.getCoupon());
				}
			}
			if(isExpired)//有效的剔除已用的
			{
				for(CouponCode code:couponCodeList)
				{
					list.remove(code.getCoupon());
				}
			}
			
			
			return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", list));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));
		}
		
	}
	
	

}
