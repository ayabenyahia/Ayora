package com.ayora.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsonUtil {

	public JsonUtil() {
	}

	public static String readRequestBody(HttpServletRequest request) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	public static void sendJson(HttpServletResponse response, String json) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.print(json);
		out.flush();
	}

	public static void sendError(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		sendJson(response, "{\"error\":\"" + escapeJson(message) + "\"}");
	}

	public static void sendSuccess(HttpServletResponse response, String message) throws IOException {
		sendJson(response, "{\"success\":true,\"message\":\"" + escapeJson(message) + "\"}");
	}

	public static String escapeJson(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("\\", "\\\\")
				   .replace("\"", "\\\"")
				   .replace("\n", "\\n")
				   .replace("\r", "\\r")
				   .replace("\t", "\\t");
	}

	public static String getStringValue(String json, String key) {
		String search = "\"" + key + "\"";
		int keyIndex = json.indexOf(search);
		if (keyIndex == -1) {
			return null;
		}
		int colonIndex = json.indexOf(":", keyIndex + search.length());
		if (colonIndex == -1) {
			return null;
		}
		int startQuote = json.indexOf("\"", colonIndex + 1);
		if (startQuote == -1) {
			return null;
		}
		int endQuote = json.indexOf("\"", startQuote + 1);
		while (endQuote > 0 && json.charAt(endQuote - 1) == '\\') {
			endQuote = json.indexOf("\"", endQuote + 1);
		}
		if (endQuote == -1) {
			return null;
		}
		return json.substring(startQuote + 1, endQuote);
	}

	public static int getIntValue(String json, String key) {
		String search = "\"" + key + "\"";
		int keyIndex = json.indexOf(search);
		if (keyIndex == -1) {
			return 0;
		}
		int colonIndex = json.indexOf(":", keyIndex + search.length());
		if (colonIndex == -1) {
			return 0;
		}
		int start = colonIndex + 1;
		while (start < json.length() && json.charAt(start) == ' ') {
			start++;
		}
		int end = start;
		while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
			end++;
		}
		if (start == end) {
			return 0;
		}
		try {
			return Integer.parseInt(json.substring(start, end));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static double getDoubleValue(String json, String key) {
		String search = "\"" + key + "\"";
		int keyIndex = json.indexOf(search);
		if (keyIndex == -1) {
			return 0;
		}
		int colonIndex = json.indexOf(":", keyIndex + search.length());
		if (colonIndex == -1) {
			return 0;
		}
		int start = colonIndex + 1;
		while (start < json.length() && json.charAt(start) == ' ') {
			start++;
		}
		int end = start;
		while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
			end++;
		}
		if (start == end) {
			return 0;
		}
		try {
			return Double.parseDouble(json.substring(start, end));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static boolean getBooleanValue(String json, String key) {
		String search = "\"" + key + "\"";
		int keyIndex = json.indexOf(search);
		if (keyIndex == -1) {
			return false;
		}
		int colonIndex = json.indexOf(":", keyIndex + search.length());
		if (colonIndex == -1) {
			return false;
		}
		String rest = json.substring(colonIndex + 1).trim();
		return rest.startsWith("true");
	}
}
