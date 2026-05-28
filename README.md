# Contacts Management

## Introduction

Managing personal and professional networks requires a fast, lightweight, and reliable tool. The **Contacts Management System** bridges the gap between classic desktop client wrappers and backend relational databases. Built natively using Java's robust `javax.swing` architecture and driven by Java Database Connectivity (JDBC), this standalone desktop utility executes all backend lifecycle logic programmatically.

Unlike conventional database applications that require manual infrastructure provisioning, schema drafting, or external script execution, this application features a self-bootstrapping engine. Upon execution, the client evaluates your database engine topology, compiles schemas on the fly, and dynamically synchronizes database mutations via an event-driven graphical interface. It provides an elegant, minimalist workspace emphasizing white backgrounds, structured card layouts, and responsive action banners to deliver an optimized user experience.

<img width="562" height="818" alt="image" src="https://github.com/user-attachments/assets/83360113-cec4-4f41-94bf-c87a115e881e" />

## How to Setup

Follow these steps to configure your local development environment and launch the desktop application:

1. **Verify Prerequisites**: Ensure your local workstation has Java JDK 11 or higher installed. You will also need an IDE such as Eclipse and a local instance of the MySQL Server database engine.
2. **Configure Database Credentials**: Open the `ContactsApp.java` file within your source code editor. Locate the authentication fields inside the database configuration block at the top of the class definition, and verify that the `DB_USER` and `DB_PASSWORD` string constants match your local MySQL configuration (the application default is set to user "root" with password "1122").
3. **Link Driver Dependencies**: This database tool requires the official MySQL Connector/J runtime driver to map Java transactions onto relational SQL queries. Download the `mysql-connector-j-x.x.x.jar` bundle from Maven Central or an official repository and include it inside your IDE project's Build Path/Libraries reference list.
4. **Compile and Build**: Trigger a project tree refresh or rebuild within Eclipse to force your environment to resolve all external dependencies, user interface imports, and core structural connections.
5. **Execute the Application Bootstrap**: Run the `ContactsApp.java` class file as a standard Java application. During its boot cycle, the program automatically calls its core SQL generator scripts to safely create the target backend schema `ContactsDB` and map out structural tables without requiring manual execution of external SQL scripts.
6. **Interact with the Contacts Registry**: Use the navigation header to flip between workspaces. You can test validations by inputting credentials in the "Add Contact" view, running sub-string checks inside the "Search Contact" filter, and toggling star bookmarks to populate the standalone "Favorites" registry view.


## Structural Architecture & Database Design

The application abstracts away backend configuration by embedding the logical design directly into the source compilation layer. On application startup, the connection architecture performs consecutive validation passes: first establishing a handshake with the root server, ensuring the target database is active, and building any missing structural properties before launching the GUI frame.

<img width="562" height="822" alt="image" src="https://github.com/user-attachments/assets/c72f1378-0acb-4eef-bc3d-192921220fab" />


### Database & Schema Specification

* **Database Target Namespace:** `ContactsDB`
* **Primary Structural Table:** `contacts`

The internal data definitions enforce domain safety boundaries, data normalization parameters, and storage constraints optimized for fast index lookups:

| Column Identity | SQL Primitive Type | Relational Rules / Constraints | Functional Purpose |
| --- | --- | --- | --- |
| `id` | `INT` | `AUTO_INCREMENT`, `PRIMARY KEY` | Unique row identifier used for relational mutations. |
| `first_name` | `VARCHAR(50)` | `NOT NULL` | The contact's primary name; required for card indices. |
| `last_name` | `VARCHAR(50)` | `NOT NULL` | The contact's family surname; used in alphabetic routing. |
| `phone_number` | `VARCHAR(15)` | `NOT NULL`, `UNIQUE` | Primary dialer string. Enforces strict global uniqueness. |
| `email` | `VARCHAR(100)` | `NULL` | Electronic mail address string. Optional field wrapper. |
| `is_favourite` | `BOOLEAN` | `DEFAULT FALSE` | Binary toggle state flag used to filter favorites. |

<img width="563" height="825" alt="image" src="https://github.com/user-attachments/assets/4879f229-11ab-4024-9397-993902a57840" />



## Workflow Mechanics (How It Works)

The application functions via a unified, single-frame view system driven by a structured execution sequence:

