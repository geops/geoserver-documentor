/**
 * TODO:
 * - layergroups
 */

package de.geops.geoserver.documentor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.KeywordInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.web.GeoServerApplication;
import org.geotools.jdbc.RegexpValidator;
import org.geotools.jdbc.VirtualTable;
import org.geotools.jdbc.VirtualTableParameter;
import org.geotools.jdbc.VirtualTableParameter.Validator;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import de.geops.geoserver.documentor.info.FeatureTypeDoc;
import de.geops.geoserver.documentor.info.LayerDoc;
import de.geops.geoserver.documentor.info.PropertyDoc;
import de.geops.geoserver.documentor.info.SqlViewDoc;
import de.geops.geoserver.documentor.info.SqlViewParameterDoc;
import de.geops.geoserver.documentor.info.StoreDoc;
import de.geops.geoserver.documentor.info.WorkspaceDoc;
import de.geops.geoserver.documentor.info.WorkspaceFullDoc;
import de.geops.geoserver.documentor.postgresql.PostgresqlAnalyzer;
import de.geops.geoserver.documentor.postgresql.PostgresqlException;

public class Harvester {

	final Catalog catalog;
	
	private static final Logger LOGGER = Logging.getLogger(Harvester.class);
	
	/**
	 * 
	 */
	public Harvester() {
		catalog = GeoServerApplication.get().getCatalog();
	}
	
	/**
	 * 
	 * @param catalog
	 */
	public Harvester(Catalog catalog) {
		this.catalog = catalog;
	}
	
	/**
	 * 
	 * @param featureTypeInfo
	 * @param storeInfo
	 * @return
	 */
	protected FeatureTypeDoc collectFeatureTypeDoc(FeatureTypeInfo featureTypeInfo, final StoreInfo storeInfo) {
		FeatureTypeDoc featureTypeDoc = new FeatureTypeDoc();
			
		MetadataMap metadata = featureTypeInfo.getMetadata();
		if (metadata.containsKey(FeatureTypeInfo.JDBC_VIRTUAL_TABLE)) {
			VirtualTable vt = (VirtualTable) metadata.get(FeatureTypeInfo.JDBC_VIRTUAL_TABLE);
			if (vt != null) {
				SqlViewDoc sqlViewDoc = new SqlViewDoc();
				sqlViewDoc.setSqlQuery(vt.getSql());
				
				//  parameters
				ArrayList<SqlViewParameterDoc> sqlViewDocParams = new ArrayList<SqlViewParameterDoc>();
				for (final String vtParamName: vt.getParameterNames()) {
					VirtualTableParameter vtParam = vt.getParameter(vtParamName);
					if (vtParam != null) {
						SqlViewParameterDoc paramDoc = new SqlViewParameterDoc();
						paramDoc.setName(vtParam.getName());
						paramDoc.setDefaultValue(vtParam.getDefaultValue());
						
						Validator validator = vtParam.getValidator();
						if (validator != null && validator instanceof RegexpValidator) {
							RegexpValidator reValidator = (RegexpValidator) validator;
							paramDoc.setValidator(reValidator.getPattern().pattern());
						}
	
						sqlViewDocParams.add(paramDoc);
					}
				}
				sqlViewDoc.setParameters(sqlViewDocParams);
				featureTypeDoc.setSqlView(sqlViewDoc);
			}
		}
		
		try {
			FeatureType featureType = featureTypeInfo.getFeatureType();

			ArrayList<PropertyDoc> properties = new ArrayList<PropertyDoc>();
			for (PropertyDescriptor descriptor: featureType.getDescriptors()) {
				PropertyDoc propertyDoc = new PropertyDoc();
				propertyDoc.setName(descriptor.getName().toString());
				propertyDoc.setType(descriptor.getType().getBinding().getSimpleName());
				properties.add(propertyDoc);
			}
			featureTypeDoc.setProperties(properties);
			
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not fetch the featuretype for "+featureTypeInfo.getName(), e);
		}
		

		// postgresql handling
		if (storeInfo.getType().equals("PostGIS")) {
			PostgresqlAnalyzer analyzer = null;
			try {
				 analyzer = new PostgresqlAnalyzer(storeInfo);
				
				SqlViewDoc sqlViewDoc = featureTypeDoc.getSqlView();
				if (sqlViewDoc != null && sqlViewDoc.getSqlQuery() != null) {
					SqlViewQueryGenerator generator = new SqlViewQueryGenerator(sqlViewDoc);
					String generatedQuery = generator.getParameterizedQuery();
					if (generatedQuery != null) {
						analyzer.analyzeQuery(generatedQuery);
					}
				} else {
					analyzer.analyzeTable(featureTypeInfo.getNativeName(), true);	
				}
				featureTypeDoc.setRelatedTables(analyzer.getTableDocs());
			} catch (PostgresqlException e) {
				LOGGER.log(Level.SEVERE, "Could not analyze postgresql contents for feature "+featureTypeInfo.getName(), e);;
			} finally {
				if (analyzer != null) {
					analyzer.dispose();
				}
			}
		}
		return featureTypeDoc;
	}
	
