/*******************************************************************************
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *******************************************************************************/

package fr.gouv.vitam.ingest.internal.client;

import java.io.InputStream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.GlobalDataRest;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.client.DefaultClient;
import fr.gouv.vitam.common.client.IngestCollection;
import fr.gouv.vitam.common.exception.BadRequestException;
import fr.gouv.vitam.common.exception.InternalServerException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.exception.VitamClientInternalException;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.exception.WorkflowNotFoundException;
import fr.gouv.vitam.common.guid.GUID;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.ProcessAction;
import fr.gouv.vitam.logbook.common.parameters.LogbookOperationParameters;


/**
 * Rest client implementation for Ingest Internal
 */
class IngestInternalClientRest extends DefaultClient implements IngestInternalClient {


    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(IngestInternalClientRest.class);

    private static final String PROCESS_WORKFLOW_NOT_FOUND_FOR_OPERATION = "Process Workflow not found for operation :";
    private static final String REQUEST_PRECONDITION_FAILED = "Request precondition failed";
    private static final String INVALID_PARSE_OPERATION = "Invalid Parse Operation";
    private static final String NOT_FOUND_EXCEPTION = "Not Found Exception";
    private static final String UNAUTHORIZED = "Unauthorized";


    private static final String LOGBOOK_URL = "/logbooks";
    private static final String INGEST_URL = "/ingests";
    private static final String BLANK_OBJECT_ID = "object identifier should be filled";
    private static final String BLANK_TYPE = "Type should be filled";

    private static final String REPORT = "/report";
    private static final String CONTEXT_ID_MUST_HAVE_A_VALID_VALUE = "Context id must have a valid value";
    private static final String BLANK_OPERATION_ID = "Operation identifier should be filled";
    private static final String OPERATION_URI = "/operations";

    IngestInternalClientRest(IngestInternalClientFactory factory) {
        super(factory);
    }

