package aion.dashboard.config;

import aion.dashboard.task.AbstractGraphingTask;
import ch.qos.logback.classic.Level;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Config {

	private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

	private static final Config INSTANCE = new Config();
	public static Config getInstance() {
		return INSTANCE;
	}

    private static final long MAX_BLK_QUERY_RANGE = 1000L;
    private static final long MIN_BLK_REORG = 0L;
    private static final long MIN_DELAY_POLL_MAIN = 5L; // 5ms
    private static final long MIN_DELAY_INTEGRITY = 5L; // 5ms
    private static final long MIN_TXN_BATCH = 100L;
    private static final boolean PERFORMANCE_FLAG = false;
    private static final boolean SORTED_CHECK = true;
    private static final boolean REQUESTED_RANGE_SIZE_CHECK = true;
    private static final boolean EXPECTED_RANGE_CHECK = true;
    private static final long READER_POLL_DELAY = 1000;
    private static final long READER_ERR_POLL_DELAY = 500;
    private final static long QUEUE_SIZE = 4;
    private String emailSenderUsername;
    private String emailSenderPassword;
    private List<String> emailRecipients = new ArrayList<>();
    private String sqlUsername;
    private String sqlPassword;
    private String sqlDbName;
    private String sqlIp;
    private long testLargeLimit;
    private long testSmallLimit;
    private String hostname;
    private String meterKeyApp;
    private String meterKeyApi;
    private long blockQueryRange = 200L;
    private long blockReorgLimit = 100L;
    private long delayPollingMain = 3_000L; // 3s
    private long delayIntegrityCheck = 86_400_000L; // 1d
    private long transactionBatchSize = 5_000L;
    private boolean Performance_Flag;
    private boolean sortedCheck;
    private boolean requestedRangeSizeCheck;
    private boolean expectedRangeCheck;
    private long readerPollDelay;
    private long readerErrPollDelay;
    private long queueSize;
    private long timeOut;
    private boolean enableReports;
    private List<String> web3Providers;


    private int BlockMaxWindowSize;
    private int TransactionWindowSize;
    private int BlockWindowCountSize;
    private int BlockWindowStableSize;
    private boolean isTest;
    private long maxHeight;
    private AbstractGraphingTask.TaskType taskType;

    private long apiTimeOut;

    private Level GeneralLevel;
    private List<String> apiConnections = new ArrayList<>();

	// no configuration for logging. control it directly from logback
	private Config() {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get("config.json"));
			String content = new String(encoded, StandardCharsets.UTF_8);
			JSONObject json = new JSONObject(content);

			JSONObject sql = json.getJSONObject("sql");

			taskType = AbstractGraphingTask.TaskType.valueOf(Optional.ofNullable(System.getenv("TASK_TYPE")).orElse("DB"));

            isTest = Optional.ofNullable(System.getenv("TEST")).orElse("false").equalsIgnoreCase("true");
            maxHeight = Long.parseLong(Optional.ofNullable(System.getenv("MAX_HEIGHT")).orElse("10000"));
			sqlUsername = Optional.ofNullable(System.getenv("DB_USER")).orElse("");//sql.getString("username");
			sqlPassword = Optional.ofNullable(System.getenv("DB_USER_PASSWORD")).orElse("");//sql.getString("password");
            GeneralLevel = Level.valueOf(Optional.ofNullable(System.getenv("ETL_LOG_LEVEL")).orElse("trace").toUpperCase());


            web3Providers = json.getJSONArray("web3").toList().stream().map(Object::toString).collect(Collectors.toList());


            String javaAPI = System.getenv("JAVA_API_URI");
            if(sqlUsername.length()==0) {
                sqlUsername = sql.getString("username");
                sqlPassword = sql.getString("password");
            }
            sqlDbName = sql.getString("dbName");
			sqlIp = sql.getString("ip");

			JSONObject email = json.optJSONObject("email");
			if (email != null) {
				emailSenderUsername = email.optString("username");
				emailSenderPassword = email.optString("password");
				JSONArray rs = email.optJSONArray("recipients");
				for (int i=0; i<rs.length(); i++) {
					String r = rs.optString(i);
					if (r != null) emailRecipients.add(r);
				}

				enableReports = email.getBoolean("enableReports");
				timeOut = email.getLong("timeOut");
			}

			JSONObject rollingMeanConfig = json.optJSONObject("rollingMean") == null ? new JSONObject() : json.optJSONObject("rollingMean");
			BlockMaxWindowSize = rollingMeanConfig.optInt("blockWindowSize", 4000);
            TransactionWindowSize = rollingMeanConfig.optInt("transactionWindowSize", 24);//in hours
            BlockWindowCountSize = rollingMeanConfig.optInt("blockWindowCountSize", 32);// the count of blocks to include
            BlockWindowStableSize = rollingMeanConfig.optInt("blockWindowTimeSize", 60);// in minutes

            apiTimeOut = json.optInt("apiTimeOut", 50000);
            JSONObject test = json.getJSONObject("test");
            testLargeLimit = test.getLong("largeLimit");
            testSmallLimit = test.getLong("smallLimit");


            if (javaAPI == null) {
                JSONArray apis = json.getJSONArray("api");
                for (int i = 0; i < apis.length(); i++) {
                    String ep = apis.optString(i);
                    if (ep != null) apiConnections.add("tcp://" + ep);
                }
            }
            else {
                String[] arr = javaAPI.split(";");
                apiConnections = Arrays.stream(arr).map(s->"tcp://"+s).collect(Collectors.toList());

            }
            if (apiConnections.isEmpty()) throw new Exception();

			Long _blockQueryRange = json.optLong("blockQueryRange");
			if (_blockQueryRange != null) {
				blockQueryRange = Math.min(_blockQueryRange, MAX_BLK_QUERY_RANGE);
			}

			Long _blockReorgLimit = json.optLong("blockReorgLimit");
			if (_blockReorgLimit != null) {
				blockReorgLimit = Math.max(_blockReorgLimit, MIN_BLK_REORG);
			}

			Long _delayPollingMain = json.optLong("delayPollingMain");
			if (_delayPollingMain != null) {
				delayPollingMain = Math.max(_delayPollingMain, MIN_DELAY_POLL_MAIN);
			}

			Long _delayIntegrityCheck = json.optLong("delayIntegrityCheck");
			if (_delayIntegrityCheck != null) {
				delayIntegrityCheck = Math.max(_delayIntegrityCheck, MIN_DELAY_INTEGRITY);
			}

			Long _transactionBatchSize = json.optLong("transactionBatchSize");
			if (_transactionBatchSize != null) {
				transactionBatchSize = Math.max(_transactionBatchSize, MIN_TXN_BATCH);
			}

			Boolean _performanceFlag = json.optBoolean("performanceFlag");

			Performance_Flag = _performanceFlag == null ? PERFORMANCE_FLAG : _performanceFlag;
            hostname = json.optString("hostname");


            JSONObject serviceIntegrityChecks = json.optJSONObject("serviceIntegrityChecks");


            Boolean _sortedCheck = serviceIntegrityChecks.getBoolean("sortedCheck");

            sortedCheck = _sortedCheck == null ? SORTED_CHECK: _sortedCheck;

            Boolean _requestedRangeCheck = json.optBoolean("requestedRangeSizeCheck");

            requestedRangeSizeCheck = _requestedRangeCheck == null ? REQUESTED_RANGE_SIZE_CHECK : _requestedRangeCheck;

            Boolean _expectedRangeCheck = json.optBoolean("expectedRangeCheck");

            expectedRangeCheck = _expectedRangeCheck == null? EXPECTED_RANGE_CHECK : _expectedRangeCheck;

            JSONObject readerConfig = json.optJSONObject("blockchainReader");
            Long _readerPollDelay = readerConfig.getLong("pollDelay");

            readerPollDelay =  _readerPollDelay == null? READER_POLL_DELAY : _readerPollDelay;


            Long _readerErrPollDelay = readerConfig.optLong("errorDelay");

            readerErrPollDelay = _readerErrPollDelay == null? READER_ERR_POLL_DELAY : _readerErrPollDelay;

            Long _queueSize = readerConfig.optLong("queueSize");

            queueSize = _queueSize == null? QUEUE_SIZE : _queueSize;

        } catch (Exception e) {
            GENERAL.debug("Config ERR: Please check that config.json exists and required fields are populated.");
            System.exit(-1);
        }
    }

	public List<String> getApiConnections() {
		return apiConnections;
	}

	public String getSqlUsername() {
		return sqlUsername;
	}

	public String getSqlPassword() {
		return sqlPassword;
	}

	public String getSqlDbName() {
		return sqlDbName;
	}

	public String getSqlIp() {
		return sqlIp;
	}

	public long getBlockQueryRange() {
		return blockQueryRange;
	}

	public long getBlockReorgLimit() {
		return blockReorgLimit;
	}

	public long getDelayPollingMain() {
		return delayPollingMain;
	}

	public long getDelayIntegrityCheck() {
		return delayIntegrityCheck;
	}

	public String getEmailSenderUsername() {
		return emailSenderUsername;
	}

	public String getEmailSenderPassword() {
		return emailSenderPassword;
	}

	public List<String> getEmailRecipients() {
		return emailRecipients;
	}

    public long getTransactionBatchSize() {
        return transactionBatchSize;
    }

    public long getTestLargeLimit() {
        return testLargeLimit;
    }

    public long getTestSmallLimit() {
        return testSmallLimit;
    }

    public void setBlockReorgLimit(long blockReorgLimit) {
        this.blockReorgLimit = blockReorgLimit;
    }

    public String getHostname() {
        return hostname;
    }

    public String getMeterKeyApp() {
        return meterKeyApp;
    }

    public String getMeterKeyApi() {
        return meterKeyApi;
    }

	public boolean performanceFlag() {
		return Performance_Flag;
	}


	public boolean isSortedCheck() {
		return sortedCheck;
	}


	public boolean isRequestedRangeSizeCheck() {
		return requestedRangeSizeCheck;
	}

	public boolean isExpectedRangeCheck() {
		return expectedRangeCheck;
	}


    public long getReaderErrPollDelay() {
        return readerErrPollDelay;
    }

    public long getReaderPollDelay() {
        return readerPollDelay;
    }

	public long getQueueSize() {
		return queueSize;
	}

    public boolean areReportsEnabled() {
        return enableReports;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public int getBlockMaxWindowSize() {
        return BlockMaxWindowSize;
    }

    public int getTransactionWindowSize() {
        return TransactionWindowSize;
    }

    public int getBlockWindowCountSize() {
        return BlockWindowCountSize;
    }

    public int getBlockWindowStableSize() {
        return BlockWindowStableSize;
	}
    public Level getGeneralLevel() {
        return GeneralLevel;
    }

    public boolean isTest() {
        return isTest;
    }

    public long getMaxHeight() {
        return maxHeight;
    }

    public AbstractGraphingTask.TaskType getTaskType() {
        return taskType;
    }

    public long getApiTimeOut() {
        return apiTimeOut;
    }

    public List<String> getWeb3Providers() {
        return web3Providers;
    }
}