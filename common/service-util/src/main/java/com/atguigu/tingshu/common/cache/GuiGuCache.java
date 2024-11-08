package com.atguigu.tingshu.common.cache;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GuiGuCache {
    /**
     * 缓存的前缀
     * @return
     */
    String prefix() default "";

    /**
     * 设置缓存的有效时间
     * 单位：秒
     * @return
     */
    long timeout() default 10 * 60L;

    /**
     * 防止雪崩设置的随机值范围
     * @return
     */
    long random() default 5 * 60L;

    /**
     * 为了防止缓存穿透，这里可以指定布隆过滤器在redis中的key
     * @return
     */
    String bloomFilter() default "";
}
