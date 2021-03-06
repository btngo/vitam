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
package fr.gouv.vitam.common;

import com.google.common.base.Strings;
import fr.gouv.vitam.common.digest.DigestType;
import fr.gouv.vitam.common.logging.SysErrLogger;

import java.io.File;
import java.util.Locale;

/**
 * This class contains default values shared among all services in Vitam
 */
public class VitamConfiguration {

    private static final VitamConfiguration DEFAULT_CONFIGURATION = new VitamConfiguration();

    /* FINAL ATTRIBUTES */
    /**
     * General Admin path
     */
    public static final String ADMIN_PATH = "/admin/v1";
    /**
     * General admin auto test path
     */
    public static final String AUTOTEST_URL = "/autotest";
    /**
     * General admin version path
     */
    public static final String VERSION_URL = "/version";
    /**
     * General status path
     */
    public static final String STATUS_URL = "/status";
    /**
     * General tenants path
     */
    public static final String TENANTS_URL = "/tenants";
    /**
     * Property Vitam Config Folder
     */
    protected static final String VITAM_CONFIG_PROPERTY = "vitam.config.folder";

    /**
     * Property Vitam Data Folder
     */
    protected static final String VITAM_DATA_PROPERTY = "vitam.data.folder";
    /**
     * Property Vitam Log Folder
     */
    protected static final String VITAM_LOG_PROPERTY = "vitam.log.folder";
    /**
     * Property Vitam Tmp Folder
     */
    protected static final String VITAM_TMP_PROPERTY = "vitam.tmp.folder";

    /**
     * Property Vitam Tmp Folder
     */
    private static final String VITAM_JUNIT_PROPERTY = "vitam.test.junit";

    /**
     * Default Connection timeout
     */
    private static final Integer CONNECT_TIMEOUT = 2000;

    /**
     * Max shutdown timeout 2 minute
     */
    private final static long MAX_SHUTDOWN_TIMEOUT = 2 * 60 * 1000;


    /**
     * OTHERS ATTRIBUTES
     */
    /**
     * Default Vitam Config Folder
     */
    private static String vitamConfigFolderDefault = "/vitam/conf";
    /**
     * Default Vitam Config Folder
     */
    private static String vitamDataFolderDefault = "/vitam/data";
    /**
     * Default Vitam Config Folder
     */
    private static String vitamLogFolderDefault = "/vitam/log";
    /**
     * Default Vitam Config Folder
     */
    private static String vitamTmpFolderDefault = "/vitam/data/tmp";



    private static Integer vitamCleanPeriod = 1;

    /**
     * Default Chunk Size
     */
    private static Integer chunkSize = 65536;
    /**
     * Default Recv Buffer Size
     */
    private static Integer recvBufferSize = 0;

    /**
     * Default Read Timeout
     */
    private static Integer readTimeout = 86400000;



    /**
     * Max total concurrent clients
     */
    private static Integer maxTotalClient = 1000;
    /**
     * Max concurrent clients associated to one host
     */
    private static Integer maxClientPerHost = 200;
    /**
     * Max delay to check an unused client in pool before being returned (Apache Only)
     */
    private static Integer delayValidationAfterInactivity = 60000;
    /**
     * Max delay to check if no buffer is available while trying to continue to read (MultipleInputStreamHandler Only)
     * <p>
     * Not final to allow Junit to decrease it
     */
    private static Integer delayMultipleInputstream = 60000;
    /**
     * Max delay to check if no buffer is available while trying to continue to read (SubStreams Only)
     */
    private static Integer delayMultipleSubinputstream = 6000;

    /**
     * Default minimum thread pool size
     */
    private static Integer minimumThreadPoolSize = 100;
    /**
     * No check of unused client within pool (Apache Only)
     */
    private static Integer noValidationAfterInactivity = -1;
    /**
     * Max delay to get a client (Apache Only)
     */
    private static Integer delayGetClient = 60000;

    /**
     * Specify the delay where connections returned to pool will be checked (Apache Only) (5 minutes)
     */
    private static int intervalDelayCheckIdle = 300000;

    /**
     * Specify the delay of unused connection returned in the pool before being really closed (Apache Only) (5 minutes)
     */
    private static int maxDelayUnusedConnection = 300000;

    /**
     * Use a new JAX_RS client each time
     */
    private static Boolean useNewJaxrClient = true;