    @Override
    public Response uploadInitialLogbook(Iterable<LogbookOperationParameters> logbookParametersList)
        throws VitamException {
        ParametersChecker.checkParameter("check Upload Parameter", logbookParametersList);

        Response response = null;
        try {
            response = performRequest(HttpMethod.POST, LOGBOOK_URL, null,
                logbookParametersList, MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_JSON_TYPE, false);
            if (response.getStatus() != Status.CREATED.getStatusCode()) {
                throw new VitamClientException(Status.fromStatusCode(response.getStatus()).getReasonPhrase());
            }
            return Response.fromResponse(response).build();

        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    @Override
    public Response upload(InputStream inputStream, MediaType archiveMimeType, String contextId) throws VitamException {
        ParametersChecker.checkParameter("Params cannot be null", inputStream, archiveMimeType);
        ParametersChecker.checkParameter("context Id Request must not be null",
            contextId);

        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_CONTEXT_ID, contextId);
        headers.add(GlobalDataRest.X_ACTION, ProcessAction.START);

        Response response = null;
        try {
            response = performRequest(HttpMethod.POST, INGEST_URL, headers,
                inputStream, archiveMimeType, MediaType.APPLICATION_OCTET_STREAM_TYPE);

            if (Status.ACCEPTED.getStatusCode() == response.getStatus()) {
                LOGGER.warn("SIP Warning : " + Status.ACCEPTED.getReasonPhrase());
            } else {
                LOGGER.error("SIP Upload Error: " + Status.fromStatusCode(response.getStatus()).getReasonPhrase());
            }
            return response;

        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    // FIXME: replace by void
    @Override
    public Response initWorkFlow(String contextId) throws VitamException {
        ParametersChecker.checkParameter("Params cannot be null", contextId);
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_CONTEXT_ID, contextId);
        headers.add(GlobalDataRest.X_ACTION, ProcessAction.INIT);

        Response response = null;
        try {
            response = performRequest(HttpMethod.POST, INGEST_URL, headers,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
            if (Status.ACCEPTED.getStatusCode() == response.getStatus()) {
                LOGGER.warn("SIP Warning : " + Status.ACCEPTED.getReasonPhrase());
            } else {
                LOGGER.error("SIP Upload Error: " + Status.fromStatusCode(response.getStatus()).getReasonPhrase());
                throw new VitamClientInternalException(Status.fromStatusCode(response.getStatus()).getReasonPhrase());
            }
            return Response.fromResponse(response).build();
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    @Override
    public void uploadFinalLogbook(Iterable<LogbookOperationParameters> logbookParametersList)
        throws VitamClientException {
        ParametersChecker.checkParameter("check Upload Parameter", logbookParametersList);
        Response response = null;
        try {
            response = performRequest(HttpMethod.PUT, LOGBOOK_URL, null,
                logbookParametersList, MediaType.APPLICATION_JSON_TYPE,
                MediaType.APPLICATION_JSON_TYPE, false);
            if (response.getStatus() != Status.OK.getStatusCode()) {
                throw new VitamClientException(Status.fromStatusCode(response.getStatus()).getReasonPhrase());
            }
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    // async
    @Override
    public Response downloadObjectAsync(String objectId, IngestCollection type) throws VitamClientException {

        ParametersChecker.checkParameter(BLANK_OBJECT_ID, objectId);
        ParametersChecker.checkParameter(BLANK_TYPE, type);

        Response response = null;

        try {
            response = performRequest(HttpMethod.GET, INGEST_URL + "/" + objectId + "/" + type.getCollectionName(),
                null, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            return response;
        } catch (final VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientException(e);
        } finally {
            if (response != null && response.getStatus() != Status.OK.getStatusCode()) {
                consumeAnyEntityAndClose(response);
            }
        }
    }

    // TODO response is not used
    @Override
    public Response storeATR(GUID guid, InputStream input) throws VitamClientException {
        Response response = null;

        try {
            response = performRequest(HttpMethod.POST, INGEST_URL + "/" + guid + REPORT,
                null, input, MediaType.APPLICATION_OCTET_STREAM_TYPE,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
            return Response.fromResponse(response).build();

        } catch (VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientException(e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

    @Override
    public Response executeOperationProcess(String operationId, String workflow, String contextId, String actionId)
        throws VitamClientException {
        ParametersChecker.checkParameter(BLANK_OPERATION_ID, operationId);
        ParametersChecker.checkParameter(CONTEXT_ID_MUST_HAVE_A_VALID_VALUE, contextId);

        Response response = null;
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_CONTEXT_ID, contextId);
        try {
            response =
                performRequest(HttpMethod.POST, OPERATION_URI + "/" + operationId, headers,
                    MediaType.APPLICATION_OCTET_STREAM_TYPE);

        } catch (VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientException(e);
        } finally {
            consumeAnyEntityAndClose(response);
        }

        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.NOT_FOUND.getReasonPhrase());
            throw new VitamClientInternalException(NOT_FOUND_EXCEPTION);
        } else if (response.getStatus() == Status.PRECONDITION_FAILED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            throw new VitamClientInternalException(REQUEST_PRECONDITION_FAILED);

        } else if (response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.UNAUTHORIZED.getReasonPhrase());
            throw new VitamClientInternalException(UNAUTHORIZED);
        } else if (response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            throw new VitamClientInternalException(INTERNAL_SERVER_ERROR);
        }
        return Response.fromResponse(response).build();

    }

    // call asynchrone.
    @Override
    public Response updateOperationActionProcess(String actionId, String operationId) throws VitamClientException {
        ParametersChecker.checkParameter(BLANK_OPERATION_ID, operationId);
        Response response = null;
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_ACTION, actionId);
        try {
            response =
                performRequest(HttpMethod.PUT, OPERATION_URI + "/" + operationId,
                    headers,
                    MediaType.APPLICATION_JSON_TYPE);

        } catch (VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientException(e);
        }
        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.NOT_FOUND.getReasonPhrase());
            consumeAnyEntityAndClose(response);
            throw new VitamClientInternalException(NOT_FOUND_EXCEPTION);
        } else if (response.getStatus() == Status.PRECONDITION_FAILED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            consumeAnyEntityAndClose(response);
            throw new VitamClientInternalException(REQUEST_PRECONDITION_FAILED);

        } else if (response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.UNAUTHORIZED.getReasonPhrase());
            consumeAnyEntityAndClose(response);
            throw new ProcessingException(UNAUTHORIZED);
        } else if (response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            consumeAnyEntityAndClose(response);
            throw new VitamClientInternalException(INTERNAL_SERVER_ERROR);
        }
        return response;

    }

    @Override
    public ItemStatus getOperationProcessStatus(String id) throws VitamClientException {
        ParametersChecker.checkParameter(BLANK_OPERATION_ID, id);
        Response response = null;
        try {
            response =
                performRequest(HttpMethod.HEAD, OPERATION_URI + "/" + id,
                    null,
                    MediaType.APPLICATION_JSON_TYPE);
        } catch (VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientException(e);
        }
        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.NOT_FOUND.getReasonPhrase());
            throw new VitamClientInternalException(NOT_FOUND_EXCEPTION);
        } else if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            throw new WorkflowNotFoundException(PROCESS_WORKFLOW_NOT_FOUND_FOR_OPERATION + id);
        } else if (response.getStatus() == Status.PRECONDITION_FAILED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            throw new VitamClientInternalException(REQUEST_PRECONDITION_FAILED);

        } else if (response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.UNAUTHORIZED.getReasonPhrase());
            throw new VitamClientInternalException(UNAUTHORIZED);
        } else if (response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            throw new VitamClientInternalException(INTERNAL_SERVER_ERROR);
        }
        return response.readEntity(ItemStatus.class);
    }

