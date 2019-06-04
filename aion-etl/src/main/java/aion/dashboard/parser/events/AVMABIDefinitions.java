package aion.dashboard.parser.events;

import aion.dashboard.util.Tuple2;
import com.google.common.base.Strings;
import org.aion.util.bytes.ByteUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AVMABIDefinitions {

    private static final Pattern NAIVE_EVENT_PATTERN;
    private static final AVMABIDefinitions INSTANCE;
    private static final String FILE_LOCATION;
    public static final String ATS_CONTRACT="ATS";
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    static {
        try {
            FILE_LOCATION = "contracts/avm";
            NAIVE_EVENT_PATTERN = Pattern.compile("^\\w*\\(((\\w*\\s*){2,3},)*((\\w*\\s*){2,3})?\\)$");
            INSTANCE = new AVMABIDefinitions();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }



    private Map<String, List<AVMEventSignature>> avmSignatureMap;

    private AVMABIDefinitions() {
//


    }

    synchronized Map<String, List<AVMEventSignature>> getAvmSignatureMap() {
        if (avmSignatureMap == null) {
            avmSignatureMap = eventsFromAllFiles();
        }
        return avmSignatureMap;
    }

    public static AVMABIDefinitions getInstance() {
        return INSTANCE;
    }

    static String hashstr(String str) {
        //avm topics are hashed:
        return Strings.padEnd(ByteUtil.toHexString(str.getBytes()), 64, '0');
    }

    public static AVMEventSignature fromLine(String line) {


        String[] nameParamsArr = line.replaceAll("\\)", "").split("\\(");//split the event signature into a name and parameter list
        var hashed = hashstr(nameParamsArr[0]);

        var params = nameParamsArr[1].split(",");// split the the parameter list into individual parameter elements


        List<Parameter> indexedParam = new ArrayList<>();
        List<Parameter> nonIndexedParam = new ArrayList<>();

        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            //NOTE: indexed parameters are found in the topics and non-indexed are found in the data of the txlog
            if (param.contains("indexed")) {// if this parameter is defined as indexed, go here
                var typeIndexNameArr = param.stripLeading().split("\\s+");

                indexedParam.add(new Parameter(typeIndexNameArr[0], typeIndexNameArr[2], true, i));
            } else {// if this parameter is not indexed go here
                var typeNameArr = param.stripLeading().split("\\s+");

                nonIndexedParam.add(new Parameter(typeNameArr[0], typeNameArr[1], false, i));
            }
        }

        return new AVMEventSignature(indexedParam, nonIndexedParam, hashed, line, nameParamsArr[0]);
    }

    public static Optional<List<AVMEventSignature>> fromFile(String fileName) {
        try {
            var lines = new String(Files.readAllBytes(Path.of(fileName))).split("\\r?\\n");

            return Optional.of(Arrays.stream(lines)
                    .parallel()
                    .map(AVMABIDefinitions::fromLine)//find the event
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    static Map<String, List<AVMEventSignature>> eventsFromAllFiles() {

        var fileLocations = fileNamesFor(FILE_LOCATION);
        if (!fileLocations.isEmpty()) {
            Map<String, List<AVMEventSignature>> signatureMap = new HashMap<>();
            for (var avmFile: fileLocations){
                var res = fromFile(avmFile);
                res.ifPresent(sig-> signatureMap.put(getContractName(avmFile), sig));
            }
            return signatureMap;
        } else {
            return Collections.emptyMap();
        }

    }



    static List<String> fileNamesFor(String dir){
        File directory = new File(dir);

        if (directory.isDirectory() && directory.listFiles() != null){
            //noinspection ConstantConditions
            return Arrays.stream(directory.listFiles()).map(File::getAbsolutePath).collect(Collectors.toList());
        }
        else{
            return Collections.emptyList();
        }
    }

    private static String getContractName(String path){
        String[] strArr = path.split("/");
        if (strArr.length >0){
            String fileName = strArr[strArr.length -1];
            return fileName.replaceFirst(".avm$","");
        }
        else return "";
    }


    public List<AVMEventSignature> allEvents() {
        return getAvmSignatureMap().values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public List<AVMEventSignature> signatureForContract(String contract) {
        return getAvmSignatureMap().getOrDefault(contract, Collections.emptyList());
    }

    public List<String> getAllHashes() {
        return getAvmSignatureMap().values().stream()
                .flatMap(Collection::stream)
                .map(e -> e.hashed)
                .collect(Collectors.toList());
    }

    /**
     * Stores formatting data about each parameter.
     */
    public static class Parameter {
        final String type;
        final String name;
        final boolean indexed;
        final int index;// used to construct the event

        Parameter(String type, String name, boolean indexed, int index) {
            this.type = type.strip();
            this.name = name.strip();
            this.indexed = indexed;
            this.index = index;
        }

        @Override
        public String toString() {

            JSONObject object = new JSONObject();
            object.put("type", type);
            object.put("name", name);
            object.put("indexed", index);
            object.put("index", index);

            return object.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Parameter)) return false;
            Parameter parameter = (Parameter) o;
            return indexed == parameter.indexed &&
                    index == parameter.index &&
                    type.equals(parameter.type) &&
                    name.equals(parameter.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name, indexed, index);
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public boolean isIndexed() {
            return indexed;
        }

        public int getIndex() {
            return index;
        }
    }

    /**
     * The event signature based on the format found in the avm folder
     */
    public static class AVMEventSignature {
        final List<Parameter> indexedParameters;
        final List<Parameter> nonIndexedParameters;
        final String hashed;
        final String signature;
        final String name;

        AVMEventSignature(List<Parameter> indexedParameters, List<Parameter> nonIndexedParameters, String hashed, String signature, String name) {
            this.indexedParameters = indexedParameters;
            this.nonIndexedParameters = nonIndexedParameters;
            this.hashed = hashed;
            this.signature = signature;
            this.name = name;
        }

        @Override
        public String toString() {
            JSONObject object = new JSONObject();
            object.put("indexedParameters", indexedParameters.stream().map(Parameter::toString).toArray());
            object.put("nonIndexedParameters", nonIndexedParameters.stream().map(Parameter::toString).toArray());
            object.put("hashed", hashed);
            object.put("signature", signature);
            object.put("name", name);
            return object.toString(4);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AVMEventSignature)) return false;
            AVMEventSignature that = (AVMEventSignature) o;
            return indexedParameters.stream().sorted(Comparator.comparingInt(p -> p.index)).collect(Collectors.toList()).equals(that.indexedParameters.stream().sorted(Comparator.comparingInt(p -> p.index)).collect(Collectors.toList())) &&
                    nonIndexedParameters.stream().sorted(Comparator.comparingInt(p -> p.index)).collect(Collectors.toList()).equals(that.nonIndexedParameters.stream().sorted(Comparator.comparingInt(p -> p.index)).collect(Collectors.toList())) &&
                    hashed.equals(that.hashed) &&
                    signature.equals(that.signature) &&
                    name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(indexedParameters, nonIndexedParameters, hashed, signature, name);
        }

        public List<Parameter> getIndexedParameters() {
            return indexedParameters;
        }

        public List<Parameter> getNonIndexedParameters() {
            return nonIndexedParameters;
        }

        public String getHashed() {
            return hashed;
        }

        public String getSignature() {
            return signature;
        }

        public String getName() {
            return name;
        }
    }
}
