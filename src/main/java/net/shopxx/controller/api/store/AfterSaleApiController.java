package net.shopxx.controller.api.store;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.shopxx.CommonAttributes;
import net.shopxx.FileType;
import net.shopxx.Pageable;
import net.shopxx.Results;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.*;
import net.shopxx.entity.BaseEntity.BaseView;
import net.shopxx.plugin.OssStoragePlugin;
import net.shopxx.service.*;
import net.shopxx.util.DateUtils;
import net.shopxx.util.OSSUtils;
import net.shopxx.util.RefundUtils;
import net.shopxx.util.StringUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonView;

import javax.inject.Inject;
import javax.mail.Multipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 售后控制器
 */

@Controller
@RequestMapping("/api/store/afterSale")
public class AfterSaleApiController extends BaseController {
    private final static Logger logger = LoggerFactory.getLogger(AfterSaleApiController.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private OSSUtils ossUtils;

    @Autowired
    private MemberService memberService;


    @Autowired
    private AftersalesService aftersalesService;

    @Autowired
    private OssStoragePlugin ossStoragePlugin;


    @Autowired
    private AftersalesItemService aftersalesItemService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private KeyValueService keyValueService ;


    @Inject
    private FileService fileService;

    @Inject
    private ProductImageService productImageService;





    /**
     * 用户的所有可售后订单订单（  售后时间可配置  暂未配置  ）
     *
     *
     *
     * @return
     */
    @GetMapping("/getCanRequestList")
    @ResponseBody
    @JsonView(BaseView.class)
    public ResponseEntity<?> getCanRequestList(int state, HttpServletRequest request, Integer page, Integer length, Pageable pageable) {
        try {



            Long memberId = (long) 10352;
            Member member = (Member) request.getSession().getAttribute("member");

            if (member != null) {
                memberId = member.getId();
                logger.info("memberId:" + memberId);
            }

            if (page == null || page == 0) {
                page = 1;
            }
            if (length == null || length == 0) {
                length = 10;
            }

            String hours = keyValueService.getKey("afterSaleHours");


            //售后申请
            if (state == 0) {
                //查询可退款的订单（已取货的24小时内订单）
                List<Map<String, Object>> list = orderService.queryByMemberId(memberId, hours, page, length);
                ArrayList resultList = new ArrayList();
                for (Map map : list) {
                    String orderId = map.get("orderId").toString();
                    //如果该订单已全额退款不在进行售后
                    Order order = orderService.find(Long.parseLong(orderId));
                    BigDecimal refundAmount = order.getRefundAmount();
                    BigDecimal price = order.getPrice();
                    //退款金额 == 订单金额
                    if (refundAmount.compareTo(price) == 0){
                        logger.info("订单已全额退款，不能进行售后");
                        continue;
                    }else {
                        //查询订单的所有订单项
                        List<Map<String,Object>> itemList = orderItemService.findByOrderId(orderId);
                        System.out.println("itemList:"+itemList);
                        for(int i =0 ;i<itemList.size();i++){
                            HashMap<String, Object> hashMap = new HashMap<>();
                            //订单项Id
                            Long orderItemId = Long.parseLong(itemList.get(i).get("id").toString());

                            OrderItem orderItem = orderItemService.find(orderItemId);
                            //订单项的总购买数
                            Integer quantity = orderItem.getQuantity();
                            //查询已退回数量
                            Integer itemQuantity = aftersalesItemService.queryQuantity(orderItemId);
                            if(quantity>itemQuantity){
                                ArrayList tempList = new ArrayList();
                                hashMap.put("sn", map.get("sn"));
                                hashMap.put("userName", map.get("userName"));
                                hashMap.put("orderId", map.get("orderId"));
                                hashMap.put("phone", map.get("phone"));
                                hashMap.put("memberId", map.get("memberId"));
                                //规格
                                String specification =itemList.get(i).get("specifications").toString();
                                //规格数组
                                ArrayList<String> specificationsList = new ArrayList<>();
                                JSONArray array = JSONArray.fromObject(specification);
                                for(int j = 0;  j< array.size(); j++) {
                                    specificationsList.add(array.get(j).toString() );
                                }
                                //替换为规格数组
                                itemList.get(i).put("specifications",specificationsList);
                                tempList.add(itemList.get(i));
                                hashMap.put("orderItems", tempList);
                                resultList.add(hashMap);
                            }
                        }
                    }



                }
                logger.info("resultList:"+resultList);
                return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", resultList));
            }




            //售后处理
            if (state == 1) {
                member = memberService.find(memberId);
                pageable.setPageNumber(page);
                pageable.setPageSize(length);
                pageable.setOrderProperty("lastModifiedDate");
                pageable.setOrderDirection(net.shopxx.Order.Direction.DESC);
                List<Aftersales> afterSalesList = aftersalesService.findPage(null, null, member, null, pageable).getContent();
                for(Aftersales sales:afterSalesList)
                {
                	Order order = this.orderService.find(sales.getOrderId());
                	sales.setSn(order.getSn());
                }
//                List<Map<String, Object>> recordList = aftersalesService.queryAfterSaleRecord(memberId, page, length);
//                List<Map<String, Object>> recordList2 = recordList;
//
//                for (int i = 0; i < recordList.size(); i++) {
//                    String orderId = recordList.get(i).get("orderId").toString();
//                    System.out.println(orderId);
//                    //查询订单的订单项
//                    List itemList = orderItemService.findByMemberId1(memberId);
//                    recordList2.get(i).put("itemList", itemList);
//                    //将时间戳转为时间
//                    String createdDate = recordList.get(i).get("createdDate").toString();
//                    System.out.println(createdDate);
//
//                    recordList2.get(i).put("createdDate", createdDate);
//                }
                logger.info("afterSalesList:"+afterSalesList);
                return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", afterSalesList));

            }

            //异常
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, null));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

    }


