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
package fr.gouv.vitam.worker.core.handler;

import static fr.gouv.vitam.common.LocalDateUtil.getString;
import static fr.gouv.vitam.logbook.common.server.database.collections.LogbookLifeCycleMongoDbName.eventDateTime;

import java.io.File;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;

import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.IngestWorkflowConstants;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.common.model.UpdateWorkflowConstants;
import fr.gouv.vitam.logbook.common.exception.LogbookClientException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientNotFoundException;
import fr.gouv.vitam.logbook.common.parameters.LogbookTypeProcess;
import fr.gouv.vitam.logbook.common.server.database.collections.LogbookDocument;
import fr.gouv.vitam.logbook.common.server.database.collections.LogbookMongoDbName;
import fr.gouv.vitam.logbook.common.server.database.collections.LogbookOperation;
import fr.gouv.vitam.logbook.lifecycles.client.LogbookLifeCyclesClient;
import fr.gouv.vitam.logbook.lifecycles.client.LogbookLifeCyclesClientFactory;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClient;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClientFactory;
import fr.gouv.vitam.processing.common.exception.ProcessingException;
import fr.gouv.vitam.processing.common.parameter.WorkerParameters;
import fr.gouv.vitam.worker.common.HandlerIO;

/**
 * ListLifecycleTraceabilityAction Plugin
 */
public class ListLifecycleTraceabilityActionHandler extends ActionHandler {
    private static final VitamLogger LOGGER =
        VitamLoggerFactory.getInstance(ListLifecycleTraceabilityActionHandler.class);
    private static final String HANDLER_ID = "PREPARE_LC_TRACEABILITY";

    private static final String JSON_EXTENSION = ".json";

    private HandlerIO handlerIO;
    private boolean asyncIO = false;

    private static final String EVENT_DATE_TIME = eventDateTime.getDbname();

    private static final int LAST_OPERATION_LIFECYCLES_RANK = 0;
    private static final int TRACEABILITY_INFORMATION_RANK = 1;

    /**
     * Empty constructor ListLifecycleTraceabilityActionPlugin
     *
     */
    public ListLifecycleTraceabilityActionHandler() {
        // Empty
    }

    @Override
    public ItemStatus execute(WorkerParameters params, HandlerIO handler) {
        handlerIO = handler;
        final ItemStatus itemStatus = new ItemStatus(HANDLER_ID);
        try {
            selectAndExportLifecycles();
            itemStatus.increment(StatusCode.OK);
        } catch (LogbookClientException e) {
            LOGGER.error("Logbook exception", e);
            itemStatus.increment(StatusCode.FATAL);
        } catch (ProcessingException | InvalidParseOperationException | InvalidCreateOperationException e) {
            LOGGER.error("Processing exception", e);
            itemStatus.increment(StatusCode.KO);
        }
        return new ItemStatus(HANDLER_ID).setItemsStatus(HANDLER_ID,
            itemStatus);
    }


    private void selectAndExportLifecycles()
        throws ProcessingException, InvalidParseOperationException, LogbookClientException,
        InvalidCreateOperationException {
        // implement me
        final LogbookOperation lastTraceabilityOperation = findLastOperationTraceabilityLifecycle();
        LocalDateTime startDate;
        exportLastOperationTraceabilityLifecycle(lastTraceabilityOperation);
        if (lastTraceabilityOperation == null) {
            startDate = LocalDateTime.MIN;
        } else {
            Date date;
            try {
                date = LocalDateUtil.getDate(lastTraceabilityOperation.getString(EVENT_DATE_TIME));
            } catch (ParseException e) {
                throw new InvalidParseOperationException("Invalid date");
            }
            startDate = LocalDateUtil.fromDate(date);
        }
        ObjectNode traceabilityInformations = JsonHandler.createObjectNode();
        traceabilityInformations.put("startDate", getString(startDate));

        long numberUnitLifecycles = 0;
        long numberObjectLifecycles = 0;
        try (LogbookLifeCyclesClient logbookLifeCyclesClient =
            LogbookLifeCyclesClientFactory.getInstance().getClient();) {
            List<JsonNode> unitLifecycles = selectUnitLifeCycleAfterDate(startDate, logbookLifeCyclesClient);
            for (JsonNode unit : unitLifecycles) {
                String unitGuid = unit.get("_id").asText();
                final File unitLifecycleTmpFile = handlerIO.getNewLocalFile(unitGuid);
                JsonHandler.writeAsFile(unit, unitLifecycleTmpFile);
                handlerIO
                    .transferFileToWorkspace(
                        UpdateWorkflowConstants.UNITS_FOLDER + "/" + unitGuid + JSON_EXTENSION,
                        unitLifecycleTmpFile, true, asyncIO);
                numberUnitLifecycles++;
            }

            List<JsonNode> objectGroupLifecycles =
                selectObjectGroupLifeCycleAfterDate(startDate, logbookLifeCyclesClient);
            for (JsonNode objectGroup : objectGroupLifecycles) {
                String objectGroupGuid = objectGroup.get("_id").asText();
                final File oGLifecycleTmpFile = handlerIO.getNewLocalFile(objectGroupGuid);
                JsonHandler.writeAsFile(objectGroup, oGLifecycleTmpFile);
                handlerIO
                    .transferFileToWorkspace(
                        IngestWorkflowConstants.OBJECT_GROUP_FOLDER + "/" + objectGroupGuid + JSON_EXTENSION,
                        oGLifecycleTmpFile, true, asyncIO);
                numberObjectLifecycles++;
            }
            JsonNode lastUnit = unitLifecycles.size() > 0 ? unitLifecycles.get(unitLifecycles.size() - 1) : null;
            JsonNode lastObject =
                objectGroupLifecycles.size() > 0 ? objectGroupLifecycles.get(objectGroupLifecycles.size() - 1) : null;
            JsonNode firstUnit = unitLifecycles.size() > 0 ? unitLifecycles.get(0) : null;
            JsonNode firstObject = objectGroupLifecycles.size() > 0 ? objectGroupLifecycles.get(0) : null;
            traceabilityInformations.put("endDate",
                extractEndDateFromOlderLifecycles(startDate.toString(), lastUnit, lastObject));
            traceabilityInformations.put("startDate",
                extractStartDateFromOlderLifecycles(startDate.toString(), firstUnit, firstObject));
        }
        traceabilityInformations.put("numberUnitLifecycles", numberUnitLifecycles);
        traceabilityInformations.put("numberObjectLifecycles", numberObjectLifecycles);
        // export in workspace
        exportTraceabilityInformation(traceabilityInformations);
    }

