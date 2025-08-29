# even-solution (Java + SQL Challenge)

## How to build & run
1. Open terminal in this folder (where `pom.xml` is).
2. Run:
   ```bash
   mvn clean install
   java -jar target/even-solution-0.0.1-SNAPSHOT.jar
   ```
3. Logs will show the webhook request & response.

## Notes
- If regNo last two digits are EVEN → runs **Question 2 SQL**
- If regNo last two digits are ODD → runs **Question 1 SQL**
- Authorization header now sends **raw token** (no "Bearer " prefix).
