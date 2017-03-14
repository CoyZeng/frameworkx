### DOC
	
#### 本模块包含两部分：Mybatis物理分页插件和Mybatis Generator部分插件

##### Mybatis分页插件：
	
	Demo:
	1、接入选择：
	    可以使用默认的Pager入参和Page返回结果
	    也可实现IPager接口和IPage接口（兼容你的系统当前的所有分页对象）
	2、接入影响
	    使用本插件的所有接口不会返回null值，List最小是size为零的集合
	3、接入配置
	
	<bean id="backendSqlSessionBean" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="typeAliasesPackage" value="" />
        <property name="configLocation" value="classpath:mybatis/mybatis.xml" />
        <property name="mapperLocations">
            <list>
                <value>classpath*:mapper/**/*.xml</value>
            </list>
        </property>
        <property name="plugins">
        	<list>
        		<bean class="com.ducesoft.support.mybatis.pager.PagerInterceptor"/>
        	</list>
        </property>
    </bean>
    
###### Xml写法：
    @Pageable
	public interface DemoDao {
		List<Demo> pageArray(Pager pager); // total参数将会被填充到pager参数里面
		Page<Demo> pagePage(Pager pager); // total参数将会被填充到返回值里面
	}
	
	<select id="pageArray" parameterType="map" resultMap="BaseResultMap">
        select 
        <include refid="Base_Column_List" />
        from t_bms_push_point 	
        where 1=1
    </select>
    <select id="pageArray" parameterType="map" resultMap="BaseResultMap">
    	select 
    	<include refid="Base_Column_List" />
    	from t_bms_push_point 	
    	where 1=1
    </select>
    <select id="pagePage" parameterType="map" resultMap="BaseResultMap">
    	select 
    	<include refid="Base_Column_List" />
    	from t_bms_push_point 	
    	where 1=1
    </select>
  	
###### Annotation写法：
	@Pageable
	public interface DemoDao {
		@Select("select * from t_demo where 1=1")
		@ResultType(Demo.class)
		List<Demo> pageArray(Pager pager); // total参数将会被填充到pager参数里面
		@Select("select * from t_demo where 1=1")
		@ResultType(Demo.class)
		Page<Demo> pagePage(Pager pager); // total参数将会被填充到返回值里面
	}
    
##### Mybatis Generator插件：
    
    <context id="context1" targetRuntime="MyBatis3" defaultModelType="flat">
		<property name="targetPackage" value="mybatis.gen" />
		<property name="targetProject" value="duce/ducesoft/ds" />
		<plugin type="org.mybatis.generator.plugins.EqualsHashCodePlugin" />                    <!-- PO equals和hashCode生成 -->
		<plugin type="com.ducesoft.common.core.mybatis.plugin.XmlFileOverwritePlugin"></plugin> <!-- 每次生成xml的时候覆盖，默认是追加 -->
		<plugin type="com.ducesoft.common.core.mybatis.plugin.DBCommentPlugin"></plugin>        <!-- 生成PO注释 -->
		<plugin type="com.ducesoft.common.core.mybatis.plugin.PagePlugin"></plugin>             <!-- Select分页支持 用了上面的分页插件，这个算鸡肋 -->
		<plugin type="com.ducesoft.common.core.mybatis.plugin.PessimisticLockPlugin"></plugin>  <!-- 悲观锁支持 -->
		<plugin type="com.ducesoft.common.core.mybatis.plugin.MultiInsertPlugin"></plugin>      <!-- 批量插入动态SQL支持 -->
		<plugin type="com.ducesoft.common.core.mybatis.plugin.NamingPlugin" />                  <!-- Mapper后缀重命名支持 -->
	</context>
	
### FAQ
	
	coyzeng@gmail.com
	

