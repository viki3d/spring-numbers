package com.viki3d.spring.numbers.server;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Main.class})
@ActiveProfiles("test")
class NumbersApiDelegateImplTests {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @LocalServerPort
  int port;

  @BeforeEach
  public void setUp() {
    logger.info("Running setUp");
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
    RestAssured.useRelaxedHTTPSValidation();
  }

  @Test
  void testHealthEndpoint() throws InterruptedException {
    when().get("/actuator/health").then().statusCode(HttpStatus.SC_OK)
        .body("status", equalTo("UP"));
  }

  @Test
  void testGetOne() throws InterruptedException {
    when().get("/api/v1/numbers/1").then().statusCode(HttpStatus.SC_OK)
        .body("word", containsString("one"));
  }

  @Test
  void testGetTwo() throws InterruptedException {
    when().get("/api/v1/numbers/2").then().statusCode(HttpStatus.SC_OK)
        .body("word", containsString("two"));
  }

  @AfterEach
  public void tearDown() {
    logger.info("Running tearDown");
  }

}
