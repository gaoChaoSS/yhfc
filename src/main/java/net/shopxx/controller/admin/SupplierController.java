package net.shopxx.controller.admin;


import net.sf.json.JSONObject;
import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.Results;
import net.shopxx.entity.Member;
import net.shopxx.entity.Order;
import net.shopxx.entity.Supplier;
import net.shopxx.service.MemberService;
import net.shopxx.service.OrderService;
import net.shopxx.service.ProductService;
import net.shopxx.service.SupplierService;
import net.shopxx.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import java.util.List;

@Controller
@RequestMapping("/admin/supplier")
public class SupplierController extends BaseController{

    @Autowired
    private SupplierService supplierService ;

    @Autowired
    private ProductService productService ;



    /**
     * 供应商列表
     */
    @GetMapping("/list")
    public String list(String position,String supplierName ,String name,String tel ,Pageable pageable, ModelMap model) {
        //分页查询
        model.addAttribute("page",supplierService.findPage(supplierName,name,position,tel,pageable));
        return "admin/supplier/list";
    }

    /**
     * 添加供应商--页面
     */
    @GetMapping("/add")
    public String addSupplier(ModelMap map){

        return  "admin/supplier/add" ;
    }
    /**
     * 保存供应商
     */
    @PostMapping("/save")
    public ResponseEntity<?> saveSupplier(Supplier supplier){
        if(supplier.getPosition() == null){
            return Results.UNPROCESSABLE_ENTITY;
        }
        if(supplier.getName() == null){
            return Results.UNPROCESSABLE_ENTITY;
        }
        if(supplier.getSupplierName() == null){
            return Results.UNPROCESSABLE_ENTITY;
        }
        if(supplier.getTel() == null){
            return Results.UNPROCESSABLE_ENTITY;
        }
        if (supplier != null){
            supplier.setId(Long.valueOf(StringUtils.getId()));
            supplierService.saveSupplier(supplier);
            return Results.OK;
        }else {
            return Results.BAD_REQUEST;
        }
    }

    /**
     * 修改供应商 -- 页面
     */
    @GetMapping("/edit")
    public String editSupplier(Long id,ModelMap map){
        Supplier supplier = supplierService.find(id);
        map.addAttribute("supplierName",supplier.getSupplierName());
        map.addAttribute("id",id);
        map.addAttribute("name",supplier.getName());
        map.addAttribute("tel",supplier.getTel());
        map.addAttribute("position",supplier.getPosition());
        return "admin/supplier/edit";
    }


    @PostMapping("update")
    public ResponseEntity<?> updateSupplier(Supplier supplier){
        if(supplier.getPosition() == null){
            return Results.UNPROCESSABLE_ENTITY;
        }
        if(supplier.getName() == null){
            return Results.UNPROCESSABLE_ENTITY;
        }
        if(supplier.getSupplierName() == null){
            return Results.UNPROCESSABLE_ENTITY;
        }
        if(supplier.getTel() == null){
            return Results.UNPROCESSABLE_ENTITY;
        }
        supplierService.updateSupplier(supplier);
        return Results.OK;

    }



    @GetMapping("delete")
    public ResponseEntity<?> deleteSupplier(Long id){
        System.err.println("删除id:"+id);
        //supplierService.deleteSupplier(id);
        //删除商品表中相关商品
        productService.deleteBySupplierId(id);
        supplierService.delete(id);
        return Results.OK;
    }
}
