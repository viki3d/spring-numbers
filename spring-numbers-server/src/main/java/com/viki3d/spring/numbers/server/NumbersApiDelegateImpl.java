package com.viki3d.spring.numbers.server;

import com.viki3d.spring.numbers.server.api.NumbersApiDelegate;
import com.viki3d.spring.numbers.server.model.NumberWord;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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
  
  // Reuse Random object for all invocations
  private Random random = new Random();

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

    // Return: ResponseEntity<NumberWord>
    return new ResponseEntity<>(numberWord, responseHeaders, HttpStatus.OK);
  }

}
