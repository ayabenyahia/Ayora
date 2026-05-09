package com.ayora.model;

public class VendorCategory {

	private int id;
	private String name;
	private String nameFr;
	private String description;
	private String icon;

	public VendorCategory() {
	}

	public VendorCategory(int id, String name, String nameFr, String description) {
		super();
		this.id = id;
		this.name = name;
		this.nameFr = nameFr;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameFr() {
		return nameFr;
	}

	public void setNameFr(String nameFr) {
		this.nameFr = nameFr;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public String toString() {
		return id + ". " + nameFr + " (" + name + ")";
	}
}
