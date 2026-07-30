# 🚀 JAVA 5-21 & SPRING BOOT COMPLETE EVOLUTION GUIDE
## Features • Annotations • Production Issues • Cheat Sheets
**Your Complete Reference for Modern Java & Spring Boot**

---

## TABLE OF CONTENTS

1. [JAVA EVOLUTION (5 to 21)](#java-evolution)
2. [SPRING BOOT EVOLUTION](#spring-boot-evolution)
3. [JAVA FEATURES CHEAT SHEET](#java-cheat-sheet)
4. [SPRING BOOT FEATURES CHEAT SHEET](#spring-boot-cheat-sheet)
5. [COMPLETE ANNOTATIONS REFERENCE](#annotations-reference)
6. [PRODUCTION ISSUES & SOLUTIONS](#production-issues)

---

<a name="java-evolution"></a>
# JAVA EVOLUTION (5 to 21)

## JAVA 5 (2004) - Generics Revolution

### Key Features:

**1. Generics**
```java
// Before Java 5 (Type-unsafe)
List list = new ArrayList();
list.add("string");
list.add(123);
String str = (String) list.get(0);  // Runtime cast error possible

// After Java 5 (Type-safe)
List<String> list = new ArrayList<String>();
list.add("string");
// list.add(123);  // Compile error - prevented at compile time
String str = list.get(0);  // No cast needed
```

**Benefit:** Type safety at compile time, no runtime ClassCastException.

**Production Issue:**
```java
// Problem: Type erasure
List<String> list = new ArrayList<String>();
list.add("test");
if (list instanceof List<String>) { }  // COMPILE ERROR - cannot check generic type at runtime

// Solution: Check raw type
if (list instanceof List) { }  // OK
```

---

**2. Enums**
```java
// Before
class Status {
    public static final int PENDING = 0;
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 2;
}

// After - Type-safe enum
enum Status {
    PENDING(0, "Pending"),
    ACTIVE(1, "Active"),
    INACTIVE(2, "Inactive");
    
    private final int code;
    private final String label;
    
    Status(int code, String label) {
        this.code = code;
        this.label = label;
    }
    
    public int getCode() { return code; }
    public String getLabel() { return label; }
}

// Usage
Status status = Status.ACTIVE;
```

**Production Use:** Database enum mappings, state machines, configuration values.

---

**3. Enhanced For Loop**
```java
// Before
for (int i = 0; i < list.size(); i++) {
    String item = list.get(i);
}

// After
for (String item : list) {
    // cleaner, no index management
}

// Works with arrays, collections, iterables
for (String[] pair : arrays) { }
```

---

**4. Autoboxing/Unboxing**
```java
// Before
Integer i = new Integer(10);
int value = i.intValue();

// After (automatic conversion)
Integer i = 10;  // Autoboxing
int value = i;   // Unboxing

// In collections
List<Integer> numbers = new ArrayList<>();
numbers.add(10);  // Autoboxing
int first = numbers.get(0);  // Unboxing
```

**Production Issue:**
```java
Integer i = null;
int value = i;  // NullPointerException during unboxing!

// Solution: Check for null
Integer i = null;
int value = i != null ? i : 0;  // Use ternary or Optional
```

---

**5. Annotations**
```java
@Deprecated
public void oldMethod() { }

@Override
public String toString() { }

@SuppressWarnings("unchecked")
List list = new ArrayList();
```

---

## JAVA 6 (2006) - Minor Improvements

- JDBC 4.0 with DriverManager improvements
- Scripting language support (JavaScript engine)
- No major language features for modern developers

---

## JAVA 7 (2011) - Try-With-Resources & More

### Key Features:

**1. Try-With-Resources**
```java
// Before Java 7 (Resource leak possible)
BufferedReader br = new BufferedReader(new FileReader("file.txt"));
try {
    String line;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }
} finally {
    br.close();  // Might not execute if exception in try
}

// After Java 7 (Automatic resource management)
try (BufferedReader br = new BufferedReader(new FileReader("file.txt"))) {
    String line;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }
}  // br.close() called automatically

// Multiple resources
try (Connection conn = DriverManager.getConnection(url);
     Statement stmt = conn.createStatement()) {
    // use conn and stmt
}  // Both closed automatically
```

**Production Benefit:** Prevents resource leaks, cleaner code.

---

**2. Diamond Operator (<>)**
```java
// Before
List<String> list = new ArrayList<String>();
Map<String, Integer> map = new HashMap<String, Integer>();

// After (type inference)
List<String> list = new ArrayList<>();
Map<String, Integer> map = new HashMap<>();
```

---

**3. Multi-Catch Exception**
```java
// Before
try {
    // code
} catch (IOException e) {
    logger.error(e);
} catch (SQLException e) {
    logger.error(e);
}

// After
try {
    // code
} catch (IOException | SQLException e) {
    logger.error(e);
}
```

---

**4. Strings in Switch**
```java
String day = "Monday";
switch (day) {
    case "Monday":
        System.out.println("Start of week");
        break;
    case "Friday":
        System.out.println("Almost weekend");
        break;
    default:
        System.out.println("Other day");
}
```

---

## JAVA 8 (2014) - Lambda Expressions & Streams

### Key Features:

**1. Lambda Expressions**
```java
// Before
Comparator<Integer> comp = new Comparator<Integer>() {
    @Override
    public int compare(Integer a, Integer b) {
        return a - b;
    }
};

// After
Comparator<Integer> comp = (a, b) -> a - b;

// Method reference
Comparator<Integer> comp2 = Integer::compare;
```

**Production Use:** Everywhere - callbacks, streaming, functional programming.

---

**2. Streams API**
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// Functional style
List<Integer> evens = numbers.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * 2)
    .collect(Collectors.toList());

// Parallel processing
int sum = numbers.parallelStream()
    .mapToInt(Integer::intValue)
    .sum();
```

**Production Issue:**
```java
// Streams are lazy - nothing happens without terminal operation
list.stream()
    .filter(n -> n > 5)
    .map(n -> n * 2);  // Nothing executed!

// Correct
List<Integer> result = list.stream()
    .filter(n -> n > 5)
    .map(n -> n * 2)
    .collect(Collectors.toList());  // Terminal operation
```

---

**3. Optional**
```java
Optional<String> opt = Optional.of("value");
opt.ifPresent(System.out::println);
opt.orElse("default");
opt.orElseThrow(() -> new Exception("Empty"));

// Method chaining
Optional<String> result = opt
    .filter(s -> s.length() > 3)
    .map(String::toUpperCase);
```

---

**4. Method References**
```java
// Constructor reference
Function<String, Integer> constructor = Integer::new;

// Static method reference
Function<Integer, Integer> abs = Math::abs;

// Instance method reference
String str = "hello";
Function<String, Boolean> startsWith = str::startsWith;

// Arbitrary instance method reference
Function<String, Integer> length = String::length;
```

---

**5. Default Methods in Interfaces**
```java
interface Shape {
    void draw();
    
    // Default method
    default void printArea() {
        System.out.println("Area calculation");
    }
}

class Circle implements Shape {
    @Override
    public void draw() { }
    // Can override or use default implementation
}
```

---

**6. @FunctionalInterface**
```java
@FunctionalInterface
interface MyFunction {
    void execute();
    // Only one abstract method allowed
    
    default void helper() { }  // OK - default method
}
```

---

## JAVA 9 (2017) - Modules & Reactive Streams

### Key Features:

**1. Module System (Project Jigsaw)**
```java
// module-info.java
module com.example.api {
    requires java.base;
    requires java.sql;
    
    exports com.example.api.service;
    exports com.example.api.model;
    
    opens com.example.api.internal;  // For reflection
}
```

**Production Issue:** Module path vs classpath confusion, JAR conflicts.

---

**2. Private Methods in Interfaces**
```java
interface MyInterface {
    void publicMethod();
    
    default void helper() {
        privateHelper();
    }
    
    private void privateHelper() {
        // Implementation
    }
    
    private static void staticHelper() {
        // Implementation
    }
}
```

---

**3. Try-With-Resources Enhancement**
```java
// Java 7-8
try (BufferedReader br = new BufferedReader(new FileReader("file"))) {
    // use br
}

// Java 9 - Can use effectively final variable
BufferedReader br = new BufferedReader(new FileReader("file"));
try (br) {
    // use br
}
```

---

**4. Stream Enhancements**
```java
// takeWhile - take while condition is true
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);
nums.stream()
    .takeWhile(n -> n < 4)  // [1, 2, 3]
    .collect(Collectors.toList());

// dropWhile - skip while condition is true
nums.stream()
    .dropWhile(n -> n < 4)  // [4, 5]
    .collect(Collectors.toList());

// Stream.ofNullable
Stream<String> stream = Stream.ofNullable(value);  // Handles null
```

---

**5. Process API Enhancements**
```java
ProcessBuilder pb = new ProcessBuilder("ls", "-la");
Process process = pb.start();

// Get process info
ProcessHandle.Info info = process.info();
System.out.println(info.command());
System.out.println(info.startInstant());
System.out.println(info.user());
```

---

## JAVA 10 (2018) - Local Variable Type Inference

### Key Features:

**1. var Keyword**
```java
// Before
ArrayList<String> list = new ArrayList<String>();
HashMap<String, Integer> map = new HashMap<String, Integer>();

// After
var list = new ArrayList<String>();
var map = new HashMap<String, Integer>();

// With streams
var evens = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());
```

**Production Use:**
```java
// Good use of var - type is obvious
var person = new Person("John", 30);
var amount = calculateTotal(items);
var config = loadConfiguration();

// Bad use of var - type is unclear
var data = processData();  // What type is data?
var result = transform(list);  // Too vague
```

---

**2. Unmodifiable Collections**
```java
// Unmodifiable (not immutable - view is read-only)
List<String> list = Collections.unmodifiableList(items);

// Attempting modification throws UnsupportedOperationException
list.add("new");  // UnsupportedOperationException
```

---

## JAVA 11 (2018) - LTS (Long Term Support)

### Key Features:

**1. String Methods**
```java
String str = "Hello World";

// isBlank()
"   ".isBlank();  // true
"abc".isBlank();  // false

// strip() - removes whitespace
"  hello  ".strip();  // "hello"

// lines() - stream of lines
"line1\nline2\nline3"
    .lines()
    .forEach(System.out::println);

// repeat()
"abc".repeat(3);  // "abcabcabc"
```

---

**2. Local Variable Syntax for Lambda Parameters**
```java
// Java 11
Function<Integer, Integer> doubler = (var x) -> x * 2;

@FunctionalInterface
interface MyFunction {
    void process(var x, var y);
}
```

---

**3. HttpClient (Async)**
```java
HttpClient client = HttpClient.newHttpClient();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/data"))
    .GET()
    .build();

// Async
client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    .thenApply(HttpResponse::body)
    .thenAccept(System.out::println)
    .join();

// Sync
HttpResponse<String> response = client.send(request, 
    HttpResponse.BodyHandlers.ofString());
```

**Production Use:** Replacing HttpUrlConnection, RestTemplate alternatives.

---

**4. Files Methods**
```java
// Read all lines
List<String> lines = Files.readAllLines(Paths.get("file.txt"));

// Read as string
String content = Files.readString(Paths.get("file.txt"));

// Write string
Files.writeString(Paths.get("output.txt"), "content");
```

---

## JAVA 12-13 (2019) - Switch Expressions (Preview)

### Key Features:

**Switch Expression (Enhanced)**
```java
// Java 11 and before
int days;
switch (month) {
    case 1: case 3: case 5:
        days = 31;
        break;
    case 2:
        days = 28;
        break;
    default:
        days = 30;
}

// Java 12-13 (Preview), Java 14 (Final)
int days = switch (month) {
    case 1, 3, 5 -> 31;
    case 2 -> 28;
    default -> 30;
};

// With expression body
String result = switch (value) {
    case 1 -> "One";
    case 2 -> "Two";
    default -> "Other";
};
```

---

**Text Blocks (Preview in 12, Final in 13)**
```java
// Before
String json = "{\"name\": \"John\", \"age\": 30}";

// After - Text block
String json = """
    {
        "name": "John",
        "age": 30
    }
    """;

// HTML
String html = """
    <html>
        <body>
            <p>Hello, World!</p>
        </body>
    </html>
    """;

// SQL
String query = """
    SELECT * FROM users
    WHERE age > 18
    AND status = 'active'
    """;
```

---

## JAVA 14 (2020) - Records (Preview)

### Key Features:

**1. Records (Preview, Final in Java 16)**
```java
// Before - Boilerplate
class Person {
    private final String name;
    private final int age;
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String name() { return name; }
    public int age() { return age; }
    
    @Override
    public boolean equals(Object o) { ... }
    
    @Override
    public int hashCode() { ... }
    
    @Override
    public String toString() { ... }
}

// After - Concise
record Person(String name, int age) { }

// Auto-generated:
// - Constructor: Person(String name, int age)
// - Accessors: name(), age()
// - equals(), hashCode(), toString()

// Usage
Person p = new Person("John", 30);
System.out.println(p.name());  // "John"

// Can add custom methods
record Person(String name, int age) {
    public boolean isAdult() {
        return age >= 18;
    }
}
```

**Production Use:**
```java
// DTO (Data Transfer Object)
record UserDTO(Long id, String username, String email) { }

// API Response
record ApiResponse<T>(int code, String message, T data) { }

// Domain model
record Product(String sku, String name, BigDecimal price) { }

// Nested records
record Address(String street, String city, String zip) { }
record Customer(String name, Address address) { }
```

---

**2. NullPointerException Message Improvement**
```java
// Before
person.getAddress().getZip();  // NPE at which line?

// After - Helpful message
// Exception in thread "main" java.lang.NullPointerException: Cannot invoke method 
// "getZip()" because "person.getAddress()" is null
```

---

## JAVA 15-16 (2020-2021) - Sealed Classes, Records Final

### Key Features:

**1. Sealed Classes (Preview in 15, Final in 17)**
```java
// Restrict which classes can extend/implement
sealed class Shape permits Circle, Rectangle, Triangle {
    abstract double getArea();
}

final class Circle extends Shape {
    private final double radius;
    Circle(double radius) { this.radius = radius; }
    
    @Override
    double getArea() { return Math.PI * radius * radius; }
}

final class Rectangle extends Shape {
    private final double width, height;
    Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    double getArea() { return width * height; }
}

// non-sealed allows further subclassing
non-sealed class CustomShape extends Shape {
    @Override
    double getArea() { return 0; }
}
```

**Production Use:**
```java
// Domain objects with restricted hierarchy
sealed interface PaymentMethod permits CreditCard, DebitCard, PayPal { }

// Result objects
sealed interface Result<T> permits Success, Failure { }

final class Success<T> implements Result<T> {
    public final T value;
    Success(T value) { this.value = value; }
}

final class Failure<T> implements Result<T> {
    public final Exception error;
    Failure(Exception error) { this.error = error; }
}

// Pattern matching on sealed types
Result<String> result = ...;
if (result instanceof Success<String> s) {
    System.out.println(s.value);
} else if (result instanceof Failure<String> f) {
    System.out.println(f.error);
}
```

---

**2. Records are now Final (Java 16)**
```java
// Records are implicitly final
record Person(String name, int age) { }

// Cannot extend another record or class
// class EmployeePerson extends Person { }  // COMPILE ERROR
```

---

## JAVA 17 (2021) - LTS, Sealed Classes Final

### Key Features:

**1. Sealed Classes Now Standard**
```java
// Full support for sealed classes and interfaces
sealed interface Animal permits Dog, Cat {
    void sound();
}

final class Dog implements Animal {
    public void sound() { System.out.println("Woof"); }
}

final class Cat implements Animal {
    public void sound() { System.out.println("Meow"); }
}
```

---

**2. Pattern Matching (Preview)**
```java
// Type pattern matching
if (obj instanceof String s) {
    System.out.println(s.length());
}

// Guarded patterns
if (obj instanceof String s && s.length() > 5) {
    System.out.println("Long string: " + s);
}

// Logical patterns
if (obj instanceof (String s && s.length() > 5) || 
    (Integer i && i > 100)) {
    // Process
}
```

---

**3. Context-Specific Deserialization Filters**
```java
// Java 17 improves serialization safety
ObjectInputFilter filter = ObjectInputFilter.Config
    .createFilter("java.base/*;!com.example.*");

ObjectInputStream ois = new ObjectInputStream(is);
ois.setObjectInputFilter(filter);
```

---

## JAVA 18 (2022) - More Pattern Matching

### Key Features:

**1. Pattern Matching for Switch (Preview)**
```java
// Java 17
String result = switch (obj) {
    case Integer i -> "Integer: " + i;
    case String s -> "String: " + s;
    default -> "Unknown";
};

// Java 18 - Null handling
String result = switch (str) {
    case null -> "Null value";
    case "" -> "Empty string";
    default -> str;
};

// Guarded patterns
String result = switch (obj) {
    case Integer i when i > 0 -> "Positive";
    case Integer i when i < 0 -> "Negative";
    case Integer i -> "Zero";
    default -> "Not integer";
};
```

---

**2. Deprecate Finalization for Removal**
```java
// finalize() is deprecated - use try-with-resources or AutoCloseable
class Resource implements AutoCloseable {
    @Override
    public void close() throws Exception {
        // Cleanup
    }
}

try (Resource res = new Resource()) {
    // Use resource
}  // Automatically closed
```

---

## JAVA 19 (2022) - Virtual Threads (Preview)

### Key Features:

**1. Virtual Threads (Preview, Finalized in 21)**
```java
// Before - Platform threads (limited)
Thread thread = new Thread(() -> {
    System.out.println("Running");
});
thread.start();

// After - Virtual threads (lightweight, millions possible)
Thread thread = Thread.ofVirtual().start(() -> {
    System.out.println("Running on virtual thread");
});

// Or with executor
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> {
            // Heavy I/O operation
            blockingIOCall();
        });
    }
}

