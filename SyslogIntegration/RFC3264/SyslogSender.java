import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SyslogSender {
    private static final String SYSLOG_HOST = "127.0.0.1";
    private static final int SYSLOG_PORT = 54527;
    
    // Syslog facility levels
    public static final int FACILITY_USER = 1;
    
    // Syslog severity levels
    public static final int SEVERITY_ERROR = 3;
    public static final int SEVERITY_WARNING = 4;
    public static final int SEVERITY_NOTICE = 5;
    public static final int SEVERITY_INFO = 6;
    public static final int SEVERITY_DEBUG = 7;
    
    private final SimpleDateFormat dateFormat;
    private final String hostname;
    
    public SyslogSender() throws IOException {
        this.dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss", Locale.US);
        this.hostname = InetAddress.getLocalHost().getHostName();
    }
    
    public void sendMessage(int facility, int severity, String tag, String message) {
        // Calculate priority
        int priority = (facility * 8) + severity;
        
        // Format the syslog message according to RFC3164
        String timestamp = dateFormat.format(new Date());
        String syslogMessage = String.format("<%d>%s %s %s: %s",
                priority, timestamp, hostname, tag, message);
        
        try (Socket socket = new Socket(SYSLOG_HOST, SYSLOG_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            writer.println(syslogMessage);
        } catch (IOException e) {
            System.err.println("Failed to send syslog message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void info(String tag, String message) {
        sendMessage(FACILITY_USER, SEVERITY_INFO, tag, message);
    }
    
    public void error(String tag, String message) {
        sendMessage(FACILITY_USER, SEVERITY_ERROR, tag, message);
    }
    
    public void debug(String tag, String message) {
        sendMessage(FACILITY_USER, SEVERITY_DEBUG, tag, message);
    }
    
    public void warning(String tag, String message) {
        sendMessage(FACILITY_USER, SEVERITY_WARNING, tag, message);
    }
    
    public void notice(String tag, String message) {
        sendMessage(FACILITY_USER, SEVERITY_NOTICE, tag, message);
    }
    
    // Example usage
    public static void main(String[] args) {
        try {
            SyslogSender sender = new SyslogSender();
            
            // Send some test messages
            sender.info("MyApp", "Application started successfully");
            sender.error("MyApp", "Failed to connect to database");
            sender.debug("MyApp", "Processing request #12345");
            sender.warning("MyApp", "High memory usage detected");
            sender.notice("MyApp", "Configuration file loaded");
            
            // Sleep briefly to ensure messages are sent
            Thread.sleep(1000);
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
