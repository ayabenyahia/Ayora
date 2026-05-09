package com.ayora.model;

public class Subscription {

	public static final String PLAN_FREE = "FREE";
	public static final String PLAN_PRO = "PRO";
	public static final String PLAN_PREMIUM = "PREMIUM";

	private int id;
	private int userId;
	private String plan;
	private int invitationsSent;
	private int maxInvitationsFree;

	public Subscription() {
		this.plan = PLAN_FREE;
		this.invitationsSent = 0;
		this.maxInvitationsFree = 10;
	}

	public Subscription(int id, int userId, String plan, int invitationsSent) {
		super();
		this.id = id;
		this.userId = userId;
		this.plan = plan;
		this.invitationsSent = invitationsSent;
		this.maxInvitationsFree = 10;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public int getUserId() { return userId; }
	public void setUserId(int userId) { this.userId = userId; }

	public String getPlan() { return plan; }
	public void setPlan(String plan) { this.plan = plan; }

	public int getInvitationsSent() { return invitationsSent; }
	public void setInvitationsSent(int invitationsSent) { this.invitationsSent = invitationsSent; }

	public int getMaxInvitationsFree() { return maxInvitationsFree; }
	public void setMaxInvitationsFree(int maxInvitationsFree) { this.maxInvitationsFree = maxInvitationsFree; }

	// Hierarchie : FREE < PRO < PREMIUM
	private int rankOf(String p) {
		if (PLAN_PREMIUM.equals(p)) return 2;
		if (PLAN_PRO.equals(p)) return 1;
		return 0;
	}

	public int getMaxInvitationsAllowed() {
		if (PLAN_PREMIUM.equals(plan)) return -1; // illimite
		if (PLAN_PRO.equals(plan)) return 50;
		return maxInvitationsFree;
	}

	public boolean canSendInvitation() {
		if (PLAN_PREMIUM.equals(plan)) return true;
		int max = getMaxInvitationsAllowed();
		return invitationsSent < max;
	}

	public int getRemainingInvitations() {
		if (PLAN_PREMIUM.equals(plan)) return -1;
		return getMaxInvitationsAllowed() - invitationsSent;
	}

	// Garde l'ancienne API
	public int getRemainingFreeInvitations() {
		return getRemainingInvitations();
	}

	/**
	 * Verifie qu'un utilisateur peut utiliser un template d'un certain niveau.
	 * FREE -> seulement FREE
	 * PRO -> FREE et PRO
	 * PREMIUM -> tous
	 */
	public boolean canUseTemplateLevel(String requiredLevel) {
		if (requiredLevel == null) return true;
		return rankOf(plan) >= rankOf(requiredLevel.toUpperCase());
	}

	@Override
	public String toString() {
		return "Subscription [userId=" + userId + ", plan=" + plan + ", sent=" + invitationsSent + "]";
	}
}