// Difference
// Platform threads: ~1000 max (OS threads)
// Virtual threads: Millions possible (user-mode threads, no OS overhead)
```

**Production Use:**
```java
// High-throughput server handling many concurrent requests
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// Before: Limited by OS threads, complex async code
// After: Can handle millions of concurrent tasks with simple blocking code

for (int i = 0; i < 1_000_000; i++) {
    executor.submit(() -> {
        Connection conn = getConnection();
        ResultSet rs = conn.executeQuery("SELECT ...");
        // Blocking code works efficiently with virtual threads
    });
}
```

---

**2. Record Patterns (Preview)**
```java
record Point(int x, int y) { }
record Circle(Point center, int radius) { }

Circle c = new Circle(new Point(10, 20), 5);

// Pattern matching on records
if (c instanceof Circle(Point(var x, var y), var r)) {
    System.out.println("Circle at " + x + ", " + y + " with radius " + r);
}
```

---

## JAVA 20 (2023) - Scoped Values (Preview)

### Key Features:

**1. Scoped Values (Preview)**
```java
// Thread-local alternative for virtual threads
static final ScopedValue<String> USER = ScopedValue.newInstance();

// Set value scoped to execution
ScopedValue.where(USER, "alice").run(() -> {
    System.out.println(USER.get());  // "alice"
});