    /**
     * Default Digest Type for SECURITY
     */
    private static DigestType securityDigestType = DigestType.SHA256;
    /**
     * Default Digest Type for Vitam
     */
    private static DigestType defaultDigestType = DigestType.SHA512;
    /**
     * Default Digest Type for time stamp generation
     */
    private static DigestType defaultTimestampDigestType = DigestType.SHA512;
    /**
     * Acceptable Request Time elaps
     */
    private static Long acceptableRequestTime = 10L;
    /**
     * MongoDB client configuration
     */
    private static Integer threadsAllowedToBlockForConnectionMultipliers = 1500;
    /**
     * Retry repetition
     */
    private static Integer retryNumber = 3;
    /**
     * Retry delay
     */
    private static Integer retryDelay = 30000;
    /**
     * Waiting delay (for wait(delay) method)
     */
    private static Integer waitingDelay = 1000;
    /**
     * Allow client and Server Encoding request or response in GZIP format
     */
    private static Boolean allowGzipEncoding = false;
    /**
     * Allow client to receive GZIP encoded response
     */
    private static Boolean allowGzipDecoding = false;
    /**
     * Read ahead x4 Buffers
     */
    private static Integer bufferNumber = 4;
    // TODO make it configurable
    /**
     * Max concurrent multiple inputstream (memory size bounded = n x bufferNumber * chunkSize)
     */
    private static Integer maxConcurrentMultipleInputstreamHandler = 200;

    /**
     * Should we export #score (only Unit and ObjectGroup)
     */

    private static boolean exportScore = false;

    /**
     * Distributor batch size
     */
    private static int distributeurBatchSize = 100;
    /**
     * Worker bulk size
     */
    private static int workerBulkSize = 10;
    /*
     * Cache delay = 60 seconds
     */
    private static int cacheControlDelay = 60;
    /*
     * Max cache entries
     */
    private static int maxCacheEntries = 10000;

    /**
     * Max Elasticsearch Bulk
     */
    private static int maxElasticsearchBulk = 1000;
    /*
     * Max Thread for ES and MongoDB
     */
    private static int numberDbClientThread = 200;
    /**
     * Max queue in ES
     */
    private static int numberEsQueue = 5000;
    /**
     * Default LANG
     */
    private static String DEFAULT_LANG = Locale.FRENCH.toString();

    private String config;

    private String log;

    private String data;

    private String tmp;

    private static String secret;

    private static Boolean filterActivation;

    private Integer connectTimeout = CONNECT_TIMEOUT;

    private static int asyncWorkspaceQueueSize = 10;



    static {
        getConfiguration().setDefault();
    }

    /**
     * Empty constructor
     */
    VitamConfiguration() {
        // empty
    }

    /**
     * Full argument constructor
     *
     * @param config
     * @param log
     * @param data
     * @param tmp
     * @throws IllegalArgumentException if one argument is null or empty
     */
    VitamConfiguration(String config, String log, String data, String tmp) {
        setConfig(config).setData(data).setLog(log).setTmp(tmp);
        checkValues();
    }

    /**
     * @return the default Vitam Configuration
     */
    public static VitamConfiguration getConfiguration() {
        return DEFAULT_CONFIGURATION;
    }

    /**
     * @param vitamConfiguration
     */
    void setInternalConfiguration(VitamConfiguration vitamConfiguration) {
        setConfig(vitamConfiguration.getConfig())
            .setData(vitamConfiguration.getData())
            .setLog(vitamConfiguration.getLog())
            .setTmp(vitamConfiguration.getTmp()).checkValues();
    }

    /**
     * Replace the default values with values embedded in the VitamConfiguration parameter
     *
     * @param vitamConfiguration the new parameter
     * @throws IllegalArgumentException if one argument is null or empty
     */
    static void setConfiguration(VitamConfiguration vitamConfiguration) {
        DEFAULT_CONFIGURATION.setConfig(vitamConfiguration.getConfig())
            .setData(vitamConfiguration.getData())
            .setLog(vitamConfiguration.getLog())
            .setTmp(vitamConfiguration.getTmp()).checkValues();
    }

    /**
     * Replace the default values with values embedded in the VitamConfiguration parameter
     *
     * @param config
     * @param log
     * @param data
     * @param tmp
     * @throws IllegalArgumentException if one argument is null or empty
     */
    static void setConfiguration(String config, String log, String data, String tmp) {
        DEFAULT_CONFIGURATION.setConfig(config)
            .setData(data).setLog(log).setTmp(tmp).checkValues();
    }

    /**
     * Set the default values
     *
     * @return this
     */
    private VitamConfiguration setDefault() {
        connectTimeout = CONNECT_TIMEOUT;
        checkVitamConfiguration();
        checkValues();
        return this;
    }

    /**
     * Get Config directory
     *
     * @return the Config directory
     */
    public String getConfig() {
        return config;
    }

    /**
     * Set Config directory
     *
     * @param config the config directory
     * @return this
     */
    public VitamConfiguration setConfig(String config) {
        ParametersChecker.checkParameter("Config directory", config);
        this.config = config;
        return this;
    }

    /**
     * Get Log directory
     *
     * @return the Log directory
     */
    public String getLog() {
        return log;
    }