1. **System Discovery Handshake:** The application contacts the local loopback server (`localhost:3306`). If the connection drops due to bad credentials or inactive processes, a modal alerts the user and halts execution to protect application integrity.
2. **Schema Verification & Generation:** The environment scans for `ContactsDB` and the `contacts` table. If missing, it builds them automatically without data loss.
3. **CardLayout Navigation Matrix:** Instead of instantiating resource-heavy independent operational viewports (which create clutter and memory leaks), a base container handles state changes using a `CardLayout`. Clicking top menu indicators flips the active card context instantly.
4. **Isolated Operational Views:**
* **Main Directory View:** Polls the database, sorts rows alphabetically by `first_name`, and populates a dynamic list.
* **Add Contact Panel:** A validation container capturing user inputs. It blocks empty fields before compiling parameters into a `PreparedStatement`.
* **Unified Multi-Column Search Engine:** Translates raw user string queries into matching patterns using SQL wildcards (`%`). A single input scan simultaneously searches `first_name`, `last_name`, and `phone_number`.
* **Favorites Filtering View:** Runs a localized query filtering for `is_favourite = TRUE` entries.
5. **Contextual Dialog Layer:** Selecting an individual contact cell interrupts the focus loop to spawn a modal dialog box. This pop-up triggers contextual database actions, such as toggling favorite metrics or dropping rows via a verified confirmation prompt.


<img width="552" height="827" alt="image" src="https://github.com/user-attachments/assets/fe84a3b8-fa2f-4e3a-927e-8dc791cf98c7" />


## Key Features

* **Self-Contained Automated Bootstrap Pipeline:** Eliminates manual database setup. The application completely auto-provisions its database environment on launch.
* **Modern Minimalist Visual Language:** Built with flat, unbordered color block buttons, spacious borders, and crisp, anti-aliased Segoe UI typography against a dominant white background.
* **Granular Input Protection Framework:** Intercepts runtime database exceptions (such as MySQL Engine Error Code `1062` for duplicate entries) and transforms them into clear, actionable user alerts instead of crashing.
* **Atomic Structural Mutations:** Protects data integrity by routing insertions, updates, and removals through secure, parameterized transactions via `PreparedStatement`.
* **Instant UI Refresh Loops:** Component arrays invalidate and repaint automatically upon data modification, ensuring backend updates reflect immediately in the UI.

<img width="562" height="827" alt="image" src="https://github.com/user-attachments/assets/2245ee95-6967-40d5-9472-c66056f7b70d" />


##Advantages & Disadvantages

### Advantages

* **Zero Configuration Overheads:** Perfect for developer evaluations and localized operations since it requires no database provisioning scripts.
* **Excellent Interface Performance:** The lightweight component tree and `CardLayout` architecture eliminate rendering delay, keeping view transitions snappy.
* **Data Consistency Protection:** The unique index constraint on phone strings prevents database duplication errors.
* **Highly Legible Layouts:** High contrast ratios, desaturated accent colors, and ample padding make navigating the layout simple and intuitive.

### Disadvantages

* **Localized Database Dependency:** Relies on local MySQL connection strings (`localhost:3306`). Network interruptions or server downtime will prevent the application from starting.
* **Plaintext Credential Storage:** Database passwords are hardcoded inside compiled variables, which is suitable for local development but unsafe for multi-tenant production builds without using environment variables.
* **Single-Session UI Constraints:** The modal dialog mechanism locks focus to the pop-up window, preventing users from opening multiple details screens at once.


## Project Setup & Execution Guide

### Prerequisites

1. **Java Runtime & Tools:** JDK 8 or higher installed on your system.
2. **Database System:** A local instance of MySQL Server running on port `3306`.
3. **Driver Dependencies:** The `mysql-connector-j-x.x.x.jar` archive added to your build class paths.

### Local Configuration

Open your project inside the Eclipse IDE environment workspace. Locate the root credential parameters block defined at the head of the application wrapper class file:

```java
private static final String MYSQL_SERVER_URL = "jdbc:mysql://localhost:3306/";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "YOUR_LOCAL_MYSQL_PASSWORD"; 

```

*Modify `DB_PASSWORD` to match your local database authentication credentials.*

### Compilation Sequence

1. Right-click the primary class source file `ContactsAppAutomated.java` inside your Eclipse project explorer pane.
2. Select **Run As** followed by **Java Application**.
3. The initialization engine will output log state verifications directly to your console, and the graphical management workspace will display on screen.

<img width="1597" height="810" alt="image" src="https://github.com/user-attachments/assets/18b797cf-44e9-4a2a-9300-ff0ba00088a7" />


## Conclusion

The **Contacts Management System** combines clean Java Swing front-ends with solid MySQL backend mechanics. Automating table generation and error handling reduces configuration hassles, letting you focus on interacting with your data. This architecture provides a scalable foundation for adding advanced features down the road, such as remote hosting integrations, contact group tagging, or cloud backups.

<img width="535" height="347" alt="image" src="https://github.com/user-attachments/assets/4862e0b8-4c32-42a6-a35d-450965e75a5c" />

