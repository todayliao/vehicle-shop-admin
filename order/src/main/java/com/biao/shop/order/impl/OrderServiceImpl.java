package com.biao.shop.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.biao.shop.common.dao.ShopOrderDao;
import com.biao.shop.common.dto.ClientQueryDTO;
import com.biao.shop.common.dto.OrderDTO;
import com.biao.shop.common.entity.ItemListEntity;
import com.biao.shop.common.entity.ShopClientEntity;
import com.biao.shop.common.entity.ShopOrderEntity;
import com.biao.shop.common.rpc.service.ShopClientRPCService;
import com.biao.shop.order.service.ItemListService;
import com.biao.shop.order.service.OrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName OrderServiceImpl
 * @Description: TODO
 * @Author Biao
 * @Date 2020/2/15
 * @Version V1.0
 **/
@Service
public class OrderServiceImpl extends ServiceImpl<ShopOrderDao, ShopOrderEntity>  implements OrderService {

    private final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private ShopOrderDao shopOrderDao;
    private ItemListService itemListService;

    @Reference(version = "1.0.0",group = "shop",interfaceClass = ShopClientRPCService.class)
    private ShopClientRPCService clientRPCService;

    @Autowired
    public OrderServiceImpl(ShopOrderDao shopOrderDao,ItemListService itemListService){
        this.shopOrderDao = shopOrderDao;
        this.itemListService =  itemListService;
    }

    @Override
    public int deleteOrderByUuid(String uuid) {
        return shopOrderDao.delete(new LambdaQueryWrapper<ShopOrderEntity>().eq(ShopOrderEntity::getOrderUuid,uuid));
    }

    @Override
    public int deleteBatchByIds(Collection<Integer> ids) {
        ids.forEach(this::deleteById);
        return 1;
    }

    @Override
    @Transactional
    public int deleteById(int id) {
        ShopOrderEntity shopOrderEntity = shopOrderDao.selectById(id);
        String orderUuid = shopOrderEntity.getOrderUuid();
        // 删除订单详细
        List<Integer> list1 = itemListService.listDetail(orderUuid).stream()
                .map(ItemListEntity::getIdList).collect(Collectors.toList());
        itemListService.deleteBatchItemList(list1);
        // todo 库存变化(-冻结)

        return shopOrderDao.deleteById(id);
    }

    @Override
    public ShopOrderEntity selectByUuId(String uuid) {
        return shopOrderDao.selectOne(new LambdaQueryWrapper<ShopOrderEntity>().eq(ShopOrderEntity::getOrderUuid,uuid));
    }

    @Override
    public int updateOrder(ShopOrderEntity orderEntity) {
        return shopOrderDao.updateById(orderEntity);
    }

    @Override
    public int createOrder(ShopOrderEntity orderEntity) {
        return shopOrderDao.insert(orderEntity);
    }

    @Override
    public ShopOrderEntity queryOrder(int id) {
        return shopOrderDao.selectById(id);
    }

    @Override
    public Page<OrderDTO> listOrderDTO(Integer current, Integer size, String orderUuid, String clientName, String phone,
                                        String vehicleSeries, String vehiclePlate, String generateDateStart,
                                        String generateDateEnd, int paidStatus) {
        // 先查询客户信息
        ClientQueryDTO clientQueryDTO = new ClientQueryDTO();
        clientQueryDTO.setClientName(clientName);
        clientQueryDTO.setPhone(phone);
        clientQueryDTO.setVehiclePlate(vehiclePlate);
        clientQueryDTO.setVehicleSeries(vehicleSeries);
        List<ShopClientEntity> clientEntities1 = clientRPCService.listByClientDto(clientQueryDTO);
        List<String> clientUidS = clientEntities1.stream().map(ShopClientEntity::getClientUuid).collect(Collectors.toList());
        // 再查订单信息
        boolean flag = clientUidS.size() > 0;
        boolean dateFlag = StringUtils.isNotEmpty(generateDateStart) && StringUtils.isNotEmpty(generateDateEnd);
        QueryWrapper<ShopOrderEntity> qw = new QueryWrapper<>();
        Map<String,Object> map = new HashMap<>(1);
        map.put("order_uuid",orderUuid);
        map.put("is_paid", paidStatus == 2 ? null : paidStatus);
        qw.allEq(true,map,false)
                .between(dateFlag,"generate_date",generateDateStart,generateDateEnd)
                .in(flag,"client_uuid",clientUidS);
        Page<ShopOrderEntity> orderEntityPage = new Page<>(current,size);
        orderEntityPage = shopOrderDao.selectPage(orderEntityPage,qw);
        List<OrderDTO> orderDTOS = orderEntityPage.getRecords().stream().map(orderEntity -> {
            OrderDTO orderDTO = new OrderDTO();
            BeanUtils.copyProperties(orderEntity, orderDTO);
            orderDTO.setClientName(clientRPCService.queryById(orderEntity.getClientUuid()).getClientName());
            return orderDTO;
        }).collect(Collectors.toList());
        Page<OrderDTO> orderDTOPage = new Page<>(current,size);
        BeanUtils.copyProperties(orderEntityPage,orderDTOPage,"records");
        return orderDTOPage.setRecords(orderDTOS);

        /** 这里使用 PageInfo 分页，发现有问题，sql只能是 LIMIT (size)，导致无法分页，故改为 mbp 分页*/
        /*PageHelper.startPage(current,size);
        logger.debug("current>>>>>>>{}//{}}",current,size);
        List<ShopOrderEntity> orderEntities = shopOrderDao.selectList(qw);
        PageInfo<ShopOrderEntity> pageInfo = PageInfo.of(orderEntities);
        List<OrderDTO> orderDTOList = pageInfo.getList().stream().map(orderEntity -> {
            OrderDTO orderDTO  = new OrderDTO();
            BeanUtils.copyProperties(orderEntity,orderDTO);
            orderDTO.setClientName(clientRPCService.queryById(orderEntity.getClientUuid()).getClientName());
            return orderDTO;
        }).collect(Collectors.toList());
        return PageInfo.of(orderDTOList);*/

    }

    @Override
    public int paidOrder(int orderId) {
        ShopOrderEntity orderEntity = this.queryOrder(orderId);
        if (Objects.isNull(orderEntity))
            return 0;
        else{
            orderEntity.setPaidStatus(true);
            // 加积分
            clientRPCService.addPoint(orderEntity.getClientUuid(),orderEntity.getAmount().intValue());
            // todo 库存（-冻结+扣减）
            /***/
        }
        return shopOrderDao.updateById(orderEntity);
    }

}