package aion.dashboard;

import aion.dashboard.config.Config;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.task.InitTask;
import org.slf4j.LoggerFactory;

public class AionMain {


	static {
	    System.setProperty("logback.configurationFile", "src/main/resources/logback.xml");

    }

	public static void main(String[] args) throws Web3ApiException {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("logger_general")).setLevel(Config.getInstance().getGeneralLevel());
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("logger_integrity")).setLevel(Config.getInstance().getIntegrityCheckLevel());
		if (args.length == 0)
			InitTask.start();
		else {
			InitTask.checkArgs(args);
		}
	}

}



