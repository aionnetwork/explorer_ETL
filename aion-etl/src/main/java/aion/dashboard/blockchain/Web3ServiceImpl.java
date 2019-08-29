package aion.dashboard.blockchain;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.*;
import aion.dashboard.config.Config;
import aion.dashboard.exception.HttpStatusException;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.service.SchedulerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aion.util.bytes.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.Closeable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Web3ServiceImpl implements Closeable, Web3Service {

    static final Web3ServiceImpl INSTANCE = new Web3ServiceImpl();
    private static final String GET_BALANCE = "eth_getBalance";
    private static final String GET_NONCE = "eth_getTransactionCount";
    private static final String GET_BLOCK_NUMBER="eth_blockNumber";
    private static final String GET_BLOCK = "eth_getBlockByNumber";
    private static final String GET_TRANSACTION_BY_HASH= "eth_getTransactionByHash";
    private static final String CALL="eth_call";
    private static final String GET_TRANSACTION_RECEIPT = "eth_getTransactionReceipt";
    private static final String GET_INTERNAL_TRANSACTION = "eth_getInternalTransactionsByHash";
    private final HttpHeaders httpHeaders;
    private final RestTemplate restExecutor;
    private List<String> web3Providers;
    private Future<?> future;
    private boolean isClosed = false;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");


    private Web3ServiceImpl() {
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        web3Providers = Config.getInstance().getWeb3Providers();
        var es = SchedulerService.getInstance().getExecutorService();
        future = es.scheduleWithFixedDelay(this::findActiveList, 0, 5, TimeUnit.SECONDS);


        restExecutor = new RestTemplateBuilder()
                .setReadTimeout(Duration.ofMillis(10_000L))
                .setConnectTimeout(Duration.ofMillis(10_000L))
                .build();

    }


    private static ObjectMapper configuredMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.USE_LONG_FOR_INTS, true);
        return mapper;
    }

    public static Web3ServiceImpl getInstance() {
        return INSTANCE;
    }


    /**
     * Builds the json object used to perform the web3 call
     * @param method the method to be executed
     * @param args the arguments passed to the kernel
     * @return the formatted json string 
     */
    private static String buildWeb3Call(String method, Object... args) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(args);

        MethodBody body = new MethodBody(method, args, 1);


        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            return "";
        }
    }


    private static String formatWeb3Provider(String provider) {
        return String.format("http://%s", provider);
    }

    private void findActiveList() {
        synchronized (activeEndpoints) {
            activeEndpoints.set(web3Providers.stream().filter(this::ping).collect(Collectors.toList()));
            activeEndpoints.notifyAll();
        }
    }

    public void setWeb3Providers(List<String> web3Providers) {
        synchronized (activeEndpoints) {
            this.web3Providers = web3Providers;
            findActiveList();
        }
    }

    private String getActiveEp() {
        synchronized (activeEndpoints) {
            while (activeEndpoints.get() == null && !Thread.currentThread().isInterrupted()) {
                try {
                    activeEndpoints.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return activeEndpoints.get().get(random.nextInt(activeEndpoints.get().size()));
    }

    private final AtomicReference<List<String>> activeEndpoints = new AtomicReference<>(null);

    public boolean ping(String ep) {
        try {
            Thread.currentThread().setName("ping");
            String res = executeCall(buildWeb3Call("ping"), ep, String.class);
            return res.equalsIgnoreCase("pong");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<APIInternalTransaction> getInternalTransaction(String transactionHash) throws Web3ApiException {
        try{
            var internalTransactions =
                    executeCall(buildWeb3Call(GET_INTERNAL_TRANSACTION, transactionHash), getActiveEp(), APIInternalTransactionResponse.class);
            return internalTransactions.getInternalTransactions();
        }catch (NoSuchElementException e){
            throw new IllegalStateException("Transaction is pending");
        }
    }

    public BigInteger getBalance(String account) throws Web3ApiException {
        var balance = executeCall(buildWeb3Call(GET_BALANCE, account))
                .replace("0x", "");
        return new BigInteger(balance, 16);
    }

    public BigInteger getBalanceAt(String account, long blockNumber) throws Web3ApiException {
        var balance = executeCall(buildWeb3Call(GET_BALANCE, account, blockNumber))
                .replace("0x", "");

        return new BigInteger(balance, 16);
    }

    @Override
    public BigInteger getNonce(String address) throws Web3ApiException {

        var balance = executeCall(buildWeb3Call(GET_NONCE, address))
                .replace("0x", "");

        return new BigInteger(balance, 16);


    }

    @Override
    public long getBlockNumber() throws Exception {
        return  executeCall(buildWeb3Call(GET_BLOCK_NUMBER), getActiveEp(), Integer.class);
    }

    @Override
    public BigInteger getNonceAt(String address, long blockNumber) throws Web3ApiException {
        var balance = executeCall(buildWeb3Call(GET_NONCE, address, blockNumber))
                .replace("0x", "");

        return new BigInteger(balance, 16);
    }

    private String executeCall(String jsonMethod) throws Web3ApiException {
        return executeCall(jsonMethod, getActiveEp(), String.class);// perform http request with the default ep
    }


    private <T> T executeCall(String jsonMethod, String endpoint, Class<T> tClass) throws Web3ApiException {
        String oldThreadName = Thread.currentThread().getName();
        validateState();
        try {
            Thread.currentThread().setName("web-3-executor");

            validateArguments(jsonMethod, endpoint);// Validate the arguments
            // start the http request
            if (GENERAL.isTraceEnabled()) {
                GENERAL.trace("Executing call: {} with EP: {}",jsonMethod, endpoint);
            }

            HttpEntity<String> entity = new HttpEntity<>(jsonMethod, httpHeaders);

            ResponseEntity<Result> responseEntity = restExecutor.exchange(formatWeb3Provider(endpoint), HttpMethod.POST, entity, Result.class);

            // query the response status

            if (responseEntity.getStatusCode() == HttpStatus.OK) {

                // if 200 extract the response

                if (GENERAL.isTraceEnabled()) {
                    GENERAL.trace("Received response: {}",responseEntity);
                }

                var res = responseEntity.getBody().getResult();

                if (res == null) {
                    throw new NoSuchElementException("Failed to get a valid response from web3. Response:\n" + responseEntity.toString());
                } else if (res instanceof String && (((String) res).isBlank() || ((String) res).isEmpty())) {
                    throw new NoSuchElementException("Failed to get a valid response from web3. Response:\n" + responseEntity.toString());
                } else  if (res instanceof String || res instanceof Integer || res instanceof Long) {
                    return tClass.cast(res);
                } else {
                    return mapper.convertValue(res, tClass);
                }

            } else {
                // throw if not 200
                throw new HttpStatusException(responseEntity.getStatusCode());
            }
        } catch (HttpStatusException e) {
            throw new Web3ApiException("Failed to execute call. Method: "+jsonMethod, e);
        }
        finally {
            Thread.currentThread().setName(oldThreadName);
        }
    }

    /**
     * Validates the arguments passed to he execute call function
     * The arguments are validated here to improve troubleshooting the application.
     *
     * @param jsonRequest
     * @param endpoint
     */
    private void validateArguments(String jsonRequest, String endpoint) {
        Objects.requireNonNull(jsonRequest, "Provided jsonRequest is empty.");
        Objects.requireNonNull(endpoint, "Provided endpoint is null.");
        if (endpoint.isEmpty()) {
            throw new IllegalArgumentException("Endpoint is empty.");
        }
        if (jsonRequest.isEmpty()) {
            throw new IllegalArgumentException("jsonRequest is empty.");
        }
    }

    private void validateState() throws Web3ApiException {
        if (isClosed) throw new Web3ApiException("Attempted to use a closed web3 Service", new IllegalStateException());
    }


    @Override
    public void close() {
        future.cancel(true);
        isClosed = true;
    }

    @Override
    public APIBlock getBlock(long blockNumber) throws Web3ApiException {
        return executeCall(buildWeb3Call(GET_BLOCK, blockNumber), getActiveEp(), APIBlock.class);
    }

    @Override
    public APITransaction getTransaction(String txHash) throws Exception {
        return executeCall(buildWeb3Call(GET_TRANSACTION_BY_HASH, txHash), getActiveEp(), APITransaction.class);
    }

    @Override
    public APITransactionReceipt getTransactionReceipt(String txHash) throws Exception {
        return executeCall(buildWeb3Call(GET_TRANSACTION_RECEIPT, txHash), getActiveEp(), APITransactionReceipt.class);
    }


    @Override
    public byte[] call(CallObject object) throws Exception {
        return ByteUtil.hexStringToBytes(executeCall(buildWeb3Call(CALL, object, "latest")));
    }
}
