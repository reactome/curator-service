package org.reactome.server.service.controller.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.neo4j.driver.*;
import org.reactome.server.service.model.GKInstance;
import org.reactome.server.service.model.Instance;
import org.reactome.server.service.params.*;
import org.reactome.server.service.persistence.AttributeQueryRequest;
import org.reactome.server.service.persistence.Neo4JAdaptor;
import org.reactome.server.service.persistence.QueryRequest;
import org.reactome.server.service.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author info@datasome.co.uk
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "database", description = "Reactome Data: Database info queries")
@RequestMapping("/data")
public class CurationController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private Neo4JAdaptor neo4JAdaptor;

    @Operation(summary = "The Schema")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/fetch/schema", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Schema getSchema() {
        infoLogger.info("Request for the Schema");
        return neo4JAdaptor.getSchema();
    }

    @Operation(summary = "The release number")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/fetch/releasenumber", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public Integer getReleaseNumber() throws Exception {
        infoLogger.info("Request for the release number");
        return neo4JAdaptor.getReleaseNumber();
    }

    @Operation(summary = "The Maximum DB_ID")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/fetch/maxdbid", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getMaxDB_ID() {
        infoLogger.info("Request for the maximum DB_ID");
        return Long.toString(neo4JAdaptor.fetchMaxDbId());
    }


    @Operation(summary = "Refresh the cache")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/cache/refresh", method = RequestMethod.GET)
    @ResponseBody
    public void refreshCache() {
        infoLogger.info("Request to refresh the cache");
        neo4JAdaptor.refreshCaches();
    }

    @Operation(summary = "Set the use cache flag")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/cache/use/{flag}", method = RequestMethod.GET)
    @ResponseBody
    public void setUseCache(@Parameter(description = "Flag", example = "true", required = true)
                            @PathVariable String flag
    ) {
        infoLogger.info("Set use cache flag to the one specified");
        neo4JAdaptor.setUseCache(Boolean.parseBoolean(flag));
    }

    @Operation(summary = "Check if the instance cache is being used")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/cache/isused", method = RequestMethod.GET)
    @ResponseBody
    public Boolean isCacheUsed() {
        infoLogger.info("Request to check if the instance cache is being used");
        return neo4JAdaptor.isUseCache();
    }

    @Operation(summary = "Clean up cache, close connection, unset schema")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/cleanup", method = RequestMethod.GET)
    @ResponseBody
    public void cleanUp() throws Exception {
        infoLogger.info("Request to clean up cache, close connection, unset schema");
        neo4JAdaptor.cleanUp();
    }

    @Operation(summary = "Return a set of DB_IDs that exist and are (or ar not - depending on inverse parameter value) in the list provided")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/fetch/existingdbids", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<Long> fetchExistingDB_IDs(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, a flag to search cache, " +
                            "and a flag to search for intersection or disjunctive union of existing DB_Ids with the list provided",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [5263598], \"checkCache\" : \"true\", \"inverse\" : \"false\"}"))

            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Check if which existing DB_Ids are (or are not, depending on parameter) in the provided list");
        ObjectMapper objectMapper = new ObjectMapper();
        ExistingInstancesData postData = objectMapper.convertValue(objectMapper.readTree(post), ExistingInstancesData.class);
        Set<Long> existingDB_IDs = neo4JAdaptor.existing(postData.getDbIds(), postData.getCheckCache(), postData.getInverse());
        return existingDB_IDs;
    }
    
    @Operation(summary = "Fetch instance by DB_ID")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/fetch/{DB_ID}", method = RequestMethod.GET)
    @ResponseBody
    public Instance fetchInstance(@Parameter(description = "DB_ID", example = "5263598", required = true)
                            @PathVariable Long DB_ID
    ) throws Exception {
        infoLogger.info("Fetch instance by DB_ID");
        return neo4JAdaptor.fetchInstance(Long.parseLong(String.valueOf(DB_ID)));
    }

    @Operation(summary = "Fetch instances by a list of class names and, optionally, by a list of DB_IDs")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/fetch/byclassname", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<Instance> fetchInstances(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a collection of class names " +
                            "(DB_ID in position N of the first collection corresponds to the class name in position N of the second collection",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [5263598], \"classNames\" : [\"PathwayDiagram\"]}"))

            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Fetch instances for a collection of class names and, optionally, by a list of DB_IDs. " +
                "If DB_IDs are provided, DB_ID in position N of the first collection corresponds to the class name in position N of the second collection");
        ObjectMapper objectMapper = new ObjectMapper();
        InstancesClassData postData = objectMapper.convertValue(objectMapper.readTree(post), InstancesClassData.class);
        List<Long> dbIds = postData.getDbIds();
        Collection<Instance> instances = new ArrayList<>();
        List<String> classNames = postData.getClassNames();
        if (dbIds.size() == 0) {
            for (String className : classNames) {
                infoLogger.info("Fetch instances for a className: " + className);
                instances.addAll(neo4JAdaptor.fetchInstancesByClass(className));
            }
        } else {
            // dbIds are provided
            boolean classNamesPresent = classNames.size() > 0;
            if (classNamesPresent && dbIds.size() != classNames.size()) {
                throw new Exception("If the list of DB_IDs is not empty, the list " +
                        "of classNames should be the same size as that of DB_IDs ");
            } else {
                if (classNamesPresent) {
                    int cnt = 0;
                    for (Long dbId : dbIds) {
                        instances.addAll(neo4JAdaptor.fetchInstances(classNames.get(cnt), Collections.singletonList(dbId)));
                        cnt++;
                    }
                } else {
                    instances.addAll(neo4JAdaptor.fetchInstance(dbIds));
                }
            }
        }
        return instances;
    }

    @Operation(summary = "Tries to get instances of class in the list of class names, with DB_ID in list provided " +
            "(DB_ID in position N of the first collection corresponds to the class name in position N of the second collection) " +
            " from instance cache, if possible.  Otherwise, creates a new instance with " +
            " the given DB_ID.  This new instance will be cached even if caching is switched off.")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/get/byclassname", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<Instance> getInstances(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a collection of class names - both of the same size",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [5263598], \"classNames\" : [\"PathwayDiagram\"]}"))
            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Fetch instances for a collection of DB_IDs");
        ObjectMapper objectMapper = new ObjectMapper();
        InstancesClassData postData = objectMapper.convertValue(objectMapper.readTree(post), InstancesClassData.class);
        List<Long> dbIds = postData.getDbIds();
        List<String> classNames = postData.getClassNames();
        List<Instance> instances = new ArrayList();
        if (dbIds.size() != classNames.size()) {
            throw new Exception("If the list of DB_IDs is not empty, the list " +
                    "of classNames should be the same size as that of DB_IDs ");
        } else {
            int cnt = 0;
            for (Long dbId : dbIds) {
                instances.add(neo4JAdaptor.getInstance(classNames.get(cnt), dbId));
                cnt++;
            }
        }
        return instances;
    }

    @Operation(summary = "Fetch instances by a list of quadruples: className, attributeName, operator and value - returns max. 100 records")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/fetch/byattributevalues", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<Instance> fetchInstancesByAttributeValues(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, a collection of class-attribute name tuples and a flag for recursive attribute value retrieval",
                    required = true,
                    // By String attribute value
                    //content = @Content(examples = @ExampleObject("[[\"Pathway\", \"_displayName\", \"=\", \"Deregulating Cellular Energetics\"]]"))
                    // By DB_ID
                    //content = @Content(examples = @ExampleObject("[[\"Pathway\", \"hasEvent\", \"\", \"5492241\"]]"))
                    // By LIKE
                    // content = @Content(examples = @ExampleObject("[[\"Pathway\", \"_displayName\", \"LIKE\", \"NFKbeta\"]]"))
                    // By NOT LIKE
                    //content = @Content(examples = @ExampleObject("[[\"Pathway\", \"_displayName\", \"NOT LIKE\", \"NFKbeta\"]]"))
                    // By REGEXP
                    content = @Content(examples = @ExampleObject("[[\"Pathway\", \"_displayName\", \"REGEXP\", \".*NFKbeta.*\"]]"))

            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Fetch instances by a list of quadruples: className, attributeName, operator and value");
        ObjectMapper objectMapper = new ObjectMapper();
        List<List<String>> postData = objectMapper.convertValue(objectMapper.readTree(post), List.class);
        List<QueryRequest> aqrList = new ArrayList<>();
        for (List<String> rec : postData) {
            String className = rec.get(0);
            String attributeName = rec.get(1);
            String operator = rec.get(2);
            Object attributeValue;
            try {
                attributeValue = Long.parseLong(rec.get(3));
            } catch (NumberFormatException nfe) {
                attributeValue = rec.get(3);
            }
            aqrList.add(new AttributeQueryRequest(neo4JAdaptor.getSchema(), className, attributeName, operator, attributeValue));
        }
        Set<Instance> instances = neo4JAdaptor.fetchInstance(aqrList);
        // Sanity filter: Return maximum 100 instances
        if (instances.size() > 100) {
            return new ArrayList<>(instances).subList(0, 100);
        }
        return instances;
    }

    @Operation(summary = "Load into memory instance attribute values for a collection of DB_IDs and a collection of class-attribute name tuples")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/attributes/load", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void loadInstanceAttributeValues(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, a collection of class-attribute name tuples and a flag for recursive attribute value retrieval",
                    required = true,
                    // 1. A list of attribute names, each in a specific class
                    // content = @Content(examples = @ExampleObject("{ \"dbIds\" : [5263598], \"classAttributeNames\" : [[\"PathwayDiagram\",\"storedATXML\"]], \"recursive\" : \"true\"}"))
                    // 2. A list of attribute names, each in any class
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [5263598], \"classAttributeNames\" : [[\"\",\"storedATXML\"]], \"recursive\" : \"true\"}"))
                    // 3. All attributes of instance with dbId: 5263598 and all subclasses of that instance's class
                    // content = @Content(examples = @ExampleObject("{ \"dbIds\" : [5263598], \"classAttributeNames\" : [], \"recursive\" : \"true\"}"))
            )
            @RequestBody String post) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        InstancesAttributesRecursiveData postData = objectMapper.convertValue(objectMapper.readTree(post), InstancesAttributesRecursiveData.class);
        Collection<Instance> instances = neo4JAdaptor.fetchInstance(postData.getDbIds());
        Set<SchemaClass> classes = new HashSet();
        Set<SchemaAttribute> attributes = new HashSet();
        if (postData.getClassAttributeNames().size() == 0) {
            // Retrieve all classes of instances and their subclasses recursively
            for (Iterator ii = instances.iterator(); ii.hasNext(); ) {
                GKInstance i = (GKInstance) ii.next();
                classes.add(i.getSchemClass());
            }
            Set tmp = new HashSet();
            Set tmp2 = new HashSet();
            tmp.addAll(classes);
            //Find the subclasses of the classes
            while (!tmp.isEmpty()) {
                for (Iterator ti = tmp.iterator(); ti.hasNext(); ) {
                    GKSchemaClass c = (GKSchemaClass) ti.next();
                    tmp2.addAll(c.getSubClasses());
                }
                tmp.clear();
                tmp.addAll(tmp2);
                classes.addAll(tmp2);
                tmp2.clear();
            }
            //Find all original attributes that those classes have
            for (Iterator ci = classes.iterator(); ci.hasNext(); ) {
                GKSchemaClass cls = (GKSchemaClass) ci.next();
                attributes.addAll(cls.getAttributes());
            }
        } else {
            for (List<String> rec : postData.getClassAttributeNames()) {
                String className = rec.get(0);
                String attributeName = rec.get(1);
                if (!className.equals("")) {
                    SchemaClass sc = neo4JAdaptor.getSchema().getClassByName(className);
                    attributes.add(sc.getAttribute(attributeName));
                } else {
                    GKSchema s = (GKSchema) neo4JAdaptor.getSchema();
                    attributes.addAll(s.getOriginalAttributesByName(attributeName));
                }
            }
        }
        neo4JAdaptor.loadInstanceAttributeValues(instances, attributes, postData.getRecursive());
        if (postData.getClassAttributeNames().size() == 0) {
            // We must have loaded all attributes - hence set the isInflated flag to true
            for (Iterator ii = instances.iterator(); ii.hasNext(); ) {
                ((GKInstance) ii.next()).setIsInflated(true);
            }
        }
        System.out.println(((GKInstance) instances.iterator().next()).getAttributes().keySet());
    }

    @Operation(summary = "Load into memory referrers to instances corresponding to collection of DB_IDs and a collection of attribute names")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/attributes/reverse/load", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void loadInstanceReverseAttributeValues(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a collection of attribute names",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [9612973], \"attributeNames\" : [\"hasEvent\"]}"))
            )
            @RequestBody String post) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        InstancesAttributesData postData = objectMapper.convertValue(objectMapper.readTree(post), InstancesAttributesData.class);
        Collection<Instance> instances = neo4JAdaptor.fetchInstance(postData.getDbIds());
        List<String> attributeNames = postData.getAttributeNames();
        String[] array = new String[attributeNames.size()];
        attributeNames.toArray(array);
        neo4JAdaptor.loadInstanceReverseAttributeValues(instances, array);
    }


    @Operation(
            summary = "Retrieve instance count for a given Class name"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/{className}/count", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getClassInstanceCount(@Parameter(description = "Class Name", example = "Pathway", required = true)
                                        @PathVariable String className) throws Exception {
        return Long.toString(neo4JAdaptor.getClassInstanceCount(className));
    }

    @Operation(
            summary = "Retrieve instance count for a given Class name"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Long> getAllInstanceCounts() throws Exception {
        return neo4JAdaptor.getAllInstanceCounts();
    }

    @Operation(
            summary = "EWAS modifications"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/fetch/ewas_modifications", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<List<Long>> fetchEWASModifications() throws Exception {
        return neo4JAdaptor.fetchEWASModifications();
    }

    @Operation(
            summary = "Stable Identifiers With Duplicate DB_IDs"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/fetch/stable_identifiers_with_dup_dbids", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> fetchStableIdentifiersWithDuplicateDBIds() {
        List<Map<String, Object>> sIds = neo4JAdaptor.fetchStableIdentifiersWithDuplicateDBIds();
        for (Map<String, Object> rec : sIds) {
            for (String key : rec.keySet()) {
                String strVal;
                if (key.equals("DB_ID")) {
                    strVal = (Long.toString(((Value) rec.get(key)).asLong()));
                } else {
                    strVal = ((Value) rec.get(key)).asString();
                }
                rec.put(key, strVal);
            }
        }
        return sIds;
    }

    /************* Write/Update/Delete End-points ***************/

    @Operation(summary = "Mint a new DB_ID")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/mint/dbid", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String mintNewDBID() throws Exception {
        infoLogger.info("Request to mint a new DB_ID");
        return Long.toString(neo4JAdaptor.mintNewDBID());
    }

    @Operation(summary = "Update in Neo4J an attribute value in instance(s) corresponding to the list of DB_IDs provided")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/attributes/updateindb", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void updateInstanceAttributes(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a collection of attribute names",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [9612973], \"attributeNames\" : [\"_displayName\"]}"))
            )
            @RequestBody String post) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        InstancesAttributesData postData =
                objectMapper.convertValue(objectMapper.readTree(post), InstancesAttributesData.class);
        List<String> attributeNames = postData.getAttributeNames();
        for (Long dbId : postData.getDbIds()) {
            // The assumption is that the instance is in the cache (hence className argument below set to null),
            // and that the attribute value has been updated in the cached instance but not yet in Neo4J
            Instance instance = neo4JAdaptor.getInstance(null, dbId);
            Driver driver = neo4JAdaptor.getConnection();
            try (Session session = driver.session(SessionConfig.forDatabase(neo4JAdaptor.getDBName()))) {
                Transaction tx = session.beginTransaction();
                for (String attributeName : attributeNames) {
                    neo4JAdaptor.updateInstanceAttribute((GKInstance) instance, attributeName, tx);
                }
                tx.commit();
            }
        }
    }

    @Operation(summary = "Update in the cache attribute values in instance(s) corresponding to the list of DB_IDs provided")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/attributes/updateincache", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void updateInstanceAttributesInCache(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a collection of attribute names and a collection of values - " +
                            "the same collection of values will be assigned to each attribute",
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            "{ \"dbIds\" : [9612973], \"className\" : \"Pathway\", " +
                                    "\"attributeNames\" : [\"_displayName\"], \"values\" : [\"Autophagy1\"]}"))
                    /* content = @Content(examples = @ExampleObject(
                    "{ \"dbIds\" : [9612973], \"className\" : \"Pathway\", " +
                            "\"attributeNames\" : [\"hasEvent\"], \"values\" : [\"69273\", \"69756\"]}")) */
            )
            @RequestBody String post) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        InstancesAttributesValuesData postData =
                objectMapper.convertValue(objectMapper.readTree(post), InstancesAttributesValuesData.class);
        List<Long> dbIds = postData.getDbIds();
        String className = postData.getClassName();
        List<String> attributeNames = postData.getAttributeNames();
        List<String> values = postData.getValues();
        if (dbIds.size() == 0 || attributeNames.size() == 0 || values.size() == 0) {
            throw new Exception("One of the required parameters is missing");
        }
        List<Instance> valueInstances = null;
        for (Long dbId : dbIds) {
            Instance instance = neo4JAdaptor.getInstance(className, dbId);
            SchemaClass sc = instance.getSchemClass();
            if (valueInstances == null && attributeNames.size() > 0) {
                valueInstances = new ArrayList(values.size());
                GKSchemaAttribute att = (GKSchemaAttribute) sc.getAttribute(attributeNames.get(0));
                if (att.isInstanceTypeAttribute()) {
                    String allowedClassName = ((GKSchemaClass) att.getAllowedClasses().iterator().next()).getName();
                    for (String value : values) {
                        try {
                            Long valueDBID = Long.parseLong(value);
                            valueInstances.add(neo4JAdaptor.getInstance(allowedClassName, valueDBID));
                        } catch (NumberFormatException e) {
                            throw new Exception("Could not parse DB_ID in: " + value);
                        }
                    }
                }
            }

            for (String attributeName : attributeNames) {
                GKSchemaAttribute att = (GKSchemaAttribute) sc.getAttribute(attributeName);
                if (att.isMultiple()) {
                    if (att.isInstanceTypeAttribute()) {
                        instance.setAttributeValue(att, valueInstances);
                    } else {
                        instance.setAttributeValue(att, values);
                    }
                } else {
                    if (att.isInstanceTypeAttribute()) {
                        instance.setAttributeValue(att, valueInstances.get(0));
                    } else {
                        instance.setAttributeValue(att, values.get(0));
                    }
                }
            }
        }
    }

    @Operation(summary = "Force-stores in Neo4J instances of class names specified in a list, corresponding to dbIds list provided. " +
            "(DB_ID in position N of the first collection corresponds to the class name in position N of the second collection")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/store", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void storeInstances(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a class",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [], \"classNames\" : []}"))

            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Force-store in Neo4J instances of a list of class names corresponding to dbIds list provided.");
        ObjectMapper objectMapper = new ObjectMapper();
        InstancesClassData postData = objectMapper.convertValue(objectMapper.readTree(post), InstancesClassData.class);
        List<Long> dbIds = postData.getDbIds();
        List<String> classNames = postData.getClassNames();
        Driver driver = neo4JAdaptor.getConnection();
        if (dbIds.size() != classNames.size()) {
            throw new Exception("If the list of DB_IDs is not empty, the list " +
                    "of classNames should be the same size as that of DB_IDs ");
        } else {
            try (Session session = driver.session(SessionConfig.forDatabase(neo4JAdaptor.getDBName()))) {
                Transaction tx = session.beginTransaction();
                int cnt = 0;
                for (Long dbId : dbIds) {
                    GKInstance instance = (GKInstance) neo4JAdaptor.getInstance(classNames.get(cnt), dbId);
                    neo4JAdaptor.storeInstance(instance, true, tx, true);
                    cnt++;
                }
                tx.commit();
            }
        }
    }

    @Operation(summary = "Force-updates in Neo4J instances of a list of class names  corresponding to dbIds list provided." +
            "(DB_ID in position N of the first collection corresponds to the class name in position N of the second collection")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/update", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void updateInstances(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a class",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [], \"classNames\" : []}"))

            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Force-store in Neo4J instances (of class: className) corresponding to dbIds list provided.");
        ObjectMapper objectMapper = new ObjectMapper();
        InstancesClassData postData = objectMapper.convertValue(objectMapper.readTree(post), InstancesClassData.class);
        List<Long> dbIds = postData.getDbIds();
        List<String> classNames = postData.getClassNames();
        if (dbIds.size() != classNames.size()) {
            throw new Exception("If the list of DB_IDs is not empty, the list " +
                    "of classNames should be the same size as that of DB_IDs ");
        } else {
            Driver driver = neo4JAdaptor.getConnection();
            try (Session session = driver.session(SessionConfig.forDatabase(neo4JAdaptor.getDBName()))) {
                Transaction tx = session.beginTransaction();
                int cnt = 0;
                for (Long dbId : dbIds) {
                    GKInstance instance = (GKInstance) neo4JAdaptor.getInstance(classNames.get(cnt), dbId);
                    if (!instance.isInflated()) {
                        throw new Exception("Instance corresponding to DB_ID: " + dbId + " is not inflated - cannot update");
                    }
                    neo4JAdaptor.updateInstance(instance, tx);
                    cnt++;
                }
                tx.commit();
            }
        }
    }

    @Operation(summary = "Delete from Neo4J instances corresponding to dbIds list and class name list provided " +
            "(DB_ID in position N of the first collection corresponds to the class name in position N of the second collection, " +
            "together with their relationships (but not instances at the end of those relationships")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/delete", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void deleteInstances(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [], \"classNames\" : []}"))

            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Delete from Neo4J instances corresponding to dbIds list provided.");
        ObjectMapper objectMapper = new ObjectMapper();
        InstancesClassData postData = objectMapper.convertValue(objectMapper.readTree(post), InstancesClassData.class);
        List<Long> dbIds = postData.getDbIds();
        List<String> classNames = postData.getClassNames();
        Driver driver = neo4JAdaptor.getConnection();
        if (dbIds.size() != classNames.size()) {
            throw new Exception("If the list of DB_IDs is not empty, the list " +
                    "of classNames should be the same size as that of DB_IDs ");
        } else {
            try (Session session = driver.session(SessionConfig.forDatabase(neo4JAdaptor.getDBName()))) {
                Transaction tx = session.beginTransaction();
                int cnt = 0;
                for (Long dbId : dbIds) {
                    GKInstance instance = (GKInstance) neo4JAdaptor.getInstance(classNames.get(cnt), dbId);
                    neo4JAdaptor.deleteInstance(instance, tx);
                    cnt++;
                }
                tx.commit();
            }
        }
    }

    /*****************************
     End-points below are experimental - an attempt to pull together DB insert/update/delete operations
     that have to be done under one transaction.
     *****************************/

    @Operation(summary =
            "Stores instance with DB_ID = storeDbId and set it as value of updateAttribute in " +
                    "another instance (with DB_ID updateDbId). " +
                    "An example of such operation can be found in CuratorTool " +
                    " in e.g. IdentifierDatabase.incrementMostRecentStableIdentifier()")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/storeupdate", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void storeUpdateInstances(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a class",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ " +
                            "\"storeDbId\" : -1, \"storeClassName\" : \"StableIdentifier\", " +
                            "\"updateAttributeName\" : \"mostRecentStableIdentifier\", " +
                            "\"updateDbId\" : -1, \"updateClassName\" : \"State\"}"))

            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Stores instance with DB_ID = storeDbId and set updateAttribute another instance (with DB_ID updateDbId) to the first instance");
        ObjectMapper objectMapper = new ObjectMapper();
        StoreUpdateInstancesData postData = objectMapper.convertValue(objectMapper.readTree(post), StoreUpdateInstancesData.class);
        Long storeDbId = postData.getStoreDbId();
        String storeClassName = postData.getStoreClassName();
        Long updateDbId = postData.getUpdateDbId();
        String updateClassName = postData.getUpdateClassName();
        String updateAttributeName = postData.getUpdateAttributeName();
        GKInstance storeInstance = (GKInstance) neo4JAdaptor.getInstance(storeClassName, storeDbId);
        GKInstance updateInstance = (GKInstance) neo4JAdaptor.getInstance(updateClassName, updateDbId);
        updateInstance.setAttributeValue(updateAttributeName, storeInstance);
        Driver driver = neo4JAdaptor.getConnection();
        try (Session session = driver.session(SessionConfig.forDatabase(neo4JAdaptor.getDBName()))) {
            Transaction tx = session.beginTransaction();
            neo4JAdaptor.storeInstance(storeInstance, tx);
            neo4JAdaptor.updateInstanceAttribute(updateInstance, updateAttributeName, tx);
            tx.commit();
        }
    }
}
