package hello;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Calendar;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Service
public class BookService {

  private final RestTemplate restTemplate;
  int counter =1;

  public BookService(RestTemplate rest) {
	  this.restTemplate = rest;
  }
  
  @HystrixCommand(fallbackMethod = "hystrixFallback") 
  //@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "`7000")
  public String readingList() {
	  URI uri = URI.create("http://localhost:8090/recommendedbooks");
	  System.out.println(" I am going to try - " + counter++ + " time " + Calendar.getInstance().getTimeInMillis());
	  return this.restTemplate.getForObject(uri, String.class);
  }
  
  //@Retryable(value={SocketException.class,SocketTimeoutException.class, ConnectException.class}, maxAttempts=3, backoff = @Backoff(delay = 1000)) 
  @Retryable(value={SocketException.class,SocketTimeoutException.class, ConnectException.class}, maxAttempts=3) 
  public String callReading(){
	 return readingList();
  }
  

@Recover
  public String retryFallback() {
	System.out.println( " retryFallback - Exceeded the retry limit" );
    return "  retryFallback - Exceeded the retry limit ";
  }

public String hystrixFallback() {
	System.out.println( " hystrixFallback - Circuit has opened as I have tried to reach external resource" );
  return " hystrixFallback - Circuit has opened as I have tried to reach external resource";
}


}