    @Override
    public ItemStatus getOperationProcessExecutionDetails(String id, JsonNode query) throws VitamClientException {
        ParametersChecker.checkParameter(BLANK_OPERATION_ID, id);
        Response response = null;

        try {
            response =
                performRequest(HttpMethod.GET, OPERATION_URI + "/" + id,
                    null,
                    MediaType.APPLICATION_JSON_TYPE);
        } catch (VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientException(e);
        }
        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.NOT_FOUND.getReasonPhrase());
            throw new VitamClientInternalException(NOT_FOUND_EXCEPTION);
        } else if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            throw new WorkflowNotFoundException(PROCESS_WORKFLOW_NOT_FOUND_FOR_OPERATION + id);
        } else if (response.getStatus() == Status.PRECONDITION_FAILED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            throw new VitamClientInternalException(REQUEST_PRECONDITION_FAILED);

        } else if (response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.UNAUTHORIZED.getReasonPhrase());
            throw new VitamClientInternalException(UNAUTHORIZED);
        } else if (response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            throw new VitamClientInternalException(INTERNAL_SERVER_ERROR);
        }
        return response.readEntity(ItemStatus.class);
    }

    @Override
    public Response cancelOperationProcessExecution(String id)
        throws VitamClientException, BadRequestException {
        ParametersChecker.checkParameter(BLANK_OPERATION_ID, id);
        Response response = null;
        try {
            response =
                performRequest(HttpMethod.DELETE, OPERATION_URI + "/" + id, null, MediaType.APPLICATION_JSON_TYPE);

        } catch (VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientException(e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.NOT_FOUND.getReasonPhrase());
            throw new WorkflowNotFoundException(NOT_FOUND_EXCEPTION);
        } else if (response.getStatus() == Status.PRECONDITION_FAILED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            throw new VitamClientInternalException(REQUEST_PRECONDITION_FAILED);
        } else if (response.getStatus() == Status.BAD_REQUEST.getStatusCode() ||
            response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.BAD_REQUEST.getReasonPhrase());
            throw new BadRequestException(UNAUTHORIZED);
        } else if (response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            throw new VitamClientInternalException(INTERNAL_SERVER_ERROR);
        }

        return Response.fromResponse(response).build();
    }

    @Override
    public ItemStatus updateVitamProcess(String contextId, String actionId, String container, String workflow)
        throws InternalServerException, BadRequestException, VitamClientException {
        ParametersChecker.checkParameter(CONTEXT_ID_MUST_HAVE_A_VALID_VALUE, contextId);
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        // FIXME : comparaison between string and enum
        if (actionId.equals(ProcessAction.START)) {
            ParametersChecker.checkParameter(CONTEXT_ID_MUST_HAVE_A_VALID_VALUE, contextId);
            headers.add(GlobalDataRest.X_CONTEXT_ID, contextId);
        }
        Response response = null;
        try {
            response =
                performRequest(HttpMethod.PUT, OPERATION_URI,
                    headers,
                    MediaType.APPLICATION_JSON_TYPE);
        } catch (VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientException(e);
        }
        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.NOT_FOUND.getReasonPhrase());
            throw new VitamClientInternalException(NOT_FOUND_EXCEPTION);
        } else if (response.getStatus() == Status.PRECONDITION_FAILED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            throw new VitamClientInternalException(REQUEST_PRECONDITION_FAILED);

        } else if (response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.UNAUTHORIZED.getReasonPhrase());
            throw new VitamClientInternalException(UNAUTHORIZED);
        } else if (response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            throw new VitamClientInternalException(INTERNAL_SERVER_ERROR);
        }

        return response.readEntity(ItemStatus.class);
    }

    @Override
    public Response initVitamProcess(String contextId, String container, String workFlow)
        throws InternalServerException, VitamClientException, BadRequestException {

        ParametersChecker.checkParameter(CONTEXT_ID_MUST_HAVE_A_VALID_VALUE, contextId);

        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(GlobalDataRest.X_CONTEXT_ID, contextId);
        headers.add(GlobalDataRest.X_ACTION, ProcessAction.INIT);

        Response response = null;
        try {
            response =
                performRequest(HttpMethod.PUT, OPERATION_URI, headers, MediaType.APPLICATION_JSON_TYPE);

        } catch (VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientException(e);
        } finally {
            consumeAnyEntityAndClose(response);
        }

        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.NOT_FOUND.getReasonPhrase());
            throw new VitamClientInternalException(NOT_FOUND_EXCEPTION);
        } else if (response.getStatus() == Status.PRECONDITION_FAILED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            throw new VitamClientInternalException(REQUEST_PRECONDITION_FAILED);

        } else if (response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.UNAUTHORIZED.getReasonPhrase());
            throw new VitamClientInternalException(UNAUTHORIZED);
        } else if (response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            LOGGER.warn("SIP Warning : " + Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            throw new VitamClientInternalException(INTERNAL_SERVER_ERROR);
        }
        return response;
    }


    @Override
    public Response listOperationsDetails() throws VitamClientInternalException {
        Response response = null;
        try {
            response = performRequest(HttpMethod.GET, OPERATION_URI, null, MediaType.APPLICATION_JSON_TYPE);
            return Response.fromResponse(response).build();

        } catch (VitamClientInternalException e) {
            LOGGER.error("VitamClientInternalException: ", e);
            throw new VitamClientInternalException(e);
        } finally {
            consumeAnyEntityAndClose(response);
        }
    }

}
