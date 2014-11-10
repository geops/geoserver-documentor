package de.geops.geoserver.documentor.postgresql;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.catalog.StoreInfo;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.logging.Logging;

import de.geops.geoserver.documentor.directive.DirectiveParser;
import de.geops.geoserver.documentor.info.PropertyDoc;
import de.geops.geoserver.documentor.info.TableDoc;

/**
 * 
 * Allows the following directives:
 * 
 *   ignore
 *   	ignore this table
 *   
 *   include-ref table:public.mytable
 *      include the table in the documentation list
 *      
 *   ignore-ref table:public.mytable
 *      ignore the reference to the table
 *      
 * 
 * @author nico
 *
 */
public class PostgresqlAnalyzer {
	
	private static final Logger LOGGER = Logging.getLogger(PostgresqlAnalyzer.class);

	private String tableSchema = "public";
	
	private final JDBCDataStore datastore;
	private final Connection connection;
	
	protected HashSet<TableDoc> tablesFound = new HashSet<TableDoc>();
	protected HashSet<TableDoc> tablesIgnored = new HashSet<TableDoc>();

	public PostgresqlAnalyzer(StoreInfo storeInfo) throws PostgresqlException {
		if (!storeInfo.getType().equals("PostGIS")) {
			throw new IllegalArgumentException("No PostGIS store given. Got: "+storeInfo.getType());
		}
		// find the schema used for the store
		Map<String, Serializable> connectionParams = storeInfo.getConnectionParameters();
		if (connectionParams.containsKey("schema")) {
			this.tableSchema = (String) connectionParams.get("schema");
		}
		
		try {
			DataStore datastoreObj = DataStoreFinder.getDataStore(connectionParams);
			if (!(datastoreObj instanceof JDBCDataStore)) {
				throw new RuntimeException("Datastore "+storeInfo.getName()+" is not an JDBC Datastore");
			}
			datastore = (JDBCDataStore) datastoreObj;
			connection = datastore.getConnection(Transaction.AUTO_COMMIT);
			connection.setAutoCommit(false);
			
		} catch (IOException e) {
			throw new PostgresqlException("Could not open Datastore "+storeInfo.getName(), e);
		} catch (SQLException e) {
			throw new PostgresqlException("Could not diable autocommit in Datastore "+storeInfo.getName(), e);
		}
	}
	
	public void analyzeQuery(String query) throws PostgresqlException {
		readReferencedTables(query);
	}
	
	public void analyzeTable(String tableName, boolean isMainTable) throws PostgresqlException {
		this.analzyeTable(this.tableSchema, tableName, isMainTable);
	}
	
	public void analzyeTable(String tableSchema, String tableName, boolean isMainTable) throws PostgresqlException {
		//LOGGER.severe("Analzying table "+tableSchema+"."+tableName);
		this.readTable(tableSchema, tableName, isMainTable);
	}
	
