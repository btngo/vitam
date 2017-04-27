package fr.gouv.vitam.access.external.client;

import java.io.InputStream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.access.external.api.AdminCollections;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientNotFoundException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientServerException;
import fr.gouv.vitam.common.GlobalDataRest;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.client.DefaultClient;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientInternalException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.logbook.common.client.ErrorMessage;

/**
 * Rest client implementation for Access External
 */
public class AdminExternalClientRest extends DefaultClient implements AdminExternalClient {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(AdminExternalClientRest.class);

    private static final String URI_NOT_FOUND = "URI not found";
    private static final String REQUEST_PRECONDITION_FAILED = "Request precondition failed";
    private static final String UPDATE_ACCESS_CONTRACT = "/accesscontract";
    private static final String UPDATE_INGEST_CONTRACT = "/contract";

    AdminExternalClientRest(AdminExternalClientFactory factory) {
        super(factory);
    }

    // FIXME replace Response by RequestResponse
    @Override
    public Status checkDocuments(AdminCollections documentType, InputStream stream, Integer tenantId)
        throws AccessExternalClientException {
        Response response = null;
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_TENANT_ID, tenantId);
        try {
            response = performRequest(HttpMethod.PUT, documentType.getName(), headers,
                stream, MediaType.APPLICATION_OCTET_STREAM_TYPE,
                MediaType.APPLICATION_JSON_TYPE);
            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new AccessExternalClientNotFoundException(URI_NOT_FOUND);
            }
            final Status status = Status.fromStatusCode(response.getStatus());
            return status;
        } catch (final VitamClientInternalException e) {
            LOGGER.error(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
            throw new AccessExternalClientServerException(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    // FIXME replace Response by RequestResponse
    @Override
    public Status createDocuments(AdminCollections documentType, InputStream stream, Integer tenantId)
        throws AccessExternalClientException {
        Response response = null;
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_TENANT_ID, tenantId);
        try {
            response = performRequest(HttpMethod.POST, documentType.getName(), headers,
                stream, MediaType.APPLICATION_OCTET_STREAM_TYPE,
                MediaType.APPLICATION_JSON_TYPE);
            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new AccessExternalClientNotFoundException(URI_NOT_FOUND);
            }
            final Status status = Status.fromStatusCode(response.getStatus());
            return status;
        } catch (final VitamClientInternalException e) {
            LOGGER.error(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
            throw new AccessExternalClientServerException(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    @Override
    public RequestResponse findDocuments(AdminCollections documentType, JsonNode select, Integer tenantId)
        throws AccessExternalClientException, InvalidParseOperationException {
        Response response = null;
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_HTTP_METHOD_OVERRIDE, HttpMethod.GET);
        headers.add(GlobalDataRest.X_TENANT_ID, tenantId);
        try {
            response = performRequest(HttpMethod.POST, documentType.getName(), headers,
                select, MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_JSON_TYPE, false);

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new AccessExternalClientNotFoundException(URI_NOT_FOUND);
            } else if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                throw new AccessExternalClientException(REQUEST_PRECONDITION_FAILED);
            }
            return RequestResponse.parseFromResponse(response);
        } catch (final VitamClientInternalException e) {
            LOGGER.error(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
            throw new AccessExternalClientServerException(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    @Override
    public RequestResponse findDocumentById(AdminCollections documentType, String documentId, Integer tenantId)
        throws AccessExternalClientException, InvalidParseOperationException {
        Response response = null;
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_HTTP_METHOD_OVERRIDE, HttpMethod.GET);
        headers.add(GlobalDataRest.X_TENANT_ID, tenantId);
        try {
            response = performRequest(HttpMethod.POST, documentType.getName() + "/" + documentId, headers,
                MediaType.APPLICATION_JSON_TYPE);

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new AccessExternalClientNotFoundException(URI_NOT_FOUND);
            } else if (response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
                throw new AccessExternalClientException(REQUEST_PRECONDITION_FAILED);
            }
            return RequestResponse.parseFromResponse(response);
        } catch (final VitamClientInternalException e) {
            LOGGER.error(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
            throw new AccessExternalClientServerException(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }


    @Override
    public RequestResponse importContracts(InputStream contracts, Integer tenantId, AdminCollections collection)
        throws AccessExternalClientException {
        // FIXME : should be replaced by createDocuments
        ParametersChecker.checkParameter("The input contracts json is mandatory", contracts, collection);
        Response response = null;
        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_TENANT_ID, tenantId);
        try {
            response = performRequest(HttpMethod.POST, collection.getName(), headers,
                contracts, MediaType.APPLICATION_OCTET_STREAM_TYPE,
                MediaType.APPLICATION_JSON_TYPE);
            // FIXME quick fix for response OK, adapt response for all response types
            if (response.getStatus() == Response.Status.OK.getStatusCode() ||
                response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                return new RequestResponseOK();
            } else {
                return RequestResponse.parseFromResponse(response);
            }
        } catch (final VitamClientInternalException e) {
            LOGGER.error(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
            throw new AccessExternalClientException(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    @Override
    public RequestResponse updateAccessContract(JsonNode queryDsl, Integer tenantId)
        throws AccessExternalClientException {
        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_TENANT_ID, tenantId);
        Response response = null;
        try {
            response = performRequest(HttpMethod.PUT, UPDATE_ACCESS_CONTRACT, headers,
                queryDsl, MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_JSON_TYPE);
            return RequestResponse.parseFromResponse(response);
        } catch (final VitamClientInternalException e) {
            LOGGER.error(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
            throw new AccessExternalClientException(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    @Override
    public RequestResponse updateIngestContract(JsonNode queryDsl, Integer tenantId)
        throws InvalidParseOperationException, AccessExternalClientException {
        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_TENANT_ID, tenantId);
        Response response = null;
        try {
            response = performRequest(HttpMethod.PUT, UPDATE_INGEST_CONTRACT, headers,
                queryDsl, MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_JSON_TYPE);
            return RequestResponse.parseFromResponse(response);
        } catch (final VitamClientInternalException e) {
            LOGGER.error(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
            throw new AccessExternalClientException(ErrorMessage.INTERNAL_SERVER_ERROR.getMessage(), e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

}
