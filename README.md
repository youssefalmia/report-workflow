# Report Workflow Management System


## **Introduction**

This project automates report management with a **structured workflow**, ensuring:

- **Owners** create reports.
- **Reviewers** review them.
- **Validators** approve or refuse them.

Security is enforced with **RBAC and JWT authentication**, while an **event-driven state update mechanism** ensures consistency. The system is **flexible**, using a **strategy pattern** to support multiple workflow management approaches, including **Camunda BPMN**. 🚀

---

## 📂 Project Structure

```
src/
 ├── main/
 │   ├── java/com/youssef/reportworkflow/
 │   │   ├── config/                    # Security & application configuration
 │   │   ├── controllers/               # REST API controllers
 │   │   ├── domain/        
 │   │   │   ├── enums/                 # Enum definitions (Roles, States, etc.)
 │   │   │   ├── entities/              # JPA entities (Report, User, TransitionLog)
 │   │   │   ├── repository/            # Spring Data JPA repositories
 │   │   ├── dto/                       # Data transfer objects (DTOs)
 │   │   ├── exception/                 # Custom error handling
 │   │   ├── handlers/                  # Global exception handlers
 │   │   ├── mapper/                    # Data mappers (Entity <-> DTO)
 │   │   ├── service/                   # Business logic services
 │   │   │   ├──camunda/                # Camunda workflow implementation
 │   │   ├── utils/                     # Utilities files
 │   ├── resources/
 │   │   ├── bpmn/                      # Camunda workflow definitions
 │   │   ├── application.properties     # Application configuration
```

# Report Workflow

## Authentication

A user starts by creating an account via `/register` or logging in via `/login`. These endpoints return a JWT containing the user roles. Once authenticated, the user includes the token in every request and can now interact with the system.

## Workflow Steps

### 1. Start Report (`/start` - Owners Only)
To start, the owner must hit the `/start` endpoint. The controller will intercept the request, create a new report, start a workflow process (e.g., Camunda BPMN), and return an `ApiResponse` containing `ReportDTO`.

