/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author coyzeng@gmail.com
 *
 */
public final class SoftMethodCache {

	private static final Logger logger = LoggerFactory.getLogger(SoftMethodCache.class);

	// private static final ReentrantReadWriteLock lock = new
	// ReentrantReadWriteLock();

	private static final class SoftMethodCacheHolder {
		private static final Map<String, Method> cache = new ConcurrentHashMap<>();
	}

	private SoftMethodCache() {
	}

	public static Method get(String key) {
		if (key.contains("!")) {
			key = key.substring(0, key.indexOf("!"));
		}
		Method method = SoftMethodCacheHolder.cache.get(key);
		if (null == method) {
			cache(key);
		}
		if (null == SoftMethodCacheHolder.cache.get(key)) {
			return null;
		}
		return SoftMethodCacheHolder.cache.get(key);
	}

	private static void cache(String key) {
		if (null != SoftMethodCacheHolder.cache.get(key)) {
			return;
		}
		int index = key.lastIndexOf(".");
		String iface = key.substring(0, index);
		try {
			Method[] methods = Class.forName(iface).getMethods();
			for (Method method : methods) {
				SoftMethodCacheHolder.cache.put(iface + "." + method.getName(), method);
			}
		} catch (Exception e) {
			logger.error("加载类失败", e);
			SoftMethodCacheHolder.cache.put(key, null);
		}
	}

	private static final class SoftMethodSignatureCacheHolder {
		private static final Map<String, MethodSignature> cache = new ConcurrentHashMap<>();
	}

	public static MethodSignature getSignature(String key) {
		if (key.contains("!")) {
			key = key.substring(0, key.indexOf("!"));
		}
		MethodSignature method = SoftMethodSignatureCacheHolder.cache.get(key);
		if (null == method) {
			cacheSignature(key);
		}
		if (null == SoftMethodSignatureCacheHolder.cache.get(key)) {
			return null;
		}
		return SoftMethodSignatureCacheHolder.cache.get(key);
	}

	private static void cacheSignature(String key) {
		if (null != SoftMethodSignatureCacheHolder.cache.get(key)) {
			return;
		}
		int index = key.lastIndexOf(".");
		String iface = key.substring(0, index);
		try {
			Method[] methods = Class.forName(iface).getMethods();
			for (Method method : methods) {
				SoftMethodSignatureCacheHolder.cache.put(iface + "." + method.getName(), new MethodSignature(method));
			}
		} catch (Exception e) {
			logger.error("加载类失败", e);
			SoftMethodSignatureCacheHolder.cache.put(key, new MethodSignature());
		}
	}

	public static final class MethodSignature {
		private Class<?> returnType;
		private Class<?>[] parameterTypes;
		private Annotation[] annotationTypes;

		MethodSignature() {
		}

		MethodSignature(Method method) {
			this.returnType = method.getReturnType();
			this.parameterTypes = method.getParameterTypes();
			Annotation[] mann = method.getAnnotations();
			Annotation[] cann = method.getDeclaringClass().getAnnotations();
			List<Annotation> all = new ArrayList<>();
			if (null != mann && mann.length > 0) {
				all.addAll(Arrays.asList(mann));
			}
			if (null != cann && cann.length > 0) {
				all.addAll(Arrays.asList(cann));
			}
			this.annotationTypes = all.toArray(new Annotation[all.size()]);
		}

		public Class<?> getReturnType() {
			return returnType;
		}

		public Class<?>[] getParameterTypes() {
			return parameterTypes;
		}

		public Annotation[] getAnnotationTypes() {
			return annotationTypes;
		}

		public boolean support(Class<?> a, Class<?> r, Class<?> p) {
			if (null == annotationOf(a)) {
				return false;
			}
			if (null == returnType || !r.isAssignableFrom(returnType)) {
				return false;
			}
			if (null == classOf(p)) {
				return false;
			}
			return true;
		}

		@SuppressWarnings("unchecked")
		public <C> Class<C> classOf(Class<C> c) {
			if (null == this.parameterTypes) {
				return null;
			}
			for (Class<?> cc : this.parameterTypes) {
				if (c.isAssignableFrom(cc)) {
					return (Class<C>) cc;
				}
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		public <A extends Annotation> A annotationOf(Class<?> c) {
			if (null == this.annotationTypes) {
				return null;
			}
			for (Annotation an : this.annotationTypes) {
				if (c.isAssignableFrom(an.annotationType())) {
					return (A) an;
				}
			}
			return null;
		}
	}
}