// Structured concurrency - properly scoped values passed to virtual threads
try (var scope = new StructuredTaskScope<Integer>()) {
    var future1 = scope.fork(task1);
    var future2 = scope.fork(task2);
    
    scope.join();  // Wait for all
    
    // Values properly cleaned up
}
```

**Production Use:** Replacing ThreadLocal with virtual threads for cleaner context management.

---

## JAVA 21 (2023) - LTS, Virtual Threads Final, Pattern Matching Final

### Key Features:

**1. Virtual Threads Finalized**
```java
// Now production-ready
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// Can handle 10M+ concurrent tasks efficiently
for (int i = 0; i < 10_000_000; i++) {
    executor.submit(() -> {
        // Lightweight virtual thread
        blockingIO();
    });
}
```

---

**2. Pattern Matching Finalized (All Forms)**
```java
// Type pattern
if (obj instanceof String s) { }

// Record pattern
if (obj instanceof Person(String name, int age)) { }

// Array pattern
if (arr instanceof int[] { 10, 20, ..., n }) { }

// Guarded pattern
if (obj instanceof String s && s.length() > 5) { }

// Switch patterns
String result = switch (obj) {
    case null -> "Null";
    case String s when s.isEmpty() -> "Empty";
    case String s && s.length() > 5 -> "Long: " + s;
    default -> "Other";
};
```

---

**3. String Templates (Preview)**
```java
String name = "World";
String greeting = STR."Hello, \{name}!";

