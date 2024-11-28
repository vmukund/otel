package com.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class SyslogSender {
    private static final String SYSLOG_HOST = "myhostname";
    // local host of collector service as configured in docker compose
    private static final int SYSLOG_PORT = 54527;
    private static final String VERSION = "1";  // RFC5424 version
    
    // Syslog facility levels
    public static final int FACILITY_USER = 1;
    
    // Syslog severity levels
    public static final int SEVERITY_ERROR = 3;
    public static final int SEVERITY_WARNING = 4;
    public static final int SEVERITY_NOTICE = 5;
    public static final int SEVERITY_INFO = 6;
    public static final int SEVERITY_DEBUG = 7;
    
    private final String hostname;
    private final DateTimeFormatter timestampFormat;
    private final String appName;
    
    public SyslogSender(String appName) throws IOException {
        this.hostname = InetAddress.getLocalHost().getHostName();
        this.appName = appName;
        // RFC5424 requires ISO 8601 timestamps with optional fractional seconds
        this.timestampFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
            .withZone(ZoneId.systemDefault());
    }
    
    private String formatStructuredData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return "-";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[meta@47450 ");
        
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String value = entry.getValue()
                .replace("\"", "\\\"")
                .replace("]", "\\]")
                .replace("\\", "\\\\");
            sb.append(entry.getKey()).append("=\"").append(value).append("\" ");
        }
        
        sb.setLength(sb.length() - 1); // Remove trailing space
        sb.append("]");
        return sb.toString();
    }
 
    @SuppressWarnings("CallToPrintStackTrace")
    public void sendMessage(int facility, int severity, String procId, String msgId,
                          Map<String, String> structuredData, String message) {
        // Calculate priority
        int priority = (facility * 8) + severity;
        
        // Get current timestamp in RFC5424 format
        String timestamp = timestampFormat.format(Instant.now());
        
        // Format structured data
        String sdElement = formatStructuredData(structuredData);
        
        procId = "-";
        // Format the syslog message according to RFC5424
        // <priority>version timestamp hostname app-name procid msgid structured-data message
        String syslogMessage = String.format("<%d>%s %s %s %s %s %s %s %s",
                priority,
                VERSION,
                timestamp,
                hostname,
                appName,
                (procId != null ? procId : "-"),
                (msgId != null ? msgId : "-"),
                sdElement,
                message);

        System.out.println("timestamp: " + timestamp ); 
        System.out.println("version: " + VERSION );
	    System.out.println("hostname: " + hostname);
        System.out.println("appname: " + appName);
	    System.out.println("procId: " + procId);
 	    System.out.println("msgId: " + msgId);
        System.out.println("sdElement: " + sdElement);
        System.out.println("message: " + message);
        System.out.println("Formed Syslog Message: " + syslogMessage);
        
        try (Socket socket = new Socket( SYSLOG_HOST, SYSLOG_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            writer.println(syslogMessage);
        } catch (IOException e) {
            System.err.println("Failed to send syslog message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Convenience methods for different severity levels
    public void info(String msgId, String message) {
        sendMessage(FACILITY_USER, SEVERITY_INFO, null, msgId, null, message);
    }
    
    public void info(String msgId, Map<String, String> structuredData, String message) {
        sendMessage(FACILITY_USER, SEVERITY_INFO, null, msgId, structuredData, message);
    }
    
    public void error(String msgId, String message) {
        sendMessage(FACILITY_USER, SEVERITY_ERROR, null, msgId, null, message);
    }
    
    public void error(String msgId, Map<String, String> structuredData, String message) {
        sendMessage(FACILITY_USER, SEVERITY_ERROR, null, msgId, structuredData, message);
    }
    
    public void debug(String msgId, String message) {
        sendMessage(FACILITY_USER, SEVERITY_DEBUG, null, msgId, null, message);
    }
    
    public void debug(String msgId, Map<String, String> structuredData, String message) {
        sendMessage(FACILITY_USER, SEVERITY_DEBUG, null, msgId, structuredData, message);
    }
    
    public void warning(String msgId, String message) {
        sendMessage(FACILITY_USER, SEVERITY_WARNING, null, msgId, null, message);
    }
    
    public void warning(String msgId, Map<String, String> structuredData, String message) {
        sendMessage(FACILITY_USER, SEVERITY_WARNING, null, msgId, structuredData, message);
    }
    
    public void notice(String msgId, String message) {
        sendMessage(FACILITY_USER, SEVERITY_NOTICE, null, msgId, null, message);
    }
    
    public void notice(String msgId, Map<String, String> structuredData, String message) {
        sendMessage(FACILITY_USER, SEVERITY_NOTICE, null, msgId, structuredData, message);
    }
}
