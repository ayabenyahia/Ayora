package com.ayora.dao;

import java.util.List;

/**
 * Interface DAO generique alignee sur le pattern du cours p02-jee
 * (le prof definit AuthorDao puis fournit AuthorDaoJdbc et AuthorDaoMemory).
 *
 * Les DAO existants (UserDao, VendorDao, ...) restent compatibles : on peut
 * progressivement leur faire implementer cette interface sans casser le
 * code existant. C'est un marqueur architectural pour formaliser la couche
 * d'acces aux donnees.
 *
 * @param <T> type de l'entite
 * @param <K> type de la cle primaire (en general Integer)
 */
public interface Dao<T, K> {

	/** Recupere une entite par sa cle primaire, ou null si introuvable. */
	T findById(K id);

	/** Liste toutes les entites. */
	List<T> findAll();

	/** Cree une nouvelle entite, retourne la cle generee (ou -1 en cas d'erreur). */
	int create(T entity);

	/** Met a jour une entite, retourne true si une ligne a ete modifiee. */
	boolean update(T entity);

	/** Supprime une entite par sa cle, retourne true si une ligne a ete supprimee. */
	boolean delete(K id);
}
