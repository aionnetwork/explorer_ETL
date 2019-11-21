package aion.dashboard.blockchain;

import aion.dashboard.blockchain.interfaces.APIService;
import aion.dashboard.blockchain.type.APIBlock;
import aion.dashboard.blockchain.type.APITransaction;
import aion.dashboard.blockchain.type.APITransactionReceipt;
import aion.dashboard.blockchain.type.CallObject;
import aion.dashboard.config.Config;
import aion.dashboard.exception.AionApiException;
import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.type.*;
import org.aion.base.type.AionAddress;
import org.aion.base.type.Hash256;
import org.aion.vm.api.interfaces.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;

public class AionService implements APIService {


	private final IAionAPI api;
	List<String> connections;
	int connectionIndex;
	private static final Object MUTEX = new Object();
	private static final AionService Instance = new AionService();

	private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

	public static AionService getInstance() {
		return Instance;
	}


	AionService() {

		requestExecutor = Executors.newSingleThreadExecutor();

		Config config = Config.getInstance();
		connections = config.getApiConnections();
		api = IAionAPI.init();
		connectionIndex = -1;

	}

	public void reconnect() throws AionApiException {

		if (isConnected())
			return; // everything's good

		// try to re-connect starting from index 0 in connection list
		while(!Thread.currentThread().isInterrupted()) {
			if (attemptReconnect()) return;
		}
		GENERAL.error("Api Connection Error: Failed to establish connection to any known Aion client.");
		throw new AionApiException();

	}

	private boolean attemptReconnect() {
		ApiMsg apiMsg = new ApiMsg();
		for (int i = 0; i < connections.size(); i++) {
			String url = connections.get(i);
			apiMsg.set(doApiRequest(iAionApi->iAionApi.connect(url), api));

			if (apiMsg.isError()) {
				GENERAL.debug("Api Connection Failed. Endpoint: [{}]. Error: [{}] {}", url, apiMsg.getErrorCode(), apiMsg.getErrString());
			} else if (api.isConnected()) {
				GENERAL.debug("Api Connected to endpoint [{}]", url);
				connectionIndex = i;

				return true;
			}
		}
		return false;
	}

	public boolean isConnected() {
		if(api == null) return false;

		synchronized (MUTEX) {
			return api.isConnected();
		}

	}



	public void connectToNearestNode() throws AionApiException{
		ApiMsg apiMsg = new ApiMsg();
		if (connectionIndex != 0){
			for (int i = 0; i< connections.size();i++) {
				String url = connections.get(i);


				apiMsg.set(doApiRequest(iAionApi->iAionApi.connect(url), api));

				if (apiMsg.isError())
					GENERAL.debug("Api Connection Failed at node {} falling back. Endpoint [{}]. Error: [{}] {}", i + 1, url, apiMsg.getErrorCode(), apiMsg.getErrString());
				else if (api.isConnected()){
					GENERAL.debug("Api Connected to endpoint [{}]", url);
					connectionIndex = i;
					return;
				}
			}

			GENERAL.error("Api Connection Error: Failed to establish connection to any known Aion client.");
			throw new AionApiException();
		}

	}


