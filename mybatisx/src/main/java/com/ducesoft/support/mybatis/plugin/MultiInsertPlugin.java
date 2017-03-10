package com.ducesoft.support.mybatis.plugin;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.db.DatabaseDialects;

/**
 * 多行插入
 * 
 * @author coyzeng@gmail.com
 */
public class MultiInsertPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
		interfaze.addMethod(insertList(introspectedTable, importedTypes));
		interfaze.addMethod(insertListSelective(introspectedTable, importedTypes));
		interfaze.addImportedTypes(importedTypes);
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		insertList(document.getRootElement(), introspectedTable);
		insertListSelective(document.getRootElement(), introspectedTable);
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	private void insertList(XmlElement parentElement, IntrospectedTable introspectedTable) {
		if (introspectedTable.getRules().generateInsert()) {
			XmlElement insert = new XmlElement("insert");
			insert.addAttribute(new Attribute("id", "insertList"));
			insert.addAttribute(new Attribute("parameterType", "list"));
			addAutoIncrementAttr(insert, introspectedTable);
			List<IntrospectedColumn> cs = ListUtilities
					.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
			StringBuffer columnBf = new StringBuffer("insert into ");
			columnBf.append(getTableName(introspectedTable)).append(" (");
			for (int index = 0; index < cs.size(); index++) {
				columnBf.append(cs.get(index).getActualColumnName());
				if (index < cs.size() - 1) {
					columnBf.append(", ");
				}
			}
			columnBf.append(") values ");
			insert.addElement(new TextElement(columnBf.toString()));
			XmlElement each = new XmlElement("foreach");
			each.addAttribute(new Attribute("collection", "list"));
			each.addAttribute(new Attribute("item", "item"));
			each.addAttribute(new Attribute("index", "index"));
			each.addAttribute(new Attribute("separator", ","));
			StringBuffer valueBf = new StringBuffer("(");
			for (int index = 0; index < cs.size(); index++) {
				valueBf.append("#{item.").append(cs.get(index).getJavaProperty());
				valueBf.append(",jdbcType=").append(cs.get(index).getJdbcTypeName()).append("}");
				if (index < cs.size() - 1) {
					valueBf.append(", ");
				}
			}
			valueBf.append(")");
			each.addElement(new TextElement(valueBf.toString()));

			if (context.getPlugins().sqlMapInsertElementGenerated(parentElement, introspectedTable)) {
				insert.addElement(each);
				parentElement.addElement(insert);
			}
		}
	}

	private void insertListSelective(XmlElement parentElement, IntrospectedTable introspectedTable) {
		if (introspectedTable.getRules().generateInsertSelective()) {
			XmlElement insertSelective = new XmlElement("insert");
			insertSelective.addAttribute(new Attribute("id", "insertListSelective"));
			insertSelective.addAttribute(new Attribute("parameterType", "list"));
			addAutoIncrementAttr(insertSelective, introspectedTable);
			List<IntrospectedColumn> cs = ListUtilities
					.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
			XmlElement each = new XmlElement("foreach");
			each.addAttribute(new Attribute("collection", "list"));
			each.addAttribute(new Attribute("item", "item"));
			each.addAttribute(new Attribute("index", "index"));
			each.addAttribute(new Attribute("separator", ";"));
			// 生成多条insert语句，否则不能知道哪些列不能插入
			each.addElement(new TextElement("insert into " + getTableName(introspectedTable)));

			XmlElement trimKey = new XmlElement("trim");
			trimKey.addAttribute(new Attribute("prefix", " ("));
			trimKey.addAttribute(new Attribute("suffix", ")"));
			trimKey.addAttribute(new Attribute("suffixOverrides", ","));
			for (int index = 0; index < cs.size(); index++) {
				XmlElement test = new XmlElement("if");
				test.addAttribute(new Attribute("test", "item." + cs.get(index).getJavaProperty() + " != null"));
				test.addElement(new TextElement(cs.get(index).getActualColumnName() + ","));
				trimKey.addElement(test);
			}
			each.addElement(trimKey);

			XmlElement trimValue = new XmlElement("trim");
			trimValue.addAttribute(new Attribute("prefix", "values ("));
			trimValue.addAttribute(new Attribute("suffix", ")"));
			trimValue.addAttribute(new Attribute("suffixOverrides", ","));
			for (int index = 0; index < cs.size(); index++) {
				XmlElement test = new XmlElement("if");
				test.addAttribute(new Attribute("test", "item." + cs.get(index).getJavaProperty() + " != null"));
				test.addElement(new TextElement("#{item." + cs.get(index).getJavaProperty() + ",jdbcType="
						+ cs.get(index).getJdbcTypeName() + "},"));
				trimValue.addElement(test);
			}
			each.addElement(trimValue);
			if (context.getPlugins().sqlMapInsertSelectiveElementGenerated(parentElement, introspectedTable)) {
				insertSelective.addElement(each);
				parentElement.addElement(insertSelective);
			}
		}
	}

	private Method insertList(IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> importedTypes) {
		Method method = new Method("insertList");
		method.addJavaDocLine("/** need allowMultiQueries=true support */");
		method.setVisibility(JavaVisibility.DEFAULT);
		method.setReturnType(FullyQualifiedJavaType.getIntInstance());
		addParameter(method, introspectedTable, importedTypes);
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		return method;
	}

	private Method insertListSelective(IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> importedTypes) {
		Method method = new Method("insertListSelective");
		method.addJavaDocLine("/** need allowMultiQueries=true support */");
		method.setVisibility(JavaVisibility.DEFAULT);
		method.setReturnType(FullyQualifiedJavaType.getIntInstance());
		addParameter(method, introspectedTable, importedTypes);
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		return method;
	}

	private void addParameter(Method method, IntrospectedTable introspectedTable,
			Set<FullyQualifiedJavaType> importedTypes) {
		FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
		importedTypes.add(recordType);
		FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
		paramType.addTypeArgument(recordType);
		Parameter parameter = new Parameter(paramType, "records");
		method.addParameter(parameter);
	}

	private String getTableName(IntrospectedTable introspectedTable) {
		TableConfiguration conf = introspectedTable.getTableConfiguration();
		String schema = conf.getSchema() == null ? "" : conf.getSchema() + ".";
		return schema + conf.getTableName();
	}

	private void addAutoIncrementAttr(XmlElement insert, IntrospectedTable introspectedTable) {
		GeneratedKey gk = introspectedTable.getGeneratedKey();
		if (gk != null) {
			IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
			// if the column is null, then it's a configuration error. The
			// warning has already been reported
			DB db = DB.currentDB(getContext());
			if (introspectedColumn != null && gk.isIdentity() && db != DB.postgresql) {
				if (gk.isJdbcStandard()) {
					insert.addElement(getSelectKey(introspectedColumn, gk));
					//insert.addAttribute(new Attribute("useGeneratedKeys", "true"));
					//insert.addAttribute(new Attribute("keyProperty", introspectedColumn.getJavaProperty()));
					//insert.addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
				} else {
					insert.addElement(getSelectKey(introspectedColumn, gk));
				}
			}
		}
	}

	private XmlElement getSelectKey(IntrospectedColumn introspectedColumn, GeneratedKey generatedKey) {
		String identityColumnType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();

		XmlElement answer = new XmlElement("selectKey");
		answer.addAttribute(new Attribute("resultType", identityColumnType));
		answer.addAttribute(new Attribute("keyProperty", introspectedColumn.getJavaProperty()));
		answer.addAttribute(new Attribute("order", generatedKey.getMyBatis3Order()));

		DB db = DB.currentDB(getContext());
		String id = DatabaseDialects.getDatabaseDialect(db.name()).getIdentityRetrievalStatement();
		answer.addElement(new TextElement(id));

		return answer;
	}
	
	enum DB { // 可以从jdbc链接获取更多
		sqlserver, mysql, postgresql, oracle;
		public static DB currentDB(Context context) {
			String driver = context.getJdbcConnectionConfiguration().getDriverClass();
			String url = context.getJdbcConnectionConfiguration().getConnectionURL();
			for (DB db : DB.values()) {
				if (driver.toLowerCase().contains(db.name().toLowerCase())) {
					return db;
				}
				if (url.toLowerCase().contains(db.name().toLowerCase())) {
					return db;
				}
			}
			throw new RuntimeException("Can not resolve db type.");
		}
	}
}
