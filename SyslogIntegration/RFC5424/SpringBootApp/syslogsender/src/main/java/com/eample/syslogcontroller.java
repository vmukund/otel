package com.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class syslogcontroller {

	@GetMapping("/")
  @SuppressWarnings("CallToPrintStackTrace")
	public String index() {
		try {
            SyslogSender sender = new  SyslogSender("MyApp");
            // Create some structured data
          Map<String, String> structuredData = new HashMap<>();
          structuredData.put("requestId", "12345");
          structuredData.put("environment", "production");
            
            // Send test messages with structured data
	          sender.info("INFO001", structuredData, "Application started successfully");
            /* sender.error("ERR001", structuredData, "Failed to connect to database");
            sender.debug("DBG001", structuredData, "Processing request #12345");
            sender.warning("WARN001", structuredData, "High memory usage detected");
            sender.notice("NOTE001", structuredData, "Configuration file loaded");
            */
            // Send test messages without structured data
            // sender.info("INFO002", "Simple info message");
            // sender.error("ERR002", "Simple error message");
            
            // Sleep briefly to ensure messages are sent
            // Thread.sleep(1000);
        
        } catch (IOException e) {
           e.printStackTrace();
	    }
		return "Greetings from Spring Boot!";
	}

}

