package com.example;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableJms
@SpringBootApplication
public class MqspringApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(MqspringApplication.class, args);
	}
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MqspringApplication.class);
    }
}

@RestController
class Controller{
	private static Logger log = LoggerFactory.getLogger(Controller.class);
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@GetMapping(value = "/hi", produces = { MediaType.APPLICATION_JSON_VALUE } )
	public ResponseEntity<ResponseDto> hi(HttpServletResponse response) {
		ResponseDto responseDto = new ResponseDto();
		responseDto.setResponse("hi");
		
		return  new ResponseEntity<>(responseDto, HttpStatus.OK); 
	}
	
	@GetMapping("send")
	public ResponseEntity<ResponseDto> send() {
		ResponseDto responseDto = new ResponseDto();
		
		try {
    		jmsTemplate.convertAndSend("SC.CASS.TNI_INPPMT", "Hello World!");
    		responseDto.setResponse("OK");
    		
    		return new ResponseEntity<>(responseDto, HttpStatus.OK);
		} catch (JmsException e) {
			e.printStackTrace();
			log.error("Exception: ", e);
			responseDto.setResponse("FAIL");
			
			return new ResponseEntity<>(responseDto, HttpStatus.OK);
		}
	}
	
	@GetMapping("receive")
	public ResponseEntity<ResponseDto> recv(){
		ResponseDto responseDto = new ResponseDto();
		
	    try{
	    	Object message = jmsTemplate.receiveAndConvert("SC.CASS.TNI_INPPMT"); // Blocking
	    	if(message != null) {
	    		log.info("Message received: {}", message);
	    	}
	    	responseDto.setResponse("OK");
	    	
	        return new ResponseEntity<>(responseDto, HttpStatus.OK); 
	    }catch(JmsException ex){
	        ex.printStackTrace();
	        responseDto.setResponse("FAIL");
	        
	        return new ResponseEntity<>(responseDto, HttpStatus.OK);
	    }
	}
}
