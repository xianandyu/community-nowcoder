package com.xianyu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Target用于描述该注解可以作用的目标类型
 * @Retention用于描述该注解被保留的时间
 * @Document用于描述该注解是否可以生成到文档里
 * @Inherited修饰，则该类的子类将自动使用@Inherited修饰。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {


}
