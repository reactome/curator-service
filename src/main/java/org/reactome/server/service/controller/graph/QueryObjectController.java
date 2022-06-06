package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.graph.curator.domain.model.DatabaseObject;
import org.reactome.server.graph.curator.exception.CustomQueryException;
import org.reactome.server.graph.curator.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.curator.service.DatabaseObjectService;
import org.reactome.server.graph.curator.service.helper.RelationshipDirection;
import org.reactome.server.graph.curator.service.util.DatabaseObjectUtils;
import org.reactome.server.service.controller.graph.util.ControllerUtils;
import org.reactome.server.service.exception.NotFoundException;
import org.reactome.server.service.exception.NotFoundTextPlainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
@RestController
@Tag(name = "query", description = "Reactome Data: Common data retrieval")
@RequestMapping("/data")
public class QueryObjectController {

    private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");

    @Autowired
    private DatabaseObjectService databaseObjectService;

    @Autowired
    private AdvancedDatabaseObjectService advancedDatabaseObjectService;

    @Operation(summary = "An entry in Reactome knowledgebase", description = "This method queries for an entry in Reactome knowledgebase based on the given identifier, i.e. stable id or database id. It is worth mentioning that the retrieved database object has all its properties and direct relationships (relationships of depth 1) filled.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data (depth 1)"),
            @ApiResponse(responseCode = "406", description = "Not acceptable according to the accept headers sent in the request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/query/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DatabaseObject findById(@Parameter(description = "DbId or StId of the requested database object", example = "R-HSA-1640170", required = true)
                                   @PathVariable String id) {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, RelationshipDirection.OUTGOING);
        if (databaseObject == null) throw new NotFoundException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for DatabaseObject for id: {}", id);
        return databaseObject;
    }

    @Operation(summary = "A single property of an entry in Reactome knowledgebase", description = "This method queries for a specific property of an entry in Reactome knowledgebase based on the given identifier, i.e. stable id or database id.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data or invalid attribute name"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/query/{id}/{attributeName}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String findById(@Parameter(description = "DbId or StId of the requested database object", example = "R-HSA-1640170", required = true)
                           @PathVariable String id,
                           @Parameter(description = "Attribute to be filtered", example = "_displayName", required = true)
                           @PathVariable String attributeName) throws InvocationTargetException, IllegalAccessException {
        DatabaseObject databaseObject = advancedDatabaseObjectService.findById(id, RelationshipDirection.OUTGOING);
        if (databaseObject == null)
            throw new NotFoundTextPlainException("Id: " + id + " has not been found in the System");
        infoLogger.info("Request for DatabaseObject for id: {}", id);
        return ControllerUtils.getProperty(databaseObject, attributeName);
    }

    @Operation(summary = "A list of entries in Reactome knowledgebase", description = "This method queries for a set of entries in Reactome knowledgebase based on the given list of identifiers. The provided list of identifiers can include stable ids, database ids or a mixture of both. It should be underlined that any duplicated ids are eliminated while only requests containing up to 20 ids are processed.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Identifier does not match with any in current data or invalid attribute name"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/query/ids", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody //TODO: Swagger is not showing the defaultValue
    public Collection<DatabaseObject> findByIds(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "A comma separated list of identifiers",
                    required = true,
                    content = @Content(examples = @ExampleObject("R-HSA-1640170, R-HSA-109581, 199420"))
            )
            @RequestBody String post) {
        Collection<Object> ids = new ArrayList<>();
        for (String id : post.split(",|;|\\n|\\t")) {
            ids.add(id.trim());
        }
        if (ids.size() > 20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Collection<DatabaseObject> databaseObjects = advancedDatabaseObjectService.findByIds(ids, RelationshipDirection.OUTGOING);
        if (databaseObjects == null || databaseObjects.isEmpty())
            throw new NotFoundException("Ids: " + ids.toString() + " have not been found in the System");
        infoLogger.info("Request for DatabaseObjects for ids: {}", ids);
        return databaseObjects;
    }

    @Operation(summary = "A list of entries with their mapping to the provided identifiers", description = "This method queries for a set of entries in Reactome knowledgebase based on the given list of identifiers. The provided list of identifiers can include stable ids, database ids, old stable ids or a mixture of all. It should be underlined that any duplicated ids are eliminated while only requests containing up to 20 ids are processed.<br>This method is particularly useful for users that still rely on the previous version of stable identifiers to query this API. Please note that those are no longer part of the retrieved objects.")
    @RequestMapping(value = "/query/ids/map", method = RequestMethod.POST, produces = "application/json", consumes = "text/plain")
    @ResponseBody //TODO: Swagger is not showing the defaultValue
    public Map<String, DatabaseObject> findByIdsMap(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "A comma separated list of identifiers",
                    required = true,
                    content = @Content(examples = @ExampleObject("R-HSA-1640170, R-HSA-109581, 199420"))
            )
            @RequestBody String post) {
        Collection<String> ids = new ArrayList<>();
        for (String id : post.split(",|;|\\n|\\t")) ids.add(id.trim());
        if (ids.size() > 20) ids = ids.stream().skip(0).limit(20).collect(Collectors.toSet());
        Map<String, DatabaseObject> map = new HashMap<>();
        for (String id : ids) {
            DatabaseObject object = advancedDatabaseObjectService.findById(id, RelationshipDirection.OUTGOING);
            if (object != null) map.put(id, object);
        }
        if (map.isEmpty()) throw new NotFoundException("Ids: " + ids + " have not been found in the System");
        infoLogger.info("Request for DatabaseObjects for ids: {}", ids);
        return map;
    }
}