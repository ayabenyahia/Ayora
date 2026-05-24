package com.ayora.assistant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpSession;

/**
 * Session-backed conversation history.
 *
 * <p>Each user's recent turns are kept on the {@link HttpSession} (not
 * in a database) — when she logs out or her session expires the
 * history is gone. That's intentional: lightweight, multi-tab safe,
 * GDPR-friendly.
 *
 * <p>Capped at {@link #MAX_TURNS} turns (user + assistant alternating)
 * so the prompt stays bounded and Gemini's context window doesn't fill
 * up after a long demo.
 */
public final class AssistantConversationStore {

	public static final int MAX_TURNS = 12;
	private static final String SESSION_ATTR = "ayora.assistant.history";

	/** Append a user turn followed (later) by an assistant turn. */
	public void appendUser(HttpSession session, String text) {
		append(session, "user", text);
	}

	public void appendAssistant(HttpSession session, String text) {
		append(session, "assistant", text);
	}

	private synchronized void append(HttpSession session, String role, String text) {
		if (session == null || text == null || text.trim().isEmpty()) return;
		List<AssistantPromptRequest.Turn> list = read(session);
		list.add(new AssistantPromptRequest.Turn(role, text));
		// Drop oldest until we're inside the cap.
		while (list.size() > MAX_TURNS) list.remove(0);
		session.setAttribute(SESSION_ATTR, list);
	}

	/** Read a defensive copy of the current history. */
	@SuppressWarnings("unchecked")
	public List<AssistantPromptRequest.Turn> read(HttpSession session) {
		if (session == null) return new ArrayList<AssistantPromptRequest.Turn>();
		Object o = session.getAttribute(SESSION_ATTR);
		if (o instanceof List) return new ArrayList<AssistantPromptRequest.Turn>((List<AssistantPromptRequest.Turn>) o);
		return new ArrayList<AssistantPromptRequest.Turn>();
	}

	/** Drop everything — invoked by {@code POST /api/assistant/reset}. */
	public void reset(HttpSession session) {
		if (session == null) return;
		session.removeAttribute(SESSION_ATTR);
	}

	public int size(HttpSession session) {
		return read(session).size();
	}

	/** Last N turns, oldest first. Used to build the prompt's history block. */
	public List<AssistantPromptRequest.Turn> tail(HttpSession session, int n) {
		List<AssistantPromptRequest.Turn> all = read(session);
		if (all.size() <= n) return all;
		return new ArrayList<AssistantPromptRequest.Turn>(all.subList(all.size() - n, all.size()));
	}
}
