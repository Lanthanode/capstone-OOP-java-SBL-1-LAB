package com.sbtms.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

/**
 * Enterprise Relational DBMS Manager for SB-TMS.
 * Manages JDBC connections, schema bootstrapping, ACID transaction lifecycle, and SQL execution.
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 * @institution Ramrao Adik Institute of Technology, Nerul
 */
public class DatabaseManager {
    private static String dbUrl = "jdbc:sqlite:project/database/sbtms_bank.db";
    private static boolean initialized = false;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager Warning]: org.sqlite.JDBC not found on standard classpath. Attempting fallback...");
        }
    }

    public static synchronized void configureDbPath(String path) {
        dbUrl = "jdbc:sqlite:" + path;
        initialized = false;
    }

    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        Connection conn = DriverManager.getConnection(dbUrl);
        // Enable foreign key constraints in SQLite
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    private static String resolvePath(String relative) {
        String[] candidates = {
            relative,
            "database/" + new File(relative).getName(),
            "project/" + relative,
            "../" + relative,
            "../../" + relative
        };
        for (String c : candidates) {
            File f = new File(c);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }
        return relative;
    }

    public static synchronized void ensureInitialized() {
        if (initialized) return;

        try {
            // Dynamically resolve db path
            File dbFile;
            if (new File("database").exists()) {
                dbFile = new File("database/sbtms_bank.db");
            } else if (new File("project/database").exists()) {
                dbFile = new File("project/database/sbtms_bank.db");
            } else {
                dbFile = new File("database/sbtms_bank.db");
            }
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            try (Connection conn = DriverManager.getConnection(dbUrl);
                 Statement stmt = conn.createStatement()) {
                
                stmt.execute("PRAGMA foreign_keys = ON;");

                // Check if ACCOUNTS table already exists
                boolean accountsExist = false;
                try (ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='ACCOUNTS';")) {
                    if (rs.next()) {
                        accountsExist = true;
                    }
                }

                if (!accountsExist) {
                    System.out.println("[DatabaseManager]: Bootstrapping Relational DBMS Schema...");
                    executeSqlScript(conn, resolvePath("database/schema.sql"));
                    System.out.println("[DatabaseManager]: Populating Benchmark Seed Dataset...");
                    executeSqlScript(conn, resolvePath("database/seed.sql"));
                    System.out.println("[DatabaseManager]: DBMS Relational Storage Ready at " + dbFile.getAbsolutePath());
                }
            }
            initialized = true;
        } catch (Exception ex) {
            System.err.println("[DatabaseManager Error]: Failed to bootstrap database: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void executeSqlScript(Connection conn, String scriptPath) throws IOException, SQLException {
        File file = new File(scriptPath);
        if (!file.exists()) {
            file = new File(resolvePath(scriptPath));
        }
        if (!file.exists()) {
            System.err.println("[DatabaseManager Warning]: SQL script not found at " + scriptPath);
            return;
        }

        StringBuilder cleanSql = new StringBuilder();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("--")) {
                cleanSql.append(line).append("\n");
            }
        }

        String[] statements = cleanSql.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String s : statements) {
                String sql = s.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
    }

    /**
     * Executes arbitrary SQL query from the Interactive SQL Inspector web console.
     * Allows professor and evaluators to run real SQL statements directly on the database.
     */
    public static Map<String, Object> executeArbitraryQuery(String sql) {
        Map<String, Object> result = new LinkedHashMap<>();
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }

        long startTime = System.currentTimeMillis();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            boolean hasResultSet = stmt.execute(sql);
            long elapsed = System.currentTimeMillis() - startTime;
            result.put("executionTimeMs", elapsed);
            result.put("sql", sql);

            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int colCount = md.getColumnCount();
                    List<String> headers = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        headers.add(md.getColumnLabel(i));
                    }
                    result.put("columns", headers);

                    List<List<Object>> rows = new ArrayList<>();
                    while (rs.next()) {
                        List<Object> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            row.add(rs.getObject(i));
                        }
                        rows.add(row);
                    }
                    result.put("rows", rows);
                    result.put("rowCount", rows.size());
                    result.put("type", "SELECT");
                    result.put("success", true);
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                result.put("affectedRows", updateCount);
                result.put("type", "UPDATE/DDL");
                result.put("success", true);
                result.put("message", "Statement executed successfully. Affected rows: " + updateCount);
            }
        } catch (SQLException ex) {
            result.put("success", false);
            result.put("error", ex.getMessage());
            result.put("sqlState", ex.getSQLState());
            result.put("errorCode", ex.getErrorCode());
        }
        return result;
    }
}
