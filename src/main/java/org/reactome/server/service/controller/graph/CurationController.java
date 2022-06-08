package org.reactome.server.service.controller.graph;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reactome.server.service.model.Instance;
import org.reactome.server.service.persistence.Neo4JAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Collections;

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

    @Operation(summary = "The name of current database")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/schemats", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getRelNum() throws Exception {
        infoLogger.info("Request for DatabaseName");
        return neo4JAdaptor.getSchemaTimestamp();
    }

    @Operation(summary = "The name of current database")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(value = "/instance", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<Instance> getInstance() throws Exception {
        infoLogger.info("Request for DatabaseInfo");
        Collection<Instance> instances =
                neo4JAdaptor.fetchInstancesByClass(
                        "ReferenceGeneProduct",
                        Collections.singletonList(48889l));
        return instances;
    }
}