// Format expressions
double price = 19.99;
String msg = STR."Price: $\{String.format("%.2f", price)}";

// Multi-line
String sql = STR."""
    SELECT * FROM users
    WHERE id = \{userId}
    AND status = 'active'
    """;
```

---

**4. Record Patterns Finalized**
```java
record Address(String city, String zip) { }
record Person(String name, Address address) { }

Person p = new Person("Alice", new Address("NYC", "10001"));

// Nested record pattern matching
if (p instanceof Person(var name, Address(var city, var zip))) {
    System.out.println(name + " lives in " + city);
}

// In switch
String location = switch (p) {
    case Person(_, Address("NYC", _)) -> "New Yorker";
    case Person(_, Address("LA", _)) -> "Californian";
    default -> "Other";
};
```

---

# SUMMARY: Java Evolution for SMTS Level

## Must Know (Daily Use):

```
Java 5:  Generics, Enums, Enhanced For Loop, Autoboxing
Java 8:  Lambda, Streams, Optional, Method References
Java 10: var keyword
Java 14-16: Records, Sealed Classes
Java 17: Pattern Matching (sealed classes final)
Java 19+: Virtual Threads (game-changer for I/O-bound apps)
Java 21: Virtual Threads finalized, String Templates
```

## Should Know (Interviews):

```
Java 7: Try-with-resources, Diamond operator
Java 9: Modules, Private interface methods, Stream enhancements
Java 11: HttpClient, String methods
Java 12-13: Switch expressions, Text blocks
Java 15+: Record patterns, Array patterns, Guarded patterns
```

---

<a name="spring-boot-evolution"></a>
# SPRING BOOT EVOLUTION

## SPRING BOOT 1.0 (2014) - The Beginning

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

**Key Features:**
- Auto-configuration
- Embedded Tomcat/Jetty
- Starters for dependencies
- Application properties
- Spring Data JPA integration
- Security auto-configuration

---

## SPRING BOOT 1.3 (2015) - Config Server, Cloud Support

```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost/db
spring.jpa.hibernate.ddl-auto=update
spring.profiles.active=dev
```

**New:** Spring Cloud Config Client, @ConfigurationProperties

---

## SPRING BOOT 1.5 (2017) - WebFlux Introduced

```java
// WebFlux for reactive programming
@RestController
public class UserReactiveController {
    
    @GetMapping("/users/{id}")
    public Mono<User> getUser(@PathVariable Long id) {
        return userService.findByIdReactive(id);
    }
    
    @GetMapping("/users")
    public Flux<User> getAllUsers() {
        return userService.getAllUsersReactive();
    }
}
```

**New Features:**
- Spring WebFlux
- Reactive streams support
- Kotlin support
- @Conditional improvements

---

## SPRING BOOT 2.0 (2018) - Major Upgrade

```java
// Spring Boot 2.0 - Java 8+ minimum, Spring 5.0+

// New servlet container options
spring.main.web-application-type=reactive  // WebFlux
spring.main.web-application-type=servlet   // Spring MVC (default)
spring.main.web-application-type=none      // No web

// Micrometer metrics (replaces Dropwizard)
@Component
public class CustomMetrics {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    public void recordRequest(String endpoint, long duration) {
        Timer.builder("http.request")
            .tag("endpoint", endpoint)
            .register(meterRegistry)
            .record(Duration.ofMillis(duration));
    }
}

// Actuator endpoints
// GET /actuator/health
// GET /actuator/metrics
// GET /actuator/env
```

**Production Change:** Metrics collection, better monitoring.

---

## SPRING BOOT 2.1 (2018) - Performance Improvements

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost/db
    username: root
    password: password
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: update
```

**New Features:**
- Hikari CP default
- Better startup time
- Lazy initialization option

---

## SPRING BOOT 2.2 (2019) - Lazy Initialization, Java 13 Support

```java
@Configuration
public class LazyConfig {
    
    @Bean
    @Lazy  // Not created until first use
    public ExpensiveService expensiveService() {
        return new ExpensiveService();
    }
}

// Or globally
spring.main.lazy-initialization=true
```

**Production Benefit:** Faster startup for microservices, init on demand.

---

## SPRING BOOT 2.3 (2020) - Layers, Improved Docker Support

```dockerfile
# Optimal Docker support
FROM openjdk:11-jre-slim

COPY target/app.jar app.jar
RUN mkdir -p /workspace
WORKDIR /workspace

# Spring Boot 2.3 creates layered JAR
RUN java -Djarmode=layertools -jar /app.jar extract
FROM openjdk:11-jre-slim

# Copy layers separately - better caching
COPY --from=0 /workspace/spring-boot-loader/ ./
COPY --from=0 /workspace/dependencies/ ./
COPY --from=0 /workspace/snapshot-dependencies/ ./
COPY --from=0 /workspace/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
```

**Production Benefit:** Much faster Docker image rebuilds.

---

## SPRING BOOT 2.4 (2020) - Config Data Import

```yaml
# application.yml
spring:
  config:
    import:
      - classpath:application-docker.yml
      - file:/etc/config/application-prod.yml
      - configtree:/etc/config/secrets
  
  datasource:
    url: jdbc:mysql://${DB_HOST}/mydb
```

**New Features:**
- Flexible config import
- Profile groups
- Java 15+ support

---

## SPRING BOOT 2.5 (2021) - Metrics, Performance

```yaml
# Improved metrics and monitoring
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
      environment: ${app.environment}
```

**Production Change:** Better metrics for observability.

---

## SPRING BOOT 2.6 (2021) - Trailing Slash Match Removed

```java
// Breaking change: Trailing slash handling changed
// /api/users now != /api/users/

// Solution
@GetMapping("/users")
public List<User> getUsers() { }

// No longer matches /users/
```

**Migration Issue:** Existing APIs might break if they relied on trailing slash.

---

## SPRING BOOT 2.7 (2022) - Last of 2.x Series

```java
// Spring Boot 2.7 - Java 8-17 supported

// New features
@Configuration
@EnableConfigurationProperties({AppProperties.class})
public class AppConfig { }

// Property binding
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    
    @NotNull
    @Pattern(regexp = "^[a-z]+$")
    private String name;
    
    @Min(1)
    @Max(65535)
    private int port;
}
```

