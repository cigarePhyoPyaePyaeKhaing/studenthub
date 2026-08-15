# StudentHub

StudentHub is a server-rendered university community application for UIT students. It provides authenticated, academically scoped announcements, deadlines, discussion rooms, profiles, administration, comments, reactions, and in-app notifications.

## Technology stack

- Java 17
- Jakarta Servlet 6 and JSP/JSTL
- JDBC with MySQL Connector/J 8.4.0
- MySQL 8
- Maven WAR packaging
- Apache Tomcat 10.1
- Bootstrap 5 plus project CSS
- BCrypt passwords and Brevo SMTP OTP delivery

## Requirements

- JDK 17
- Maven 3.9+
- Apache Tomcat 10.1 (the documented local setup uses port `8081`)
- MySQL 8 (the documented local setup uses port `3307`)
- A Brevo SMTP account when registration verification and password reset email are required

## Database setup

For a new database, execute `database/schema.sql` from an authenticated MySQL client session. Do not rerun or reset a populated database merely to deploy a new WAR.

For an older existing StudentHub database, apply migrations in this order:

1. `database/migrations/V2__authentication.sql`
2. `database/migrations/V3__notifications.sql`

PowerShell examples:

```powershell
mysql --host=localhost --port=3307 --user=root --password --execute="SOURCE C:/StudentHub/database/migrations/V2__authentication.sql"
mysql --host=localhost --port=3307 --user=root --password --execute="SOURCE C:/StudentHub/database/migrations/V3__notifications.sql"
```

V2 and V3 are additive and preserve existing application data. V3 creates scoped notifications and per-user read receipts. If a Windows `mysql.exe` build cannot authenticate because of an authentication-plugin compatibility issue, connect with a compatible authenticated MySQL client first, then run:

```sql
SOURCE C:/StudentHub/database/migrations/V2__authentication.sql;
SOURCE C:/StudentHub/database/migrations/V3__notifications.sql;
```

Do not apply a migration again when the environment has already been migrated unless its idempotency and current schema have been checked.

## Configuration

Set database variables in the environment that starts Tomcat:

```powershell
$env:STUDENTHUB_DB_URL = 'jdbc:mysql://localhost:3307/studenthub_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8'
$env:STUDENTHUB_DB_USER = 'your_mysql_user'
$env:STUDENTHUB_DB_PASSWORD = 'your_mysql_password'
```

Email verification and password reset use:

```text
BREVO_SMTP_HOST
BREVO_SMTP_PORT
BREVO_SMTP_USERNAME
BREVO_SMTP_PASSWORD
BREVO_FROM_EMAIL
BREVO_FROM_NAME
```

Keep passwords, SMTP credentials, API keys, and production URLs outside source control.

## Roles and features

- `STUDENT`: views scoped content, participates in discussions, comments/reacts, manages their own profile, and reads notifications.
- `CR`: student capabilities plus announcement/deadline creation and ownership-based management.
- `ADMIN`: administrative dashboards, user management, role management, and authorized management of community content.

Visibility is derived server-side from the authenticated user's database profile. Announcements support `ALL`, `SEMESTER`, and `SECTION`; discussion rooms and notifications enforce the equivalent scope rules. Deadlines use semester and optional section scope.

## Security

- Authenticated identity and role come from the server session.
- State-changing operations use POST and require CSRF validation.
- DAOs use prepared statements and close JDBC resources.
- Role, ownership, academic scope, and notification visibility are enforced server-side.
- Public registration cannot choose privileged roles.
- Admin role changes protect self-demotion and the final administrator.
- JSP database text is escaped with JSTL output tags.
- Custom 403, 404, and 500 pages do not render internal exception details.

Never trust browser-supplied user IDs, author IDs, roles, semesters, sections, verification state, or admin state.

## Build and tests

From `C:\StudentHub`:

```powershell
mvn clean test
mvn clean package
```

The test suite covers authentication validation, authorization, announcement visibility, deadline scope and ownership, discussions, profiles, comments/reactions, administration, notification scope, notification JavaBean/JSP compatibility, and validation rules. These unit/regression tests do not replace live MySQL, Tomcat, or browser testing.

The resulting artifact is:

```text
C:\StudentHub\target\studenthub.war
```