    /**
     * Set Log directory
     *
     * @param log the Log directory
     * @return this
     */
    public VitamConfiguration setLog(String log) {
        ParametersChecker.checkParameter("Log directory", log);
        this.log = log;
        return this;
    }

    /**
     * Get Data directory
     *
     * @return the Data directory
     */
    public String getData() {
        return data;
    }

    /**
     * Set Data directory
     *
     * @param data the Data directory
     * @return this
     */
    public VitamConfiguration setData(String data) {
        ParametersChecker.checkParameter("Data directory", data);
        this.data = data;
        return this;
    }

    /**
     * Get Tmp directory
     *
     * @return the Tmp directory
     */
    public String getTmp() {
        return tmp;
    }

    /**
     * Set Tmp directory
     *
     * @param tmp tmp the Tmp directory
     * @return this
     */
    public VitamConfiguration setTmp(String tmp) {
        ParametersChecker.checkParameter("Tmp directory", tmp);
        this.tmp = tmp;
        return this;
    }

    /**
     * import not null parameters configuration from VitamConfigurationParameters
     *
     * @param parameters
     */
    public static void importConfigurationParameters(VitamConfigurationParameters parameters) {


        if (null != parameters.getVitamConfigFolderDefault()) {
            setVitamConfigFolderDefault(parameters.getVitamConfigFolderDefault());
        }
        if (null != parameters.getVitamDataFolderDefault()) {
            setVitamDataFolderDefault(parameters.getVitamDataFolderDefault());
        }
        if (null != parameters.getVitamLogFolderDefault()) {
            setVitamLogFolderDefault(parameters.getVitamLogFolderDefault());
        }
        if (null != parameters.getVitamTmpFolderDefault()) {
            setVitamTmpFolderDefault(parameters.getVitamTmpFolderDefault());
        }
        if (null != parameters.getChunkSize()) {
            setChunkSize(parameters.getChunkSize());
        }
        if (null != parameters.getRecvBufferSize()) {
            setRecvBufferSize(parameters.getRecvBufferSize());
        }
        if (null != parameters.getRecvBufferSize()) {
            setRecvBufferSize(parameters.getRecvBufferSize());
        }
        if (null != parameters.getConnectTimeout()) {
            setConnectTimeout(parameters.getConnectTimeout());
        }
        if (null != parameters.getReadTimeout()) {
            setReadTimeout(parameters.getReadTimeout());
        }
        if (null != parameters.getMaxTotalClient()) {
            setMaxTotalClient(parameters.getMaxTotalClient());
        }
        if (null != parameters.getMaxClientPerHost()) {
            setMaxClientPerHost(parameters.getMaxClientPerHost());
        }
        if (null != parameters.getDelayValidationAfterInactivity()) {
            setDelayValidationAfterInactivity(parameters.getDelayValidationAfterInactivity());
        }
        if (null != parameters.getDelayMultipleInputstream()) {
            setDelayMultipleInputstream(parameters.getDelayMultipleInputstream());
        }
        if (null != parameters.getDelayMultipleSubinputstream()) {
            setDelayMultipleSubinputstream(parameters.getDelayMultipleSubinputstream());
        }
        if (null != parameters.getMinimumThreadPoolSize()) {
            setMinimumThreadPoolSize(parameters.getMinimumThreadPoolSize());
        }
        if (null != parameters.getNoValidationAfterInactivity()) {
            setNoValidationAfterInactivity(parameters.getNoValidationAfterInactivity());
        }
        if (null != parameters.getDelayGetClient()) {
            setDelayGetClient(parameters.getDelayGetClient());
        }
        if (null != parameters.getIntervalDelayCheckIdle()) {
            setIntervalDelayCheckIdle(parameters.getIntervalDelayCheckIdle());
        }
        if (null != parameters.getMaxDelayUnusedConnection()) {
            setMaxDelayUnusedConnection(parameters.getMaxDelayUnusedConnection());
        }
        if (null != parameters.getSecurityDigestType()) {
            DigestType digestType = DigestType.valueOf(parameters.getSecurityDigestType());
            setSecurityDigestType(digestType);

        }
        if (null != parameters.getDefaultDigestType()) {
            DigestType digestType = DigestType.valueOf(parameters.getDefaultDigestType());
            setDefaultDigestType(digestType);

        }
        if (null != parameters.getDefaultTimestampDigestType()) {
            DigestType digestType = DigestType.valueOf(parameters.getDefaultTimestampDigestType());
            setDefaultTimestampDigestType(digestType);

        }
        if (null != parameters.getAcceptableRequestTime()) {
            setAcceptableRequestTime(parameters.getAcceptableRequestTime());
        }
        if (null != parameters.getThreadsAllowedToBlockForConnectionMultipliers()) {
            setThreadsAllowedToBlockForConnectionMultipliers(
                parameters.getThreadsAllowedToBlockForConnectionMultipliers());
        }
        if (null != parameters.getRetryNumber()) {
            setRetryNumber(parameters.getRetryNumber());
        }
        if (null != parameters.getRetryDelay()) {
            setRetryDelay(parameters.getRetryDelay());
        }
        if (null != parameters.getWaitingDelay()) {
            setWaitingDelay(parameters.getWaitingDelay());
        }
        if (null != parameters.getBufferNumber()) {
            setBufferNumber(parameters.getBufferNumber());
        }
        if (null != parameters.isAllowGzipEncoding()) {
            setAllowGzipEncoding(parameters.isAllowGzipEncoding());
        }
        if (null != parameters.isAllowGzipDecoding()) {
            setAllowGzipDecoding(parameters.isAllowGzipDecoding());
        }
        if (null != parameters.isUseNewJaxrClient()) {
            setUseNewJaxrClient(parameters.isUseNewJaxrClient());
        }
        if (null != parameters.isFilterActivation()) {
            setFilterActivation(parameters.isFilterActivation());
        }
        if (null != parameters.getMaxConcurrentMultipleInputstreamHandler()) {
            setMaxConcurrentMultipleInputstreamHandler(parameters.getMaxConcurrentMultipleInputstreamHandler());
        }
        if (null != parameters.getVitamCleanPeriod()) {
            setVitamCleanPeriod(parameters.getVitamCleanPeriod());
        }
        if (null != parameters.isExportScore()) {
            setExportScore(parameters.isExportScore());
        }
        if (null != parameters.getDistributeurBatchSize()) {
            setDistributeurBatchSize(parameters.getDistributeurBatchSize());
        }
        if (null != parameters.getWorkerBulkSize()) {
            setWorkerBulkSize(parameters.getWorkerBulkSize());
        }
        if (null != parameters.getMaxElasticsearchBulk()) {
            setMaxElasticsearchBulk(parameters.getMaxElasticsearchBulk());
        }
        if (null != parameters.getNumberDbClientThread()) {
            setNumberDbClientThread(parameters.getNumberDbClientThread());
        }
        if (null != parameters.getNumberEsQueue()) {
            setNumberEsQueue(parameters.getNumberEsQueue());
        }
        if (null != parameters.getCacheControlDelay()) {
            setCacheControlDelay(parameters.getCacheControlDelay());
        }
        if (null != parameters.getMaxCacheEntries()) {
            setMaxCacheEntries(parameters.getMaxCacheEntries());
        }

    }

