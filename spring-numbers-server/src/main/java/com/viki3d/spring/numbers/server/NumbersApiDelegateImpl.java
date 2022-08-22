package com.viki3d.spring.numbers.server;

import com.viki3d.spring.numbers.server.api.NumbersApiDelegate;
import com.viki3d.spring.numbers.server.model.NumberWord;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Receives an integer number and responds with a word, describing this integer, wrapped as 
 * {@code NumberWord}.
 *
 * @author Victor Kirov
 */
@Component
public class NumbersApiDelegateImpl implements NumbersApiDelegate {

  private static final boolean RANDOM_PROCESSING_DELAY = true;
  private static final long MIN_PROCESSING_TIME_MS = 100;
  private static final long MAX_PROCESSING_TIME_MS = 400;
  private static final String KEY_SESION_COUNTER = "session_counter";

  private static Map<String, String> map = new HashMap<>();
  
  static {
    // Initialize the test data: 1..100
    map.put("1", "one");
    map.put("2", "two");
    map.put("3", "three");
    map.put("4", "four");
    map.put("5", "five");
    map.put("6", "six");
    map.put("7", "seven");
    map.put("8", "eight");
    map.put("9", "nine");
    map.put("10", "ten");
    map.put("11", "eleven");
    map.put("12", "twelve");
    map.put("13", "thirtheen");
    map.put("14", "fourteen");
    map.put("15", "fifteen");
    map.put("16", "sixteen");
    map.put("17", "seventeen");
    map.put("18", "eighteen");
    map.put("19", "nineteen");
    map.put("20", "twenty");

    for (Integer i = 21; i < 100; i++) {
      map.put(i.toString(), "number-" + i.toString());
    }
  }
  
  public static HttpSession getSession() {
	    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder
	    		.currentRequestAttributes();
	    return attr.getRequest().getSession(true); // true == allow create
  }  
  
  // Define logger
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  // Reuse this Random object for all invocations
  private Random random = new Random();
  
  // Use counter, shared between all controller invocations (concurrency unsafe)
  private AtomicInteger commonCounter = new AtomicInteger();
  
  // Use counter specific for every controller instance
  @Bean
  @Scope(value = WebApplicationContext.SCOPE_REQUEST)
  private Integer specificCounter1() {
	  return 0;
  }

  // Use counter specific for every controller instance
  private Integer specificCounter2() {
	  Integer c = (Integer) getSession().getAttribute(KEY_SESION_COUNTER);
	  if (c==null) {
		  c = 0;
	  }
	  return c;
  }
  
  
  @Override
  public ResponseEntity<NumberWord> getNumberName(String id) {
    // Calculate processing time delay to be simulated
    long processingTimeInMs;
    if (RANDOM_PROCESSING_DELAY) {
      int diff = (int) ((MAX_PROCESSING_TIME_MS - MIN_PROCESSING_TIME_MS) / 100); 
      processingTimeInMs = MIN_PROCESSING_TIME_MS + random.nextInt(diff) * 100;
    } else {
      processingTimeInMs = MAX_PROCESSING_TIME_MS;
    }

    // Simulate the calculated processing time
    try {
      Thread.sleep(processingTimeInMs);
    } catch (InterruptedException e) {
      e.printStackTrace();
      // Restore interrupted state...
      Thread.currentThread().interrupt();
    }

    // Store processing time in milliseconds in the response header
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("ProcessingTimeInMs:", Long.toString(processingTimeInMs));

    // Set the final result body
    NumberWord numberWord = new NumberWord();
    numberWord.setWord(map.get(id) + " / processingTimeInMs=" + processingTimeInMs);
    
    // Test counters
    testCounters();

    // Return: ResponseEntity<NumberWord>
    return new ResponseEntity<>(numberWord, responseHeaders, HttpStatus.OK);
  }

  private void testCounters() {
    int i;
    commonCounter.addAndGet(1);
    i = commonCounter.addAndGet(1);
    logger.debug("Common counter = {}", i);

    i = specificCounter1();
    i++;
    i++;
    logger.debug("specificCounter1 = {}", i); //always == 2 (controller instance specific counter)

    i = specificCounter2();
    i++;
    i++;
    logger.debug("specificCounter2 = {}", i); //always == 2 (controller instance specific counter)
  }
  
}