    private List<JsonNode> selectUnitLifeCycleAfterDate(LocalDateTime startDate,
        LogbookLifeCyclesClient logbookLifeCyclesClient)
        throws InvalidCreateOperationException, InvalidParseOperationException, LogbookClientException {
        final Query parentQuery = QueryHelper.gte("evDateTime", startDate.toString());
        final Query sonQuery = QueryHelper.gte(LogbookDocument.EVENTS + ".evDateTime", startDate.toString());
        final Select select = new Select();
        select.setQuery(QueryHelper.or().add(parentQuery, sonQuery));
        select.addOrderByAscFilter("evDateTime");
        try {
            RequestResponseOK requestResponseOK =
                RequestResponseOK.getFromJsonNode(logbookLifeCyclesClient.selectUnitLifeCycle(select.getFinalSelect()));
            List<JsonNode> foundUnitLifecycles = requestResponseOK.getResults();
            return foundUnitLifecycles;
        } catch (LogbookClientNotFoundException e) {
            LOGGER.warn("No LFC to be handled");
        }
        return new ArrayList<>();
    }

    private List<JsonNode> selectObjectGroupLifeCycleAfterDate(LocalDateTime startDate,
        LogbookLifeCyclesClient logbookLifeCyclesClient)
        throws InvalidCreateOperationException, InvalidParseOperationException, LogbookClientException {
        final Query parentQuery = QueryHelper.gte("evDateTime", startDate.toString());
        final Query sonQuery = QueryHelper.gte(LogbookDocument.EVENTS + ".evDateTime", startDate.toString());
        final Select select = new Select();
        select.setQuery(QueryHelper.or().add(parentQuery, sonQuery));
        select.addOrderByAscFilter("evDateTime");
        try {
            RequestResponseOK requestResponseOK =
                RequestResponseOK
                    .getFromJsonNode(logbookLifeCyclesClient.selectObjectGroupLifeCycle(select.getFinalSelect()));
            List<JsonNode> foundObjectGroupLifecycles = requestResponseOK.getResults();
            return foundObjectGroupLifecycles;
        } catch (LogbookClientNotFoundException e) {
            LOGGER.warn("No LFC to be handled");
        }
        return new ArrayList<>();
    }


    private LogbookOperation findLastOperationTraceabilityLifecycle()
        throws InvalidCreateOperationException, InvalidParseOperationException, LogbookClientException {
        LogbookOperation lastOperationTraceabilityLifecycle = null;
        final Select select = new Select();
        final Query type = QueryHelper.eq("evTypeProc", LogbookTypeProcess.TRACEABILITY.name());
        final Query findEvent = QueryHelper
            .eq(String.format("%s.%s", LogbookDocument.EVENTS, LogbookMongoDbName.outcomeDetail.getDbname()),
                "LOGBOOK_LC_SECURISATION.OK");

        select.setLimitFilter(0, 1);
        select.setQuery(QueryHelper.and().add(type, findEvent));

        select.addOrderByDescFilter("evDateTime");
        try (LogbookOperationsClient logbookOperationsClient =
            LogbookOperationsClientFactory.getInstance().getClient();) {
            RequestResponseOK requestResponseOK =
                RequestResponseOK.getFromJsonNode(logbookOperationsClient.selectOperation(select.getFinalSelect()));
            List<ObjectNode> foundOperation = requestResponseOK.getResults();
            if (foundOperation != null && foundOperation.size() >= 1) {
                lastOperationTraceabilityLifecycle = new LogbookOperation(foundOperation.get(0));
            }
            return lastOperationTraceabilityLifecycle;
        } catch (LogbookClientNotFoundException e) {
            LOGGER.warn("Logbook not found, this is the first Operation of this type");
        }
        return null;
    }

