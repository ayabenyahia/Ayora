package com.ayora.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logger minimal Ayora base sur java.util.logging (stdlib Java, aucune
 * dependance externe).
 *
 * Avantages vs System.out.println :
 *   - niveau (DEBUG / INFO / WARN / ERROR)
 *   - timestamp ISO 8601 automatique
 *   - stack-trace propre sur exception
 *   - meme handler central, configurable
 *
 * Utilisation :
 *   private static final Log log = Log.of(MyClass.class);
 *   log.info("event {0} pour user {1}", action, userId);
 *   log.error("Echec migration", e);
 */
public final class Log {

	private static volatile boolean configured = false;

	private final Logger logger;

	private Log(Logger l) { this.logger = l; }

	public static Log of(Class<?> c) {
		configure();
		return new Log(Logger.getLogger(c.getName()));
	}

	public static Log of(String name) {
		configure();
		return new Log(Logger.getLogger(name));
	}

	public void debug(String msg, Object... args) { if (logger.isLoggable(Level.FINE)) logger.log(Level.FINE, format(msg, args)); }
	public void info(String msg, Object... args)  { logger.log(Level.INFO, format(msg, args)); }
	public void warn(String msg, Object... args)  { logger.log(Level.WARNING, format(msg, args)); }
	public void error(String msg, Throwable t)    { logger.log(Level.SEVERE, msg, t); }
	public void error(String msg, Object... args) { logger.log(Level.SEVERE, format(msg, args)); }

	private static String format(String msg, Object... args) {
		if (args == null || args.length == 0) return msg;
		String out = msg;
		for (int i = 0; i < args.length; i++) {
			out = out.replace("{" + i + "}", String.valueOf(args[i]));
		}
		return out;
	}

	/**
	 * Configure le root logger pour une sortie console concise et lisible.
	 * Synchronise + double-check pour eviter la reconfiguration multiple.
	 */
	private static synchronized void configure() {
		if (configured) return;
		Logger root = Logger.getLogger("");
		for (Handler h : root.getHandlers()) root.removeHandler(h);
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		ch.setFormatter(new Formatter() {
			@Override public String format(LogRecord r) {
				String ts = java.time.LocalDateTime.now().toString();
				String level = abbrev(r.getLevel());
				String src = r.getSourceClassName();
				if (src != null && src.contains(".")) src = src.substring(src.lastIndexOf('.') + 1);
				String msg = formatMessage(r);
				StringBuilder sb = new StringBuilder(160);
				sb.append(ts).append(' ').append(level).append(' ').append(src).append(" - ").append(msg).append('\n');
				if (r.getThrown() != null) {
					java.io.StringWriter sw = new java.io.StringWriter();
					r.getThrown().printStackTrace(new java.io.PrintWriter(sw));
					sb.append(sw.toString());
				}
				return sb.toString();
			}
		});
		root.addHandler(ch);
		root.setLevel(Level.INFO);
		configured = true;
	}

	private static String abbrev(Level l) {
		if (l == Level.SEVERE)  return "ERROR";
		if (l == Level.WARNING) return "WARN ";
		if (l == Level.INFO)    return "INFO ";
		if (l == Level.FINE)    return "DEBUG";
		return l.getName();
	}
}
