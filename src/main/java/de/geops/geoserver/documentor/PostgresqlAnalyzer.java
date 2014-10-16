package de.geops.geoserver.documentor;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import de.geops.geoserver.documentor.info.PropertyDoc;
import de.geops.geoserver.documentor.info.TableDoc;

public class PostgresqlAnalyzer {
	
	private static final Logger LOGGER = Logging.getLogger(PostgresqlAnalyzer.class);

	private String tableSchema = "public";
	
	private final JDBCDataStore datastore;
	private final Connection connection;
	
	protected HashSet<TableDoc> tablesFound = new HashSet<TableDoc>();

	public PostgresqlAnalyzer(StoreInfo storeInfo) {
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
			
		} catch (IOException e) {
			throw new RuntimeException("Could not open Datastore "+storeInfo.getName(), e);
		}
	}
	
	public void analzyeTable(String tableSchema, String tableName, boolean isMainTable) {
		//LOGGER.severe("Analzying table "+tableSchema+"."+tableName);
		this.readTable(tableSchema, tableName, isMainTable);
	}
	
	public void analzyeTable(String tableName, boolean isMainTable) {
		this.analzyeTable(this.tableSchema, tableName, isMainTable);
	}
	
	
	public void analyzeQuery(String query) {
		//LOGGER.severe("Analzying query "+query);
		
		// TODO
	}
	
	protected boolean alreadyRead(String tableSchema, String tableName) {
		TableDoc testTableDoc = new TableDoc();
		testTableDoc.setTableName(tableName);
		testTableDoc.setTableSchema(tableSchema);
		return this.tablesFound.contains(testTableDoc);
	}
	
	public List<TableDoc> getTableDocs() {
		return new ArrayList<TableDoc>(this.tablesFound);
	}
	
	public void dispose() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				LOGGER.warning(e.getMessage());
			}
		}
		if (datastore != null) {
			datastore.dispose();
		}
	}
	
	protected String quote(String str) {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement("select quote_ident(?) as q");
			stmt.setString(1, str);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			return rs.getString("q");
			
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not quote "+str, e);
			throw new RuntimeException(e);
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
	
	protected void readTable(String tableSchema, String tableName, boolean isMainTable) {
		if (alreadyRead(tableSchema, tableName)) {
			return;
		}
		
		String query = "select pc.relname, pns.nspname, pd.description,"
				+" case when pc.relkind = 'r' then 'TABLE'"
				+"      when pc.relkind = 'v' then 'VIEW' end as kind,"
				+" case when pc.relkind = 'v' then pg_get_viewdef(pc.oid, true) end as definition,"
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
			if (rsTable.next()) {
				TableDoc tableDoc = new TableDoc();
				tableDoc.setMainTable(isMainTable);
				tableDoc.setTableName(rsTable.getString("relname"));
				tableDoc.setTableSchema(rsTable.getString("nspname"));
				tableDoc.setComment(rsTable.getString("description"));
				
				String kind = rsTable.getString("kind");
				tableDoc.setType(kind);
				
				// fetch columns for tables
				if (kind != null && kind.equals("TABLE")) {
					stmtColumns = connection.prepareStatement(
							"select pa.attname, pt.typname, pd.description"
							+" from pg_attribute pa"
							+" join pg_type pt on pa.atttypid = pt.oid"
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
				}
				this.tablesFound.add(tableDoc);
			}			
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Counld not execute "+query, e);
			throw new RuntimeException(e);
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