---

## SPRING BOOT 3.0 (2022) - Major Upgrade, Java 17+ Required

```java
// Spring Boot 3.0 - Spring 6.0, Java 17 minimum
// Major changes:

// 1. Java EE → Jakarta EE
import jakarta.persistence.*;  // NOT javax.persistence
import jakarta.servlet.*;      // NOT javax.servlet

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(nullable = false)
    private String name;
}

// 2. Virtual Threads Support
spring:
  threads:
    virtual:
      enabled: true  # Handle millions of requests

// 3. Observability (metrics, tracing, logging)
@Observed(name = "user.service")
public class UserService {
    public User findById(Long id) {
        // Automatically traced, metered, logged
    }
}

// 4. AOT (Ahead-of-Time) Compilation Support
// GraalVM native image compatible
```

**Production Impact:** Must migrate from javax to jakarta, better performance with native images.

---

## SPRING BOOT 3.1 (2023) - Virtual Threads Production Ready

```java
// Virtual thread support is now stable
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Virtual threads executor for async processing
        configurer.setTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}

// Or in properties
spring:
  threads:
    virtual:
      enabled: true

// Massive throughput improvement for I/O-bound operations
```

**Production Benefit:** Handle 100k+ concurrent requests with simple blocking code.

---

## SPRING BOOT 3.2 (2023) - Improved Native Support

```properties
# Spring Boot 3.2
spring.docker.compose.enabled=true  # Docker Compose support

# JVM Configuration
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
```

**New Features:**
- Docker Compose auto-startup
- HTTP/2 by default
- Better observability

---

## SPRING BOOT 3.3 (2024) - Latest LTS Candidate

```java
// Spring Boot 3.3
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // Improved error handling with Problem Details
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setTitle("User Not Found");
        detail.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
    }
}
```

---

# Spring Boot Evolution Summary

```
SB 1.0 (2014):  First release, auto-configuration
SB 1.5 (2017):  WebFlux introduced
SB 2.0 (2018):  Spring 5.0, Java 8+, major upgrade
SB 2.1 (2018):  HikariCP, performance
SB 2.2 (2019):  Lazy initialization
SB 2.3 (2020):  Layered JAR, Docker improvements
SB 2.4 (2020):  Config data import
SB 2.7 (2022):  Last 2.x, Java 17 support
SB 3.0 (2022):  Jakarta EE, Java 17 minimum, major breaking change
SB 3.1 (2023):  Virtual Threads production-ready
SB 3.3 (2024):  Latest, improved native support
```

---

<a name="java-cheat-sheet"></a>
# JAVA FEATURES CHEAT SHEET

## Java 8+ Feature Quick Reference

| Feature | Java Version | Use Case | Example |
|---------|------|----------|---------|
| Generics | 5 | Type-safe collections | `List<String>` |
| Enums | 5 | Fixed set of constants | `enum Status { ACTIVE, INACTIVE }` |
| Enhanced For | 5 | Clean iteration | `for (String s : list) {}` |
| Autoboxing | 5 | Auto conversion | `Integer i = 5;` |
| Try-with-resources | 7 | Resource cleanup | `try (Connection c = ...) {}` |
| Diamond Operator | 7 | Type inference | `new ArrayList<>()` |
| Lambda | 8 | Functional programming | `(x, y) -> x + y` |
| Streams | 8 | Functional collections | `list.stream().filter(...).map(...)` |
| Optional | 8 | Null handling | `Optional.of(value).orElse(null)` |
| Method References | 8 | Code clarity | `String::valueOf` |
| Default Methods | 8 | Interface implementation | `default void method() {}` |
| var Keyword | 10 | Type inference | `var list = new ArrayList<>();` |
| Text Blocks | 13 | Multi-line strings | `""" ... """` |
| Records | 14 | Data classes | `record User(String name, int age) {}` |
| Sealed Classes | 17 | Restrict inheritance | `sealed class Shape permits Circle {}` |
| Pattern Matching | 17 | Enhanced type checking | `if (obj instanceof String s)` |
| Virtual Threads | 19 | Lightweight concurrency | `Thread.ofVirtual().start(...)` |
| String Templates | 21 | String interpolation | `STR."Hello \{name}"` |

---

<a name="spring-boot-cheat-sheet"></a>
# SPRING BOOT FEATURES CHEAT SHEET

## Essential Spring Boot Features

| Feature | Version | Use Case | Example |
|---------|---------|----------|---------|
| Auto-Configuration | 1.0 | Automatic bean creation | `@SpringBootApplication` |
| Starters | 1.0 | Dependency bundles | `spring-boot-starter-web` |
| Embedded Server | 1.0 | No server install | JAR executable |
| Application Properties | 1.0 | External configuration | `application.properties` |
| Actuator | 1.0 | Monitoring endpoints | `/actuator/health` |
| WebFlux | 1.5 | Reactive programming | `@RestController` with `Flux` |
| ConfigurationProperties | 1.0 | Type-safe config | `@ConfigurationProperties` |
| Profiles | 1.0 | Environment-specific config | `application-prod.yml` |
| CloudConfigClient | 1.3 | Centralized config | Config server integration |
| Micrometer | 2.0 | Metrics collection | Prometheus, Grafana integration |
| HttpClient | 2.1 | HTTP requests | `WebClient.Builder` |
| Lazy Initialization | 2.2 | Performance | `@Lazy` or property |
| Layered JAR | 2.3 | Docker optimization | Multi-stage JAR |
| ConfigData Import | 2.4 | Flexible config | `spring.config.import` |
| Virtual Threads | 3.1 | High throughput | `newVirtualThreadPerTaskExecutor()` |
| Jakarta EE | 3.0 | New namespace | `jakarta.persistence.*` |
| AOT Compilation | 3.0 | GraalVM support | Native image creation |
| Problem Details | 3.3 | Standard error response | `ProblemDetail` |

---

<a name="annotations-reference"></a>
# COMPLETE ANNOTATIONS REFERENCE

## Spring Core Annotations

### @Component & Stereotypes

```java
// @Component - Generic component
@Component
public class MyComponent {
    public void doSomething() { }
}

// @Service - Business logic layer
@Service
public class UserService {
    public User findUser(Long id) { }
}

// @Repository - Data access layer
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

// @Controller - Web controller
@Controller
public class PageController {
    @GetMapping("/")
    public String home() { return "index"; }
}

// @RestController - REST API (equivalent to @Controller + @ResponseBody)
@RestController
@RequestMapping("/api/users")
public class UserRestController {
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { }
}

// Usage: Create a single instance (singleton) in ApplicationContext
// Spring scans packages for these and auto-registers as beans
```

---

### @Autowired & Dependency Injection

