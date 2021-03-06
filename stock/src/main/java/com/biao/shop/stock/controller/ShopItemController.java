package com.biao.shop.stock.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.biao.shop.common.bo.OrderBo;
import com.biao.shop.common.bo.ShopItemEntityBo;
import com.biao.shop.common.dto.ShopItemEntityDto;
import com.biao.shop.common.entity.ShopClientEntity;
import com.biao.shop.common.entity.ShopItemEntity;
import com.biao.shop.common.enums.RespStatusEnum;
import com.biao.shop.common.response.ObjectResponse;
import com.biao.shop.stock.manager.ShopItemManager;
import com.biao.shop.stock.service.ShopItemService;
import com.github.pagehelper.PageInfo;
import javassist.expr.Instanceof;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.dromara.soul.client.common.annotation.SoulClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author XieXiaobiao
 * @since 2020-01-06
 */
@RestController
@RequestMapping("/stock")
@MapperScan(basePackages = "com.biao.shop.common.dao")
public class ShopItemController {

    private ShopItemService shopItemService;

    private ShopItemManager shopItemManager;

    @Autowired
    public ShopItemController(ShopItemService shopItemService,ShopItemManager shopItemManager){
        this.shopItemService = shopItemService;
        this.shopItemManager = shopItemManager;
    }

    @SoulClient(path = "/vehicle/stock/item/del", desc = "删除一个商品")
    // 由于soul网关使用中发现无法支持DELETE转发，只能改为使用GET
    // @DeleteMapping("/item/del")
    @GetMapping("/item/del")
    public int deleteItem(@RequestParam("ids") String ids){
        if (ids.contains(",")){
            List<Integer> list = new ArrayList<>(8);
            String[] strings = ids.split(",");
            for (int i = 0; i < strings.length; i++) {
                list.add(Integer.valueOf(strings[i]));
            }
            return shopItemService.deleteBatchItems(list);
        }
        return shopItemService.deleteById(Integer.valueOf(ids));
    }

    /** ShopItemEntityBo内使用List<String>来对应接收前端的String数组，只需名字一致即可自动解析，</>*/
    @SoulClient(path = "/vehicle/stock/item/update", desc = "更新一个商品")
    @PostMapping("/item/update")
    public int updateItem(@RequestBody ShopItemEntityBo itemEntityBo) {
        // 使用json解析方式获取http的内容，当然也可以使用spring的自动解析更简单,与下面的方法对比：addItem(@RequestBody String jsonStr)
        /*System.out.println(jsonStr);
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        ShopItemEntity itemEntity = new ShopItemEntity();
        itemEntity.setIdItem((Integer) jsonObject.get("idItem"));
        String brandName = (String) jsonObject.get("brandName");
        itemEntity.setBrandName(StringUtils.isEmpty(brandName)? null : brandName);
        itemEntity.setCategory((String) jsonObject.get("category"));//
        itemEntity.setItemName((String) jsonObject.get("itemName"));//
        String purPrice = (String) jsonObject.get("purchasePrice");
        itemEntity.setPurchasePrice( StringUtils.isEmpty(purPrice)? new BigDecimal("0.00") : new BigDecimal(purPrice));
        String sellPrice = (String) jsonObject.get("sellPrice");
        itemEntity.setSellPrice( StringUtils.isEmpty(sellPrice)? new BigDecimal("0.00") : new BigDecimal(sellPrice));
        String specification = (String) jsonObject.get("specification");
        itemEntity.setSpecification(StringUtils.isEmpty(specification) ? null : specification);
        itemEntity.setItemUuid((String) jsonObject.get("itemUuid"));//
        String description = (String) jsonObject.get("description");
        itemEntity.setDescription(StringUtils.isEmpty(description)? null : description);
        Boolean shipment = (Integer) jsonObject.get("shipment") == 1;
        itemEntity.setShipment(shipment);*/
        return shopItemService.updateItemDto(itemEntityBo);
    }

    @SoulClient(path = "/vehicle/stock/item/**", desc = "由id查询一个商品")
    @GetMapping("/item/{id}")
    public ShopItemEntity queryById(@PathVariable("id") String id) {
        return shopItemService.queryById(id);
    }
    /*@PathVariable其他格式的：*/
    //@GetMapping("/path/{id}/name")
    //@SoulClient(path = "/test/path/**/name", desc = "test restful风格支持")

    @SoulClient(path = "/vehicle/stock/item/uid/**", desc = "由uid查询一个商品")
    @GetMapping("/item/uid/{uid}")
    public ObjectResponse<ShopItemEntityDto> queryByUId(@PathVariable("uid") String uid) {
        ShopItemEntityDto itemEntityDto = shopItemManager.queryByUId(uid);
        ObjectResponse<ShopItemEntityDto> response = new ObjectResponse<>();
        response.setCode(RespStatusEnum.SUCCESS.getCode());
        response.setMessage(RespStatusEnum.SUCCESS.getMessage());
        response.setData(itemEntityDto);
        return response;
    }