    /**
     * Check if each directory not null and exists
     *
     * @throws IllegalArgumentException if one condition failed
     */
    private void checkValues() {
        ParametersChecker.checkParameter("Check directories", tmp, data, log, config);
        final File tmpDir = new File(tmp);
        final File logDir = new File(log);
        final File dataDir = new File(data);
        final File configDir = new File(config);
        if (!tmpDir.isDirectory()) {
            tmpDir.mkdirs();
        }
        if (!(tmpDir.isDirectory() && logDir.isDirectory() && dataDir.isDirectory() && configDir.isDirectory())) {
            SysErrLogger.FAKE_LOGGER.syserr("One of the directories in the VitamConfiguration is not correct");
        }
    }

    /**
     * Check if Vitam Configuration is specified using directives on JVM. If an issue is detected, it only logs the
     * status on STDERR.
     */
    static void checkVitamConfiguration() {
        if (!(SystemPropertyUtil.contains(VITAM_TMP_PROPERTY) && SystemPropertyUtil.contains(VITAM_CONFIG_PROPERTY) &&
            SystemPropertyUtil.contains(VITAM_DATA_PROPERTY) &&
            SystemPropertyUtil.contains(VITAM_LOG_PROPERTY))) {
            SysErrLogger.FAKE_LOGGER.syserr(
                "One of the directives is not specified: -Dxxx=path where xxx is one of -D" + VITAM_TMP_PROPERTY +
                    " -D" + VITAM_CONFIG_PROPERTY + " -D" + VITAM_DATA_PROPERTY + " -D" + VITAM_LOG_PROPERTY);
        }
        String data = vitamDataFolderDefault;
        if (SystemPropertyUtil.contains(VITAM_DATA_PROPERTY)) {
            data = SystemPropertyUtil.get(VITAM_DATA_PROPERTY);
        }
        String tmp = vitamTmpFolderDefault;
        if (SystemPropertyUtil.contains(VITAM_TMP_PROPERTY)) {
            tmp = SystemPropertyUtil.get(VITAM_TMP_PROPERTY);
        }
        String config = vitamConfigFolderDefault;
        if (SystemPropertyUtil.contains(VITAM_CONFIG_PROPERTY)) {
            config = SystemPropertyUtil.get(VITAM_CONFIG_PROPERTY);
        }
        String log = vitamLogFolderDefault;
        if (SystemPropertyUtil.contains(VITAM_LOG_PROPERTY)) {
            log = SystemPropertyUtil.get(VITAM_LOG_PROPERTY);
        }
        setConfiguration(config, log, data, tmp);
    }

