package com.sbtms.web;

import com.sbtms.model.Account;
import com.sbtms.model.AuditRecord;
import com.sbtms.model.Beneficiary;
import com.sbtms.model.Transaction;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON serialization and parsing utility.
 * Guarantees 100% portability without external library bloat (Jackson/Gson).
 * 
 * @author Anish Vyapari (Roll No: 25CA1012 | PRN: DY25ENGU0AIM012 | Batch: A/A1)
 */
public class JsonHelper {

    public static String toJson(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return String.valueOf(obj);
        }
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection) {
            Collection<?> col = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj.getClass().isArray()) {
            Object[] arr = (Object[]) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : arr) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Transaction) {
            Transaction t = (Transaction) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("txnId", t.getTxnId());
            map.put("accountNumber", t.getAccountNumber());
            map.put("txnType", t.getTxnType());
            map.put("amount", t.getAmount());
            map.put("balanceAfter", t.getBalanceAfter());
            map.put("referenceNote", t.getReferenceNote());
            map.put("counterpartyAccount", t.getCounterpartyAccount());
            map.put("timestamp", t.getTimestamp());
            return toJson(map);
        }
        if (obj instanceof Beneficiary) {
            Beneficiary b = (Beneficiary) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", b.getId());
            map.put("sourceAccount", b.getSourceAccount());
            map.put("beneficiaryAccount", b.getBeneficiaryAccount());
            map.put("beneficiaryName", b.getBeneficiaryName());
            map.put("bankIfsc", b.getBankIfsc());
            map.put("maxLimit", b.getMaxLimit());
            map.put("addedAt", b.getAddedAt());
            return toJson(map);
        }
        if (obj instanceof AuditRecord) {
            AuditRecord ar = (AuditRecord) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("logId", ar.getLogId());
            map.put("action", ar.getAction());
            map.put("accountNumber", ar.getAccountNumber());
            map.put("details", ar.getDetails());
            map.put("status", ar.getStatus());
            map.put("ipAddress", ar.getIpAddress());
            map.put("timestamp", ar.getTimestamp());
            return toJson(map);
        }
        if (obj instanceof Account) {
            Account acc = (Account) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("accountNumber", acc.getAccountNumber());
            map.put("holderName", acc.getHolderName());
            map.put("email", acc.getEmail());
            map.put("phone", acc.getPhone());
            map.put("accountType", acc.getAccountType());
            map.put("balance", acc.getBalance());
            map.put("availableFunds", acc.getAvailableFunds());
            map.put("status", acc.getStatus());
            map.put("createdAt", acc.getCreatedAt());
            return toJson(map);
        }

        return "\"" + escape(obj.toString()) + "\"";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Parses a flat or simple JSON payload into a key-value Map.
     */
    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1).trim();
        }
        if (json.isEmpty()) return map;

        // Tokenize comma-separated key-value pairs (respecting strings)
        List<String> pairs = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (c == ',' && !inQuotes) {
                pairs.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) {
            pairs.add(cur.toString().trim());
        }

        for (String pair : pairs) {
            int colonIdx = pair.indexOf(':');
            if (colonIdx > 0) {
                String k = pair.substring(0, colonIdx).trim();
                String v = pair.substring(colonIdx + 1).trim();
                if (k.startsWith("\"") && k.endsWith("\"")) {
                    k = k.substring(1, k.length() - 1);
                }
                if (v.startsWith("\"") && v.endsWith("\"")) {
                    v = v.substring(1, v.length() - 1);
                }
                map.put(k, v);
            }
        }
        return map;
    }
}
