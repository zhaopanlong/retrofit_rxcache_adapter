package com.zhaopanlong.rxcacheadapter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Yale on 2017/6/13.
 */

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
    CacheStrategy strategy() default CacheStrategy.CacheAndRemoteStrategy;

}