## Tomcat deployment

Run Administrator PowerShell:

```powershell
Set-Location -LiteralPath 'C:\StudentHub'
mvn clean package
& 'C:\Program Files\Apache\apache-tomcat-10.1.57\bin\shutdown.bat'
Copy-Item -LiteralPath 'C:\StudentHub\target\studenthub.war' -Destination 'C:\Program Files\Apache\apache-tomcat-10.1.57\webapps\studenthub.war' -Force
& 'C:\Program Files\Apache\apache-tomcat-10.1.57\bin\startup.bat'
```

Ensure the Tomcat process receives the database and Brevo environment variables. If Tomcat runs as a Windows service, configure variables for that service account and restart the actual installed service instead of using the batch scripts.

## Application URLs

- `http://localhost:8081/studenthub/`
- `http://localhost:8081/studenthub/register`
- `http://localhost:8081/studenthub/login`
- `http://localhost:8081/studenthub/home`
- `http://localhost:8081/studenthub/announcements`
- `http://localhost:8081/studenthub/deadlines`
- `http://localhost:8081/studenthub/discussions`
- `http://localhost:8081/studenthub/notifications`
- `http://localhost:8081/studenthub/profile`
- `http://localhost:8081/studenthub/admin`
- `http://localhost:8081/studenthub/admin/users`
- `http://localhost:8081/studenthub/health`
- `http://localhost:8081/studenthub/does-not-exist` (custom 404 check)

## Demo Flow

1. Login as `STUDENT`.
2. Show the dashboard.
3. Show scoped announcements.
4. React and comment.
5. Show deadlines.
6. Show Section, Semester, and All discussion rooms.
7. Show the profile.
8. Login as `CR`.
9. Create an announcement and deadline.
10. Show ownership-based management.
11. Login as `ADMIN`.
12. Show Admin Dashboard.
13. Show user management.
14. Show notifications and unread state.
15. Show responsive/mobile UI.

Use accounts prepared by the presenter; no demo passwords or credentials are stored in this repository.

## Known limitations

- Discussions use normal HTTP requests rather than WebSockets, so updates are not real-time push events.
- Notification delivery is in-app only.
- The UI uses Bootstrap from a CDN; offline environments must provide that asset separately.
- Database-backed behavior requires a configured MySQL instance and the correct migrations.
- Deadline `DATETIME` values are local wall-clock values; Tomcat and MySQL should use consistent timezone configuration.
- Automated tests do not prove live SMTP delivery, Tomcat deployment, responsive rendering, or every notification scope/read-state combination.

## Production Deployment

Recommended architecture:

```text
Browser
   |
   | HTTPS
   v
Railway
   |-- Tomcat 10.1
   |-- StudentHub ROOT.war
   |-- Jakarta Servlets and JSP
   |
   +---- verified TLS ----> Aiven MySQL
   |
   +---- STARTTLS -------> Brevo SMTP
```

Vercel is not required for the current StudentHub architecture. Railway serves both the Jakarta backend and JSP-rendered frontend. JSP cannot be deployed directly to Vercel, and introducing a duplicate frontend would add unnecessary authentication and CORS complexity. `FRONTEND_URL` and `LOCAL_FRONTEND_URL` are optional CORS allowlist values only if an independent `/api/*` client is introduced later.

### Production environment variables

Railway must receive these values as secrets or normal runtime configuration as appropriate:

```text
APP_ENV=production
STUDENTHUB_DB_URL=jdbc:mysql://AIVEN_HOST:AIVEN_PORT/AIVEN_DATABASE?sslMode=VERIFY_IDENTITY&characterEncoding=UTF-8&serverTimezone=UTC
STUDENTHUB_DB_USER=AIVEN_USER
STUDENTHUB_DB_PASSWORD=AIVEN_PASSWORD
BREVO_SMTP_HOST=smtp-relay.brevo.com
BREVO_SMTP_PORT=587
BREVO_SMTP_USERNAME=BREVO_SMTP_LOGIN
BREVO_SMTP_PASSWORD=BREVO_SMTP_PASSWORD
BREVO_FROM_EMAIL=VERIFIED_SENDER_EMAIL
BREVO_FROM_NAME=StudentHub
```

