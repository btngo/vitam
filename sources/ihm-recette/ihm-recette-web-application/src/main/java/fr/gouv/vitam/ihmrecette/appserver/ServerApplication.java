/**
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
 */
package fr.gouv.vitam.ihmrecette.appserver;

import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.base.Throwables;

import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.ServerIdentity;
import fr.gouv.vitam.common.VitamConfiguration;
import fr.gouv.vitam.common.exception.VitamApplicationServerException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.server.HeaderIdContainerFilter;
import fr.gouv.vitam.common.server.VitamServer;
import fr.gouv.vitam.common.server.application.AbstractVitamApplication;
import fr.gouv.vitam.common.server.application.ConsumeAllAfterResponseFilter;
import fr.gouv.vitam.common.server.application.GenericExceptionMapper;
import fr.gouv.vitam.common.server.application.resources.AdminStatusResource;
import fr.gouv.vitam.common.server.application.resources.VitamServiceRegistry;
import fr.gouv.vitam.ihmrecette.appserver.applicativetest.ApplicativeTestResource;
import fr.gouv.vitam.ihmrecette.appserver.applicativetest.ApplicativeTestService;
import fr.gouv.vitam.ihmrecette.appserver.performance.PerformanceResource;
import fr.gouv.vitam.ihmrecette.appserver.performance.PerformanceService;

/**
 * Server application for ihm-recette
 */
