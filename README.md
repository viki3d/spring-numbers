# Numbers
Java, Spring, Reactive (Spring Webflux), Java Concurrency (ExecutorService),
Simultaneous Spring WebClient Calls.  
  
This demo application demonstrates multiple calls to the server. Observing the
WebClient internal capabilites for simulateneous calls. How to build and
configure the client. OpenApi _Delegate Pattern_ explained.

### USER STORY  
 * The client provides an integer number and receives the English word, describing this number.
 * The client requests a list of integers and receives a list of English words. 

#### Basic calls
_Client_  
`http://localhost:8081/get/1`  
_Server_  
`http://localhost:8080/api/v1/numbers/1`  

#### Single call
![spring-numbers-01.png](spring-numbers-01.png?id=v1)  

```
http://localhost:8081/get/1      -->  {"word":"one"}
http://localhost:8081/get/2      -->  {"word":"two"}
...
http://localhost:8081/get/12     -->  {"word":"twelve"}
...
```


#### Parallel calls
![spring-numbers-02.png](spring-numbers-02.png?id=v1)  

##### Webclient parallelisation
The WebClient has internal parallelisation capabilities. Do not create multiple
instances of the WebClient but use its internal capabilites for concurrency. 
(Actually WebClient uses custom Thread Pool for this).  

```
http://localhost:8081/list1      -->

[
   {"word":"four"},
   {"word":"five"},
   {"word":"one" },
   ...
]
```

##### Java thread pool parallelisation
This type of concurrency is recommended to be used with lower level - TcpClient 
or HttpClient. For WebClient use its concurrency capabilites.  

```
http://localhost:8081/list2      -->

[
   {"word":"six"},
   {"word":"two"},
   {"word":"one"},
   ...
]
```


### The API  

**numbers-api-v1.0.0.yaml**  

```
openapi: 3.0.0
info:
  title: Numbers API
  version: "1.0.0"
servers:
  - url: https://viki3d.com/
tags:
  - name: Numbers
paths:
  /api/v1/numbers/{id}:
    get:
      tags:
        - numbers
      summary: Returns the word, describing a given number.
      operationId: getNumberName
      description: Gets the English text word for an integer number.
      parameters:
        - in: "path"
          name: "id"
          description: The integer number itself
          required: true
          schema:
            type: string
      responses:
        '200':
          description: search results matching criteria
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/NumberWord'
        '400':
          description: bad input parameter
components:
  schemas:
    NumberWord:
      properties:    
        word:
          type: string

```

### The OpenApi _Delegate Pattern_  
![spring-numbers-03.png](spring-numbers-03.png?id=v1)  

### The Client Configuration  
![spring-numbers-04.png](spring-numbers-04.png?id=v1)  

ConnectionPool - pool overloading

### The Numbers Client  

### The Numbers Server  

