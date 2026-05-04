package com.ayora.model;

/**
 * Choix d'un prestataire par la mariee parmi ses recommandations.
 *
 * Une seule selection par categorie : la mariee retient UN salle,
 * UN traiteur, UN photographe, etc.
 */
public class UserPick {

	private int id;
	private int userId;
	private int vendorId;
	private int categoryId;
	private String pickedAt;

	public UserPick() {}
}