```java
// Field injection (not recommended)
@Component
public class MyService {
    @Autowired
    private UserRepository repository;
}

// Constructor injection (recommended)
@Component
public class MyService {
    private final UserRepository repository;
    
    public MyService(UserRepository repository) {
        this.repository = repository;
    }
}

// Setter injection
@Component
public class MyService {
    private UserRepository repository;
    
    @Autowired
    public void setRepository(UserRepository repository) {
        this.repository = repository;
    }
}

// Optional dependency
@Component
public class MyService {
    @Autowired(required = false)
    private OptionalService optionalService;
}

// Multiple implementations with @Qualifier
@Service
public class MyService {
    @Autowired
    @Qualifier("emailNotificationService")
    private NotificationService notificationService;
}

// Production Issue: Circular dependency
@Component
public class ServiceA {
    @Autowired
    private ServiceB serviceB;
}

@Component
public class ServiceB {
    @Autowired
    private ServiceA serviceA;  // Circular!
}

// Solution: Use ObjectProvider
@Component
public class ServiceB {
    private final ObjectProvider<ServiceA> serviceAProvider;
    
    public ServiceB(ObjectProvider<ServiceA> serviceAProvider) {
        this.serviceAProvider = serviceAProvider;
    }
    
    public void useServiceA() {
        serviceAProvider.ifAvailable(serviceA -> {...});
    }
}
```

---

### @Configuration & @Bean

```java
// Configuration class
@Configuration
public class AppConfig {
    
    // Create bean using method
    @Bean
    public UserService userService() {
        return new UserService();
    }
    
    // Bean with constructor injection
    @Bean
    public UserController userController(UserService service) {
        return new UserController(service);
    }
    
    // Named bean
    @Bean(name = "customUserService")
    public UserService anotherUserService() {
        return new UserService();
    }
    
    // Singleton scope (default)
    @Bean
    @Scope("singleton")
    public UserService singletonService() {
        return new UserService();
    }
    
    // Prototype scope - new instance each time
    @Bean
    @Scope("prototype")
    public RequestContext requestContext() {
        return new RequestContext();
    }
    
    // Lazy initialization
    @Bean
    @Lazy
    public ExpensiveService expensiveService() {
        return new ExpensiveService();
    }
    
    // Conditional bean creation
    @Bean
    @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
    public FeatureService featureService() {
        return new FeatureService();
    }
}

// Usage
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
UserService service = context.getBean(UserService.class);
UserController controller = context.getBean(UserController.class);
```

---

### @Scope Annotations

```java
// Singleton (default) - one instance per ApplicationContext
@Component
@Scope("singleton")
public class SingletonService {
    // One instance shared across application
}

// Prototype - new instance each time
@Component
@Scope("prototype")
public class PrototypeService {
    // New instance on each injection
}

// Request scope - one instance per HTTP request
@Component
@Scope("request")
public class RequestService {
    // New instance per HTTP request
}

// Session scope - one instance per user session
@Component
@Scope("session")
public class SessionService {
    // New instance per user session
}

// Application scope - one instance per ServletContext
@Component
@Scope("application")
public class ApplicationService {
    // Shared across entire web application
}

// WebSocket scope - one instance per WebSocket session
@Component
@Scope("websocket")
public class WebSocketService {
    // One instance per WebSocket connection
}

// Production use:
@Component
@Scope("request")
public class UserContext {
    private String userId;
    private String username;
    
    // Gets new instance per request
    // But same instance within single request
}
```

---

## Web Annotations

### @RequestMapping & Variants

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // GET /api/users
    @GetMapping
    public List<User> getAllUsers() { }
    
    // GET /api/users/{id}
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { }
    
    // POST /api/users
    @PostMapping
    public User createUser(@RequestBody User user) { }
    
    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) { }
    
    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) { }
    
    // PATCH /api/users/{id}
    @PatchMapping("/{id}")
    public User partialUpdate(@PathVariable Long id, @RequestBody User user) { }
}
```

---

### @RequestParam, @PathVariable, @RequestBody

```java
@RestController
public class MyController {
    
    // Query parameters
    @GetMapping("/search")
    public List<User> search(@RequestParam String name,
                             @RequestParam(required = false) Integer age) {
        // GET /search?name=John&age=30
    }
    
    // Path variable
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        // GET /users/123
    }
    
    // Request body
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        // POST /users with JSON body
    }
    
    // Request header
    @GetMapping("/data")
    public String getData(@RequestHeader("Authorization") String token) {
        // Header: Authorization: Bearer token
    }
    
    // Cookie
    @GetMapping("/profile")
    public User getProfile(@CookieValue("SESSION_ID") String sessionId) {
        // Cookie: SESSION_ID=abc123
    }
    
    // Combine multiple
    @PostMapping("/transfer/{fromId}/to/{toId}")
    public TransactionResult transfer(
            @PathVariable Long fromId,
            @PathVariable Long toId,
            @RequestBody TransferRequest request,
            @RequestHeader("X-Request-ID") String requestId) {
        // POST /transfer/1/to/2 with request body and header
    }
}
```

---

### @ExceptionHandler & Error Handling

```java
@RestController
public class UserController {
    
    // Handle specific exception
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "User not found",
            ex.getMessage()
        );
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }
    
    // Handle multiple exceptions
    @ExceptionHandler({ValidException.class, InvalidException.class})
    public ResponseEntity<ErrorResponse> handleValidationErrors(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            ex.getMessage()
        );
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }
}

// Global exception handler
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal server error",
            ex.getMessage()
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
    
    // Problem Details (Spring Boot 3.3+)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(UserNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setTitle("User Not Found");
        detail.setDetail(ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(detail);
    }
}
```

---

## Data Access Annotations

### @Transactional

```java
@Service
public class UserService {
    
    // Default: Read-write, required transaction
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
        // Committed if no exception
        // Rolled back if exception thrown
    }
    
    // Read-only optimization
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
        // Database knows it's read-only, optimizes
    }
    
    // Custom isolation level
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateUser(User user) {
        userRepository.save(user);
    }
    
    // Propagation behavior
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTransaction() {
        // Always creates new transaction, even if one exists
    }
    
    // Timeout
    @Transactional(timeout = 10)  // 10 seconds
    public void longRunningOperation() {
        // Rolled back if takes > 10 seconds
    }
    
    // No rollback for specific exceptions
    @Transactional(noRollbackFor = {ValidationException.class})
    public void processData() {
        // If ValidationException thrown, transaction still commits
    }
    
    // Rollback for specific exceptions
    @Transactional(rollbackFor = {CustomException.class})
    public void criticalOperation() {
        // If CustomException thrown, transaction rolled back
    }
}

// Production Issue: N+1 query problem
@Service
public class UserService {
    
    @Transactional(readOnly = true)
    public List<User> getUsersWithOrders() {
        List<User> users = userRepository.findAll();  // 1 query
        
        for (User user : users) {
            user.getOrders();  // N queries - one per user!
        }
        
        return users;
    }
    
