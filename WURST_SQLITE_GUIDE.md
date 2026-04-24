# Wurst SQLite Compiletime Support Guide

This guide covers setting up an SQLite database locally alongside Wurst scripts, allowing you to load and persist structured data during Wurst compilation using the new SQLite JDBC bindings.

### 1. Database Setup / Initialization
You don’t strictly *need* an initial physical file if you use `:memory:` or execute table creation directly through Wurst. However, best practice for map development is to use a dedicated standalone file inside your Wurst project (for example, `wurst_data.db`).

If you'd rather not manually create the `.db` file using `sqlite3`, you can rely on Wurst to generate the target `.db` file and its tables during `@compiletime`! SQLite JDBC will automatically create the file if it does not exist when using `sqlite_open()`.

***

### 2. Full Test Snippet: Wurst Script
Save this code in your project (e.g. `SQLiteIntegrationTest.wurst`). This snippet demonstrates creating the database, loading dummy values into it using pure `INSERT` statements, updating it, and executing `sqlite_select` (with assertions) to ensure everything behaves identically during compiletime and testing.

> **Note on Persisted Databases:** 
> When testing with physical `.db` files across multiple builds, use `DROP TABLE IF EXISTS` before `CREATE TABLE` to ensure your build scripts remain idempotent.

```wurst
package SQLiteIntegrationTest
import LinkedList
import ErrorHandling

// Natively exposed bindings
@extern native sqlite_open(string path) returns int
@extern native sqlite_prepare(int conn, string q) returns int
@extern native sqlite_step(int stmt) returns boolean
@extern native sqlite_column_string(int stmt, int idx) returns string
@extern native sqlite_column_count(int stmt) returns int
@extern native sqlite_exec(int conn, string q)
@extern native sqlite_finalize(int stmt)
@extern native sqlite_close(int conn)

// The generalized result class with variable binding
public class SqlResult
    string array cols

// A full SQL-Select helper
public function sqlite_select(int db, string query) returns LinkedList<SqlResult>
    let list = new LinkedList<SqlResult>()
    let stmt = sqlite_prepare(db, query)
    let cols = sqlite_column_count(stmt)
    
    while sqlite_step(stmt)
        let row = new SqlResult()
        // SQLite permits up to 2000 columns per result set.
        let limit = cols > 2000 ? 2000 : cols
        for i = 0 to limit - 1
            row.cols[i] = sqlite_column_string(stmt, i)
            
        list.add(row)
        
    sqlite_finalize(stmt)
    return list

// ============================================
// Database Tests & Population Functions
// ============================================

function buildAndVerifyDatabase()
    // Open a database connection (Use path like "heroes.db" for persistent storage)
    // We use :memory: here for rapid temporary testing
    let db = sqlite_open(":memory:")
    
    // 1. Create table structured schema
    sqlite_exec(db, "DROP TABLE IF EXISTS Heroes")
    sqlite_exec(db, "CREATE TABLE Heroes (id INTEGER PRIMARY KEY, name TEXT, role TEXT, power_level INTEGER)")
    
    // 2. Insert dummy data 
    sqlite_exec(db, "INSERT INTO Heroes (name, role, power_level) VALUES ('Arthur', 'Paladin', 9000)")
    sqlite_exec(db, "INSERT INTO Heroes (name, role, power_level) VALUES ('Merlin', 'Mage', 8500)")
    sqlite_exec(db, "INSERT INTO Heroes (name, role, power_level) VALUES ('Robin', 'Archer', 7200)")

    // 3. Execute select to read all fields
    let results = sqlite_select(db, "SELECT name, role, power_level FROM Heroes ORDER BY power_level DESC")
    
    // 4. Verify length matches insertions
    if results.size() != 3
        error("Expected 3 database entries, found " + results.size().toString())

    // 5. Verify the highest power is returned first correctly (Arthur is 9000 -> cols[2])
    let topHero = results.get(0)
    if topHero.cols[0] != "Arthur" or topHero.cols[2] != "9000"
        error("Expected Arthur as highest power, but got " + topHero.cols[0] + " with " + topHero.cols[2])

    // 5b. Verify standard fetch
    let mageHero = results.get(1)
    if mageHero.cols[0] != "Merlin" or mageHero.cols[1] != "Mage"
        error("Validation Failed for Merlin row!")
        
    // 6. Output to the developer console log
    print("Database built and successfully validated all rows!")
    
    // Close the connection
    sqlite_close(db)

// Executes inside Wurst Unit Test run
@test function databaseSystemTest()
    buildAndVerifyDatabase()
    
// Executes during Build or IDE evaluation
@compiletime function compilerDatabaseLoad()
    buildAndVerifyDatabase()
```

### 3. Running & Verifying
If you drop the file above into your repo, you can immediately test it utilizing VSCode:
1. Open the file in the editor.
2. Click the `Run Test` CodeLens helper right above `@test function databaseSystemTest()`
3. In VSCode's Output/Wurst terminal you should see: *"Database built and successfully validated all rows!"* with a green checkmark indicating successful unit execution.
4. If you intentionally sabotage an assertion (e.g., checking if Arthur's power level was 5000), you'll see a red underline directly in VSCode exactly where the query assertion or extraction fails.
