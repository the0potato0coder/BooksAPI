# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.6/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.6/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.6/reference/web/servlet.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.6/reference/using/devtools.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

## Book Search Endpoints

Base path: `/api/books`

1. `GET /api/books?query={keyword}`
	- Returns all books whose title, subtitle, author, description, publisher or ISBN contain the (case-insensitive) keyword. If `query` is omitted, all books are returned.

2. `POST /api/books/search`
	- Request Body JSON:
	  `{ "keywords": ["java", "press"], "matchMode": "ANY" }`
	  - `matchMode`: `ANY` (default) matches if any keyword is present; `ALL` requires every keyword to be present across the searchable fields.
	- Response: JSON array of matching book objects.

3. `GET /api/books/reload`
	- Forces (lazy) load of the books data (first call fetches remote URL, failing over to bundled `books.json` classpath resource). Returns metadata (`count`, `lastLoaded`).

### Fallback Logic
On first access the service tries to download the remote JSON from the configured property `books.remote.url` (see `application.properties`). If that HTTP request fails, the service transparently falls back to `classpath:books.json`.

### Example curl Usage
```
curl http://localhost:8080/api/books?query=javascript
curl -H "Content-Type: application/json" -d '{"keywords":["modern","press"],"matchMode":"ANY"}' http://localhost:8080/api/books/search
```

### Tests
Run the test suite:
```
mvnw.cmd test
```