    /**
     * 用户申请售后 根据订单项 查询出对应的商品
     */
    @GetMapping("/applicationAfterSale")
    @ResponseBody
    public ResponseEntity<?> requestAfterSaleList(Long orderItemId) {
        try {
            //进入申请售后页面,用户申请售后选择原因数量的页面
            List<Map<String,Object>> orderItem = orderItemService.findById(orderItemId);
            //总数量
            Integer allQuantity = Integer.valueOf(orderItem.get(0).get("allQuantity").toString());
            //可售后数量
            Object quantity = orderItem.get(0).get("quantity");
            if (null == quantity  ){//没有售后项
                quantity = allQuantity ;
                orderItem.get(0).put("quantity",quantity);
            }
            System.err.println("quantity:"+quantity);
            if (Integer.valueOf(quantity.toString()) == 0){
                return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, "已退:"+allQuantity+"件,没有可退商品！", null));
            }

            //规格
            String specification =orderItem.get(0).get("specifications").toString();
            //规格数组
            ArrayList<String> specificationsList = new ArrayList<>();
            JSONArray array = JSONArray.fromObject(specification);
            for(int j = 0;  j< array.size(); j++) {
                specificationsList.add(array.get(j).toString() );
            }
            //替换为规格数组
            orderItem.get(0).put("specifications",specificationsList);


            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", orderItem.get(0)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

    }

    /**
     * 上传图片返回地址
     *
     * @return
     */
    /*@PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadPic(MultipartFile[] files) {
        try {
            String url = "";
            for (MultipartFile file : files) {
                InputStream inputStream = file.getInputStream();
                String time = new Date().getTime() + "";
                url = ossUtils.upload(time, inputStream);
                String[] split = url.split("[?]");
                //url = split[0];
                System.out.println("图片地址：" + url);

                String newUrl = "http://jdimg-test.jdsq360.com/"+time+"?"+split[1];
                System.out.println("newUrl:"+newUrl);
            }
            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", url));
        } catch (Exception e) {
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

    }*/
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadPic(MultipartFile files) {
        try {
            FileType fileType  = FileType.IMAGE ;
            if (fileType == null || files == null || files.isEmpty()) {
                return Results.UNPROCESSABLE_ENTITY;
            }
            if (!fileService.isValid(fileType, files)) {
                return Results.unprocessableEntity("common.upload.invalid");
            }
           // String url = fileService.upload(fileType, files, false);
           // System.err.println("url:"+url);
           // if (org.apache.commons.lang.StringUtils.isEmpty(url)) {
           //     return Results.unprocessableEntity("common.upload.error");
           // }
            if (files == null || files.isEmpty()) {
                return Results.UNPROCESSABLE_ENTITY;
            }
            if (!fileService.isValid(FileType.IMAGE, files)) {
                return Results.unprocessableEntity("common.upload.invalid");
            }
            ProductImage productImage = productImageService.generate(files);
            String thumbnail = productImage.getThumbnail();
            if (productImage == null) {
                return Results.unprocessableEntity("common.upload.error");
            }
            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", thumbnail));
        } catch (Exception e) {
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

    }





    @RequestMapping("/deletePic")
    @ResponseBody
    public ResponseEntity<?> deletePic(String url) {
        try {
            System.out.println(url);
            //String urlAll = "http://jd-test-public.oss-cn-hangzhou.aliyuncs.com/" + url;
            List list = new ArrayList<>();
            list.add(url);
            ossStoragePlugin.delete(list);
            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", null));
        } catch (Exception e) {
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }


    /**
     * 申请售后原因
     *
     * @return
     */
    @RequestMapping("queryRequestCause")
    @ResponseBody
    public ResponseEntity<?> queryRequestCause() {
        //查询退款原因
        //List<Map<String, Object>> list = aftersalesService.queryRefundCause();
        String value = keyValueService.getKey("afterSaleCause");
        String[] split = value.split(",");

        return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", split));
    }


    /**
     * 保存售后申请
     * @return
     */
    /**
     * @param afterQuantity  售后数量
     * @param orderItemId    订单项id
     * @param afterSaleCause 售后原因
     * @param pic            图片
     * @param des            描述
     * @return
     */
    @RequestMapping("/saveAfterSale")
    @ResponseBody
    public ResponseEntity<?> saveAfterSale(HttpServletRequest request, int afterQuantity, String storeId, String orderItemId, String afterSaleCause, String pic, String des) {
        try {

            Long memberId = null;
            Member member = (Member) request.getSession().getAttribute("member");
            if (member != null) {
                memberId = member.getId();
                logger.info("memberId:" + memberId);
            }

            //查询订单id
            OrderItem orderItem = orderItemService.find(Long.parseLong(orderItemId));
            Long orderId = orderItem.getOrder().getId();
            Long orderStoreId = orderItem.getOrder().getStore().getId();

            ArrayList<String> strings = new ArrayList<>();
            JSONArray jsonArray = JSONArray.fromObject(pic);
            for (int i=0 ; i<jsonArray.size();i++){
                strings.add(CommonAttributes.TX_UPLOAD_PATH +jsonArray.get(i).toString() );
            }
            JSONArray jsonArray1 = JSONArray.fromObject(strings);


            //判断该订单是否存在售后记录，不存在就添加售后记录  存在只添加售后项
            //List<Map<String, Object>> list = aftersalesService.queryAfterSaleRecordByOrderId(orderId);
            //不存在售后记录
//            if (list == null || list.size() == 0) {
//            } else {//存在售后记录时  只添加售后项
//                //售后id
//                String id = list.get(0).get("id").toString();
//                System.out.println("id:" + id);
//
//                //保存售后记录
//                //aftersalesService.saveAfterSaleItems(StringUtils.getId(), orderItemId, id, afterQuantity);
//                AftersalesItem aftersalesItem = new AftersalesItem();
//                aftersalesItem.setOrderItem(orderItemService.find(Long.parseLong(orderItemId)));
//                aftersalesItem.setQuantity(afterQuantity);
//                aftersalesItem.setAftersales(aftersalesService.find(Long.parseLong(id)));
//                aftersalesItemService.save(aftersalesItem);
//
//            }



            //订单项的总购买数
            Integer quantity = orderItem.getQuantity();
            //查询已退回数量
            Integer itemQuantity = aftersalesItemService.queryQuantity(Long.valueOf(orderItemId));

            System.err.println("quantity"+quantity);
            System.err.println("itemQuantity"+itemQuantity);
            if(quantity>itemQuantity){
                Aftersales aftersales = new AftersalesReturns();;
                /*if (afterSaleCause.equals("维修")) {
                    aftersales = new AftersalesRepair();
                } else if (afterSaleCause.equals("换货")) {
                    aftersales = new AftersalesReplacement();
                } else if (afterSaleCause.equals("退货")) {
                    aftersales = new AftersalesReturns();
                } else if (afterSaleCause.equals("退款")) {
                    aftersales = new AftersalesReturns();
                } else {
                    aftersales = new Aftersales();
                }*/

                aftersales.setMember(memberService.find(memberId));
                aftersales.setStatus(Aftersales.Status.PENDING);
                aftersales.setReason(afterSaleCause);
                aftersales.setPicture(jsonArray1.toString());
                aftersales.setDescription(des);
                aftersales.setOrderId(orderId);
                aftersales.setStore(storeService.find(orderStoreId));
                aftersalesService.save(aftersales);


                //保存售后项
                // aftersalesService.saveAfterSaleItems(StringUtils.getId(), orderItemId, afterSaleId, afterQuantity);
                AftersalesItem aftersalesItem = new AftersalesItem();
                aftersalesItem.setOrderItem(orderItemService.find(Long.parseLong(orderItemId)));
                aftersalesItem.setQuantity(afterQuantity);
                aftersalesItem.setAftersales(aftersales);
                aftersalesItemService.save(aftersalesItem);
                return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", null));
            }
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, "无可售后数量", null));
        } catch (Exception e) {
            return ResponseEntity.ok(this.data(HttpStatus.OK, e.getMessage(), null));
        }

    }


}
