package aion.dashboard.email;

import aion.dashboard.config.Config;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * This service is to be used to notify the maintainers any issue with the etl
 */
public class EmailService {

	private String senderUsername;
	private String senderPassword;
	private List<String> recipientList;
    private static final long TIME_OUT = Config.getInstance().getTimeOut();
    private Stopwatch timer;//ensures that the maintainers of the server aren't spammed with too many notifications
	private Properties props;
	private String hostName;

	private ExecutorService executor;


	private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
	private static final EmailService INSTANCE = new EmailService();
	public static EmailService getInstance() {
		return INSTANCE;
	}

	private EmailService() {
		if (INSTANCE != null)//Ignore linting rule this is to enforce the singleton
			throw new IllegalStateException("EmailService already instantiated!");

        timer = null;
		Config config = Config.getInstance();

		senderUsername = config.getEmailSenderUsername();
		senderPassword = config.getEmailSenderPassword();
		recipientList = config.getEmailRecipients();
		hostName = config.getHostname();
		props = new Properties();


		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		executor = Executors.newSingleThreadExecutor();
	}

	public boolean send(String subject, String content) {
        if (!Config.getInstance().areReportsEnabled()) return false;

		if ( (senderUsername == null || senderPassword == null || recipientList == null) || (timer != null && timer.elapsed(TimeUnit.MINUTES) <= TIME_OUT))
			return false;
        if (timer == null || timer.elapsed(TimeUnit.MINUTES) > TIME_OUT)
            timer = Stopwatch.createStarted();//reset the timer if the timeout has elapsed or this is the first time the send method runs
		executor.execute(taskSendEmail(subject, content));
		return true;
	}

	private Runnable taskSendEmail(final String subject, final String content) {
		return () ->{
			for (String recipient : recipientList) {
				sendEmail(recipient, subject, String.format("Hostname: %s%n%s", hostName, content ));
			}
		};
	}
	
	private void sendEmail(String recipient, String subject, String body) {
		try {
			Session session = Session.getDefaultInstance(props, null);

			MimeMessage msg = new MimeMessage(session);
			//set message headers
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");
			msg.setFrom(new InternetAddress(senderUsername, hostName+": Report"));
			msg.setReplyTo(InternetAddress.parse(senderUsername, false));
			msg.setSubject(subject, "UTF-8");
			msg.setContent(body, "text/html; charset=utf-8");
			msg.setSentDate(new Date());
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
			Transport transport = session.getTransport("smtp");
			transport.connect("smtp.gmail.com", Config.getInstance().getEmailSenderUsername(), Config.getInstance().getEmailSenderPassword() );

			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
		}
		catch (Exception e) {
			GENERAL.debug("Caught an exception in email service: ",e);
			GENERAL.debug("Failed to send message :\n {} ",body);
		}
	}


	public void close(){
		executor.shutdown();

		try {
			if (executor.awaitTermination(1500, TimeUnit.MILLISECONDS)){
				executor.shutdownNow();

			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