    public List<BlockDetails> getBlockDetailsByRange(long start, long end) throws AionApiException {
		List<BlockDetails> result;
		ApiMsg apiMsg;
		try {
			reconnect();
			GENERAL.debug("Calling getBlockDetailsByRange({},{}) size: [{}]", start, end, end - start + 1);
			apiMsg = doApiRequest(iAionApi -> iAionApi.getAdmin().getBlockDetailsByRange(start, end), api);

			if (apiMsg.isError()) {
				throw new AionApiException(formatError(apiMsg));
			} else {
				result = apiMsg.getObject();
			}
		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in getBlockDetailsByRange()", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}


        return result;
	}

	public String getBlockHashbyNumber(long blockNumber) throws AionApiException {
		String result;
		try {
			Block b = doGetBlock(blockNumber);
			result = b.getHash().toString();



		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in getBlockHashByNumber()", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}

		return result;
	}

	private Block doGetBlock(long blockNumber) throws AionApiException {

		reconnect();
		var apiMsg =(doApiRequest(iAionApi->iAionApi.getChain().getBlockByNumber(blockNumber), api));

		if (apiMsg.isError()) {
			throw new AionApiException(formatError(apiMsg));
		}


		return apiMsg.getObject();
	}

	public long getBlockNumber() throws AionApiException {
		long result;
		ApiMsg apiMsg = new ApiMsg();
		try {
			reconnect();
			apiMsg.set(doApiRequest(iAionApi-> iAionApi.getChain().blockNumber(), api));

			if (apiMsg.isError()) {
				throw new AionApiException(formatError(apiMsg));
			} else {
				result = apiMsg.getObject();
			}

		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in getBlockNumber()", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}

		return result;
	}

	@Override
	public byte[] call(CallObject object) throws Exception {
		TxArgs args = object.toTxArgs();
		final byte[] result;

		ApiMsg apiMsg = new ApiMsg();
		try {
			reconnect();
			apiMsg.set(doApiRequest(iAionApi-> iAionApi.getTx().call(args), api));

			if (apiMsg.isError()) {
				throw new AionApiException(formatError(apiMsg));
			} else {
				result = apiMsg.getObject();
			}

		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in getBlockNumber()", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}

		return result;

	}

	/**
	 * Gets a contract from the chain
	 * @param from the contracts creator
	 * @param contractAddr the address at which the contract would be stored
	 * @param abi the interface of the contract
	 * @return the contract object
	 * @throws AionApiException thrown if the contract could not be retrieved or does not exist
	 */

	public IContract getContract(Address from, Address contractAddr, String abi) throws AionApiException {
		IContract result;
		try {
			reconnect();
			synchronized (MUTEX){
				result = api.getContractController().getContractAt(from, contractAddr, abi);//no need for an
				// async call this is basically just the construction of the contract object
			}

			if (result == null) throw new AionApiException("Contract not found at address");
			if (result.getAbiDefinition() == null || result.getAbiDefinition().isEmpty()) throw new AionApiException("Contract failed to return ABI");


        }
		catch (AionApiException e){
			GENERAL.debug("AionApi: threw Exception in getContract()", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}
		return result;
	}


	/**
	 * Calls the named function from the supplied contract.
	 * @param contract The contract method on which the method should be called
	 * @param functionName The name of the function that should be called
	 * @param args The arguments to be supplied in the declared order
	 * @return
	 * @throws AionApiException Thrown if the api failed to call the function
	 */
	public List<Object> callContractFunction(IContract contract, String functionName, ISolidityArg... args) throws AionApiException {


		try {
			reconnect();
			ApiMsg apiMsg = (doApiRequest(contractObj -> {
				contractObj.newFunction(functionName);

				for (var arg : args) contractObj.setParam(arg); // set all the arguments associated with the contract

				contractObj.setTxNrgLimit(NRG_LIMIT_TX_MAX);
				contractObj.setTxNrgPrice(NRG_PRICE_MIN);//Get the recommended NRG price from the connected kernel
				contractObj.build();
				return contractObj.call();
			}, contract));

			if (apiMsg.isError())
				throw new AionApiException(formatError(apiMsg));
			else {
				ContractResponse response = apiMsg.getObject();

				if (response.isStatusError() || response.isTxError()) {
					String error;
					if (response.isTxError()) {
						error = response.getError();
					} else {
						error = response.statusToString();
					}

					throw new AionApiException(formatError(apiMsg) + "\nTransaction error: " + error);

				}
				return response.getData();
			}
		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in callContractFunction() ", e);
			throw  e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}

	}


	/**
	 * Obtains the ABI definition from a contract
	 * @param src Contract source code
	 * @param contractName Name of the declared contracts
	 * @return ABI
	 * @throws AionApiException If the compilation fails
	 */
	public String contractABI(String src, String contractName) throws AionApiException {

        return compileResponse(src, contractName).getAbiDefString();
    }



    public BigInteger getBalance(String address) throws AionApiException {

		reconnect();
		BigInteger result;
		ApiMsg apiMsg = new ApiMsg();
		try {
			apiMsg.set(doApiRequest(iAionAPI -> iAionAPI.getChain().getBalance(AionAddress.wrap(address)), api));

			if (apiMsg.isError()) {
				throw new AionApiException(formatError(apiMsg));
			} else {
				result = apiMsg.getObject();
			}
		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in getNonce()", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}

		return result;

	}


    public BigInteger getNonce(String address) throws AionApiException{
        reconnect();
		BigInteger result;
		ApiMsg apiMsg = new ApiMsg();
		try {

			apiMsg.set(doApiRequest(iAionApi-> iAionApi.getChain().getNonce(AionAddress.wrap(address)), api));

			if (apiMsg.isError()) {
				throw new AionApiException(formatError(apiMsg));
			} else {
				result = apiMsg.getObject();
			}

		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in getNonce()", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}

        return result;


    }

    /**
     * Obtains the compile response for a specified contract
     *
     * @param src          Contract source code
     * @param contractName Name of the declared contracts
     * @return ABI
     * @throws AionApiException If the compilation fails
     */
    public CompileResponse compileResponse(String src, String contractName) throws AionApiException {

        Map<String, CompileResponse> compileResponse;
		ApiMsg apiMsg = new ApiMsg();

		try {

			reconnect();
			apiMsg.set(doApiRequest(aionAPI -> aionAPI.getTx().compile(src), api));

			if (apiMsg.isError())
				throw new AionApiException(formatError(apiMsg));
			compileResponse = apiMsg.getObject();


		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in compileResponse() ", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}


        return compileResponse.get(contractName);
    }
	@Override
	public void close() {
		if (api != null) api.destroyApi();
	}

	@Override
	public APIBlock getBlock(long blockNumber) throws AionApiException {
		try {
			return APIBlock.from(doGetBlock(blockNumber));
		}catch (RuntimeException e){
			throw new AionApiException(formatRuntimeException(e));
		}
	}

    @Override
    public APITransaction getTransaction(String txHash) throws Exception {


		ApiMsg apiMsg = new ApiMsg();
		Transaction transaction;
		try {

			apiMsg.set(doApiRequest(iAionApi-> iAionApi.getChain().getTransactionByHash(Hash256.wrap(txHash)), api));

			if (apiMsg.isError()) {
				throw new AionApiException(formatError(apiMsg));
			} else {
				transaction = apiMsg.getObject();
			}

		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in getNonce()", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}

		return APITransaction.from(transaction);
    }

	@Override
	public APITransactionReceipt getTransactionReceipt(String txHash) throws Exception {
		return null;
	}

	public BigInteger getBlockReward(long blockNumber) throws AionApiException {
    	try{
    		ApiMsg msg = doApiRequest(iAionApi-> iAionApi.getChain().getBlockReward(blockNumber), api);
    		if (msg.isError()){

    			throw new AionApiException(formatError(msg));
			}
    		return msg.getObject();
		}catch (AionApiException e){
			GENERAL.debug("AionApi: threw Exception in getBlockReward()", e);
			throw new AionApiException();
		}catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}
	}


    private String formatRuntimeException(RuntimeException e) {
		return "AionService: Caught a runtime exception while reading Aionapi: EXCEPTION_MESSAGE[" + e.getMessage()+"]";
	}

	private String formatError(ApiMsg apiMsg){
    	var startStr = "AionApi: ERR_CODE[" +apiMsg.getErrorCode()+"]: ";
    	if (apiMsg.getErrorCode()==-404)return startStr + "Api request timed out.";// custom error message
		else if (apiMsg.getErrorCode()==-405) return startStr + "Api request failed with a runtime error.";
    	else return startStr + apiMsg.getErrString();
	}


	private ExecutorService requestExecutor;

	private <T> ApiMsg doApiRequest(Function<T, ApiMsg> request, T resource){
		Future<ApiMsg> res = CompletableFuture
				.supplyAsync(()-> {
					synchronized (MUTEX) {
						return request.apply(resource);
					}
				}, requestExecutor);
		try {

			return res.get(Config.getInstance().getApiTimeOut(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return new ApiMsg(-1);
		} catch (ExecutionException e) {
			return new ApiMsg(-405);
		} catch (TimeoutException e) {
			return new ApiMsg(-404);
		}
		finally {
			if (!res.isDone()) {
				res.cancel(true);
				api.destroyApi();
			}
		}
	}
}