    /**
     * @return the VitamTmpFolder path
     */
    public static String getVitamTmpFolder() {
        if (SystemPropertyUtil.contains(VITAM_TMP_PROPERTY)) {
            return SystemPropertyUtil.get(VITAM_TMP_PROPERTY);
        }
        return getConfiguration().getTmp();
    }

    /**
     * @return the VitamLogFolder path
     */
    public static String getVitamLogFolder() {
        return getConfiguration().getLog();
    }

    /**
     * @return the VitamDataFolder path
     */
    public static String getVitamDataFolder() {
        return getConfiguration().getData();
    }

    /**
     * @return the VitamConfigFolder path
     */
    public static String getVitamConfigFolder() {
        return getConfiguration().getConfig();
    }

    /**
     * @return the default chunk size
     */
    public static Integer getChunkSize() {
        return chunkSize;
    }

    /**
     * @return the default connect timeout
     */
    public static Integer getConnectTimeout() {
        return getConfiguration().connectTimeout;
    }

    /**
     * Junit facility
     *
     * @param timeout to set
     */
    public static void setConnectTimeout(int timeout) {
        getConfiguration().connectTimeout = timeout;
    }

    /**
     * @return the default read timeout
     */
    public static Integer getReadTimeout() {
        return readTimeout;
    }

    /**
     * @return the default read timeout
     */
    public static long getShutdownTimeout() {
        return MAX_SHUTDOWN_TIMEOUT;
    }

    /**
     * @return the maxTotalClient
     */
    public static Integer getMaxTotalClient() {
        return maxTotalClient;
    }

    /**
     * @return the maxClientPerHost
     */
    public static Integer getMaxClientPerHost() {
        return maxClientPerHost;
    }

    /**
     * @return the delayValidationAfterInactivity
     */
    public static Integer getDelayValidationAfterInactivity() {
        return delayValidationAfterInactivity;
    }

    /**
     * @return the delayGetClient
     */
    public static Integer getDelayGetClient() {
        return delayGetClient;
    }

    /**
     * @return the intervalDelayCheckIdle
     */
    public static Integer getIntervalDelayCheckIdle() {
        return intervalDelayCheckIdle;
    }

    /**
     * @return the maxDelayUnusedConnection
     */
    public static Integer getMaxDelayUnusedConnection() {
        return maxDelayUnusedConnection;
    }

    /**
     * @return the secret
     */
    public static String getSecret() {
        if (Strings.isNullOrEmpty(secret)) {
            return "";
        }
        return secret;
    }

    /**
     * @param secretValue the secret to set
     */
    public static void setSecret(String secretValue) {
        ParametersChecker.checkParameter("Platform secret", secretValue);
        secret = secretValue;
    }

    /**
     * @return the filterActivation
     */
    public static Boolean isFilterActivation() {
        return filterActivation;
    }

    /**
     * @param filterActivationValue the filterActivation to set
     */
    public static void setFilterActivation(Boolean filterActivationValue) {
        filterActivation = filterActivationValue;
    }

    /**
     * @return the acceptableRequestTime
     */
    public static Long getAcceptableRequestTime() {
        return acceptableRequestTime;
    }

    /**
     * @return the securityDigestType
     */
    public static DigestType getSecurityDigestType() {
        return securityDigestType;
    }

    /**
     * @return the Default DigestType
     */
    public static DigestType getDefaultDigestType() {
        return defaultDigestType;
    }

    /**
     * @return the Default DigestType for time stamp generation
     */
    public static DigestType getDefaultTimestampDigestType() {
        return defaultTimestampDigestType;
    }


    /**
     * @return the threadsAllowedToBlockForConnectionMultipliers for MongoDb Client
     */
    public static Integer getThreadsAllowedToBlockForConnectionMultipliers() {
        return threadsAllowedToBlockForConnectionMultipliers;
    }

    /**
     * @return the retryNumber
     */
    public static Integer getRetryNumber() {
        return retryNumber;
    }

    /**
     * @return the retryDelay
     */
    public static Integer getRetryDelay() {
        return retryDelay;
    }

    /**
     * @return the waiting Delay (wait)
     */
    public static Integer getWaitingDelay() {
        return waitingDelay;
    }


    /**
     * @return the size of the queue of async workspace
     */
    public static Integer getAsyncWorkspaceQueueSize() {
        return asyncWorkspaceQueueSize;
    }


    /**
     * @return the receive Buffer Size
     */
    public static Integer getRecvBufferSize() {
        return recvBufferSize;
    }