    private void exportTraceabilityInformation(JsonNode traceabilityInformations)
        throws InvalidParseOperationException, ProcessingException {
        File tempFile = handlerIO.getNewLocalFile(handlerIO.getOutput(TRACEABILITY_INFORMATION_RANK).getPath());
        // Create json file
        JsonHandler.writeAsFile(traceabilityInformations, tempFile);
        handlerIO.addOuputResult(TRACEABILITY_INFORMATION_RANK, tempFile, true, false);
    }

    private void exportLastOperationTraceabilityLifecycle(LogbookOperation lastOperationTraceability)
        throws InvalidParseOperationException, ProcessingException {
        File tempFile = handlerIO.getNewLocalFile(handlerIO.getOutput(LAST_OPERATION_LIFECYCLES_RANK).getPath());
        if (lastOperationTraceability == null) {
            // empty json file
            JsonHandler.writeAsFile(JsonHandler.createObjectNode(), tempFile);
            handlerIO.addOuputResult(LAST_OPERATION_LIFECYCLES_RANK, tempFile, true, false);
        } else {
            // Create json file
            JsonHandler.writeAsFile(lastOperationTraceability, tempFile);
            handlerIO.addOuputResult(LAST_OPERATION_LIFECYCLES_RANK, tempFile, true, false);
        }

    }

    private String extractEndDateFromOlderLifecycles(String startDate, JsonNode unitLifecycle,
        JsonNode objectLifecycle) {
        String evDateTimeUnit = null;
        String evDateTimeObject = null;
        if (unitLifecycle != null) {
            ArrayNode eventsUnit = (ArrayNode) unitLifecycle.get(LogbookDocument.EVENTS);
            JsonNode lastEventUnit = (JsonNode) Iterables.getLast(eventsUnit);
            evDateTimeUnit = lastEventUnit.get(LogbookMongoDbName.eventDateTime.getDbname()).asText();
        }
        if (objectLifecycle != null) {
            ArrayNode eventsObject = (ArrayNode) objectLifecycle.get(LogbookDocument.EVENTS);
            JsonNode lastEventObject = (JsonNode) Iterables.getLast(eventsObject);
            evDateTimeObject = lastEventObject.get(LogbookMongoDbName.eventDateTime.getDbname()).asText();
        }

        if (evDateTimeUnit != null && evDateTimeObject != null) {
            return evDateTimeUnit.compareTo(evDateTimeObject) > 0 ? evDateTimeUnit : evDateTimeObject;
        } else if (evDateTimeUnit != null) {
            return evDateTimeUnit;
        } else if (evDateTimeObject != null) {
            return evDateTimeObject;
        } else {
            return startDate;
        }


    }

    private String extractStartDateFromOlderLifecycles(String startDate, JsonNode unitLifecycle,
        JsonNode objectLifecycle) {
        String evDateTimeUnit = null;
        String evDateTimeObject = null;
        if (unitLifecycle != null) {
            ArrayNode eventsUnit = (ArrayNode) unitLifecycle.get(LogbookDocument.EVENTS);
            JsonNode lastEventUnit = (JsonNode) Iterables.getLast(eventsUnit);
            evDateTimeUnit = lastEventUnit.get(LogbookMongoDbName.eventDateTime.getDbname()).asText();
        }
        if (objectLifecycle != null) {
            ArrayNode eventsObject = (ArrayNode) objectLifecycle.get(LogbookDocument.EVENTS);
            JsonNode lastEventObject = (JsonNode) Iterables.getLast(eventsObject);
            evDateTimeObject = lastEventObject.get(LogbookMongoDbName.eventDateTime.getDbname()).asText();
        }

        if (evDateTimeUnit != null && evDateTimeObject != null) {
            return evDateTimeUnit.compareTo(evDateTimeObject) > 0 ? evDateTimeObject : evDateTimeUnit;
        } else if (evDateTimeUnit != null) {
            return evDateTimeUnit;
        } else if (evDateTimeObject != null) {
            return evDateTimeObject;
        } else {
            return startDate;
        }
    }

    /**
     * @return HANDLER_ID
     */
    public static final String getId() {
        return HANDLER_ID;
    }

    @Override
    public void checkMandatoryIOParameter(HandlerIO handler) throws ProcessingException {
        // Nothing to check
    }
}