	/**
	 * shutdown all db and store connections
	 * 
	 * must be called before the object goes out of scope
	 */
	public void dispose() {
		if (connection != null) {
			try {
				connection.rollback();
				connection.close();
			} catch (SQLException e) {
				LOGGER.warning(e.getMessage());
			}
		}
		if (datastore != null) {
			datastore.dispose();
		}
	}
	
	
	private String executeAndFetchString(String query) throws PostgresqlException {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (!rs.next()) {
				throw new PostgresqlException("Query \""+query+"\" did not return anything");
			}
			return rs.getString(1);
		} catch (SQLException e) {
			throw new PostgresqlException("Could not execute query: "+query, e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "could not close statement", e);
				}
			}
		}	
	}
	
	public String getDatabaseName() throws PostgresqlException {
		return executeAndFetchString("select current_database()");	
	}
	
	public String getPostgisVersion() throws PostgresqlException {
		String postgisVersion = null;
		Statement stmtNspname = null;
		try {
			stmtNspname = connection.createStatement();
			ResultSet rsNspname = stmtNspname.executeQuery(
					"select pp.proname, pns.nspname "
					+" from pg_catalog.pg_proc pp "
					+" join pg_catalog.pg_namespace pns on pp.pronamespace = pns.oid "
					+" where pp.proname = 'postgis_version' and pp.pronargs = 0 and not pp.proisagg and not pp.proretset");
			if (rsNspname.next()) {
				postgisVersion = executeAndFetchString("select "+rsNspname.getString(2)+"."+rsNspname.getString(1)+"()");
			}
		} catch (SQLException e) {
			throw new PostgresqlException("Could not fetch the Postgresql version", e);
		} finally {
			if (stmtNspname != null) {
				try {
					stmtNspname.close();
				} catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "could not close statement", e);
				}
			}
		}
		return postgisVersion;
	}
	
	public String getPostgresqlVersion() throws PostgresqlException {
		return executeAndFetchString("select version()");	
	}
	
	public List<TableDoc> getTableDocs() {
		return new ArrayList<TableDoc>(this.tablesFound);
	}
	
	/**
	 * handle all directives which must be handled after the current entity is parsed.
	 * 
	 * @param dp
	 * @throws PostgresqlException 
	 */
	private void handleAfterDirectives(DirectiveParser directiveParser) throws PostgresqlException {
		// handle include directives
		for (String includeRef: directiveParser.getIncludeReferences()) {
			if (includeRef.startsWith("table:")) {
				String includeTableFullName = includeRef.replaceFirst("table:", "");
				try {
					Table table = Table.fromString(includeTableFullName);
					readTable(table.getTableSchema(), table.getTableName(), false);
				} catch (IllegalArgumentException e) {
					LOGGER.log(Level.WARNING, "Could not parse the table name form include directive: "+includeTableFullName, e);
				}
			}
		}
		// handle ignore directives
		for (String ignoreRef: directiveParser.getIgnoreReferences()) {
			if (ignoreRef.startsWith("table:")) {
				String ignoreTableFullName = ignoreRef.replaceFirst("table:", "");
				try {
					Table table = Table.fromString(ignoreTableFullName);
						ignoreTable(table.getTableSchema(), table.getTableName());
					
				} catch (IllegalArgumentException e) {
					LOGGER.log(Level.WARNING, "Could not parse the table name form ignore directive: "+ignoreTableFullName, e);
				}
			}
		}	
	}
	
	/**
	 * handle all directives which must be handled before the current entity is parsed.
	 * 
	 * @param dp
	 * @return boolean if the parsing of this entity should continue
	 */
	private boolean handleBeforeDirectives(DirectiveParser directiveParser) {
		return directiveParser.ignoreThisEntity();	
	}
	
	
	protected void ignoreTable(String tableSchema, String tableName) {
		TableDoc testTableDoc = new TableDoc();
		testTableDoc.setTableName(tableName);
		testTableDoc.setTableSchema(tableSchema);
		
		this.tablesIgnored.add(testTableDoc);
		this.tablesFound.remove(testTableDoc);
		
	}
	
	protected boolean isAlreadyRead(String tableSchema, String tableName) {
		TableDoc testTableDoc = new TableDoc();
		testTableDoc.setTableName(tableName);
		testTableDoc.setTableSchema(tableSchema);
		return this.tablesFound.contains(testTableDoc);
	}
	
	protected boolean isIgnored(String tableSchema, String tableName) {
		TableDoc testTableDoc = new TableDoc();
		testTableDoc.setTableName(tableName);
		testTableDoc.setTableSchema(tableSchema);
		return this.tablesIgnored.contains(testTableDoc);
	}
	
	protected String quoteIdent(String str) throws PostgresqlException {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement("select quote_ident(?) as q");
			stmt.setString(1, str);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			return rs.getString("q");
			
		} catch (SQLException e) {
			throw new PostgresqlException("Could not quote "+str, e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "could not close statement", e);
				}
			}
		}	
	}
	
	public void readReferencedTables(String query) throws PostgresqlException {
		String analyzableQuery = ExplainAnalyzer.createQuery(query);
		String plan = executeAndFetchString(analyzableQuery);

		ExplainAnalyzer explainAnalyzer = new ExplainAnalyzer(plan);
		for (Table table: explainAnalyzer.getReferencedTables()) {
			this.readTable(table.getTableSchema(), table.getTableName(), false);
		}
	}
	
	public void readReferencedTables(String tableSchema, String tableName) throws PostgresqlException {
		StringBuilder queryBuilder = new StringBuilder()
			.append("select * from ")
			.append(quoteIdent(tableSchema))
			.append(".")
			.append(quoteIdent(tableName));
		readReferencedTables(queryBuilder.toString());
	}
	
	/**
	 * 
	 * @param tableSchema
	 * @param tableName
	 * @param isMainTable
	 * @throws PostgresqlException
	 */
	protected void readTable(String tableSchema, String tableName, boolean isMainTable) throws PostgresqlException {
		if (isAlreadyRead(tableSchema, tableName) || isIgnored(tableSchema, tableName)) {
			return;
		}
		
		String query = "select pc.relname, pns.nspname, pd.description,"
				+" case when pc.relkind = 'r' then 'TABLE'"
				+"      when pc.relkind = 'v' then 'VIEW' end as kind,"
				+" case when pc.relkind = 'v' then "
				+"		'CREATE OR REPLACE VIEW '||quote_ident(pns.nspname)||'.'||quote_ident(pc.relname)||E' AS\\n'||pg_get_viewdef(pc.oid, true) "
				+" end as definition,"
				+" pc.oid"
				+" from pg_class pc"
				+" join pg_namespace pns on pns.oid = pc.relnamespace"
				+" left join pg_description pd on pd.objoid = pc.oid"
				+" where pns.nspname = ? and pc.relname = ?";
		PreparedStatement stmtTable = null;
		PreparedStatement stmtColumns = null;
		try {
			stmtTable = connection.prepareStatement(query);
			stmtTable.setString(1, tableSchema);
			stmtTable.setString(2, tableName);
			ResultSet rsTable = stmtTable.executeQuery();
			if (!rsTable.next()) {
				LOGGER.warning("Did not find relation "+tableSchema+"."+tableName);
				return;
			}
			
			//analyze the comment for any documentor directives
			String comment = rsTable.getString("description");
			DirectiveParser directiveParser = new DirectiveParser(comment);
			comment = directiveParser.getClearedInput(); // remove all directives from the comment
			if (handleBeforeDirectives(directiveParser)) {
				LOGGER.fine("Ignoring relation "+tableSchema+"."+tableName+" because of ignore directive");
				return;
			}	
						
			TableDoc tableDoc = new TableDoc();
			tableDoc.setMainTable(isMainTable);
			tableDoc.setTableName(rsTable.getString("relname"));
			tableDoc.setTableSchema(rsTable.getString("nspname"));
			tableDoc.setDefinition(rsTable.getString("definition"));
			tableDoc.setComment(comment);
			
			String kind = rsTable.getString("kind");
			tableDoc.setType(kind);
			
			// fetch columns for tables
			if (kind != null) {
				if (kind.equals("TABLE")) {
					stmtColumns = connection.prepareStatement(
							"select pa.attname, coalesce(parent_type.typname, pt.typname)::text||repeat('[]', pa.attndims) as typname, pd.description"
							+" from pg_attribute pa"
							+" join pg_type pt on pa.atttypid = pt.oid"
							+" left join pg_type parent_type on pt.typelem != 0 and pt.typelem = parent_type.oid"
							+" left join pg_description pd on pd.objoid = pa.attrelid and pd.objsubid=pa.attnum"
							+" where pa.attrelid = ? and pa.attnum > 0");
					stmtColumns.setInt(1, rsTable.getInt("oid"));
					ResultSet rsColumns = stmtColumns.executeQuery();
					
					ArrayList<PropertyDoc> columnsDocs = new ArrayList<PropertyDoc>();
					while (rsColumns.next()) {
						PropertyDoc columnDoc = new PropertyDoc();
						columnDoc.setComment(rsColumns.getString("description"));
						columnDoc.setName(rsColumns.getString("attname"));
						columnDoc.setType(rsColumns.getString("typname"));
						columnsDocs.add(columnDoc);
					}
					tableDoc.setColumns(columnsDocs);
					
				} else if (kind.equals("VIEW")) {
					this.readReferencedTables(rsTable.getString("nspname"), rsTable.getString("relname"));
				}
			}
			this.tablesFound.add(tableDoc);	
			
			handleAfterDirectives(directiveParser);
			
		} catch (SQLException e) {
			throw new PostgresqlException("Could not read table definition", e);
		} finally {
			if (stmtTable != null) {
				try {
					stmtTable.close();
				} catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "could not close statement", e);
				}
			}
			if (stmtColumns != null) {
				try {
					stmtColumns.close();
				} catch (SQLException e) {
					LOGGER.log(Level.SEVERE, "could not close statement", e);
				}
			}
		}
	}
}