    /**
     * @return the use New Jaxr Client each time a getClient() from Factory is used
     */
    public static Boolean isUseNewJaxrClient() {
        return useNewJaxrClient;
    }


    /**
     * @return true if is integration Test
     */
    public static boolean isIntegrationTest() {
        return SystemPropertyUtil.get(VITAM_JUNIT_PROPERTY, false);
    }

    /**
     * setIntegrationTest
     */
    public static void setIntegrationTest(boolean value) {
        SystemPropertyUtil.set(VITAM_JUNIT_PROPERTY, value);
    }


    /**
     * getter for VITAM_CONFIG_PROPERTY
     *
     * @return
     */
    public static String getVitamConfigProperty() {
        return VITAM_CONFIG_PROPERTY;
    }



    /**
     * getter for VITAM_DATA_PROPERTY
     *
     * @return
     */
    public static String getVitamDataProperty() {
        return VITAM_DATA_PROPERTY;
    }


    /**
     * getter for VITAM_LOG_PROPERTY
     *
     * @return
     */
    public static String getVitamLogProperty() {
        return VITAM_LOG_PROPERTY;
    }

    /**
     * getter for VITAM_TMP_PROPERTY
     *
     * @return
     */
    public static String getVitamTmpProperty() {
        return VITAM_TMP_PROPERTY;
    }


    /**
     * getter for vitamConfigFolderDefault
     *
     * @return
     */
    public static String getVitamConfigFolderDefault() {
        return vitamConfigFolderDefault;
    }

    /**
     * setter for vitamConfigFolderDefault
     *
     * @return
     */
    private static void setVitamConfigFolderDefault(String vitamConfigFolderDefault) {
        VitamConfiguration.vitamConfigFolderDefault = vitamConfigFolderDefault;
    }

    /**
     * getter for vitamDataFolderDefault
     *
     * @return
     */
    public static String getVitamDataFolderDefault() {
        return vitamDataFolderDefault;
    }

    /**
     * setter for vitamDataFolderDefault
     *
     * @return
     */
    private static void setVitamDataFolderDefault(String vitamDataFolderDefault) {
        VitamConfiguration.vitamDataFolderDefault = vitamDataFolderDefault;
    }

    /**
     * getter for vitamLogFolderDefault
     *
     * @return
     */
    public static String getVitamLogFolderDefault() {
        return vitamLogFolderDefault;
    }

    /**
     * setter for vitamLogFolderDefault
     *
     * @return
     */
    private static void setVitamLogFolderDefault(String vitamLogFolderDefault) {
        VitamConfiguration.vitamLogFolderDefault = vitamLogFolderDefault;
    }

    /**
     * getter for vitamTmpFolderDefault
     *
     * @return
     */
    public static Integer getVitamCleanPeriod() {
        return vitamCleanPeriod;
    }


    /**
     * setter for vitamLogFolderDefault
     *
     * @return
     */
    private static void setVitamCleanPeriod(Integer vitamCleanPeriod) {
        VitamConfiguration.vitamCleanPeriod = vitamCleanPeriod;
    }

    /**
     * getter for vitamTmpFolderDefault
     *
     * @return
     */
    public static String getVitamTmpFolderDefault() {
        return vitamTmpFolderDefault;
    }

    /**
     * setter for vitamTmpFolderDefault
     *
     * @return
     */
    private static void setVitamTmpFolderDefault(String vitamTmpFolderDefault) {
        VitamConfiguration.vitamTmpFolderDefault = vitamTmpFolderDefault;
    }

    /**
     * setter for chunkSize
     *
     * @return
     */
    private static void setChunkSize(int chunkSize) {
        VitamConfiguration.chunkSize = chunkSize;
    }

    /**
     * @return the size of the queue of async workspace
     */
    public static void setAsyncWorkspaceQueueSize(int queueSize) {
        asyncWorkspaceQueueSize = queueSize;
    }


    /**
     * setter for recvBufferSize
     *
     * @return
     */
    private static void setRecvBufferSize(int recvBufferSize) {
        VitamConfiguration.recvBufferSize = recvBufferSize;
    }

    /**
     * setter for readTimeout
     *
     * @return
     */
    private static void setReadTimeout(int readTimeout) {
        VitamConfiguration.readTimeout = readTimeout;
    }

    /**
     * setter for maxTotalClient
     *
     * @return
     */
    private static void setMaxTotalClient(int maxTotalClient) {
        VitamConfiguration.maxTotalClient = maxTotalClient;
    }

    /**
     * setter for maxClientPerHost
     *
     * @return
     */
    private static void setMaxClientPerHost(int maxClientPerHost) {
        VitamConfiguration.maxClientPerHost = maxClientPerHost;
    }

    /**
     * setter for delayValidationAfterInactivity
     *
     * @return
     */
    private static void setDelayValidationAfterInactivity(int delayValidationAfterInactivity) {
        VitamConfiguration.delayValidationAfterInactivity = delayValidationAfterInactivity;
    }