Railway supplies `PORT`; do not set a fixed production HTTP port unless Railway explicitly requires it. The container validates that `PORT` is numeric and updates Tomcat's HTTP connector before startup.

`STUDENTHUB_DB_URL` is the application's existing JDBC convention, so separate `DB_HOST`, `DB_PORT`, and `DB_NAME` variables are not required. Never use `useSSL=false`, `sslMode=DISABLED`, or a trust-all certificate implementation with Aiven.

For an Aiven CA that is not already trusted by the container JVM, create a Java truststore outside the repository from the CA downloaded through the authenticated Aiven console. Mount it as secret material and configure the JVM with deployment-specific options such as:

```text
JAVA_OPTS=-Djavax.net.ssl.trustStore=/run/secrets/aiven-truststore.p12 -Djavax.net.ssl.trustStorePassword=RUNTIME_SECRET -Djavax.net.ssl.trustStoreType=PKCS12
```

Do not commit the CA bundle, truststore, password, Aiven service URI, or credentials. Keep `sslMode=VERIFY_IDENTITY` so the server certificate and hostname are verified.

### Aiven database procedure

1. Create an Aiven MySQL 8 service and database.
2. Confirm the database and tables use `utf8mb4`; the supplied schema uses `utf8mb4_unicode_ci` for Myanmar text, Unicode, and emoji.
3. Download the Aiven CA through its authenticated console and prepare/mount the truststore when required.
4. Connect using a compatible authenticated TLS MySQL client.
5. For a new empty database, run `SOURCE database/schema.sql;`. The current schema already includes authentication and notification tables; do not then reapply V2/V3 blindly.
6. For a compatible older populated StudentHub database, back it up, inspect its schema, then run V2 followed by V3 using the migration commands documented above.
7. Never reset a populated production database, disable foreign keys, or run destructive initialization during container startup.

### Railway deployment procedure

1. Create a Railway project from the repository.
2. Select Dockerfile deployment; no separate start command is required because the image runs `catalina.sh run`.
3. Add the production variables listed above and any required truststore secret mount/JVM options.
4. Deploy the image. The build stage runs Maven and packages `studenthub.war`; the runtime stage deploys it as Tomcat `ROOT.war`.
5. Wait for Railway to assign a public HTTPS domain. No domain is hardcoded in StudentHub.
6. Check `https://RAILWAY_ASSIGNED_DOMAIN/health`. A healthy configured database returns HTTP 200; a database failure returns 503.
7. Open `https://RAILWAY_ASSIGNED_DOMAIN/`. In Railway the application context is `/`; local manual Tomcat deployment remains `http://localhost:8081/studenthub/`.

All JSP links and redirects use the servlet request context path, so both deployment modes are supported:

- Local `studenthub.war`: `/studenthub`
- Railway `ROOT.war`: `/`

With `APP_ENV=production`, the session cookie is `HttpOnly` and `Secure`. The packaged Tomcat context config sets `SameSite=Lax`. CSRF tokens and server-side authentication/authorization remain active behind Railway HTTPS.

### Brevo production setup

1. Verify the sender address or domain in Brevo.
2. Create SMTP credentials and store them only in Railway variables.
3. Use `smtp-relay.brevo.com` on port `587`; the application requires authentication and STARTTLS.
4. Set `BREVO_FROM_EMAIL` to the verified sender and configure a non-secret sender name.
5. Test registration verification and password reset after deployment. OTP values are hashed in the database and are not written to application output.

### Production smoke-test checklist

After obtaining the actual Railway HTTPS domain, manually verify:

1. Registration and Brevo OTP delivery
2. Login and CSRF-protected logout
3. `STUDENT`, `CR`, and `ADMIN` authorization boundaries
4. Scoped announcements and deadlines
5. Section, Semester, and All discussions
6. Comment and reaction ownership behavior
7. Notification audience, unread badge, and mark-as-read authorization
8. Profile updates and session refresh
9. Admin dashboard and user role protections
10. Custom 403, 404, and 500 behavior
11. Mobile layout on the deployed HTTPS site

These are manual production checks. A successful Maven or Docker build does not prove that Aiven, Brevo, Railway routing, or browser behavior is configured correctly.
