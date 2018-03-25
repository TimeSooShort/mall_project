package com.mall.util;

import com.mall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);

        //取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);

        //忽略空Bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);

        //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    public static <T> String obj2String(T obj){
        if (obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse obj to string error", e);
            return null;
        }
    }

    public static <T> String obj2StringPretty(T obj){
        if (obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse obj to string error", e);
            return null;
        }
    }

    public static <T> T str2Obj(String str, Class<T> clazz){
        //对于isBlank（）方法，如果字符串中含有空，则返回false
        if (StringUtils.isEmpty(str) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (Exception e) {
            log.warn("Parse String to object error",e);
            return null;
        }
    }

    public static <T> T str2Obj(String str, TypeReference<T> typeReference){
        if (StringUtils.isEmpty(str) || typeReference == null){
            return null;
        }
        try {
            return typeReference.getType().equals(String.class) ? (T) str : objectMapper.readValue(str, typeReference);
        } catch (Exception e) {
            log.warn("Parse String to object error",e);
            return null;
        }
    }

    public static <T> T str2Obj(String str, Class<?> collectionClass, Class<?>... elementClass){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClass);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            log.warn("Parse String to object error",e);
            return null;
        }
    }



    public static void main(String[] args) {
        User user = new User();
        user.setId(1);
        user.setEmail("32165");

        User user2 = new User();
        user2.setId(2);
        user2.setEmail("222222");

        String userJson = JsonUtil.obj2String(user);

        String userJsonPretty = JsonUtil.obj2StringPretty(user);

        log.info("userJson:{}",userJson);
        log.info("userJsonPretty:{}",userJsonPretty);

        User user1 = JsonUtil.str2Obj(userJson, User.class);

        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user2);

        String userList = JsonUtil.obj2StringPretty(users);
        log.info("==============");
        log.info(userList);

//        List<User> list = JsonUtil.str2Obj(userList, List.class); //得到的list里装的时LinkedHashMap

        List<User> list = JsonUtil.str2Obj(userList, new TypeReference<List<User>>() {});

        System.out.println("end");
    }
}