public class ServerApplication extends AbstractVitamApplication<ServerApplication, WebApplicationConfig> {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ServerApplication.class);
    private static final String CONF_FILE_NAME = "ihm-recette.conf";
    private static final String SHIRO_FILE = "shiro.ini";
    private static final String MODULE_NAME = ServerIdentity.getInstance().getRole();

    static VitamServiceRegistry serviceRegistry = null;

    /**
     * ServerApplication constructor
     *
     * @param configuration the ihm-recette server configuration
     */
    public ServerApplication(String configuration) {
        super(WebApplicationConfig.class, configuration);
    }

    /**
     * ServerApplication constructor
     *
     * @param configuration
     */
    ServerApplication(WebApplicationConfig configuration) {
        super(WebApplicationConfig.class, configuration);
    }

    /**
     * Start a service of Test Admin Web Application with the args as config
     *
     * @param args as String
     * @throws URISyntaxException the string could not be passed as a URI reference
     */
    public static void main(String[] args) throws URISyntaxException {
        try {
            if (args == null || args.length == 0) {
                LOGGER.error(format(VitamServer.CONFIG_FILE_IS_A_MANDATORY_ARGUMENT, CONF_FILE_NAME));
                throw new IllegalArgumentException(
                    format(VitamServer.CONFIG_FILE_IS_A_MANDATORY_ARGUMENT, CONF_FILE_NAME));
            }
            final ServerApplication application = new ServerApplication(args[0]);
            if (serviceRegistry == null) {
                LOGGER.error("ServiceRegistry is not allocated");
                System.exit(1);
            }
            serviceRegistry.checkDependencies(VitamConfiguration.getRetryNumber(), VitamConfiguration.getRetryDelay());
            application.run();
        } catch (final Exception e) {
            LOGGER.error(format(VitamServer.SERVER_CAN_NOT_START, MODULE_NAME) + e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void setServiceRegistry(VitamServiceRegistry newServiceRegistry) {
        serviceRegistry = newServiceRegistry;
    }

    @Override
    protected int getSession() {
        return ServletContextHandler.SESSIONS;
    }

    /**
     * replace original implementation to fit specific IHM configuration
     */
    @Override
    protected Handler buildApplicationHandler() throws VitamApplicationServerException {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(JacksonJsonProvider.class)
            .register(JacksonFeature.class)
            // FIXME find a way to remove VitamSession from ihm-recette
            .register(HeaderIdContainerFilter.class)
            // Register a Generic Exception Mapper
            .register(new GenericExceptionMapper());
        // Register Jersey Metrics Listener
        clearAndconfigureMetrics();
        checkJerseyMetrics(resourceConfig);
        // Use chunk size also in response
        resourceConfig.property(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, VitamConfiguration.getChunkSize());
        // Not supported MultiPartFeature.class
        resourceConfig.register(ConsumeAllAfterResponseFilter.class);
        registerInResourceConfig(resourceConfig);

        final ServletContainer servletContainer = new ServletContainer(resourceConfig);
        final ServletHolder sh = new ServletHolder(servletContainer);
        final ServletContextHandler context = new ServletContextHandler(getSession());
        // Removed setContextPath to be set later on for IHM
        context.addServlet(sh, "/*");

        // Cleaner filter
        /*
         * context.addFilter(ConsumeAllAfterResponseFilter.class, "/*", EnumSet.of( DispatcherType.INCLUDE,
         * DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ERROR, DispatcherType.ASYNC));
         */
        // No Authorization Filter

        // Replace here setFilter by adapted one for IHM
        if (getConfiguration().isSecure()) {
            File shiroFile = null;

            try {
                shiroFile = PropertiesUtils.findFile(SHIRO_FILE);
            } catch (final FileNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
                throw new VitamApplicationServerException(e.getMessage());
            }
            context.setInitParameter("shiroConfigLocations", "file:" + shiroFile.getAbsolutePath());
            context.addEventListener(new EnvironmentLoaderListener());
            context.addFilter(ShiroFilter.class, "/*", EnumSet.of(
                DispatcherType.INCLUDE, DispatcherType.REQUEST,
                DispatcherType.FORWARD, DispatcherType.ERROR, DispatcherType.ASYNC));
        }
        context.setContextPath(getConfiguration().getBaseUrl());
        context.setVirtualHosts(new String[] {getConfiguration().getServerHost()});

        // Static Content
        final ResourceHandler staticContentHandler = new ResourceHandler();
        staticContentHandler.setDirectoriesListed(true);
        staticContentHandler.setWelcomeFiles(new String[] {"index.html"});
        staticContentHandler.setResourceBase(getConfiguration().getStaticContent());

        // wrap to context handler
        final ContextHandler staticContext = new ContextHandler("/ihm-recette"); /* the server uri path */
        staticContext.setHandler(staticContentHandler);

        // Set Handlers (Static content and REST API)
        final HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[] {staticContext, context, new DefaultHandler()});
        return handlerList;
    }

    @Override
    protected void registerInResourceConfig(ResourceConfig resourceConfig) {
        setServiceRegistry(new VitamServiceRegistry());
        final WebApplicationResourceDelete deleteResource = new WebApplicationResourceDelete(getConfiguration());
        final WebApplicationResource resource = new WebApplicationResource(getConfiguration().getTenants(), getConfiguration().getSecureMode());
        serviceRegistry.register(deleteResource.getMongoDbAccessAdmin());
        Path sipDirectory = Paths.get(getConfiguration().getSipDirectory());
        Path reportDirectory = Paths.get(getConfiguration().getPerformanceReportDirectory());

        if (!Files.exists(sipDirectory)) {
            Exception sipNotFound =
                new FileNotFoundException(String.format("directory %s does not exist", sipDirectory));
            throw Throwables.propagate(sipNotFound);
        }

        if (!Files.exists(reportDirectory)) {
            Exception reportNotFound =
                new FileNotFoundException(format("directory %s does not exist", reportDirectory));
            throw Throwables.propagate(reportNotFound);
        }

        PerformanceService performanceService = new PerformanceService(sipDirectory, reportDirectory);

        resourceConfig
            .register(resource)
            .register(deleteResource)
            .register(new PerformanceResource(performanceService));

        String testSystemSipDirectory = getConfiguration().getTestSystemSipDirectory();
        String testSystemReportDirectory = getConfiguration().getTestSystemReportDirectory();
        ApplicativeTestService applicativeTestService =
            new ApplicativeTestService(Paths.get(testSystemReportDirectory));

        resourceConfig.register(new ApplicativeTestResource(applicativeTestService,
            testSystemSipDirectory));

    }

    @Override
    protected boolean registerInAdminConfig(ResourceConfig resourceConfig) {
        resourceConfig.register(new AdminStatusResource(serviceRegistry));
        return true;
    }

}