    /**
     * getter for delayMultipleInputstream
     *
     * @return
     */
    public static Integer getDelayMultipleInputstream() {
        return delayMultipleInputstream;
    }

    /**
     * setter for delayMultipleSubinputstream
     *
     * @return
     */
    public static void setDelayMultipleInputstream(int delayMultipleInputstream) {
        VitamConfiguration.delayMultipleInputstream = delayMultipleInputstream;
    }

    /**
     * getter for delayMultipleSubinputstream
     *
     * @return
     */
    public static Integer getDelayMultipleSubinputstream() {
        return delayMultipleSubinputstream;
    }

    /**
     * setter for delayMultipleSubinputstream
     *
     * @return
     */
    private static void setDelayMultipleSubinputstream(int delayMultipleSubinputstream) {
        VitamConfiguration.delayMultipleSubinputstream = delayMultipleSubinputstream;
    }

    /**
     * getter for minimumThreadPoolSize
     *
     * @return
     */
    public static Integer getMinimumThreadPoolSize() {
        return minimumThreadPoolSize;
    }

    /**
     * setter for minimumThreadPoolSize
     *
     * @return
     */
    private static void setMinimumThreadPoolSize(int minimumThreadPoolSize) {
        VitamConfiguration.minimumThreadPoolSize = minimumThreadPoolSize;
    }

    /**
     * getter for noValidationAfterInactivity
     *
     * @return
     */
    public static Integer getNoValidationAfterInactivity() {
        return noValidationAfterInactivity;
    }

    /**
     * setter for noValidationAfterInactivity
     *
     * @return
     */
    private static void setNoValidationAfterInactivity(int noValidationAfterInactivity) {
        VitamConfiguration.noValidationAfterInactivity = noValidationAfterInactivity;
    }

    /**
     * setter for delayGetClient
     *
     * @return
     */
    private static void setDelayGetClient(int delayGetClient) {
        VitamConfiguration.delayGetClient = delayGetClient;
    }

    /**
     * setter for intervalDelayCheckIdle
     *
     * @return
     */
    private static void setIntervalDelayCheckIdle(int intervalDelayCheckIdle) {
        VitamConfiguration.intervalDelayCheckIdle = intervalDelayCheckIdle;
    }

    /**
     * setter for maxDelayUnusedConnection
     *
     * @return
     */
    private static void setMaxDelayUnusedConnection(int maxDelayUnusedConnection) {
        VitamConfiguration.maxDelayUnusedConnection = maxDelayUnusedConnection;
    }

    /**
     * setter for useNewJaxrClient
     *
     * @return
     */
    private static void setUseNewJaxrClient(Boolean useNewJaxrClient) {
        VitamConfiguration.useNewJaxrClient = useNewJaxrClient;
    }



    /**
     * setter for securityDigestType
     *
     * @return
     */
    private static void setSecurityDigestType(DigestType securityDigestType) {
        VitamConfiguration.securityDigestType = securityDigestType;
    }

    /**
     * setter for defaultDigestType
     *
     * @return
     */
    private static void setDefaultDigestType(DigestType defaultDigestType) {
        VitamConfiguration.defaultDigestType = defaultDigestType;
    }

    /**
     * setter for defaultTimestampDigestType
     *
     * @return
     */
    private static void setDefaultTimestampDigestType(DigestType defaultTimestampDigestType) {
        VitamConfiguration.defaultTimestampDigestType = defaultTimestampDigestType;
    }

    /**
     * setter for acceptableRequestTime
     *
     * @return
     */
    private static void setAcceptableRequestTime(Long acceptableRequestTime) {
        VitamConfiguration.acceptableRequestTime = acceptableRequestTime;
    }

    /**
     * setter for threadsAllowedToBlockForConnectionMultipliers
     *
     * @return
     */
    private static void setThreadsAllowedToBlockForConnectionMultipliers(
        Integer threadsAllowedToBlockForConnectionMultipliers) {
        VitamConfiguration.threadsAllowedToBlockForConnectionMultipliers =
            threadsAllowedToBlockForConnectionMultipliers;
    }

    /**
     * setter for retryNumber
     *
     * @return
     */
    private static void setRetryNumber(int retryNumber) {
        VitamConfiguration.retryNumber = retryNumber;
    }

    /**
     * setter for retryDelay
     *
     * @return
     */
    private static void setRetryDelay(int retryDelay) {
        VitamConfiguration.retryDelay = retryDelay;
    }

    /**
     * setter for waitingDelay
     *
     * @return
     */
    private static void setWaitingDelay(int waitingDelay) {
        VitamConfiguration.waitingDelay = waitingDelay;
    }

