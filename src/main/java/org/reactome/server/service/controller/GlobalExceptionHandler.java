package org.reactome.server.service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.http.HttpException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.reactome.server.search.exception.SolrSearcherException;
import org.reactome.server.service.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;

/**
 * Global exception handler controller
 * This controller will deal with all exceptions thrown by the other controllers if they don't treat them individually
 * <p>
 * Created by:
 *
 * @author Florian Korninger (florian.korninger@ebi.ac.uk)
 * @since 18.05.16.
 */
@ControllerAdvice
@Hidden
@SuppressWarnings("unused")
class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger("errorLogger");
    private static final Logger onlyEmailLogger = LoggerFactory.getLogger("onlyEmailLogger");

    //================================================================================
    // NotFound
    //================================================================================

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    ResponseEntity<String> handleNotFoundException(HttpServletRequest request, NotFoundException e) {
        //no logging here!
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundTextPlainException.class)
    @ResponseBody
    ResponseEntity<String> handleNotFoundTextPlainException(HttpServletRequest request, NotFoundTextPlainException e) {
        //no logging here!
        return toJsonResponse(HttpStatus.NOT_FOUND, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResultsFoundException.class)
    @ResponseBody
    ResponseEntity<String> handleNoResultsFoundException(HttpServletRequest request, NoResultsFoundException e) {
        //no logging here!
        StringBuffer requestURL = (request == null) ? new StringBuffer("") : request.getRequestURL();
        ErrorInfo errorInfo = new ErrorInfo(HttpStatus.NOT_FOUND, requestURL, e.getTargets(), e.getMessage());

        // TODO targets : "[ { term:"aaa", target:"yes" } ... ]

        return toJsonResponse(HttpStatus.NOT_FOUND, request, errorInfo);
    }

    //================================================================================
    // Interactors
    //================================================================================

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(UnprocessableEntityException.class)
    @ResponseBody
    ResponseEntity<String> handleUnprocessableEntityException(HttpServletRequest request, UnprocessableEntityException e) {
        logger.warn("UnprocessableEntityException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.UNPROCESSABLE_ENTITY, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    ResponseEntity<String> handleMaxUploadSizeExceededException(HttpServletRequest request, MaxUploadSizeExceededException e) {
        logger.warn("UnsupportedMediaTypeException was caught for request: " + request.getRequestURL());
        String msg = "Maximum upload size of " + e.getMaxUploadSize() + " bytes exceeded";
        return toJsonResponse(HttpStatus.PAYLOAD_TOO_LARGE, request, e.getMessage());
    }

    //================================================================================
    // Neo4j
    //================================================================================
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Neo4jException.class)
    @ResponseBody
    ResponseEntity<String> handleNeo4jConnectionException(HttpServletRequest request, Neo4jException e) {
        logger.error("Neo4j ConnectionException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    //================================================================================
    // Default
    //================================================================================

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CertificateException.class)
    @ResponseBody
    ResponseEntity<String> handleCertificateException(HttpServletRequest request, CertificateException e) {
        logger.error("CertificateException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({InvocationTargetException.class, IllegalAccessException.class})
    @ResponseBody
    ResponseEntity<String> handleReflectionError(HttpServletRequest request, Exception e) {
        logger.error("ReflectionException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    ResponseEntity<String> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        logger.warn("IllegalArgumentException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ClassNotFoundException.class)
    @ResponseBody
    ResponseEntity<String> handleClassNotFoundException(HttpServletRequest request, ClassNotFoundException e) {
        logger.warn("ClassNotFoundException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Specified class was not found");
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
        logger.warn("HttpRequestMethodNotSupportedException: " + e.getMessage() + " for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.METHOD_NOT_ALLOWED, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpMediaTypeNotSupportedException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {
        logger.warn("HttpMediaTypeNotSupportedException: " + request.getRequestURL(), e.getMessage());
        return toJsonResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpMediaTypeNotAcceptableException(HttpServletRequest request, HttpMediaTypeNotAcceptableException e) {
        logger.warn("HttpMediaTypeNotSupportedException: " + request.getRequestURL(), e.getMessage());
        return toJsonResponse(HttpStatus.NOT_ACCEPTABLE, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    ResponseEntity<String> handleMethodArgumentTypeMismatchException(HttpServletRequest request, MethodArgumentTypeMismatchException e) {
        logger.warn("MethodArgumentTypeMismatchException: " + request.getRequestURL(), e.getMessage());
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    ResponseEntity<String> handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException e) {
        logger.warn("MissingServletRequestParameterException: " + request.getRequestURL(), e.getMessage());
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {
        logger.warn("HttpMessageNotReadableException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.UNPROCESSABLE_ENTITY, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpException.class)
    @ResponseBody
    ResponseEntity<String> handleHttpRequestException(HttpServletRequest request, HttpException e) {
        logger.warn("HttpRequestException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Cannot connect to Neo4j Server. Please contact Reactome at help@reactome.org.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    ResponseEntity<String> handleClientAbortException(HttpServletRequest request, ClientAbortException e) {
        // Wrap an IOException identifying it as being caused by an abort of a request by a remote client.
        logger.warn("ClientAbortException was caught, we can ignore it");
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DiagramExporterException.class)
    @ResponseBody
    ResponseEntity<String> handleRasterException(HttpServletRequest request, DiagramExporterException e) {
        logger.warn("DiagramExporterException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FireworksExporterException.class)
    @ResponseBody
    ResponseEntity<String> handleFireworksExporterException(HttpServletRequest request, FireworksExporterException e) {
        logger.warn("FireworksExporterException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MissingSBXXException.class)
    @ResponseBody
    ResponseEntity<String> handleMissingSBMLException(HttpServletRequest request, MissingSBXXException e) {
        logger.error("MissingSBMLException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    ResponseEntity<String> handleUnclassified(HttpServletRequest request, Exception e) {
        logger.error("An unspecified exception was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    ResponseEntity<String> handleNullPointerException(HttpServletRequest request, NullPointerException e) {
        onlyEmailLogger.error("NullPointerException was caught for request: " + request.getRequestURL(), e);
        return toJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Something unexpected happened and the error has been reported.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    ResponseEntity<String> handleBadRequestException(HttpServletRequest request, BadRequestException e) {
        logger.warn("BadRequestException was caught for request: " + request.getRequestURL());
        return toJsonResponse(HttpStatus.BAD_REQUEST, request, e.getMessage());
    }

    /*
     * Adding a JSON String manually to the response.
     *
     * Some services return a binary file or text/plain, etc. Then an ErrorInfo instance is manually converted
     * to JSON and written down in the response body.
     */
    private ResponseEntity<String> toJsonResponse(HttpStatus status, HttpServletRequest request, String exceptionMessage) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json");
        try {
            StringBuffer requestURL = (request == null) ? new StringBuffer("") : request.getRequestURL();
            ObjectMapper mapper = new ObjectMapper();
            return ResponseEntity.status(status)
                    .headers(responseHeaders)
                    .body(mapper.writeValueAsString(new ErrorInfo(status, requestURL, exceptionMessage)));
        } catch (JsonProcessingException e1) {
            logger.error("Could not process to JSON the given ErrorInfo instance", e1);
            return ResponseEntity.status(status).headers(responseHeaders).body("");
        }
    }

    private ResponseEntity<String> toJsonResponse(HttpStatus status, HttpServletRequest request, ErrorInfo errorInfo) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            return ResponseEntity.status(status)
                    .headers(responseHeaders)
                    .body(mapper.writeValueAsString(errorInfo));
        } catch (JsonProcessingException e1) {
            logger.error("Could not process to JSON the given ErrorInfo instance", e1);
            return ResponseEntity.status(status).headers(responseHeaders).body("");
        }
    }
}
