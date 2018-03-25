package com.mall.common;

import com.mall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static JedisPool pool;
    //最大链接数
    private static Integer maxTotal = PropertiesUtil.getIntProperty("redis.max.total","20");
    //在jedispool中最大的idle状态（空闲的）的jedis实例的个数
    private static Integer maxIdle = PropertiesUtil.getIntProperty("redis.max.idle","10");
    //在jedispool中最小的idle状态（空闲的）的jedis实例的个数
    private static Integer minIdle = PropertiesUtil.getIntProperty("redis.min.idle","2");
    //在borrow一个jedis实例的时候，是否要进行验证；true则的到的jedis肯定可用
    private static Boolean testOnBorrow = PropertiesUtil.getBooleanProperty("redis.test.borrow","true");
    //在return一个jedis的实例的时候，是否进行验证；true则pool中的jedis实例肯定是可用的
    private static Boolean testOnReturn = PropertiesUtil.getBooleanProperty("redis.test.return","true");
    //Redis ip
    private static String redisIp = PropertiesUtil.getProperty("redis.ip", "127.0.0.1");
    //redis端口号
    private static Integer redisPort = PropertiesUtil.getIntProperty("redis.port", "6379");

    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();

        config.setBlockWhenExhausted(true); //连接耗尽的时候，是否阻塞，false则抛异常;默认就是true
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxTotal);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        pool = new JedisPool(config, redisIp, redisPort, 1000*2);
    }

    static{
        initPool();
    }

    public static Jedis getJedis(){
        return pool.getResource();
    }

    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = pool.getResource();
        jedis.set("miao", "miaoValue");
        returnResource(jedis);

        pool.destroy();//；临时调用，销毁连接池中的所有连接
        System.out.println("program is end");
    }

}
