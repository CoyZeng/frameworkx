/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分页支持，需结合{@link PagerInterceptor}使用
 * 如果注解在类上，则只会对返回值是{@link Pageable#pager()}的方法有效
 * 
 * @author coyzeng@gmail.com
 *
 */
@Inherited
@Documented
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Pageable {

	/** 分页参数 */
	Class<? extends IPager> pager() default Pager.class;
	
	/** 分页结果 */
	Class<? extends IPage> page() default Page.class;

	/** 异常时封装异常 */
	boolean wrap() default false;

}
