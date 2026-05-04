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

	// Joined fields (depuis vendors lors du SELECT JOIN dans le DAO)
	private String vendorName;
	private String vendorCategory;
	private String vendorGamme;
	private double vendorPrixMin;
	private String vendorCity;
	private String vendorPhone;
	private String vendorInstagram;
	private String vendorDescription;

	public UserPick() {}

	public UserPick(int userId, int vendorId, int categoryId) {
		this.userId = userId;
		this.vendorId = vendorId;
		this.categoryId = categoryId;
	}
}
