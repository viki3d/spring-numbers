package com.viki3d.spring.numbers.client;

import com.viki3d.spring.numbers.client.api.NumbersApi;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * Spring configuration for the {@code NumbersApi}.
 * 
 * <ul>
 *   <li>S1488: Immediately return this expression instead of assigning it to the temporary variable
 *   </li>
 * </ul>
 *
 * @author User
 */
@Configuration
@PropertySource("classpath:numbers.properties")
@SuppressWarnings("squid:S1488")
public class NumbersApiConfig {

  // Maximum connections allowed for the connection pool: active and/or idle.
  public static final int MAX_CONNECTIONS = 10;

  // How many connections can be stored idle/waiting (in the connPool) before launched
  private static final int MAX_CONNECTIONS_TO_STOCKPILE = 10;

  // How much time can pass (in milliseconds) before we consider this connection timeout.
  private static final int MAX_CONNECTION_TIMEOUT_MS = 2000;

  @Value("${numbers.server.url}")
  private String restUrl;

  /** Prepare ConnectionProvider. */
  @Bean
  public ConnectionProvider connectionProvider() {
    ConnectionProvider connectionProvider = ConnectionProvider.builder("connProvider1")
        .maxConnections(MAX_CONNECTIONS)
        .pendingAcquireMaxCount(MAX_CONNECTIONS_TO_STOCKPILE)
        .build();
    return connectionProvider; //readability&debug friendly: do not return directly
  }

  /** Prepare HttpClient. */
  @Bean
  public HttpClient httpClient(ConnectionProvider connectionProvider) {
    HttpClient httpClient = HttpClient.create(connectionProvider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, MAX_CONNECTION_TIMEOUT_MS)
        .responseTimeout(Duration.ofMillis(MAX_CONNECTION_TIMEOUT_MS));
    return httpClient;
  }

  /** Prepare WebClient. */
  @Bean
  public WebClient webClient(HttpClient httpClient) {
    WebClient webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
    return webClient;
  }

  /** Prepare ApiClient. */
  @Bean
  public ApiClient apiClient(WebClient webClient) {
    ApiClient apiClient = new ApiClient(webClient);
    apiClient.setBasePath(restUrl);
    return apiClient;
  }

  /** Prepare NumbersApi. */
  @Bean
  public NumbersApi numbersApi(ApiClient apiClient) {
    NumbersApi numbersApi = new NumbersApi(apiClient);
    return numbersApi;
  }

}

/*

  // Alternative chain fork to produce HttpClient
  // import reactor.netty.tcp.TcpClient;
  @Bean
  public TcpClient tcpClient(ConnectionProvider connectionProvider) {
    TcpClient tcpClient = TcpClient.create(connectionProvider)
        .host("localhost")
        .port(8080);
    return tcpClient;
  }

  @Bean
  public HttpClient httpClient(TcpClient tcpClient) {
    @SuppressWarnings("deprecation")
    HttpClient httpClient = HttpClient.from(tcpClient)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, MAX_CONNECTION_TIMEOUT_MS)
        .responseTimeout(Duration.ofMillis(MAX_CONNECTION_TIMEOUT_MS));
    return httpClient;
  }


*/
