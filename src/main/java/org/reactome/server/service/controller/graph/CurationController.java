package org.reactome.server.service.controller.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.neo4j.driver.Value;
import org.reactome.server.service.model.GKInstance;
import org.reactome.server.service.model.Instance;
import org.reactome.server.service.params.*;
import org.reactome.server.service.persistence.Neo4JAdaptor;
import org.reactome.server.service.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
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
        infoLogger.info("Request for the Schema");
        neo4JAdaptor.refresh();
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

    @Operation(summary = "Check if cache is being used")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/cache/isused", method = RequestMethod.GET)
    @ResponseBody
    public Boolean isCacheUsed() {
        infoLogger.info("Request to check if cache is being used");
        return neo4JAdaptor.isUseCache();
    }

    @Operation(summary = "Clean up cache, close connection, unset schema")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/cleanup", method = RequestMethod.GET)
    @ResponseBody
    public void cleanUp() throws Exception {
        infoLogger.info("Request for the Schema");
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
                    description = "Json containing a collection of DB_IDs, a flag to search cache, and a flag to search for intersection or disjunctive union of existing DB_Ids with the list provided",
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

    @Operation(summary = "Fetch instances by Class name and, optionally, by a list of DB_IDs")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/fetch/byclassname", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<Instance> fetchInstances(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a class",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [5263598], \"className\" : \"PathwayDiagram\"}"))

            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Fetch instances for a collection of DB_IDs");
        ObjectMapper objectMapper = new ObjectMapper();
        InstancesClassData postData = objectMapper.convertValue(objectMapper.readTree(post), InstancesClassData.class);
        List<Long> dbIds = postData.getDbIds();
        Collection<Instance> instances;
        String className = postData.getClassName();
        if (dbIds.size() == 0) {
            infoLogger.info("Fetch instances for a className");
            instances = neo4JAdaptor.fetchInstancesByClass(className);
        } else {
            if (!className.equals("")) {
                infoLogger.info("Fetch instances for a collection of DB_IDs and a className");
                instances = neo4JAdaptor.fetchInstances(className, dbIds);
            } else {
                instances = neo4JAdaptor.fetchInstance(dbIds);
            }
        }
        return instances;
    }

    @Operation(summary = "Tries to get instances of the given class, with DB_ID in list provided, from " +
            " instance cache, if possible.  Otherwise, creates a new instance with " +
            " the given DB_ID.  This new instance will be cached even if caching is switched off.")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/get/byclassname", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<Instance> getInstance(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Json containing a collection of DB_IDs, and a class",
                    required = true,
                    content = @Content(examples = @ExampleObject("{ \"dbIds\" : [5263598], \"className\" : \"PathwayDiagram\"}"))

            )
            @RequestBody String post) throws Exception {
        infoLogger.info("Fetch instances for a collection of DB_IDs");
        ObjectMapper objectMapper = new ObjectMapper();
        InstancesClassData postData = objectMapper.convertValue(objectMapper.readTree(post), InstancesClassData.class);
        List<Long> dbIds = postData.getDbIds();
        String className = postData.getClassName();
        List<Instance> instances = new ArrayList();
        for (Long dbId : dbIds) {
            instances.add(neo4JAdaptor.getInstance(className, dbId));
        }
        return instances;
    }

    @Operation(summary = "Fetch instances by a list of quadruples: className, attributeName, operator and value")
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
        List<Neo4JAdaptor.QueryRequest> aqrList = new ArrayList<>();
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
            aqrList.add(neo4JAdaptor.createAttributeQueryRequest(className, attributeName, operator, attributeValue));
        }
        Set<Instance> instances = neo4JAdaptor.fetchInstance(aqrList);
        // Sanity filter: Return maximum 100 instances
        if (instances.size() > 100) {
            return new ArrayList<>(instances).subList(0, 100);
        }
        return instances;
    }

    @Operation(summary = "Load instance attribute values for a collection of DB_IDs and a collection of class-attribute name tuples")
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
                    // 3. All attributes of class with dbId: 5263598 and all its subclasses
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
        System.out.println(((GKInstance) instances.iterator().next()).getAttributes().keySet());
    }

    @Operation(summary = "Load referrers to instances corresponding to collection of DB_IDs and a collection of attribute names")
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

    @Operation(summary = "Mint new DB_ID")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/mint/dbid", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String mintNewDBID() throws Exception {
        infoLogger.info("Request for the maximum DB_ID");
        return Long.toString(neo4JAdaptor.mintNewDBID());
    }

    @Operation(summary = "Update in Neo4J an attribute value in instance(s) corresponding to the list of DB_IDs provided")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instances/attributes/updateindb", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public void txUpdateInstanceAttributes(
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
            for (String attributeName : attributeNames) {
                neo4JAdaptor.txUpdateInstanceAttribute((GKInstance) instance, attributeName);
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
                    for (String value : values) {
                        try {
                            Long valueDBID = Long.parseLong(value);
                            // The assumption is that the instance is in the cache (hence className argument below set to null)
                            valueInstances.add(neo4JAdaptor.getInstance(null, valueDBID));
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
}
