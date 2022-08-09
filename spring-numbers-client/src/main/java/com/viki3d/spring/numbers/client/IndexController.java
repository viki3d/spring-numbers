package com.viki3d.spring.numbers.client;

import com.viki3d.spring.numbers.client.api.NumbersApi;
import com.viki3d.spring.numbers.client.model.NumberWord;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono; 

/**
 * A client controller, querying the server application.
 *
 * @author viki3d
 */
@RestController
@PropertySource("classpath:numbers.properties")
public class IndexController {

  private static final int ITEMS_TO_RETRIEVE = 10;
  
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  @Autowired
  private NumbersApi numbersApi;

  @Value("${numbers.server.url}")
  private String restUrl;

  /**
   * Provide an integer and receives a word, describing this integer.
   *
   * @param id The integer
   * @return The word.
   */
  @GetMapping("/get/{id}")
  public NumberWord getSingle(@PathVariable(value = "id") String id) {
    NumberWord word = numbersApi.getNumberName(id).block();
    logger.debug("Requested: {}, received: {}", id, word);
    return word;
  }

  /**
   * Get all number-words in parallel.
   *
   * @return List of the number-words responses.
   */
  @GetMapping("/list1")
  public List<NumberWord> getAllInParallel() {
    // Define start-time
    final Instant startTime = Instant.now();

    // Prepare range of numbers to be sent as requests
    List<String> list = IntStream.range(1, ITEMS_TO_RETRIEVE).mapToObj(Integer::toString)
        .collect(Collectors.toCollection(ArrayList::new));

    // Request all in parallel
    Flux<NumberWord> fn = fetchNumbers(list);

    // Collect all requested
    List<NumberWord> numbersWordsList = fn.toStream()
        .collect(Collectors.toCollection(ArrayList::new));

    // Define final-time 
    final Instant finalTime = Instant.now();
    long timeElapsedInMillis = Duration.between(startTime, finalTime).toMillis();

    // Log the execution time
    logger.debug("/list1: Execution time = {}.{} sec", 
        timeElapsedInMillis / 1000, timeElapsedInMillis % 1000);
    
    return numbersWordsList;
  }

  private Mono<NumberWord> singleCall(String id) {
    return numbersApi.getNumberName(id);
  }

  private Flux<NumberWord> fetchNumbers(List<String> numberIds) {
    // The maximum number of in-flight inner sequences
    int concurrency = NumbersApiConfig.MAX_CONNECTIONS;

    // Do in parallel max=concurrency single calls:
    return Flux.fromIterable(numberIds).flatMap(this::singleCall, concurrency);
  }

  /**
   * Get all number-words in parallel using {@code ExecutorService} - pure Java approach.
   * 
   * <p>
   * This approach is discouraged on higher level. Don't use it with WebClient but with TcpClient, 
   * HttpClient or else. This method is here for illustration. WebClient uses internally 
   * {@code ExecutorService} for implementing parallelization.
   * </p>
   *
   * @return List of the number-words responses.
   */
  @GetMapping("/list2")
  public List<NumberWord> getAllInParallelPureJava() {

    ExecutorService executor = Executors.newWorkStealingPool(256);

    List<NumberWord> numbersWordsList = Collections.synchronizedList(new ArrayList<>());

    final Instant startTime = Instant.now();

    List<String> list = IntStream.range(1, ITEMS_TO_RETRIEVE).mapToObj(Integer::toString)
        .collect(Collectors.toCollection(ArrayList::new));

    list.stream().forEach(key ->
        executor.submit(() -> {
          NumbersApi numbersApiClient = new NumbersApi();
          numbersApiClient.getApiClient().setBasePath(restUrl);
          NumberWord numberWord = numbersApiClient.getNumberName(key).block(); 
          numbersWordsList.add(numberWord);
        })
    );

    executor.shutdown();

    // Blocks until all tasks have completed execution after a shutdown request, or the timeout 
    // occurs, or the current thread is interrupted, whichever happens first.
    try { 
      executor.awaitTermination(10L, TimeUnit.SECONDS); 
    } catch (InterruptedException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }

    // Define final-time 
    final Instant finalTime = Instant.now();
    long timeElapsedInMillis = Duration.between(startTime, finalTime).toMillis();

    // Log the execution time
    logger.debug("/list2: Execution time = {}.{} sec", 
        timeElapsedInMillis / 1000, timeElapsedInMillis % 1000);
    
    
    return numbersWordsList;
  }

}
