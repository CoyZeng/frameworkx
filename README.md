### DOC
	
##### 分页插件：
	
	@Pageable
	public interface DemoDao {
		List<Demo> pageArray(Pager pager); // total参数将会被填充到pager参数里面
		Page<Demo> pagePage(Page pager); // total参数将会被填充到返回值里面
		
	}
	
	<bean id="backendSqlSessionBean" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="typeAliasesPackage"
                  value="" />
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
    
##### generator插件：
    
    <context id="context1" targetRuntime="MyBatis3" defaultModelType="flat">
		<property name="targetPackage" value="mybatis.gen" />
		<property name="targetProject" value="duce/ducesoft/ds" />
		<plugin type="org.mybatis.generator.plugins.EqualsHashCodePlugin" />
		<plugin type="com.ducesoft.common.core.mybatis.plugin.XmlFileOverwritePlugin"></plugin>
		<plugin type="com.ducesoft.common.core.mybatis.plugin.DBCommentPlugin"></plugin>
		<plugin type="com.ducesoft.common.core.mybatis.plugin.PagePlugin"></plugin>
		<plugin type="com.ducesoft.common.core.mybatis.plugin.PessimisticLockPlugin"></plugin>
		<plugin type="com.ducesoft.common.core.mybatis.plugin.MultiInsertPlugin"></plugin>
		<plugin type="com.ducesoft.common.core.mybatis.plugin.NamingPlugin" />
	</context>
	
### FAQ
	
	coyzeng@gmail.com
	

