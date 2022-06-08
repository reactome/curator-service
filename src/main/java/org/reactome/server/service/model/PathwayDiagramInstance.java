/*
 * Created on Dec 15, 2008
 *
 */
package org.reactome.server.service.model;

import java.util.List;

import org.reactome.server.service.schema.SchemaAttribute;
import org.reactome.server.service.schema.SchemaClass;

public class PathwayDiagramInstance extends GKInstance {
    
    public PathwayDiagramInstance() {
        
    }
    
    public PathwayDiagramInstance(SchemaClass schemaClass, 
                                 Long dbId, 
                                 PersistenceAdaptor dbAdaptor) {
        super(schemaClass, dbId, dbAdaptor);
    }

    @Override
    public Object getAttributeValueNoCheck(SchemaAttribute attribute) {
        loadDynamicValue(attribute.getName());
        return super.getAttributeValueNoCheck(attribute);
    }

    @Override
    public List getAttributeValuesList(SchemaAttribute attribute)
            throws Exception {
        loadDynamicValue(attribute.getName());
        return super.getAttributeValuesList(attribute);
    }

    private void loadDynamicValue(String attributeName) {
        if (attributeName.equals(ReactomeJavaConstants.height) ||
            attributeName.equals(ReactomeJavaConstants.width) ||
            attributeName.equals(ReactomeJavaConstants.storedATXML)) {
            try {
                SchemaAttribute attribute = getSchemClass().getAttribute(attributeName);
                getDbAdaptor().loadInstanceAttributeValues(this, 
                                                           attribute);
            }
            catch(Exception e) {
                System.err.println("PathwayDiagramInstance.loadDynamicValue(): " + e);
                e.printStackTrace();
            }
        }
    }

}