    // Solution: Use JOIN FETCH or @EntityGraph
    @Transactional(readOnly = true)
    public List<User> getUsersWithOrdersFixed() {
        return userRepository.findAllWithOrders();  // 1 query with JOIN
    }
}
```

---

## Configuration Annotations

### @ConfigurationProperties

```java
// Type-safe configuration
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private String name;
    private String version;
    private int port;
    private List<String> allowedHosts;
    private Map<String, String> settings;
    
    // Getters and setters required
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    // Usage in properties
    // app.name=MyApp
    // app.version=1.0.0
    // app.port=8080
    // app.allowed-hosts[0]=localhost
    // app.settings.timeout=30
}

// Validation
@Component
@ConfigurationProperties(prefix = "database")
@Validated
public class DatabaseProperties {
    
    @NotNull
    private String url;
    
    @NotNull
    @Email
    private String adminEmail;
    
    @Min(1)
    @Max(100)
    private int poolSize;
    
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    private String username;
}

// Nested properties
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private Database database = new Database();
    private Cache cache = new Cache();
    
    public static class Database {
        private String host;
        private int port;
        private String name;
        // getters/setters
    }
    
    public static class Cache {
        private String type;
        private int ttl;
        // getters/setters
    }
    
    // YAML usage:
    // app:
    //   database:
    //     host: localhost
    //     port: 5432
    //     name: mydb
    //   cache:
    //     type: redis
    //     ttl: 3600
}

// Production use
@Configuration
public class MyConfig {
    
    @Autowired
    private AppProperties appProperties;
    
    @Bean
    public DataSource dataSource() {
        return new DataSource(
            appProperties.getDatabase().getUrl(),
            appProperties.getDatabase().getPort()
        );
    }
}
```

---

### @Conditional Annotations

```java
// Condition on missing bean
@Configuration
public class MyConfig {
    
    @Bean
    @ConditionalOnMissingBean(UserService.class)
    public UserService defaultUserService() {
        return new DefaultUserService();
    }
}

// Condition on property
@Bean
@ConditionalOnProperty(
    name = "feature.advanced.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public AdvancedService advancedService() {
    return new AdvancedService();
}

// Condition on class presence
@Configuration
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
public class KafkaConfig {
    // Only loaded if Kafka is on classpath
}

// Condition on missing class
@Bean
@ConditionalOnMissingClass("com.example.legacy.OldService")
public NewService newService() {
    return new NewService();
}

// Custom condition
@Component
@Conditional(CustomCondition.class)
public class ConditionalService {
}

public class CustomCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String env = context.getEnvironment().getProperty("app.environment");
        return "production".equals(env);
    }
}
```

---

## AOP & Aspect Annotations

### @Aspect & @Pointcut

```java
@Aspect
@Component
public class LoggingAspect {
    
    // Define pointcut
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethods() {
    }
    
    // Before advice
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Calling: " + joinPoint.getSignature());
    }
    
    // After returning advice
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturning(Object result) {
        System.out.println("Result: " + result);
    }
    
    // After throwing advice
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logAfterThrowing(Exception ex) {
        System.out.println("Exception: " + ex.getMessage());
    }
    
    // Around advice
    @Around("serviceMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Before: " + joinPoint.getSignature());
        Object result = joinPoint.proceed();
        System.out.println("After: " + result);
        return result;
    }
}

// Production use - Performance monitoring
@Aspect
@Component
public class PerformanceAspect {
    
    @Around("execution(* com.example.service.*.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 1000) {  // Slow method
                System.out.println("SLOW: " + pjp.getSignature() + " took " + duration + "ms");
            }
        }
    }
}
```

---

<a name="production-issues"></a>
# PRODUCTION ISSUES & SOLUTIONS

## Issue 1: Memory Leaks from Resources

**Problem:**
```java
@Service
public class DataService {
    
    public void processData() throws IOException {
        FileInputStream fis = new FileInputStream("data.txt");
        // If exception here, stream never closed
        byte[] data = fis.readAllBytes();
        fis.close();
    }
}
```

**Solution:**
```java
// Use try-with-resources
@Service
public class DataService {
    
    public void processData() throws IOException {
        try (FileInputStream fis = new FileInputStream("data.txt")) {
            byte[] data = fis.readAllBytes();
            // fis automatically closed
        }
    }
    
    // Or use Spring's RestTemplate/WebClient which handle cleanup
    @Autowired
    private RestTemplate restTemplate;
    
    public ResponseEntity<String> fetchData() {
        return restTemplate.getForEntity("https://api.example.com/data", String.class);
        // Connection automatically closed
    }
}
```

---

## Issue 2: Unboxing NullPointerException

**Problem:**
```java
public class OrderService {
    
    @Transactional(readOnly = true)
    public int getTotalOrders(Long userId) {
        Integer count = orderRepository.countByUserId(userId);
        return count;  // NPE if count is null!
    }
}
```

**Solution:**
```java
public class OrderService {
    
    @Transactional(readOnly = true)
    public int getTotalOrders(Long userId) {
        Integer count = orderRepository.countByUserId(userId);
        return count != null ? count : 0;  // Safe unboxing
    }
    
    // Or better - use Optional
    @Transactional(readOnly = true)
    public int getTotalOrders(Long userId) {
        return orderRepository.countByUserId(userId)
            .orElse(0);
    }
}
```

---

## Issue 3: Circular Bean Dependency

**Problem:**
```java
@Service
public class UserService {
    @Autowired
    private OrderService orderService;
}

@Service
public class OrderService {
    @Autowired
    private UserService userService;  // Circular!
}
```

**Solution:**
```java
// Use ObjectProvider for lazy resolution
@Service
public class UserService {
    private final ObjectProvider<OrderService> orderServiceProvider;
    
    public UserService(ObjectProvider<OrderService> orderServiceProvider) {
        this.orderServiceProvider = orderServiceProvider;
    }
    
    public void process() {
        orderServiceProvider.ifAvailable(orderService -> {
            // Use orderService
        });
    }
}

// Or use setter injection instead of constructor
@Service
public class UserService {
    private OrderService orderService;
    
    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
```

---

## Issue 4: N+1 Query Problem

**Problem:**
```java
@Service
public class UserService {
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersWithOrders() {
        List<User> users = userRepository.findAll();  // 1 query
        
        return users.stream()
            .map(user -> new UserDTO(
                user.getId(),
                user.getName(),
                user.getOrders().size()  // N queries!
            ))
            .collect(Collectors.toList());
    }
}
```

**Solution:**
```java
// Option 1: JOIN FETCH
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
    List<User> findAllWithOrders();
}

// Option 2: @EntityGraph
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @EntityGraph(attributePaths = {"orders"})
    List<User> findAll();
}

