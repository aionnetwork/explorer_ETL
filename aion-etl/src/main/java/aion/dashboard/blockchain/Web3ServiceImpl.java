package aion.dashboard.blockchain;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.*;
import aion.dashboard.config.Config;
import aion.dashboard.exception.HttpStatusException;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.service.SchedulerService;
import com.fasterxml.jackson.core.JsonGenerator;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Web3ServiceImpl implements Closeable, Web3Service {

    static final Web3ServiceImpl INSTANCE = new Web3ServiceImpl();
    private static final String GET_BALANCE = "eth_getBalance";
    private static final String GET_NONCE = "eth_getTransactionCount";
    private static final String GET_BLOCK_NUMBER="eth_blockNumber";
    private static final String GET_BLOCK = "eth_getBlockByNumber";
    private static final String GET_TRANSACTION_BY_HASH= "eth_getTransactionByHash";
    private static final String CALL="eth_call";
    private final AtomicReference<String> endpoint;
    private final AtomicInteger count;
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
        future = es.scheduleWithFixedDelay(this::heartBeat, 0, 100, TimeUnit.SECONDS);
        count = new AtomicInteger(0);
        endpoint = new AtomicReference<>(web3Providers.get(count.get()));


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

    private void heartBeat() {

        if (!ping(endpoint.get())) {
            endpoint.set(findEndPoint(web3Providers, count.get()));
        }


    }

    String findEndPoint(List<String> ep, int curr) {
        int i = (curr + 1) % web3Providers.size();
        int size = ep.size();
        String result = "";
        while (!Thread.currentThread().isInterrupted() && result.equalsIgnoreCase("")) {
            if (ping(ep.get(i))) {

                result = ep.get(i);
            }

            i = (i + 1) % size;
        }
        return result;
    }

    public boolean ping(String ep) {
        try {
            Thread.currentThread().setName("ping");
            String res = executeCall(buildWeb3Call("ping"), ep, String.class);
            return res.equalsIgnoreCase("pong");
        } catch (Exception e) {
            return false;
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
        return  executeCall(buildWeb3Call(GET_BLOCK_NUMBER), endpoint.get(), Integer.class);
    }

    @Override
    public BigInteger getNonceAt(String address, long blockNumber) throws Web3ApiException {
        var balance = executeCall(buildWeb3Call(GET_NONCE, address, blockNumber))
                .replace("0x", "");

        return new BigInteger(balance, 16);
    }

    private String executeCall(String jsonMethod) throws Web3ApiException {
        return executeCall(jsonMethod, endpoint.get(), String.class);// perform http request with the default ep
    }


    private <T> T executeCall(String jsonMethod, String endpoint, Class<T> tClass) throws Web3ApiException {
        validateState();
        try {

            validateArguments(jsonMethod, endpoint);// Validate the arguments
            // start the http request
            if (GENERAL.isTraceEnabled()) {
                GENERAL.trace("Executing call: {}",jsonMethod);
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
        } catch (Exception e) {
            throw new Web3ApiException("Failed to execute call. Method: "+jsonMethod, e);
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
        endpoint.set(null);
    }

    @Override
    public APIBlock getBlock(long blockNumber) throws Web3ApiException {
        return executeCall(buildWeb3Call(GET_BLOCK, blockNumber), endpoint.get(), APIBlock.class);
    }

    @Override
    public APITransaction getTransaction(String txHash) throws Exception {
        return executeCall(buildWeb3Call(GET_TRANSACTION_BY_HASH, txHash), endpoint.get(), APITransaction.class);
    }


    @Override
    public byte[] call(CallObject object) throws Exception {
        return ByteUtil.hexStringToBytes(executeCall(buildWeb3Call(CALL, object, "latest")));
    }
}