**Under the hood:**
- Calls the service layer to handle report creation.
- Uses `workflowService` to start the workflow process with `reportId` and `ownerId`.
- `workflowService` follows a **strategy pattern + factory** to allow easy switching of workflow engines or simply any workflow and state management strategy. (See [Workflow Strategy & Factory Pattern Implementation](#1-workflow-strategy--factory-pattern-implementation))
- The system now waits for the owner to confirm the report creation.

### 2. Confirm Report (`/{reportId}/confirm` - Owners Only)
This endpoint confirms the creation of the report by calling the report service, which then calls `workflowService` to complete the **user task "createTask"** from the process identified by `reportId`.

**Under the hood:**
- A BPMN user task is pre-configured with a **task listener**. (See [Camunda BPMN Workflow Overview](#2-camunda-bpmn-workflow-overview))
- Completing the task triggers `ReportStateListener`, which: 
    - Publishes `ReportStateChangedEvent` (reportId, userId, new state).
    - `ReportStateEventListener` intercepts the event, logs, and persists the changes in the database.
  
    **(See [Event-Driven State Updates](#3-event-driven-state-updates))**
- The system now waits for a reviewer to review the report.

### 3. Review Report (`/{reportId}/review` - Reviewers Only)
This endpoint allows reviewers to review the report. It takes `reportId` and returns an `ApiResponse` of `ReportDTO`.

**Under the hood:**
- Calls report service to validate the report state (ensuring workflow engine is the single source of truth).
- Fetches the user and rechecks if they have the **REVIEWER** role for additional security. **(Note: This step is technically redundant since user roles are already enforced via `securityFilterChain` and `@PreAuthorize`. However, the additional check ensures an extra layer of security.)**
- Calls `workflowService` to complete a BPMN user task.
- `ReportStateListener` logs and persists the state changes. **(See [Event-Driven State Updates](#3-event-driven-state-updates))**
- The system now waits for report validation.

### 4. Validate Report (`/{reportId}/validate` - Validators Only)
This endpoint validates or refuses the report based on a boolean flag. It returns an `ApiResponse` of `ReportDTO`.

**Under the hood:**
- Calls report service to:
    - Validate the report state.
    - Fetch the user and check their **VALIDATOR** role.
- Calls `workflowService` to complete a BPMN user task.
- The process moves through an **exclusive gateway** that checks the `isApproved` variable to decide if the report is **approved or refused**.
- **End events** in BPMN (with execution listeners) notify the system, log, and persist data. **(See [Event-Driven State Updates](#3-event-driven-state-updates))**

# A more in depth look at component stated in previous section

## 1. Workflow Strategy & Factory Pattern Implementation

To keep our workflow system **flexible and scalable**, we implemented a **Strategy Pattern combined with a Factory Pattern**. This approach allows us to easily switch between different workflow engines (e.g., Camunda, or any custom solution) without modifying the core logic of report processing.

---

### Why I Used This Pattern?
1. **Decoupling Logic** – Instead of hardcoding a specific workflow engine, we abstracted the logic using `IReportWorkflowStrategy`. This ensures that our core services don’t depend on a single BPM engine.
2. **Extensibility** – If we ever need to add a new workflow engine, we just need to implement `IReportWorkflowStrategy` and register it in the factory. No need to touch existing code.
3. **Single Responsibility** – Each workflow engine has its own strategy class, making the system easier to maintain.

---

### How It Works?
1. **Interface (`IReportWorkflowStrategy`)**
  - Defines all the operations needed for handling reports, such as `startWorkflow()`, `createReport()`, `reviewReport()`, and `processValidationDecision()`.
  - Any workflow engine must implement this interface.

   ```java
   public interface IReportWorkflowStrategy {
       String startWorkflow(Long reportId, Long ownerId);
       void createReport(Long reportId, Long ownerId);
       void reviewReport(Long reportId, Long reviewerId);
       void processValidationDecision(Long reportId, Long validatorId, boolean isApproved);
       ReportState getReportState(Long reportId);
   }
   ```

2. **Factory (`ReportWorkflowFactory`)**
  - Maintains a map of available workflow strategies.
  - Automatically registers new implementations (`CamundaReportWorkflow`, future engines).
  - Allows selecting a workflow engine dynamically based on configuration.

   ```java
   @Component
   public class ReportWorkflowFactory {
       private final Map<String, IReportWorkflowStrategy> strategies;

       @Autowired
       public ReportWorkflowFactory(List<IReportWorkflowStrategy> strategyList) {
           this.strategies = new HashMap<>();
           for (IReportWorkflowStrategy strategy : strategyList) {
               if (strategy instanceof CamundaReportWorkflow) {
                   strategies.put("camunda", strategy);
               }
               // Add other workflow engines later
           }
       }

       public IReportWorkflowStrategy getWorkflowStrategy(String engineType) {
           return strategies.get(engineType);
       }
   }
   ```

3. **Service (`ReportWorkflowService`)**
  - At startup, retrieves the appropriate strategy from `ReportWorkflowFactory` based on `workflow.engine` config.
  - Calls workflow operations without worrying about which engine is in use.

   ```java
   @Service
   public class ReportWorkflowService {
       private final IReportWorkflowStrategy workflowStrategy;

       @Autowired
       public ReportWorkflowService(ReportWorkflowFactory workflowFactory,
                                    @Value("${workflow.engine}") String engineType) {
           this.workflowStrategy = workflowFactory.getWorkflowStrategy(engineType);
       }

       public String startWorkflow(Long reportId, Long ownerId) {
           return workflowStrategy.startWorkflow(reportId, ownerId);
       }

       public void createReport(Long reportId, Long ownerId) {
           workflowStrategy.createReport(reportId, ownerId);
       }
   }
   ```

---

### Added Value
- **Switching Engines is Effortless** – If we decide to move away from Camunda or support multiple engines, we don’t need to rewrite core logic.
- **Better Maintainability** – Each workflow engine's logic is isolated in its own class.
- **Scalability** – New workflow implementations can be added **without modifying existing code**, following Open-Closed Principle.

This ensures our workflow management remains **robust, efficient, and adaptable** as requirements evolve.

## 2. Camunda BPMN Workflow Overview

For this project, **Camunda** was chosen as the BPMN engine due to its **scalability, flexibility, and built-in tools**. BPMN (Business Process Model and Notation) provides a **visual representation of workflows**, making it easier to manage complex processes while keeping them structured and maintainable.

---

### Why Camunda?
1. **Separation of Concerns** – The business process is **handled externally**, allowing us to modify workflows without changing the core logic.
2. **Event-Driven Architecture** – Task listeners and execution listeners help **trigger events and notify the system** when tasks are completed.
3. **Built-in UI & Monitoring** – Camunda provides **Cockpit, Tasklist, and Admin Panel** to visualize and manage processes.
4. **Scalability** – Camunda is designed for handling **large-scale workflows**, making it future-proof for more complex scenarios.

---

### Understanding the BPMN Diagram

![BPMN](https://github.com/youssefalmia/report-workflow/blob/main/resources/bpm/camunda/bpmn-camunda.png)

The workflow consists of **three user tasks**:
- **Create Report** (Owners) – Any user with the **Owner** role can initiate this.
- **Review Report** (Reviewer) – A **Reviewer** reviews the report.
- **Validate Report** (Validator) – A **Validator** decides whether to approve or refuse the report.

Each of these represents a step in the report approval process.

1. **Start Event** – The process is triggered when a report is created.
2. **Create Report Task** – This is assigned to **any user with the Owner role** and has a **TaskListener** to notify the system once completed.
3. **Review Report Task** – The **Reviewer** takes over. This also has a **TaskListener** to track when the review is done.
4. **Validate Report Task** – The **Validator** checks whether the report should be approved or refused.
5. **Exclusive Gateway** – A decision point where the process evaluates the `isApproved` variable:
  - If **approved**, the process moves to the **Report Validated** end event.
  - If **refused**, it moves to the **Report Refused** end event.
6. **End Events** – Both end events have **Execution Listeners** that notify the system of which outcome happened (**approved or refused**).

---

### Camunda’s Built-in Tools

Camunda provides:
- **Cockpit** – A dashboard to **monitor process instances, check active tasks, and debug workflows**.
- **Tasklist** – A UI where users can claim and complete tasks.
- **Admin Panel** – Manages users, permissions, and deployments.

🚨🚨 **Important Security Note:** 🚨🚨  

For this project, **Camunda’s tasklist security is disabled** to prevent unauthorized users from completing tasks directly via the UI. However, you **can still access the Camunda Cockpit UI to monitor process progress**.

⚠️ In a **real-world scenario**, a better approach would be to integrate Camunda’s identity provider with **Spring Security Context or Keycloak**, ensuring secure authentication and role-based access control.

---

This setup ensures that our workflow remains **secure, maintainable, and event-driven**, leveraging Camunda’s strengths while enforcing the right security measures.

## 3. Event-Driven State Updates

As you probably already noticed, I’m **not updating the report state directly** from `ReportService` or `CamundaReportWorkflow`. Instead, I implemented an **event-driven state update mechanism** to keep things **decoupled and scalable**.

---

### How It Works

Camunda emits **events** when:
1. A **user task** (e.g., `Create Report`, `Review Report`) is completed.
2. An **execution event** (e.g., `Approved`, `Refused`) occurs.

These events are intercepted by **`ReportStateListener`**, which then **publishes a `ReportStateChangedEvent`**.

However, I **do not update the state inside `ReportStateListener`** itself. This keeps the implementation **agnostic**—whether I’m using Camunda or another strategy, **all state updates go through `ReportStateEventListener`**.

This way, **every workflow engine will follow the same event-based state update approach**, keeping the logic centralized.

---

### `ReportStateListener` – Capturing Events

This component listens for **task completion and execution events** in Camunda, then **publishes the event** without modifying any report state directly.

#### **Handling User Tasks (Create & Review Report)**
```java
@Component
@RequiredArgsConstructor
public class ReportStateListener implements TaskListener, ExecutionListener {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notify(DelegateTask delegateTask) {
        Long reportId = (Long) delegateTask.getVariable("reportId");
        Long userId = (Long) delegateTask.getVariable("userId");

        ReportState newState = switch (delegateTask.getTaskDefinitionKey()) {
            case "createTask" -> ReportState.CREATED;
            case "reviewTask" -> ReportState.REVIEWED;
            default -> null;
        };

        if (newState != null) {
            eventPublisher.publishEvent(new ReportStateChangedEvent(this, reportId, userId, newState));
        }
    }
}
```

#### **Handling Execution Events (Approved & Refused)**
```java
@Override
public void notify(DelegateExecution execution) {
    Long reportId = (Long) execution.getVariable("reportId");
    Long userId = (Long) execution.getVariable("userId");

    ReportState newState = switch (execution.getCurrentActivityId()) {
        case "endValidated" -> ReportState.VALIDATED;
        case "endRefused" -> ReportState.REFUSED;
        default -> null;
    };

    if (newState != null) {
        eventPublisher.publishEvent(new ReportStateChangedEvent(this, reportId, userId, newState));
    }
}
```

---

### `ReportStateChangedEvent` – The Published Event

Once a task is completed, `ReportStateListener` **publishes an event**, which contains:
- `reportId` – The report being updated
- `userId` – The user who completed the task
- `newState` – The new report state

```java
@Getter
@AllArgsConstructor
public class ReportStateChangedEvent {
    private final Object source;
    private final Long reportId;
    private final Long userId;
    private final ReportState newState;
}
```

---

### `ReportStateEventListener` – Updating the Database

This component **listens for `ReportStateChangedEvent`** and updates the report **inside the database**.  
Since we use **async processing**, this also helps with handling high loads.

```java
  @Component
  @RequiredArgsConstructor
  @Slf4j
  public class ReportStateEventListener {
      private final ReportRepository reportRepository;
      private final UserRepository userRepository;
      private final ReportTransitionLogRepository transitionLogRepository;
  
      @Async
      @EventListener
      @Transactional
      public void onReportStateChanged(ReportStateChangedEvent event) {
          Report report = reportRepository.findById(event.getReportId())
                  .orElseThrow(() -> new ReportNotFoundException(event.getReportId()));
  
          User user = userRepository.findById(event.getUserId())
                  .orElseThrow(UserNotFoundException::new);
  
          report.setState(event.getNewState());
  
          // Assign the user based on the role
          switch (getRoleForState(event.getNewState())) {
              case OWNER -> report.setOwner(user);
              case REVIEWER -> report.setReviewer(user);
              case VALIDATOR -> report.setValidator(user);
          }
  
          // Log the state transition along with updating the report
          transitionLogRepository.save(new ReportTransitionLog(report, user, event.getNewState()));
  
          reportRepository.save(report);
  
          log.info("Report ID {} updated to state: {} by User ID {}",
                  event.getReportId(), event.getNewState(), event.getUserId());
      }
  }
```

#### Why async ?
State updates run in a **parallel thread**, not the main Spring thread, ensuring **non-blocking execution**. Since each actor (Owner, Reviewer, Validator) takes time to complete their task, an instant update isn’t necessary. **@Async** allows the system to continue processing requests while updates happen in the background, improving performance and scalability.

---

### Why This Approach?

1. **Decoupling & Flexibility** – The state update is independent of the workflow engine. Any new workflow strategy will follow the same event-based update mechanism.
2. **Single Source of Truth** – Instead of updating the report state from multiple places, everything flows through `ReportStateEventListener`.
3. **Asynchronous Processing** – High-load scenarios won’t block execution.
4. **Better Debugging & Logging** – Logging is **centralized** in one place, ensuring every state transition is **tracked and easily traceable**.


With this setup, the **report state updates itself automatically based on workflow events**, keeping the logic clean, modular, and scalable.

## 4. Security Mechanism

Initially, I considered **Access Control Lists (ACLs)** for fine-grained permissions, but they introduced **unnecessary complexity and performance overhead**. Since this project only requires **role-based restrictions**, ensuring **Owners create, Reviewers review, and Validators validate**, a **simpler Role-Based Access Control (RBAC) approach** is the better fit. It keeps things **efficient, scalable, and easy to maintain** while still enforcing strict access control.

---

### Role-Based Access Control (RBAC)

RBAC ensures that:
- **Owners** can create reports.
- **Reviewers** can review reports.
- **Validators** can validate or refuse reports.

Instead of handling access logic **inside service methods**, I enforce security **at the request level** using **Spring Security’s `SecurityFilterChain`**. This ensures that **unauthorized requests are blocked before reaching the service layer**.

---

### Token-Based Authentication

The project uses **JWT (JSON Web Tokens)** for authentication. Every request must include a valid **JWT token**, which is validated by the security filter before granting access.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/auth/register",
                            "/api/v1/auth/login", "/swagger-ui/**",
                            "/v3/api-docs/**", "/camunda/**",
                            "/h2-console/**").permitAll()
                    .requestMatchers("/api/v1/reports/*/confirm", "/api/v1/reports/start").hasRole("OWNER") // Only Owners can create reports
                    .requestMatchers("/api/v1/reports/*/review").hasRole("REVIEWER") // Only Reviewers can review reports
                    .requestMatchers("/api/v1/reports/*/validate").hasRole("VALIDATOR") // Only Validators can validate/refuse reports
                    .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                    .accessDeniedHandler(customAccessDeniedHandler) // Handle unauthorized access
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}
```

This ensures:  
**Authentication is stateless** – JWT tokens are used instead of session-based authentication.  
**Role checks happen before execution** – Unauthorized requests are blocked **before reaching service methods**.  
**Custom access handling** – Unauthorized access is **logged and handled properly** using `CustomAccessDeniedHandler`.

---

### Enforcing Security at the Service Layer

Even though **requests are already filtered** at the security level, I **reinforce role-based security** inside the **service layer** using `@PreAuthorize`:

```java
@PreAuthorize("hasRole('OWNER')")
public void startReport(Long ownerId) { ... }

@PreAuthorize("hasRole('REVIEWER')")
public void reviewReport(Long reviewerId) { ... }

@PreAuthorize("hasRole('VALIDATOR')")
public void validateReport(Long validatorId, boolean isApproved) { ... }
```

This **double-checks role permissions**, ensuring that even if an attacker bypasses request filtering, they **cannot execute restricted actions** at the service level.

---

### Why This Approach?

1. **Performance-Efficient** – Role-based checks are enforced at the **earliest stage**, reducing unnecessary processing.
2. **Simpler & Maintainable** – No need for complex ACL management; **roles are sufficient** for this use case.
3. **Layered Security :** 
   - **JWT Authentication** ensures only valid users can interact with the system.
   - **Security Filter** blocks unauthorized requests before reaching services.
   - **Service-Level Checks** prevent bypassing security at the API level.

With this setup, the **system remains secure, efficient, and easy to maintain**, ensuring that **only authorized users can perform actions based on their roles**.


---

## Class Diagram Overview

![Class diagram](https://github.com/youssefalmia/report-workflow/blob/main/resources/bpm/camunda/class-diagram.png)


The class diagram focuses on the **core domain model**, representing the essential entities and their relationships within the system.

- **User**: Represents system users, each with a unique `id`, `username`, `password`, and assigned `roles`.
- **Report**: The main entity being processed, with attributes such as `title`, `state`, timestamps (`createdAt`, `completedAt`), and references to the `owner`, `reviewer`, and `validator`.
- **ReportState**: An enumeration defining the possible states of a report: `CREATED`, `REVIEWED`, `VALIDATED`, or `REFUSED`.
- **Role**: An enumeration defining user roles (`OWNER`, `REVIEWER`, `VALIDATOR`) to enforce access control.
- **ReportTransitionLog**: Tracks all state changes of a report, storing the `report_id`, the `user` who performed the change (`performedBy_id`), the `newState`, and the timestamp of the transition.


## Report Lifecycle State Diagram

![State diagram](https://github.com/youssefalmia/report-workflow/blob/main/resources/bpm/camunda/state-diagram.png)

The state diagram illustrates the **lifecycle of a report**, detailing how it transitions between different states based on user actions:

1. **Created** – A report is created by an **Owner**.
2. **Reviewed** – A **Reviewer** reviews the report and moves it forward.
3. **Validated or Refused** – A **Validator** makes the final decision:
   - If **approved**, the report reaches the **Validated** state.
   - If **rejected**, it transitions to the **Refused** state.



## Setup & Usage

Getting the project up and running is **super easy**—just follow these steps:

1. **Clone the repository**
   ```sh
   git clone <repo-url>
   cd <repo-folder>
   ```
2. **Install dependencies**
   ```sh
   mvn clean install
   ```  
3. **Run the project**
   ```sh
   mvn spring-boot:run
   ```  

That's it! No need to configure anything—thanks to the **H2 in-memory database**, everything is set up automatically.

---

### API Documentation & Testing

Once the project is running, you can access the **Swagger UI** to view and test all API endpoints:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

![Swagger](https://github.com/youssefalmia/report-workflow/blob/main/resources/bpm/camunda/swagger.png)


---

### Running Tests

You can also execute tests with:

```sh
mvn test
```
or
```sh
mvn verify -P coverage
```  

This will run all tests and generate a **test coverage report**.

---

🎉 **You're all set! Enjoy working with the project.** 🚀
## **Report Workflow Management - Secure & Scalable System**
  
