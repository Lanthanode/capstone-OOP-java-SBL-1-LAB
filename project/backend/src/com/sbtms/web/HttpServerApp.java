package com.sbtms.web;

import com.sbtms.db.DatabaseManager;
import com.sbtms.exception.BankingException;
import com.sbtms.model.*;
import com.sbtms.service.AnalyticsService;
import com.sbtms.service.BankingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP Server & RESTful API Gateway for SB-TMS.
 * Built entirely on standard Java SE com.sun.net.httpserver (zero external servlet container needed).
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 * @institution Ramrao Adik Institute of Technology, Nerul
 */
public class HttpServerApp {
    private final int port;
    private final BankingService bankingService;
    private final AnalyticsService analyticsService;
    private HttpServer server;

    public HttpServerApp(int port) {
        this.port = port;
        this.bankingService = new BankingService();
        this.analyticsService = new AnalyticsService();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // REST API endpoints
        server.createContext("/api/", new ApiHandler());

        // Static Frontend Asset server
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("================================================================================");
        System.out.println("   RAMRAO ADIK INSTITUTE OF TECHNOLOGY, NERUL - DEPT OF COMPUTER ENGG.");
        System.out.println("   SMART BANKING TRANSACTION & ACCOUNT PORTFOLIO MANAGEMENT SYSTEM (SB-TMS)");
        System.out.println("   Candidate: Anish Vyapari | Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1");
        System.out.println("================================================================================");
        System.out.println(">>> HTTP REST Server actively listening at: http://localhost:" + port + "/");
        System.out.println(">>> Real-time Web Dashboard: http://localhost:" + port + "/index.html");
        System.out.println(">>> Live SQL & DBMS Inspector: http://localhost:" + port + "/index.html#sql-inspector");
        System.out.println("================================================================================\n");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[HttpServerApp]: Server stopped.");
        }
    }

    // --- REST API DISPATCHER ---
    private class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Enable CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            try {
                if (path.equals("/api/health") && "GET".equalsIgnoreCase(method)) {
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("status", "HEALTHY");
                    resp.put("system", "Smart Banking Transaction & Portfolio Management System (SB-TMS)");
                    resp.put("candidate", "Anish Vyapari");
                    resp.put("prn", "DY25ENGU0AIM012");
                    resp.put("batch", "A/A1");
                    resp.put("department", "Computer Engineering");
                    resp.put("institution", "Ramrao Adik Institute of Technology, Nerul");
                    resp.put("port", port);
                    sendJsonResponse(exchange, 200, resp);

                } else if (path.equals("/api/accounts") && "GET".equalsIgnoreCase(method)) {
                    List<Account> accounts = bankingService.getAllAccounts();
                    sendJsonResponse(exchange, 200, accounts);

                } else if (path.startsWith("/api/accounts/") && "GET".equalsIgnoreCase(method)) {
                    String accNo = path.substring("/api/accounts/".length());
                    Account acc = bankingService.getAccount(accNo);
                    sendJsonResponse(exchange, 200, acc);

                } else if (path.equals("/api/accounts") && "POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = JsonHelper.parseSimpleJson(body);
                    String accNo = data.get("accountNumber");
                    String name = data.get("holderName");
                    String email = data.get("email");
                    String phone = data.get("phone");
                    String type = data.get("accountType");
                    double initDeposit = parseDouble(data.get("initialDeposit"), 0.0);

                    Account acc;
                    if ("CURRENT".equalsIgnoreCase(type)) {
                        double odLimit = parseDouble(data.get("overdraftLimit"), 50000.0);
                        acc = new CurrentAccount(accNo, name, email, phone, initDeposit, odLimit, 0.0, "ACTIVE", null);
                    } else if ("FIXED_DEPOSIT".equalsIgnoreCase(type)) {
                        int tenure = (int) parseDouble(data.get("tenureMonths"), 12.0);
                        double rate = parseDouble(data.get("interestRate"), 7.5);
                        acc = new FixedDepositAccount(accNo, name, email, phone, initDeposit, tenure, rate, "ACTIVE", null);
                    } else {
                        double rate = parseDouble(data.get("interestRate"), 4.25);
                        acc = new SavingsAccount(accNo, name, email, phone, initDeposit, rate, "ACTIVE", null);
                    }

                    Account created = bankingService.createAccount(acc);
                    sendJsonResponse(exchange, 201, created);

                } else if (path.equals("/api/transactions/deposit") && "POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = JsonHelper.parseSimpleJson(body);
                    String accNo = data.get("accountNumber");
                    double amount = parseDouble(data.get("amount"), 0.0);
                    String memo = data.get("referenceNote");

                    Transaction txn = bankingService.deposit(accNo, amount, memo);
                    sendJsonResponse(exchange, 200, txn);

                } else if (path.equals("/api/transactions/withdraw") && "POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = JsonHelper.parseSimpleJson(body);
                    String accNo = data.get("accountNumber");
                    double amount = parseDouble(data.get("amount"), 0.0);

                    Transaction txn = bankingService.withdraw(accNo, amount);
                    sendJsonResponse(exchange, 200, txn);

                } else if (path.equals("/api/transactions/transfer") && "POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = JsonHelper.parseSimpleJson(body);
                    String sourceAcc = data.get("sourceAccount");
                    String destAcc = data.get("destAccount");
                    double amount = parseDouble(data.get("amount"), 0.0);
                    String memo = data.get("referenceNote");

                    Map<String, Object> result = bankingService.transfer(sourceAcc, destAcc, amount, memo);
                    sendJsonResponse(exchange, 200, result);

                } else if (path.equals("/api/batch/quarter-end") && "POST".equalsIgnoreCase(method)) {
                    List<Map<String, Object>> result = bankingService.executeQuarterEndServicing();
                    sendJsonResponse(exchange, 200, result);

                } else if (path.startsWith("/api/ledger/") && "GET".equalsIgnoreCase(method)) {
                    String accNo = path.substring("/api/ledger/".length());
                    Map<String, Object> passbook = bankingService.getPassbookStatement(accNo);
                    sendJsonResponse(exchange, 200, passbook);

                } else if (path.equals("/api/analytics") && "GET".equalsIgnoreCase(method)) {
                    Map<String, Object> metrics = analyticsService.getPortfolioMetrics();
                    sendJsonResponse(exchange, 200, metrics);

                } else if (path.equals("/api/audit-logs") && "GET".equalsIgnoreCase(method)) {
                    List<AuditRecord> logs = bankingService.getAuditLogs(100);
                    sendJsonResponse(exchange, 200, logs);

                } else if (path.startsWith("/api/beneficiaries/") && "GET".equalsIgnoreCase(method)) {
                    String accNo = path.substring("/api/beneficiaries/".length());
                    List<Beneficiary> benList = bankingService.getBeneficiaries(accNo);
                    sendJsonResponse(exchange, 200, benList);

                } else if (path.equals("/api/beneficiaries") && "POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = JsonHelper.parseSimpleJson(body);
                    Beneficiary b = new Beneficiary(
                            data.get("sourceAccount"),
                            data.get("beneficiaryAccount"),
                            data.get("beneficiaryName"),
                            data.get("bankIfsc"),
                            parseDouble(data.get("maxLimit"), 50000.0)
                    );
                    boolean ok = bankingService.addBeneficiary(b);
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("success", ok);
                    sendJsonResponse(exchange, 201, resp);

                } else if (path.equals("/api/sql/query") && "POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = JsonHelper.parseSimpleJson(body);
                    String sql = data.get("sql");
                    Map<String, Object> queryResult = DatabaseManager.executeArbitraryQuery(sql != null ? sql : "SELECT 1;");
                    sendJsonResponse(exchange, 200, queryResult);

                } else if (path.equals("/api/system/reset") && "POST".equalsIgnoreCase(method)) {
                    bankingService.resetBenchmarkData();
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("success", true);
                    resp.put("message", "Database successfully restored to Experiment 11 benchmark seed data.");
                    sendJsonResponse(exchange, 200, resp);

                } else {
                    sendError(exchange, 404, "Endpoint not found: " + path);
                }
            } catch (BankingException be) {
                sendDomainError(exchange, 400, be.getErrorCode(), be.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                sendError(exchange, 500, "Internal Server Fault: " + ex.getMessage());
            }
        }
    }

    // --- STATIC ASSET FILE HANDLER ---
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Map URL to project/frontend directory
            File file = new File("project/frontend" + path);
            if (!file.exists()) {
                // Try relative fallback
                file = new File("frontend" + path);
            }

            if (file.exists() && !file.isDirectory()) {
                String mimeType = getMimeType(file.getName());
                exchange.getResponseHeaders().set("Content-Type", mimeType);
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String notFound = "<h1>404 Not Found</h1><p>Resource " + path + " does not exist.</p>";
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(404, notFound.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = JsonHelper.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendDomainError(HttpExchange exchange, int statusCode, String code, String message) throws IOException {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("success", false);
        err.put("errorCode", code);
        err.put("errorMessage", message);
        sendJsonResponse(exchange, statusCode, err);
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("success", false);
        err.put("error", message);
        sendJsonResponse(exchange, statusCode, err);
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private double parseDouble(String s, double fallback) {
        if (s == null) return fallback;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (lower.endsWith(".json")) return "application/json; charset=utf-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }
}
