package aion.dashboard.blockchain;

import aion.dashboard.config.Config;
import aion.dashboard.exception.AionApiException;
import org.aion.api.IAionAPI;
import org.aion.api.IContract;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.type.*;
import org.aion.base.type.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.aion.api.ITx.NRG_LIMIT_TX_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;

public class AionService implements AutoCloseable{


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
			synchronized (MUTEX) {
				apiMsg.set(api.connect(url));
			}
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

				synchronized (MUTEX){
					apiMsg.set(api.connect(url));
				}

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
			GENERAL.debug("Calling getBlockDetailsByRange({},{}) size: [{}]", start, end, end - start + 1);
			synchronized (MUTEX) {
				apiMsg = api.getAdmin().getBlockDetailsByRange(start, end);
			}
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
		ApiMsg apiMsg = new ApiMsg();
		try {
			synchronized (MUTEX) {
				apiMsg.set(api.getChain().getBlockByNumber(blockNumber));
			}
			if (apiMsg.isError()) {
				throw new AionApiException(formatError(apiMsg));
			} else {
				Block b = apiMsg.getObject();
				result = b.getHash().toString();
			}


		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in getBlockHashByNumber()", e);
			throw e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}

		return result;
	}

	public long getBlockNumber() throws AionApiException {
		long result;
		ApiMsg apiMsg = new ApiMsg();
		try {
			synchronized (MUTEX) {
				apiMsg.set(api.getChain().blockNumber());
			}
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
			synchronized (MUTEX){
				result = api.getContractController().getContractAt(from, contractAddr, abi);
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


        ContractResponse response;
		ApiMsg apiMsg;
		try {
			synchronized (MUTEX) {
				contract.newFunction(functionName);

				for (var arg : args) contract.setParam(arg); // set all the arguments associated with the contract

				contract.setTxNrgLimit(NRG_LIMIT_TX_MAX);
				contract.setTxNrgPrice(NRG_PRICE_MIN);//Get the recommended NRG price from the connected kernel
				contract.build();


				apiMsg = (contract.call());
				if (apiMsg.isError())
					throw new AionApiException(formatError(apiMsg));
				else response = apiMsg.getObject();
			}
			if (response.isStatusError() || response.isTxError()) {
				String error;
				if (response.isTxError()) {
					error = response.getError();
				} else {
					error = response.statusToString();
				}

				throw new AionApiException(formatError(apiMsg) + "\nTransaction error: " + error);

			}

		} catch (AionApiException e) {
			GENERAL.debug("AionApi: threw Exception in callContractFunction() ", e);
			throw  e;
		} catch (NullPointerException e){
			throw new AionApiException(formatRuntimeException(e));
		}

		return response.getData();
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

		BigInteger result;
		ApiMsg apiMsg = new ApiMsg();
		try {
			synchronized (MUTEX) {
				apiMsg.set(api.getChain().getBalance(Address.wrap(address)));
			}
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
        BigInteger result;
		ApiMsg apiMsg = new ApiMsg();
		try {
			synchronized (MUTEX) {
				apiMsg.set(api.getChain().getNonce(Address.wrap(address)));
			}
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
			synchronized (MUTEX) {
				apiMsg.set(api.getTx().compile(src));
			}

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


	private String formatRuntimeException(RuntimeException e) {
		return "AionService: Caught a runtime exception while reading Aionapi: EXCEPTION_MESSAGE[" + e.getMessage()+"]";
	}

	private String formatError(ApiMsg apiMsg){
    	return "AionApi: ERR_CODE[" + apiMsg.getErrorCode() + "]: " + apiMsg.getErrString();
	}
}
