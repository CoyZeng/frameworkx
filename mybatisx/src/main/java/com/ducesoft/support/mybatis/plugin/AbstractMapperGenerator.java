package com.ducesoft.support.mybatis.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ducesoft.support.mybatis.pager.DPattern;


/**
 * @author coyzeng@gmail.com
 */
public abstract class AbstractMapperGenerator {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractMapperGenerator.class);

	public void execute(String[] args) {
		String path = configXmlPath();
		List<String> warnings = new ArrayList<String>();
		ConfigurationParser cp = new ConfigurationParser(warnings);
		try {
			boolean overwrite = true;
			Configuration config = null;
			if (null == path || DPattern.BLANK.matcher(path).matches()) {
				config = cp.parseConfiguration(new File("./src/main/java/com/ducesoft/support/mybatis/plugin/generatorConfig.xml"));
			} else {
				File configFile = new File(path);
				if (configFile.exists()) {
					config = cp.parseConfiguration(configFile);
				} else
					logger.info("Config xml can not be found");
			}
			config(config);
			for (Context context : config.getContexts()) {
				context.addProperty("overwrite", String.valueOf(overwrite));
			}
			DefaultShellCallback callback = new DefaultShellCallback(overwrite);
			MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
			myBatisGenerator.generate(null);
			logger.info("Success");
			System.out.println("Success");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void config(Configuration config) {
		String[] tables = tables();
		if (null == tables || tables.length == 0) {
			return;
		}
		Context context = config.getContexts().get(0);
		for (String table : tables) {
			TableConfiguration tconfig = new TableConfiguration(context);
			tconfig.setTableName(table);
			context.addTableConfiguration(tconfig);
		}
	}

	protected String[] tables() {
		return new String[0];
	}

	/**
	 * 配置文件中的项目地址和包引用地址要用绝对地址，否则不能生成
	 * 
	 * <pre>
	 * "./src/test/java/me/ele/zs/mybatis/gen/generatorConfig_coyzeng.xml"
	 * </pre>
	 */
	protected String configXmlPath() {
		return null;
	}
}
