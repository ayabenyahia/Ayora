package com.ayora.model;

public class Subscription {

	private int id;
	private int userId;
	private String plan;
	private int invitationsSent;
	private int maxInvitationsFree;

	public Subscription() {
		this.plan = "FREE";
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

	public int getInvitationsSent() {
		return invitationsSent;
	}

	public void setInvitationsSent(int invitationsSent) {
		this.invitationsSent = invitationsSent;
	}

	public int getMaxInvitationsFree() {
		return maxInvitationsFree;
	}

	public void setMaxInvitationsFree(int maxInvitationsFree) {
		this.maxInvitationsFree = maxInvitationsFree;
	}

	public boolean canSendInvitation() {
		if (plan.equals("PREMIUM")) {
			return true;
		}
		return invitationsSent < maxInvitationsFree;
	}

	public int getRemainingFreeInvitations() {
		if (plan.equals("PREMIUM")) {
			return -1;
		}
		return maxInvitationsFree - invitationsSent;
	}

	@Override
	public String toString() {
		return "Subscription [userId=" + userId + ", plan=" + plan + ", sent=" + invitationsSent + "]";
	}
}