	/**
	 * 
	 * @param layerInfo
	 * @return
	 */
	protected LayerDoc collectLayerDoc(LayerInfo layerInfo) {
		ResourceInfo resourceInfo = layerInfo.getResource();
		StoreInfo storeInfo = resourceInfo.getStore();
		
		LayerDoc layer = new LayerDoc();
		layer.setName(resourceInfo.getName());
		layer.setNativeName(resourceInfo.getNativeName());
		layer.setWorkspaceName(storeInfo.getWorkspace().getName());
		layer.setTitle(resourceInfo.getTitle());
		layer.setDescription(resourceInfo.getAbstract());
		layer.setType(layerInfo.getType().toString().toLowerCase());
		layer.setAdvertized(layerInfo.isAdvertised());
		layer.setEnabled(layerInfo.isEnabled());
		
		ArrayList<String> keywordList = new ArrayList<String>();
		List<KeywordInfo> resourceKeywords = resourceInfo.getKeywords();
		if (resourceKeywords != null) {
			for (KeywordInfo resourceKeyword: resourceKeywords) {
				keywordList.add(resourceKeyword.getValue());
			}
		}
		layer.setKeywords(keywordList);		
		
		layer.setStore(this.collectStoreDoc(storeInfo));
		
		if (resourceInfo instanceof FeatureTypeInfo) {
			layer.setFeatureType(this.collectFeatureTypeDoc((FeatureTypeInfo) resourceInfo, storeInfo));

		}
		return layer;
	}
	
	/**
	 * 
	 * @param storeInfo
	 * @return
	 */
	protected StoreDoc collectStoreDoc(StoreInfo storeInfo) {
		StoreDoc storeDoc = new StoreDoc();
		storeDoc.setType(storeInfo.getType());
		storeDoc.setName(storeInfo.getName());
		storeDoc.setDescription(storeInfo.getDescription());
		return storeDoc;
	}; 
	
	/**
	 * 
	 * @return
	 */
	public List<WorkspaceFullDoc> getComplete() {
		HashMap<String, WorkspaceFullDoc> workspaceFullDocs = new HashMap<String, WorkspaceFullDoc>();
		
		List<LayerInfo> layerInfos = catalog.getLayers();
		for(LayerInfo layerInfo: layerInfos) {
			String layerWorkspaceName = layerInfo.getResource().getStore().getWorkspace().getName();
			
			if (!workspaceFullDocs.containsKey(layerWorkspaceName)) {
				WorkspaceFullDoc newWorkspaceFull = new WorkspaceFullDoc();
				newWorkspaceFull.setName(layerWorkspaceName);
				workspaceFullDocs.put(layerWorkspaceName, newWorkspaceFull);
			}
			WorkspaceFullDoc wsFull = workspaceFullDocs.get(layerWorkspaceName);
			wsFull.getLayers().add(collectLayerDoc(layerInfo));
		}
		return new ArrayList<WorkspaceFullDoc>(workspaceFullDocs.values());
	}
	
	/**
	 * 
	 * @param workspaceName
	 * @return
	 */
	public WorkspaceFullDoc getWorkspaceFull(String workspaceName) {
		WorkspaceFullDoc workspace = new WorkspaceFullDoc();
		workspace.setName(workspaceName);
		
		ArrayList<LayerDoc> layerList = new ArrayList<LayerDoc>();
		List<LayerInfo> layerInfos = catalog.getLayers();
		for(LayerInfo layerInfo: layerInfos) {
			String layerWorkspaceName = layerInfo.getResource().getStore().getWorkspace().getName();
			if (layerWorkspaceName.equals(workspaceName)) {
				layerList.add(collectLayerDoc(layerInfo));
			}
		}
		workspace.setLayers(layerList);
		
		return workspace;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<WorkspaceDoc> getWorkspaces() {
		ArrayList<WorkspaceDoc> workspaceList = new ArrayList<WorkspaceDoc>();
		for (WorkspaceInfo wsInfo: catalog.getWorkspaces()) {
			WorkspaceDoc workspace = new WorkspaceDoc();
			workspace.setName(wsInfo.getName());
			workspaceList.add(workspace);
		}
		return workspaceList;
	}
}
