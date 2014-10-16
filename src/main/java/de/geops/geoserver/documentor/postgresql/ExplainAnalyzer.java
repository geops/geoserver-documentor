package de.geops.geoserver.documentor.postgresql;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class ExplainAnalyzer {

	class Table {
		String tableName;
		String tableSchema;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Table other = (Table) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (tableName == null) {
				if (other.tableName != null)
					return false;
			} else if (!tableName.equals(other.tableName))
				return false;
			if (tableSchema == null) {
				if (other.tableSchema != null)
					return false;
			} else if (!tableSchema.equals(other.tableSchema))
				return false;
			return true;
		}

		private ExplainAnalyzer getOuterType() {
			return ExplainAnalyzer.this;
		}

		public String getTableName() {
			return tableName;
		}

		public String getTableSchema() {
			return tableSchema;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((tableName == null) ? 0 : tableName.hashCode());
			result = prime * result
					+ ((tableSchema == null) ? 0 : tableSchema.hashCode());
			return result;
		}

		public boolean isValid() {
			return (this.tableName != null && this.tableSchema != null);
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public void setTableSchema(String tableSchema) {
			this.tableSchema = tableSchema;
		}
	}

	/**
	 * create q query which will provide analyzable input from a given query
	 * 
	 * @param query
	 * @return
	 */
	public static String createQuery(String query) {
		StringBuilder queryBuilder = new StringBuilder()
		// "verbose" is required to make postgres include the schema of tables
				.append("explain (format xml, verbose) ").append(query);
		return queryBuilder.toString();
	}

	private final Document document;
	private final XPathFactory xpathFactory;

	public ExplainAnalyzer(String explainXmlStr) throws PostgresqlException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(explainXmlStr));
			document = builder.parse(is);

			xpathFactory = XPathFactory.newInstance();
		} catch (ParserConfigurationException e) {
			throw new PostgresqlException(e);
		} catch (SAXException e) {
			throw new PostgresqlException(e);
		} catch (IOException e) {
			throw new PostgresqlException(e);
		}
	}

	private Element getFirstChildByTagName(Element parent, String name) {
		for (Node child = parent.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE
					&& name.equals(child.getNodeName())) {
				return (Element) child;
			}
		}
		return null;
	}
	
	private String getFirstChildByTagNameText(Element parent, String name) {
		Element element = getFirstChildByTagName(parent, name);
		if (element != null && element.hasChildNodes()) {
			return element.getFirstChild().getNodeValue();
		}
		return null;
	}
	
	public List<Table> getReferencedTables() throws PostgresqlException {
		HashSet<Table> referencedTables = new HashSet<Table>();

		XPath xpath = xpathFactory.newXPath();
		try {
			XPathExpression expr = xpath.compile("//Plan");
			NodeList planNodes = (NodeList) expr.evaluate(document,
					XPathConstants.NODESET);
			for (int i = 0; i < planNodes.getLength(); i++) {
				Node planNode = planNodes.item(i);
				if (planNode.getNodeType() == Node.ELEMENT_NODE) {
					Element planELement = (Element) planNode;
					
					Table table = new Table();
					table.setTableName(getFirstChildByTagNameText(planELement, "Relation-Name"));
					table.setTableSchema(getFirstChildByTagNameText(planELement, "Schema"));
					if (table.isValid()) {
						referencedTables.add(table);
					}
				}
			}

		} catch (XPathExpressionException e) {
			throw new PostgresqlException(e);
		}
		return new ArrayList<Table>(referencedTables);
	}
}
