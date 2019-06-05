package aion.dashboard.parser.events;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.InitializationException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.aion.api.IUtils;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.ContractAbiIOParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.aion.api.impl.Contract.*;


/**
 * Defines the abi for the ATS and ERC20 Tokens
 *
 *
 * TODO move the contract source code to the Resources folder
 * TODO replace all accessors of the ATS ad ERC20Token lists with accessors to the map
 * TODO implement a map that stores the JSON representation of ABIs
 */
public class SolABIDefinitions {


    public static final String ATS_CONTRACT = "ATS";
    public static final String ERC_20_CONTRACT = "ERC20";
    public static final String TRS_CONTRACT = "TRS";
    public static final String BRIDGE_EVENTS= "BridgeEvents";
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    static {
        try {
            instance = new SolABIDefinitions();
        } catch (InitializationException e) {
            System.out.println("Failed to read the abi");
            System.exit(-1);
        }
    }


    private static SolABIDefinitions instance;



    private Map<String, List<ContractAbiEntry>> contractABIEntryMap;
    private Map<String, String> contractJSONABIMap;


    public SolABIDefinitions() throws InitializationException {

        try {
            AionService service = AionService.getInstance();
            service.reconnect();
            contractABIEntryMap = readAllABIs(service);
            contractJSONABIMap = readALLJSONABIs(service);

        }
        catch (Exception e){
            GENERAL.debug("Threw an error in ABIDefinitions. ", e);
            throw new InitializationException();
        }


    }

    public static SolABIDefinitions getInstance() {

        return instance;
    }

    private static final String CONTRACT_LOCATION = "./contracts/sol";

    private static Map<String,String> listABIFiles(){
        File contractFolder = new File(CONTRACT_LOCATION);




        return Arrays.stream(Objects.requireNonNull(contractFolder.listFiles()))
                .map(File::getName)
                .filter(s -> s.endsWith("js"))// get only the js files
                .collect(Collectors.toMap((String s)-> s.replace(".js",""), s -> CONTRACT_LOCATION+"/"+s));//use contract name as the key
    }


    private static Map<String, String> listSolFiles(){
        //The solidity contracts must have the same name as the stored contract
        File contractFolder = new File(CONTRACT_LOCATION);
        return Arrays.stream(Objects.requireNonNull(contractFolder.listFiles()))
                .map(File::getName)
                .filter(s -> s.endsWith("sol"))//filter out the js files
                .collect(Collectors.toMap((String s)-> s.replace(".sol",""), s -> CONTRACT_LOCATION+"/"+s));//Use the contract name as the key in the key value pair
    }




    public static List<ContractAbiEntry> generateABIFromJson(String jsonString){

        Type abiType = new TypeToken<ArrayList<ContractAbiEntry>>() {
        }.getType();


        List<ContractAbiEntry> entries = new Gson().fromJson(jsonString, abiType);

        List<ContractAbiEntry> list = new ArrayList<>();
        for (ContractAbiEntry entry : entries) {
            try {
                Field hashedFiled;
                switch (entry.type) {//code straight out fresh from the kernel
                    case SC_FN_EVENT:


                        hashedFiled = entry.getClass().getDeclaredField("hashed");

                        hashedFiled.setAccessible(true);
                        hashedFiled.set(entry, Objects.requireNonNull(IUtils
                                .bytes2Hex(
                                        ApiUtils.KC_256.digest(buildMethodSignature(entry).getBytes())))
                                .substring(0, 64));
                        entry.setEvent(true);

                        break;
                    case SC_FN_FUNC:
                        hashedFiled = entry.getClass().getDeclaredField("hashed");
                        hashedFiled.setAccessible(true);
                        hashedFiled.set(entry, Objects.requireNonNull(IUtils
                                .bytes2Hex(
                                        ApiUtils.KC_256.digest(buildMethodSignature(entry).getBytes())))
                                .substring(0, 8));
                        break;
                    case SC_FN_CONSTRUCTOR:
                        Field constructorField = entry.getClass().getDeclaredField("isConstructor");
                        constructorField.setAccessible(true);
                        constructorField.set(entry, true);

                        break;
                    case SC_FN_FALLBACK:
                        Field fallBackField = entry.getClass().getDeclaredField("isFallback");
                        fallBackField.setAccessible(true);
                        fallBackField.set(entry, true);

                        break;
                    default:
                        throw new IllegalArgumentException("entry.type#" + entry.type);
                }


                setParamLenghts(entry.inputs);
                setParamLenghts(entry.outputs);


            } catch (NoSuchFieldException | IllegalAccessException e) {
                GENERAL.debug("",e);
            }


            list.add(entry);
        }
        return Collections.unmodifiableList(list);
    }



