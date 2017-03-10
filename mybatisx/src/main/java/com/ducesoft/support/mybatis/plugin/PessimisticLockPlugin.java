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
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;

/**
 * @author coyzeng@gmail.com
 */
public class PessimisticLockPlugin extends PluginAdapter {

	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
		interfaze.addMethod(generateLockByPrimaryKey(introspectedTable, importedTypes));
		//interfaze.addMethod(generateLockByExample(introspectedTable, importedTypes));
		interfaze.addImportedTypes(importedTypes);
		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		generateLockByPrimaryKeyXml(document.getRootElement(), introspectedTable);
		//generateLockByExample(document.getRootElement(), introspectedTable);
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	private void generateLockByPrimaryKeyXml(XmlElement parentElement, IntrospectedTable introspectedTable) {
		if (introspectedTable.getRules().generateSelectByPrimaryKey()) {
			List<IntrospectedColumn> primaryKeys = introspectedTable.getPrimaryKeyColumns();

			XmlElement lockByPrimaryKey = new XmlElement("select");
			lockByPrimaryKey.addAttribute(new Attribute("id", "lockByPrimaryKey"));
			lockByPrimaryKey.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

			String parameterType = primaryKeys.get(0).getFullyQualifiedJavaType().getFullyQualifiedName();
			if (introspectedTable.getPrimaryKeyColumns().size() > 1) {
				parameterType = FullyQualifiedJavaType.getNewMapInstance().getShortName();
			}
			lockByPrimaryKey.addAttribute(new Attribute("parameterType", parameterType));

			lockByPrimaryKey.addElement(new TextElement("select "));

			XmlElement columnList = new XmlElement("include");
			columnList.addAttribute(new Attribute("refid", introspectedTable.getBaseColumnListId()));
			lockByPrimaryKey.addElement(columnList);

			TableConfiguration conf = introspectedTable.getTableConfiguration();
			String schema = conf.getSchema() == null ? "" : conf.getSchema() + ".";
			lockByPrimaryKey.addElement(new TextElement("from " + schema + conf.getTableName()));

			StringBuilder bu = new StringBuilder("");
			boolean and = false;
			for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
				bu.setLength(0);
				if (and) {
					bu.append("  and ");
				} else {
					bu.append("where ");
					and = true;
				}
				bu.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
				bu.append(" = ").append("#{").append(introspectedColumn.getJavaProperty()).append("} ");
			}
			bu.append(" for update ");
			TextElement text = new TextElement(bu.toString());
			lockByPrimaryKey.addElement(text);

			if (context.getPlugins().sqlMapSelectByPrimaryKeyElementGenerated(lockByPrimaryKey, introspectedTable)) {
				parentElement.addElement(lockByPrimaryKey);
			}
		}
	}

	protected void generateLockByExample(XmlElement parentElement, IntrospectedTable introspectedTable) {
		if (introspectedTable.getRules().generateSQLExampleWhereClause()) {
			XmlElement lockByExample = new XmlElement("select");
			lockByExample.addAttribute(new Attribute("id", "lockByExample"));
			lockByExample.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
			lockByExample.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));

			lockByExample.addElement(new TextElement("select "));
			XmlElement ifElement = new XmlElement("if");
			ifElement.addAttribute(new Attribute("test", "distinct"));
			ifElement.addElement(new TextElement("distinct"));
			lockByExample.addElement(ifElement);

			XmlElement includeElement = new XmlElement("include");
			includeElement.addAttribute(new Attribute("refid", introspectedTable.getBaseColumnListId()));
			lockByExample.addElement(includeElement);

			TableConfiguration conf = introspectedTable.getTableConfiguration();
			String schema = conf.getSchema() == null ? "" : conf.getSchema() + ".";
			lockByExample.addElement(new TextElement("from " + schema + conf.getTableName()));

			XmlElement parameterElement = new XmlElement("if");
			parameterElement.addAttribute(new Attribute("test", "_parameter != null"));
			XmlElement paramIncludeElement = new XmlElement("include");
			paramIncludeElement.addAttribute(new Attribute("refid", introspectedTable.getExampleWhereClauseId()));
			parameterElement.addElement(paramIncludeElement);
			lockByExample.addElement(parameterElement);

			XmlElement orderByElement = new XmlElement("if");
			orderByElement.addAttribute(new Attribute("test", "orderByClause != null"));
			orderByElement.addElement(new TextElement(" order by #{orderByClause} "));
			lockByExample.addElement(orderByElement);
			XmlElement result = generateLimitElement(lockByExample);

			result.addElement(new TextElement(" for update "));

			if (context.getPlugins().sqlMapExampleWhereClauseElementGenerated(result, introspectedTable)) {
				parentElement.addElement(result);
			}
		}
	}

	private Method generateLockByPrimaryKey(IntrospectedTable introspectedTable,
			Set<FullyQualifiedJavaType> importedTypes) {
		Method method = new Method("lockByPrimaryKey");
		method.setVisibility(JavaVisibility.DEFAULT);
		method.setReturnType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
		generatePrimaryKey(method, introspectedTable, importedTypes);
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		return method;
	}

	protected Method generateLockByExample(IntrospectedTable introspectedTable,
			Set<FullyQualifiedJavaType> importedTypes) {
		FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
		FullyQualifiedJavaType exType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
		importedTypes.add(listType);
		importedTypes.add(recordType);
		importedTypes.add(exType);
		Method method = new Method("lockByExample");
		method.setVisibility(JavaVisibility.DEFAULT);
		listType.addTypeArgument(recordType);
		method.setReturnType(listType);
		method.addParameter(new Parameter(exType, "example"));
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		return method;
	}

	private void generatePrimaryKey(Method method, IntrospectedTable introspectedTable,
			Set<FullyQualifiedJavaType> importedTypes) {
		importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
		List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
			importedTypes.add(type);
			Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
			parameter.addAnnotation("@Param(\"" + introspectedColumn.getJavaProperty() + "\")");
			method.addParameter(parameter);
		}
	}

	/**
	 * 分页节点，生成然后add 进 parentElement
	 * 
	 * <pre>
	 * &lt;if test="limitStart >= 0" &gt;
	 *    limit ${limitStart} , ${limitEnd}
	 * &lt;/if&gt;
	 * </pre>
	 * 
	 * @return 返回null将不会生成分页节点
	 */
	private XmlElement generateLimitElement(XmlElement parentElement) {
		DB currentDB = DB.currentDB(getContext());
		switch (currentDB) {
		case mysql:
			return generateMysqlPage(parentElement);
		case oracle:
			return generateOraclePage(parentElement);
		case postgresql:
			return generatePostgresqlPage(parentElement);
		case sqlserver:
			return generateSqlServerPage(parentElement);
		}
		return null;
	}

	private XmlElement generatePostgresqlPage(XmlElement parentElement) {
		int index = parentElement.getElements().size();
		XmlElement page = new XmlElement("if");
		page.addAttribute(new Attribute("test", "page != null and pageSize != null and page > 0 and pageSize > 0"));
		page.addElement(new TextElement(" limit #{ (page - 1) * pageSize } offset #{pageSize}"));
		parentElement.addElement(index, page);
		return parentElement;
	}

	private XmlElement generateMysqlPage(XmlElement parentElement) {
		int index = parentElement.getElements().size();
		XmlElement page = new XmlElement("if");
		page.addAttribute(new Attribute("test", "page != null and pageSize != null and page >= 0 && pageSize > 0"));
		page.addElement(new TextElement(" limit #{page} , #{pageSize} "));
		parentElement.addElement(index, page);
		return parentElement;
	}

	private XmlElement generateOraclePage(XmlElement parentElement) {
		return null;
	}

	private XmlElement generateSqlServerPage(XmlElement parentElement) {
		/*XmlElement newElement = new XmlElement("select");
		List<Attribute> attributes = parentElement.getAttributes();
		for(Attribute attr : attributes) {
			newElement.addAttribute(attr);
		}
		List<Element> elements = parentElement.getElements();
		newElement.addElement(elements.get(0));
		newElement.addElement(elements.get(1));
		newElement.addElement(elements.get(2));
		newElement.addElement(new TextElement(" from (select *,row_number() over ("));
		newElement.addElement(elements.get(5));
		newElement.addElement(new TextElement(") as rank "));
		newElement.addElement(elements.get(3));
		newElement.addElement(elements.get(4));
		newElement.addElement(new TextElement(")"));
		XmlElement page = new XmlElement("if");
		page.addAttribute(new Attribute("test", "page != null and pageSize != null and page > 0 and pageSize > 0"));
		page.addElement(new TextElement(
				" as temp where temp.rank between (#{page} * #{pageSize} - #{pageSize} + 1) and (#{page} * #{pageSize}) "));
		newElement.addElement(page);*/
		return parentElement;
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
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
