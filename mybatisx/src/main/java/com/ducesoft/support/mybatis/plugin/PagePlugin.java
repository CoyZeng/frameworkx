package com.ducesoft.support.mybatis.plugin;

import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InitializationBlock;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;

/**
 * 兼容大部分常用数据库的分页
 * 
 * @author coyzeng@gmail.com
 */
public class PagePlugin extends PluginAdapter {

	// 统一参数名称
	private final static String page = "page";
	private final static String pageSize = "pageSize";
	private final static String example = "example";

	private XmlElement withBlob;
	private XmlElement withoutBlob;

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

	public void generatePageParameter(Method method) {
		method.getParameters().get(0).addAnnotation("@Param(\"" + example + "\")");
		Parameter pageParam = new Parameter(FullyQualifiedJavaType.getIntInstance(), page);
		pageParam.addAnnotation("@Param(\"" + page + "\")");
		Parameter pageSizeParam = new Parameter(FullyQualifiedJavaType.getIntInstance(), pageSize);
		pageSizeParam.addAnnotation("@Param(\"" + pageSize + "\")");
		int index = method.getParameters().size();
		method.addParameter(index, pageParam);
		method.addParameter(index + 1, pageSizeParam);
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		// generatePageParameter(method);
		return super.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		// generatePageParameter(method);
		return super.clientSelectByExampleWithBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		// generatePageParameter(method);
		return super.clientSelectByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		// generatePageParameter(method);
		return super.clientSelectByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		withoutBlob = generateXml(element, true);
		if (DB.currentDB(getContext()).equals(DB.sqlserver)) {
			return false;
		}
		return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
	}

	@Override
	public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		withBlob = generateXml(element, false);
		if (DB.currentDB(getContext()).equals(DB.sqlserver)) {
			return false;
		}
		return super.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable);
	}

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		if (null != withBlob && introspectedTable.hasBLOBColumns()) {
			document.getRootElement().addElement(withBlob);
		}
		if (null != withoutBlob) {
			document.getRootElement().addElement(withoutBlob);
		}
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		addPage(topLevelClass, introspectedTable, "page");
		addPage(topLevelClass, introspectedTable, "pageSize");
		List<IntrospectedColumn> cs = introspectedTable.getPrimaryKeyColumns();
		InitializationBlock init = new InitializationBlock();
		if (null != cs && cs.size() > 0) {
			init.addBodyLine("orderByClause = \"" + cs.get(0).getActualColumnName() + " asc\";");
			topLevelClass.addInitializationBlock(init);
		} else {
			init.addBodyLine("orderByClause = \"" + introspectedTable.getAllColumns().get(0).getActualColumnName() + " asc\";");
			topLevelClass.addInitializationBlock(init);
		}
		return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
	}

	private void addPage(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String name) {
		CommentGenerator commentGenerator = context.getCommentGenerator();
		Field field = new Field();
		field.setVisibility(JavaVisibility.PROTECTED);
		field.setType(PrimitiveTypeWrapper.getIntegerInstance());
		field.setName(name);
		commentGenerator.addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);
		char c = name.charAt(0);
		String camel = Character.toUpperCase(c) + name.substring(1);
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("set" + camel);
		method.addParameter(new Parameter(PrimitiveTypeWrapper.getIntegerInstance(), name));
		method.addBodyLine("this." + name + "=" + name + ";");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
		method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(PrimitiveTypeWrapper.getIntegerInstance());
		method.setName("get" + camel);
		method.addBodyLine("return " + name + ";");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
	}

	private XmlElement generateXml(XmlElement element, boolean simple) {
		DB currentDB = DB.currentDB(getContext());
		switch (currentDB) {
		case mysql:
			return generateMysqlPage(element);
		case oracle:
			return generateOraclePage(element);
		case postgresql:
			return generatePostgresqlPage(element);
		case sqlserver:
			return generateSqlServerPage(element, simple);
		}
		return null;
	}

	private XmlElement generatePostgresqlPage(XmlElement parentElement) {
		parentElement.addAttribute(new Attribute("parameterType", "map"));

		int index = parentElement.getElements().size();
		XmlElement page = new XmlElement("if");
		page.addAttribute(new Attribute("test", "page != null and pageSize != null and page > 0 and pageSize > 0"));
		page.addElement(new TextElement(" limit #{ (page - 1) * pageSize } offset #{pageSize}"));
		parentElement.addElement(index, page);
		return parentElement;
	}

	private XmlElement generateMysqlPage(XmlElement parentElement) {
		parentElement.addAttribute(new Attribute("parameterType", "map"));
		addExampleAlias(parentElement);
		int index = parentElement.getElements().size();
		XmlElement page = new XmlElement("if");
		page.addAttribute(new Attribute("test", "page != null and pageSize != null && page >= 0 and pageSize > 0"));
		page.addElement(new TextElement(" limit #{page} , #{pageSize} "));
		parentElement.addElement(index, page);
		return parentElement;
	}

	private void addExampleAlias(XmlElement parentElement) {
		XmlElement distinct = (XmlElement) parentElement.getElements().get(1);
		distinct.addAttribute(new Attribute("test", "distinct"));
	}

	private XmlElement generateOraclePage(XmlElement parentElement) {
		return null;
	}

	private XmlElement generateSqlServerPage(XmlElement parentElement, boolean simple) {
		List<Attribute> attrs = parentElement.getAttributes();
		XmlElement newElement = new XmlElement(parentElement.getName());
		for (Attribute attr : attrs) {
			// if (attr.getName().equals("parameterType")) {
			// newElement.addAttribute(new Attribute("parameterType", ""));
			// } else {
			newElement.addAttribute(attr);
			// }
		}
		List<Element> elements = parentElement.getElements();
		newElement.addElement(elements.get(0));
		XmlElement distinct = (XmlElement) elements.get(1);
		// distinct.addAttribute(new Attribute("test", "distinct"));
		newElement.addElement(distinct);
		newElement.addElement(elements.get(2));
		if (simple) {
			newElement.addElement(new TextElement(" from (select *,row_number() over ("));
			newElement.addElement(elements.get(5));
			newElement.addElement(new TextElement(") as rank "));
			newElement.addElement(elements.get(3));
			newElement.addElement(elements.get(4));
			newElement.addElement(new TextElement(") as temp "));
			XmlElement page = new XmlElement("if");
			page.addAttribute(new Attribute("test", "page != null and pageSize != null and page > 0 and pageSize > 0"));
			page.addElement(new TextElement(
					"where temp.rank between (#{page} * #{pageSize} - #{pageSize} + 1) and (#{page} * #{pageSize}) "));
			newElement.addElement(page);
		} else {
			newElement.addElement(elements.get(3));
			newElement.addElement(elements.get(4));
			newElement.addElement(new TextElement(" from (select *,row_number() over ("));
			newElement.addElement(elements.get(7));
			newElement.addElement(new TextElement(") as rank "));
			newElement.addElement(elements.get(5));
			newElement.addElement(elements.get(6));
			newElement.addElement(new TextElement(") as temp "));
			XmlElement page = new XmlElement("if");
			page.addAttribute(new Attribute("test", "page > 0 and pageSize > 0"));
			page.addElement(new TextElement(
					"where temp.rank between #{(page * pageSize - pageSize + 1} and #{page * pageSize} "));
			newElement.addElement(page);
		}
		return newElement;
	}
}
