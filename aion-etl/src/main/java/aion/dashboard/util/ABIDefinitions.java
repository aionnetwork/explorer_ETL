package aion.dashboard.util;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.InitializationException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.aion.api.IUtils;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.type.CompileResponse;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.ContractAbiIOParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
public class ABIDefinitions {

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    public final String atstokenabi;

    static {
        try {
            Instance = new ABIDefinitions();
        } catch (InitializationException e) {
            System.out.println("Failed to read the abi");
            System.exit(-1);
        }
    }

    public final String erc20Tokenabi;

    public final List<ContractAbiEntry> atstokenabientries;


    private static ABIDefinitions Instance;
    private final List<ContractAbiEntry> erc20Tokenabientries;


    private final Map<String, List<ContractAbiEntry>> contractABIEntryMap;
    private final Map<String, String> contractJSONABIMap;


    public ABIDefinitions() throws InitializationException {
        String atsTokenSrc;
        String erc20TokenSrc;

        try {
            AionService service = AionService.getInstance();
            service.reconnect();
            contractABIEntryMap = readAllABIs(service);
            contractJSONABIMap = readALLJSONABIs(service);

             atsTokenSrc = new String(Files.readAllBytes(Paths.get("contracts/ATS.sol")));
             erc20TokenSrc = new String(Files.readAllBytes(Paths.get("contracts/ERC20.sol")));

            CompileResponse ats = service.compileResponse(atsTokenSrc, "ATS");
            CompileResponse erc20 = service.compileResponse(erc20TokenSrc, "ERC20");
            atstokenabi = ats.getAbiDefString();
            atstokenabientries = Collections.unmodifiableList(ats.getAbiDefinition());

            erc20Tokenabi = erc20.getAbiDefString();
            erc20Tokenabientries = Collections.unmodifiableList(erc20.getAbiDefinition());


        }
        catch (Exception e){
            GENERAL.debug("Threw an error in ABIDefinitions. ", e);
            throw new InitializationException();
        }


    }

    public static ABIDefinitions getInstance() {

        return Instance;
    }

    private static Map<String,String> listABIFiles(){
        File contractFolder = new File("./contracts");




        return Arrays.stream(Objects.requireNonNull(contractFolder.listFiles()))
                .map(File::getName)
                .filter(s -> s.endsWith("js"))// get only the js files
                .collect(Collectors.toMap((String s)-> s.replace(".js",""), s -> "contracts/"+s));//use contract name as the key
    }


    private static Map<String, String> listSolFiles(){
        //The solidity contracts must have the same name as the stored contract
        File contractFolder = new File("./contracts");
        return Arrays.stream(Objects.requireNonNull(contractFolder.listFiles()))
                .map(File::getName)
                .filter(s -> s.endsWith("sol"))//filter out the js files
                .collect(Collectors.toMap((String s)-> s.replace(".sol",""), s -> "contracts/"+s));//Use the contract name as the key in the key value pair
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
                e.printStackTrace();
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

        Map<String, List<ContractAbiEntry>> entriesMap = new HashMap<>();

        for(var entrySet : jsonAbi.entrySet()){

            entriesMap.put(entrySet.getKey(), generateABIFromJson((entrySet.getValue())));

        }

        for(var entrySet : solFiles.entrySet()){

            entriesMap.put(entrySet.getKey(), Collections.unmodifiableList(service.compileResponse(entrySet.getValue(), entrySet.getKey()).getAbiDefinition()));

        }



        return Collections.unmodifiableMap(entriesMap);

    }


    public static Map<String,String> readALLJSONABIs(AionService service){
        Map<String, String> jsonAbi  = getJSONEntryStream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

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
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        HashMap<String,String> map = new HashMap<>();
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

                        e.printStackTrace();
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
                    } catch (Exception e) {
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

}