    /**
     * getter for allowGzipEncoding
     *
     * @return
     */
    public static Boolean isAllowGzipEncoding() {
        return allowGzipEncoding;
    }

    /**
     * setter for allowGzipEncoding
     *
     * @return
     */
    private static void setAllowGzipEncoding(Boolean allowGzipEncoding) {
        VitamConfiguration.allowGzipEncoding = allowGzipEncoding;
    }

    /**
     * getter for allowGzipDecoding
     *
     * @return
     */
    public static Boolean isAllowGzipDecoding() {
        return allowGzipDecoding;
    }

    /**
     * setter for allowGzipDecoding
     *
     * @return
     */
    private static void setAllowGzipDecoding(Boolean allowGzipDecoding) {
        VitamConfiguration.allowGzipDecoding = allowGzipDecoding;
    }

    /**
     * getter for bufferNumber
     *
     * @return
     */
    public static Integer getBufferNumber() {
        return bufferNumber;
    }

    /**
     * setter for bufferNumber
     *
     * @return
     */
    private static void setBufferNumber(int bufferNumber) {
        VitamConfiguration.bufferNumber = bufferNumber;
    }

    /**
     * getter for maxConcurrentMultipleInputstreamHandler
     *
     * @return
     */
    public static Integer getMaxConcurrentMultipleInputstreamHandler() {
        return maxConcurrentMultipleInputstreamHandler;
    }

    /**
     * setter for maxConcurrentMultipleInputstreamHandler
     *
     * @return
     */
    private static void setMaxConcurrentMultipleInputstreamHandler(int maxConcurrentMultipleInputstreamHandler) {
        VitamConfiguration.maxConcurrentMultipleInputstreamHandler = maxConcurrentMultipleInputstreamHandler;
    }

    /**
     * getter for DEFAULT_LANG
     *
     * @return
     */
    public static String getDefaultLang() {
        return DEFAULT_LANG;
    }

    /**
     * setter for DEFAULT_LANG
     *
     * @return
     */
    public static void setDefaultLang(String defaultLang) {
        DEFAULT_LANG = defaultLang;
    }

    /**
     * Getter for distributeurBatchSize;
     */
    public static int getDistributeurBatchSize() {
        return distributeurBatchSize;
    }

    /**
     * Setter for distributeurBatchSize;
     */
    public static void setDistributeurBatchSize(int distributeurBatchSize) {
        VitamConfiguration.distributeurBatchSize = distributeurBatchSize;
    }

    /**
     * Getter for worker bulk size
     * @return
     */
    public static int getWorkerBulkSize() {
        return workerBulkSize;
    }

    /**
     * Setter worker bulk size
     * @param workerBulkSize
     */
    public static void setWorkerBulkSize(int workerBulkSize) {
        VitamConfiguration.workerBulkSize = workerBulkSize;
    }

    /**
     * Getter for cacheControlDelay;
     */
    public static int getCacheControlDelay() {
        return cacheControlDelay;
    }

    /**
     * Setter for cacheControlDelay;
     */
    public static void setCacheControlDelay(int cacheControlDelay) {
        VitamConfiguration.cacheControlDelay = cacheControlDelay;
    }

    /**
     * Getter for maxCacheEntries;
     */
    public static int getMaxCacheEntries() {
        return maxCacheEntries;
    }

    /**
     * Setter for maxCacheEntries;
     */
    public static void setMaxCacheEntries(int maxCacheEntries) {
        VitamConfiguration.maxCacheEntries = maxCacheEntries;
    }

    /**
     * Getter for exportScore;
     */
    public static boolean isExportScore() {
        return exportScore;
    }

    /**
     * Setter for exportScore;
     */
    private static void setExportScore(boolean exportScore) {
        VitamConfiguration.exportScore = exportScore;
    }



    /**
     * Getter for maxElasticsearchBulk;
     */
    public static int getMaxElasticsearchBulk() {
        return maxElasticsearchBulk;
    }

    /**
     * Setter for maxElasticsearchBulk;
     */
    private static void setMaxElasticsearchBulk(int maxElasticsearchBulk) {
        VitamConfiguration.maxElasticsearchBulk = maxElasticsearchBulk;
    }

    /**
     * Getter for numberDbClientThread;
     */
    public static int getNumberDbClientThread() {
        return numberDbClientThread;
    }

    /**
     * Setter for numberDbClientThread;
     */
    private static void setNumberDbClientThread(int numberDbClientThread) {
        VitamConfiguration.numberDbClientThread = numberDbClientThread;
    }

    /**
     * Getter for numberEsQueue;
     */

    public static Integer getNumberEsQueue() {
        return numberEsQueue;
    }

    /**
     * Setter for numberEsQueue;
     */
    private static void setNumberEsQueue(int numberEsQueue) {
        VitamConfiguration.numberEsQueue = numberEsQueue;
    }


}
