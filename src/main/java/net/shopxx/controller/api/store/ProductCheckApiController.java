package net.shopxx.controller.api.store;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.shopxx.Pageable;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.OrderCountByMember;
import net.shopxx.entity.OrderItem;
import net.shopxx.service.OrderItemService;
import net.shopxx.service.ProductCheckService;
import net.shopxx.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 店铺订单（到货）盘点 控制器
 */

@Controller
@RequestMapping("/api/store/productCheck")
public class ProductCheckApiController extends BaseController {

    @Autowired
    private OrderItemService orderItemService;

    @Inject
    private ProductCheckService productCheckService;


    /**
     * 盘点订单分页查询
     * <p>
     * 店铺  查看昨天 11点之前 订单 对应的商品数量
     *
     * @param storeId   商铺 Id
     * @param goodsName 商品名（不传为全查）
     * @param page      当前第几页  (初始页为 第一页;page = 1)
     * @param
     * @returnlength    每页的条数
     */
    @GetMapping(path = "/productCheckList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> list(Long storeId, String goodsName, int page, int length) {
        try {
            if (page == 0) {
                page = 1;
            }
            if (length <= 0) {
                //默认 10 条
                length = 10;
            }
            //起始下标
            int startIndex = (page - 1) * length;
            //查询 当天之前该商铺 配送（已付款已发货）订单的对应商品 未盘点的各自数量
            List<Map<String, Object>> inventoryList = orderItemService.queryInventoryList(storeId, goodsName, startIndex, length);
            //System.err.println("inventoryList:" + inventoryList);
            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", inventoryList));
        } catch (Exception e) {
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        }
    }


    /**
     * 商品到铺后  店铺填写 实际到货数目
     */
    @GetMapping(path = "/updateProductCheck")
    @ResponseBody
    public ResponseEntity<?> updateProductCheck(int id ,int productCheck) {
        try{
            System.err.println("id:"+id);
            System.err.println("productCheck:"+productCheck);
            //添加对应商品的盘点
            orderItemService.updateProductCheck(id,productCheck);
            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", null));
        }catch (Exception e){
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        }

    }


    /**
     * 测试
     * @param sn
     * @return
     */
    @GetMapping(path = "/selectImg")
    @ResponseBody
    public ResponseEntity<?> selectImg(String sn) {

        String imgBySn = productCheckService.queryImgBySn(sn);

        return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", imgBySn));
    }


}
