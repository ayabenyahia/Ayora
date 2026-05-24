package com.ayora.dao;

/**
 * Interface DAO principale de l'application.
 *
 * Toutes les classes DAO de l'application (UserDao, VendorDao, GuestDao,
 * InvitationDao, QuestionnaireDao, RecommendationDao, SubscriptionDao,
 * UserPickDao, DevisDao, RendezVousDao, AdminStatsDao) implementent cette
 * interface.
 *
 * Convention : chaque classe DAO recoit un Database par constructeur et
 * utilise ses methodes (queryList, queryOne, queryInt, executeUpdate,
 * insertReturningKey, ...) pour ne pas dupliquer le boilerplate JDBC.
 *
 * Les operations specifiques a chaque entite (findByUserId, search, etc.)
 * sont declarees directement sur la classe concrete : pas d'interface DAO
 * separee par entite.
 */
public interface IDao {
}