// Option 3: Batch fetching
@Entity
public class User {
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 20)  // Load 20 at a time
    private List<Order> orders;
}

// Usage
@Service
public class UserService {
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersWithOrders() {
        List<User> users = userRepository.findAllWithOrders();  // 1 query now
        
        return users.stream()
            .map(user -> new UserDTO(...))
            .collect(Collectors.toList());
    }
}
```

---

## Issue 5: LazyInitializationException

**Problem:**
```java
@Service
@Transactional
public class UserService {
    
    @Transactional(readOnly = true)
    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getOrders()  // LazyInitializationException! Session closed
        );
    }
}
```

**Solution:**
```java
// Option 1: Eager load in query
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findByIdWithOrders(Long id);
}

// Option 2: Keep session open
@Service
public class UserService {
    
    @Transactional(readOnly = true)  // Keeps session open
    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getOrders()  // Now works - session still open
        );
    }
}

// Option 3: Initialize before returning
@Service
public class UserService {
    
    @Transactional(readOnly = true)
    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        
        Hibernate.initialize(user.getOrders());  // Force load
        
        return new UserDTO(...);
    }
}
```

---

## Issue 6: Database Connection Pool Exhaustion

**Problem:**
```java
@Service
public class DataService {
    
    public void processData() {
        for (int i = 0; i < 10000; i++) {
            dataRepository.findById(i);  // Each gets connection
        }
        // Connections accumulate, pool exhausted!
    }
}
```

**Solution:**
```java
// Batch processing
@Service
public class DataService {
    
    @Transactional
    public void processData() {
        for (int i = 0; i < 10000; i += 100) {
            List<Data> items = dataRepository.findByIdBetween(i, i + 100);
            // Process batch
            
            if (i % 1000 == 0) {
                // Flush and clear session periodically
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}

// Or configure pool correctly
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Max connections
      minimum-idle: 5        # Min idle connections
      idle-timeout: 600000   # 10 minutes
      max-lifetime: 1800000  # 30 minutes
      connection-timeout: 30000  # 30 seconds
```

---

## Issue 7: Thread Safety with ThreadLocal

**Problem:**
```java
@Component
public class UserContext {
    private static final ThreadLocal<String> user = new ThreadLocal<>();
    
    public void setUser(String userId) {
        user.set(userId);
    }
    
    public String getUser() {
        return user.get();  // Leaks if not cleaned up!
    }
}
```

**Solution:**
```java
// Use try-finally to ensure cleanup
@Component
public class UserContext {
    private static final ThreadLocal<String> user = new ThreadLocal<>();
    
    public void setUser(String userId) {
        user.set(userId);
    }
    
    public String getUser() {
        return user.get();
    }
    
    public void clear() {
        user.remove();  // IMPORTANT
    }
}

// Or use Filter to auto-clean
@Component
public class UserContextFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserContext userContext;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response,
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        try {
            String userId = extractUserIdFromRequest(request);
            userContext.setUser(userId);
            filterChain.doFilter(request, response);
        } finally {
            userContext.clear();  // Always cleaned up
        }
    }
}

// Or use Spring's ScopedValue (Java 19+)
static final ScopedValue<String> USER = ScopedValue.newInstance();

ScopedValue.where(USER, "userId").run(() -> {
    // USER.get() returns "userId"
});
// Automatically cleaned up after run()
```

---

## Issue 8: Transactional Method Pitfalls

**Problem:**
```java
@Service
public class OrderService {
    
    @Transactional
    public void createOrder(Order order) {
        orderRepository.save(order);
        
        // This bypasses @Transactional (calls same object, not proxy)
        this.sendNotification(order);  // May fail, but transaction commits
    }
    
    @Transactional
    public void sendNotification(Order order) {
        // This transaction is ignored
        emailService.send(...);
    }
}
```

**Solution:**
```java
// Inject self and call through it
@Service
public class OrderService {
    
    @Autowired
    private OrderService self;
    
    @Transactional
    public void createOrder(Order order) {
        orderRepository.save(order);
        self.sendNotification(order);  // Goes through proxy, @Transactional works
    }
    
    @Transactional
    public void sendNotification(Order order) {
        emailService.send(...);
    }
}

// Or better - separate services
@Service
public class OrderService {
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public void createOrder(Order order) {
        orderRepository.save(order);
        notificationService.sendNotification(order);  // Different bean
    }
}

@Service
public class NotificationService {
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(Order order) {
        emailService.send(...);  // New transaction
    }
}
```

---

## Issue 9: Java Stream Memory Issues

**Problem:**
```java
@Service
public class ReportService {
    
    public List<ReportLine> generateLargeReport() {
        // Loads ALL data into memory
        return dataRepository.findAll().stream()
            .filter(...)
            .map(...)
            .collect(Collectors.toList());  // ALL results in memory!
    }
}
```

**Solution:**
```java
// Use pagination/batching
@Service
public class ReportService {
    
    public void generateLargeReport(Consumer<ReportLine> processor) {
        int pageSize = 1000;
        int pageNo = 0;
        
        while (true) {
            Page<Data> page = dataRepository.findAll(PageRequest.of(pageNo, pageSize));
            
            if (page.isEmpty()) break;
            
            page.getContent().stream()
                .filter(...)
                .map(...)
                .forEach(processor);  // Process and discard
            
            pageNo++;
        }
    }
}

// Or use parallel streams carefully
@Service
public class ReportService {
    
    public List<ReportLine> generateReport() {
        return dataRepository.findAll().parallelStream()  // Use all cores
            .filter(...)
            .map(...)
            .collect(Collectors.toList());
        
        // Monitor: parallelStream overhead > benefit for small datasets
    }
}
```

---

## Summary of Critical Production Issues

```
1. Resource Leaks → Use try-with-resources
2. Null Pointer → Use Optional or null checks
3. Circular Dependencies → Use ObjectProvider, setter injection
4. N+1 Queries → Use JOIN FETCH, @EntityGraph, @BatchSize
5. LazyInitializationException → Eager load or keep session open
6. Pool Exhaustion → Batch processing, proper configuration
7. ThreadLocal Leaks → Always remove() in finally or use Filter
8. @Transactional Bypass → Use proxy (inject self or separate bean)
9. Memory Issues → Use pagination, not loading everything at once
```

---

# END OF COMPLETE JAVA & SPRING BOOT GUIDE

**This is your complete reference document. It covers:**
- ✅ Every important Java feature (5-21)
- ✅ Every important Spring Boot feature
- ✅ Complete annotation reference
- ✅ Real production issues and solutions
- ✅ Code examples for everything

**Use this document to:**
1. Understand evolution and history
2. Know what to use and when
3. Avoid common production pitfalls
4. Answer "What's new in Java/Spring" questions
5. Demonstrate deep knowledge in interviews

This is your go-to reference for any Java or Spring Boot interview question.
