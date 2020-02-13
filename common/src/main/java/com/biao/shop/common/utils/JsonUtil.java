package com.biao.shop.common.utils;

import com.biao.shop.common.bo.OrderBO;
import com.biao.shop.common.utils.CustomDateDeserializer;
import com.biao.shop.common.utils.CustomDateSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

/**
 * @ClassName Json
 * @Description: TODO
 * @Author Biao
 * @Date 2020/2/12
 * @Version V1.0
 **/
public class JsonUtil {

    public static String convertToJson(Object o) throws JsonProcessingException {
        //转换为json, 但要注意时间的序列化
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDateTime.class, new CustomDateSerializer());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(timeModule);
        String JsonStr = objectMapper.writeValueAsString(o);
        return JsonStr;
    }

    public static Object convertJsonToObject(String jsonStr, Class<Object> clazz) throws JsonProcessingException {
        //json反转化, 但要注意时间的序列化
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDateTime.class, new CustomDateSerializer());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(timeModule);
        Object object = objectMapper.readValue(jsonStr,clazz);
        return object;
    }
}