    public static String buildMethodSignature(ContractAbiEntry entry){
        StringBuilder b = new StringBuilder();

        b.append(entry.name).append("(");
        List<ContractAbiIOParam> inputs = entry.inputs;
        int i = 0;

        while (i < inputs.size()) {
            ContractAbiIOParam temp = inputs.get(i);//add parameter list
            b.append(temp.getType());
            if (i != inputs.size() -1)
                b.append(",");
            i++;
        }

        b.append(")");

        return b.toString();
    }
    /**
     * Read all abi's and store them in a map
     * @param service
     * @return
     * @throws AionApiException
     */
    public static Map<String, List<ContractAbiEntry>> readAllABIs(AionService service) throws AionApiException {


        Map<String, String> jsonAbi  = getJSONEntryStream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));



        Map<String, String> solFiles = getSOLEntryStream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, List<ContractAbiEntry>> entriesMap = new ConcurrentHashMap<>();

        for(var entry : jsonAbi.entrySet()){

            entriesMap.put(entry.getKey(),
                    Collections.unmodifiableList(
                            new CopyOnWriteArrayList<>(generateABIFromJson(entry.getValue()))
                    ));

        }

        for(var entry : solFiles.entrySet()){

            entriesMap.put(entry.getKey(),
                    Collections.unmodifiableList(
                            new CopyOnWriteArrayList<>(service.compileResponse(entry.getValue(), entry.getKey()).getAbiDefinition())
                    ));

        }



        return Collections.unmodifiableMap(entriesMap);

    }


    public static Map<String,String> readALLJSONABIs(AionService service){
        Map<String, String> jsonAbi  = getJSONEntryStream()
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, String> solFiles = getSOLEntryStream()
                .map(e -> {
                    try{
                        e.setValue( service.compileResponse(e.getValue(),e.getKey()).getAbiDefString());
                        return Optional.of(e);
                    } catch (AionApiException e1) {
                        return Optional.<Map.Entry<String, String>>empty();
                    }

                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String,String> map = new ConcurrentHashMap<>();
        map.putAll(jsonAbi);
        map.putAll(solFiles);
        return Collections.unmodifiableMap(map);
    }

    /**
     *
     * @return A stream containing all the contract names and sol source strings as a key value pair (name-source)
     */
    private static Stream<Map.Entry<String, String>> getSOLEntryStream() {
        return listSolFiles().entrySet().stream()
                .map(entry -> {
                    Optional<Map.Entry<String, String>> src = Optional.empty();
                    try {
                        entry.setValue(new String(Files.readAllBytes(Paths.get(entry.getValue()))));
                        src = Optional.of(entry);
                    } catch (Exception e) {
                        GENERAL.debug("",e);

                    }

                    return src;
                })
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     *
     * @return A stream containing all the contract names and json strings as a key value pair (name-source)
     */
    private static Stream<Map.Entry<String, String>> getJSONEntryStream() {
        return listABIFiles().entrySet().stream()
                .map(entry -> {
                    try {
                        entry.setValue(new String(Files.readAllBytes(Paths.get(entry.getValue()))));
                        return Optional.of(entry);
                    } catch (IOException e) {
                        return Optional.<Map.Entry<String, String>>empty();
                    }


                })
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public List<ContractAbiEntry> getAllEvents(){
        return contractABIEntryMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(ContractAbiEntry::isEvent)
                .collect(Collectors.toUnmodifiableList());//store as a unmodifiable list to avoid accidental updates
    }


    /**
     * Returns the contractAbiEntry of the specified contract
     * @param contractName
     * @return an unmodifiable list
     */
    public List<ContractAbiEntry> getABI(String contractName){
        return contractABIEntryMap.get(contractName);

    }

    public String getJSONString(String contractName){
        return contractJSONABIMap.get(contractName);
    }

    private static void setParamLenghts(List<ContractAbiIOParam> params) throws NoSuchFieldException, IllegalAccessException {
        for(var param: params){

            Field paramList = param.getClass().getDeclaredField("paramLengths");
            paramList.setAccessible(true);
            paramList.set(param, setParametersList(param.getType()));

        }
    }


    private static List<Integer> setParametersList(String in) {
        final Matcher m = ELEMENT_PATTERN.matcher(in);
        List<Integer> dParams = new ArrayList<>();

        while (m.find()) {
            if (!m.group(1).equals("")) {
                dParams.add(Integer.parseInt(m.group(1)));
            } else {
                if (GENERAL.isDebugEnabled()) {
                    GENERAL.debug("[setParamList] Unsupported parameter type?");
                }
                dParams.add(-1);
            }
        }
        return dParams;
    }

    public void update(AionService service) throws AionApiException {
        contractABIEntryMap = readAllABIs(service);
        contractJSONABIMap = readALLJSONABIs(service);
    }
}
