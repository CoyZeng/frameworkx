/**
 * 
 */
package com.ducesoft.support.mybatis.pager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.ducesoft.support.mybatis.pager.SoftMethodCache.MethodSignature;

/**
 * 需要和｛{@link Page}配合使用
 * 
 * @author coyzeng@gmail.com
 *
 */
@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
		RowBounds.class, ResultHandler.class }) })
public class PagerInterceptor implements Interceptor {

	protected Field additionalParametersField;

	private static Class<?>[][] SUPPORT_TYPE = { { Pageable.class, IPage.class, IPager.class },
			{ Pageable.class, IPage.class, IPager.class }, { Pageable.class, List.class, IPager.class } };

	public static <A extends Annotation> boolean support(MethodSignature signature) {
		if (null == signature) {
			return false;
		}
		for (Class<?>[] support : SUPPORT_TYPE) {
			if (signature.support(support[0], support[1], support[2])) {
				return true;
			}
		}
		return false;
	}

	private void first(MappedStatement ms) throws SQLException {
		if (null == PagerDialectSupport.currentDB()) {
			DatabaseMetaData meta = ms.getConfiguration().getEnvironment().getDataSource().getConnection()
					.getMetaData();
			PagerDialectSupport.init(meta);
		}
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		Object parameter = args[1];
		String id = ms.getId();

		first(ms);

		if (ignore(id)) {
			return invocation.proceed();
		}

		MethodSignature signature = SoftMethodCache.getSignature(id);
		if (support(signature)) {
			Class<?> clazz = signature.getReturnType();
			IPager pager = getPager(signature, ms.getBoundSql(parameter).getParameterObject());

			Long total = count(invocation);

			if (IPage.class.isAssignableFrom(clazz)) {
				if (total == 0) {
					return wrapPage(signature, pager, total, new ArrayList<>(0), true);
				} else {
					List<?> result = getList(invocation, pager, signature);
					return wrapPage(signature, pager, total, result, true);
				}
			} else {
				if (total == 0) {
					return wrapPage(signature, pager, total, new ArrayList<>(0), false);
				} else {
					List<?> result = getList(invocation, pager, signature);
					return wrapPage(signature, pager, total, result, false);
				}
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object o) {
		try {
			additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
			additionalParametersField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Mybatis BoundSql版本问题，不存在additionalParameters", e);
		}
		return Plugin.wrap(o, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> wrapPage(MethodSignature signature, IPager local, Long total, Object result, boolean wrap) {
		if (wrap) {
			Class<?> reclass = signature.getReturnType();
			try {
				IPage page = (IPage) reclass.newInstance();
				page.setIndex(local.getIndex());
				page.setLimit(local.getLimit());
				page.setOrderBy(local.getOrderBy());
				page.setTotal(new BigDecimal(total).intValue());
				page.setData(result);
				List<T> localarray = new ArrayList<>(1);
				localarray.add((T) page);
				return localarray;
			} catch (Exception e) {
				throw new RuntimeException("Call constructor of IPage implement class fail.", e);
			}
		} else {
			local.setTotal(new BigDecimal(total).intValue());
			return (List<T>) result;
		}
	}

	@SuppressWarnings("unchecked")
	private Long count(Invocation invocation) throws Throwable {
		Executor executor = (Executor) invocation.getTarget();
		MappedStatement originms = (MappedStatement) invocation.getArgs()[0];
		Object params = invocation.getArgs()[1];
		ResultHandler resultHandler = (ResultHandler) invocation.getArgs()[3];

		String origin = originms.getBoundSql(params).getSql().trim();
		String cql = PagerCountSupport.dealCount(origin);

		BoundSql newSql = newBoundSql(cql, invocation);

		MappedStatement countms = newCountMappedStatement(originms);
		CacheKey countKey = executor.createCacheKey(countms, params, RowBounds.DEFAULT, newSql);

		Object cresult = executor.query(countms, params, RowBounds.DEFAULT, resultHandler, countKey, newSql);
		Long count = ((List<Long>) cresult).get(0);
		return count;
	}

	@SuppressWarnings("unchecked")
	private IPager getPager(MethodSignature signature, Object params) {
		if (null == params) {
			Class<?>[] pclasses = signature.getParameterTypes();
			if (null != pclasses && pclasses.length == 1 && null != signature.classOf(IPager.class)) {
				params = newPager(signature);
				return (IPager) params;
			}
			return (IPager) params;
		}
		if (IPager.class.isAssignableFrom(params.getClass())) {
			return (IPager) params;
		}
		if (Map.class.isAssignableFrom(params.getClass())) {
			Map<String, Object> maps = (Map<String, Object>) params;
			if (null != maps && maps.size() > 0) {
				for (Entry<String, Object> param : maps.entrySet()) {
					if (null == param.getValue()) {
						continue;
					}
					if (IPager.class.isAssignableFrom(param.getValue().getClass())) {
						return (IPager) param.getValue();
					}
				}
			}
		}
		return newPager(signature);
	}

	public static IPager newPager(MethodSignature signature) {
		try {
			Class<? extends IPager> pclass = signature.classOf(IPager.class);
			if (null != pclass) {
				return pclass.newInstance();
			}
			Pageable annotaion = signature.annotationOf(Pageable.class);
			return annotaion.pager().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Pager query, pager parameter can not be null.", e);
		}
	}

	private List<?> getList(Invocation invocation, IPager pager, MethodSignature signature) throws Throwable {
		int limit = pager.getLimit() < 1 ? 10 : pager.getLimit();
		int index = pager.getIndex() - pager.indexOffset();
		index = index < 1 ? 0 : index;
		Executor executor = (Executor) invocation.getTarget();
		MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
		Object params = invocation.getArgs()[1];
		String sql = PagerDialect.to(ms.getBoundSql(params).getSql().trim(), pager.getOrderBy(), index, limit);
		ResultHandler rhandler = (ResultHandler) invocation.getArgs()[3];
		BoundSql newSql = newBoundSql(sql, invocation);
		CacheKey cacheKey = executor.createCacheKey(ms, params, RowBounds.DEFAULT, newSql);
		ResultType type = signature.annotationOf(ResultType.class);
		if (null != type) {
			Class<?> rt = type.value();
			ms = newLimitMappedStatement(ms, rt);
		}
		return executor.query(ms, params, RowBounds.DEFAULT, rhandler, cacheKey, newSql);
	}

	public static MappedStatement newCountMappedStatement(MappedStatement ms) {
		return newMappedStatement(ms, ms.getId() + "Count_Interceptor_Generate_Ignore", Long.class);
	}

	public static MappedStatement newLimitMappedStatement(MappedStatement ms, Class<?> resultType) {
		return newMappedStatement(ms, ms.getId() + "Limit_Interceptor_Generate_Ignore", resultType);
	}

	private static final String IGNORE_ID = "Interceptor_Generate_Ignore";

	private static boolean ignore(String id) {
		return id.endsWith(IGNORE_ID);
	}

	public static MappedStatement newMappedStatement(MappedStatement ms, String id, Class<?> resultType) {
		MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), id, ms.getSqlSource(),
				ms.getSqlCommandType());
		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		if (null != ms.getKeyProperties() && ms.getKeyProperties().length > 0) {
			StringBuilder keyProperties = new StringBuilder();
			for (String keyProperty : ms.getKeyProperties()) {
				keyProperties.append(keyProperty).append(",");
			}
			keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
			builder.keyProperty(keyProperties.toString());
		}
		builder.timeout(ms.getTimeout());
		builder.parameterMap(ms.getParameterMap());
		List<ResultMap> resultMaps = new ArrayList<ResultMap>();
		ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), resultType,
				new ArrayList<ResultMapping>(0)).build();
		resultMaps.add(resultMap);
		builder.resultMaps(resultMaps);
		builder.resultSetType(ms.getResultSetType());
		builder.cache(ms.getCache());
		builder.flushCacheRequired(ms.isFlushCacheRequired());
		builder.useCache(ms.isUseCache());

		return builder.build();
	}

	@SuppressWarnings("unchecked")
	private BoundSql newBoundSql(String sql, Invocation invocation) throws Throwable {
		MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
		Object params = invocation.getArgs()[1];
		BoundSql bql = ms.getBoundSql(params);
		Map<String, Object> additionalParameters = (Map<String, Object>) additionalParametersField.get(bql);
		BoundSql newBound = new BoundSql(ms.getConfiguration(), sql, bql.getParameterMappings(), params);
		for (String key : additionalParameters.keySet()) {
			newBound.setAdditionalParameter(key, additionalParameters.get(key));
		}
		return newBound;
	}
}
