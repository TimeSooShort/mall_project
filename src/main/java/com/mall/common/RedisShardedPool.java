package com.mall.common;

import com.google.common.collect.Lists;
import com.mall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

public class RedisShardedPool {

    private static ShardedJedisPool pool;//sharded jedis连接池
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

    //Redis2 ip
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip", "127.0.0.1");
    //redis2端口号
    private static Integer redis2Port = PropertiesUtil.getIntProperty("redis2.port", "6380");

    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();

        config.setBlockWhenExhausted(true); //连接耗尽的时候，是否阻塞，false则抛异常;默认就是true
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxTotal);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        JedisShardInfo info1 = new JedisShardInfo(redisIp, redisPort, 1000*2);
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 1000*2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>(2);

        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static{
        initPool();
    }

    public static ShardedJedis getJedis(){
        return pool.getResource();
    }

    public static void returnBrokenResource(ShardedJedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(ShardedJedis jedis){
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();

        for (int i = 0; i < 10; i++){
            jedis.set("key"+i, "value"+i);
        }
        returnResource(jedis);
        System.out.println("program is end");
    }
}
