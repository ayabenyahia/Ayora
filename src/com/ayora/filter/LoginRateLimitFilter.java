package com.ayora.filter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * Filter anti brute-force pour /api/auth/login.
 *
 * Regle : si une IP cumule >= MAX_FAILS echecs (HTTP 401) dans la fenetre
 * WINDOW_MS, les requetes suivantes sont bloquees pendant LOCK_MS.
 *
 * Stockage : ConcurrentHashMap en memoire (suffit pour un nœud unique
 * Tomcat). Pour une vraie infra multi-nœuds : a deplacer dans Redis ou
 * une table SQL. Ici on reste fidele a la methode du prof : zero dep
 * externe, code Java pur.
 */
@WebFilter(urlPatterns = {"/api/auth/login"})
public class LoginRateLimitFilter implements Filter {

	private static final int  MAX_FAILS = 5;
	private static final long WINDOW_MS = 15 * 60_000L;  // 15 minutes
	private static final long LOCK_MS   = 15 * 60_000L;  // verrou 15 minutes

	private static final ConcurrentHashMap<String, Bucket> BUCKETS = new ConcurrentHashMap<String, Bucket>();

	private static final class Bucket {
		volatile int fails;
		volatile long firstFailMs;
		volatile long lockedUntilMs;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) req;
		HttpServletResponse httpRes = (HttpServletResponse) res;
		String ip = clientIp(httpReq);
		long now = System.currentTimeMillis();

		Bucket b = BUCKETS.get(ip);
		if (b != null && b.lockedUntilMs > now) {
			long retrySec = (b.lockedUntilMs - now) / 1000;
			httpRes.setHeader("Retry-After", String.valueOf(retrySec));
			httpRes.setStatus(429);
			httpRes.setContentType("application/json; charset=utf-8");
			httpRes.getWriter().write("{\"error\":\"Trop de tentatives. Reessayez dans "
				+ Math.max(1, retrySec / 60) + " minutes.\"}");
			return;
		}

		// On wrappe la response pour capturer le status final
		StatusCapturingResponse wrapped = new StatusCapturingResponse(httpRes);
		chain.doFilter(req, wrapped);
		int status = wrapped.getStatus();

		if (status == 401) {
			b = BUCKETS.computeIfAbsent(ip, k -> new Bucket());
			synchronized (b) {
				if (now - b.firstFailMs > WINDOW_MS) {
					b.firstFailMs = now;
					b.fails = 1;
				} else {
					b.fails++;
				}
				if (b.fails >= MAX_FAILS) {
					b.lockedUntilMs = now + LOCK_MS;
				}
			}
		} else if (status >= 200 && status < 300) {
			// Login reussi : on reset le bucket de cette IP
			BUCKETS.remove(ip);
		}
	}

	private static String clientIp(HttpServletRequest req) {
		String xff = req.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isEmpty()) {
			int c = xff.indexOf(',');
			return (c > 0 ? xff.substring(0, c) : xff).trim();
		}
		return req.getRemoteAddr();
	}

	/** Wrapper pour lire le status final apres l'execution du servlet. */
	private static final class StatusCapturingResponse extends HttpServletResponseWrapper {
		private int statusCode = 200;
		StatusCapturingResponse(HttpServletResponse r) { super(r); }
		@Override public void setStatus(int sc) { this.statusCode = sc; super.setStatus(sc); }
		@Override public void sendError(int sc) throws IOException { this.statusCode = sc; super.sendError(sc); }
		@Override public void sendError(int sc, String m) throws IOException { this.statusCode = sc; super.sendError(sc, m); }
		@Override public int getStatus() { return statusCode; }
	}
}
