package com.biao.shop.common.dao;

import com.biao.shop.common.entity.ShopClientEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 客户表 Mapper 接口
 * </p>
 *
 * @author XieXiaobiao
 * @since 2020-01-06
 */
@Repository
@Mapper
public interface ShopClientDao extends BaseMapper<ShopClientEntity> {

}