    @SoulClient(path = "/vehicle/stock/item/save", desc = "新增商品")
    @PostMapping("/item/save")
    public ObjectResponse<Integer> addItem(@RequestBody ShopItemEntityBo itemEntityBo) throws MQClientException {
        int result = shopItemService.saveItemDto(itemEntityBo);
        ObjectResponse<Integer> response = new ObjectResponse<>();
        response.setCode(RespStatusEnum.SUCCESS.getCode());
        response.setMessage(RespStatusEnum.SUCCESS.getMessage());
        response.setData(result);
        return response;
    }

    @SoulClient(path = "/vehicle/stock/item/categories", desc = "商品类别列表")
    @GetMapping("/item/categories")
    public ObjectResponse<List<String>> listCategory(){
        List<String> list = shopItemService.listCategory(1,100);
        ObjectResponse<List<String>> response = new ObjectResponse<>();
        response.setCode(RespStatusEnum.SUCCESS.getCode());
        response.setMessage(RespStatusEnum.SUCCESS.getMessage());
        response.setData(list);
        return response;
    }
//    public int addItem(@RequestBody ShopItemEntity  itemEntity) {
//        /*JSONObject jsonObject = JSONObject.parseObject(jsonStr);
//        ShopItemEntity itemEntity = new ShopItemEntity();
//        String brand = (String) jsonObject.get("brandName");
//        itemEntity.setBrandName(StringUtils.isEmpty(brand)? null : brand);
//        itemEntity.setCategory((String) jsonObject.get("category"));//
//        itemEntity.setItemName((String) jsonObject.get("itemName"));//
//        String purPrice = (String) jsonObject.get("purchasePrice");
//        itemEntity.setPurchasePrice( StringUtils.isEmpty(purPrice)? new BigDecimal("0.00") : new BigDecimal(purPrice));
//        String price = (String) jsonObject.get("sellPrice");
//        itemEntity.setSellPrice( StringUtils.isEmpty(price)? new BigDecimal("0.00") : new BigDecimal(price));
//        String specification = (String) jsonObject.get("specification");
//        itemEntity.setSpecification(StringUtils.isEmpty(specification)? null : specification);
//        itemEntity.setItemUuid((String) jsonObject.get("itemUuid"));//
//        String description = (String) jsonObject.get("description");
//        itemEntity.setDescription(StringUtils.isEmpty(description)? null : description);
//        Boolean shipment = ((Integer) jsonObject.get("shipment") == 1);
//        itemEntity.setShipment(shipment);*/
//        return shopItemService.addItem(itemEntity);
//    }

    //
/*    @SoulClient(path = "/vehicle/stock/brand/list", desc = "获取品牌列表")
    @GetMapping("/brand/list")
    public Page<String> listBrand(@RequestParam("pageNum") int current,@RequestParam("pageSize")int size){
        return shopItemService.listBrand(current,size);
    }*/

    /*@SoulClient(path = "/vehicle/stock/category/list", desc = "获取类别列表")
    @GetMapping("/category/list")
    public List<String> listCategory(@RequestParam("pageNum") int current,@RequestParam("pageSize")int size){
        return shopItemService.listCategory(current,size);
    }*/

    @SoulClient(path = "/vehicle/stock/item/list", desc = "获取商品列表")
    @GetMapping("/item/list")
    public ObjectResponse<Page<ShopItemEntityDto>> listItem(@RequestParam("pageNum") int current, @RequestParam("pageSize")int size,
                                                @RequestParam(value = "itemName",required = false) String itemName,
                                                @RequestParam(value = "itemUuid",required = false)String itemUuid,
                                                @RequestParam(value = "category",required = false) String category,
                                                @RequestParam(value = "brandName",required = false)String brandName,
                                                @RequestParam(value = "shipment",required = false) Integer shipment){
        int shipmentTemp = Objects.isNull(shipment)? 2: shipment;
        // 这里的shipment最好设计为int，可以接收 0 1 2 ，boolean型，只能是0 1，前端传来都会自带默认0，导致无法查询无此条件限制的
        Page<ShopItemEntityDto> pageInfo = shopItemService.listItem(current,size,itemName,itemUuid,category,brandName,shipmentTemp);
        ObjectResponse<Page<ShopItemEntityDto>> response = new ObjectResponse<>();
            response.setCode(RespStatusEnum.SUCCESS.getCode());
            response.setMessage(RespStatusEnum.SUCCESS.getMessage());
            response.setData(pageInfo);
        return response;
    }

    @SoulClient(path = "/vehicle/stock/item/maxId", desc = "获取商品最大编号值")
    @GetMapping("/item/maxId")
    public ObjectResponse<String> maxItemId(){
        ObjectResponse<String> response = new ObjectResponse<>();
        String maxId = shopItemService.getMaxItemUuid();
        if (StringUtils.isBlank(maxId)){
            response.setCode(RespStatusEnum.FAIL.getCode());
            response.setMessage(RespStatusEnum.FAIL.getMessage());
        }else {
            response.setCode(RespStatusEnum.SUCCESS.getCode());
            response.setMessage(RespStatusEnum.SUCCESS.getMessage());
            response.setData(maxId);
        }
        return response;
    }
}

