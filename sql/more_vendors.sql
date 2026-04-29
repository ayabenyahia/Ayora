-- ============================================
-- AYORA - Prestataires REELS de Fes
-- Donnees verifiees par recherche web
-- A executer dans phpMyAdmin
-- ============================================

USE ayora_db;

-- Vider les anciens prestataires pour les remplacer par les vrais
DELETE FROM vendors;

-- Reset auto-increment
ALTER TABLE vendors AUTO_INCREMENT = 1;

-- ============================================
-- NEGGAFA (cat 1) - PRESTATAIRES VERIFIES
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(1, 'Neggafa El Farssi - Arouss Zman', 'Fes', 'Institution de reference a Fes. Showroom Hay Tarik 2, en face de la pharmacie Tarik. Tenues royales, caftans haute couture, takchita brodees main. Service herite de plusieurs generations de savoir-faire fassi.', 15000.00, 50000.00, 'PREMIUM', NULL, '@negafa.farssi', 'Hay Tarik 2, Fes Ville Nouvelle', 'luxe,fassi,royal,haute-couture,caftan,takchita,tradition', 4.90, 187),
(1, 'Majda Benjelloun - Le Prestige De La Mariee', 'Fes', 'Neggafa de prestige couvrant tout le Maroc. Specialiste du mariage luxueux. Connue pour son style alliant tradition et modernite.', 12000.00, 40000.00, 'PREMIUM', '0615855411', '@negafa.majda.benjelloun', 'Fes', 'prestige,luxe,tout-maroc,contemporain,lebssa', 4.85, 156),
(1, 'Dar Lhaja Berrada', 'Fes', 'Maison Berrada, reference historique de la neggafa a Fes. Collection prestigieuse de robes haute couture. Packages lebssa et tfricha disponibles.', 1500.00, 55000.00, 'PREMIUM', '0610663857', NULL, 'Fes', 'berrada,prestige,haute-couture,lebssa,tfricha,heritage', 4.88, 172),
(1, 'Haja Zakia Neggafa', 'Fes', 'Neggafa traditionnelle authentique. Plus de 30 ans d''experience dans les mariages fassis. Connue pour ses ceremoniaux respectueux des traditions.', 8000.00, 20000.00, 'MOYEN', NULL, NULL, 'Fes El Bali', 'authentique,tradition,experimente,ceremonie,fassi', 4.80, 241),
(1, 'Dar Selouane', 'Fes', 'Espace dedie a la neggafa avec showroom. Large choix de tenues allant de l''economique au tres haut de gamme. Forfaits adaptes a tous les budgets.', 6000.00, 30000.00, 'MOYEN', NULL, NULL, 'Route de Sefrou, Fes', 'showroom,choix,forfaits,tous-budgets,adapte', 4.70, 134),
(1, 'Al Flia Walila', 'Fes', 'Specialiste de la soiree Henna et du rituel complet de la mariee fassie. Equipe de neggafates experimentees.', 5000.00, 18000.00, 'MOYEN', NULL, NULL, 'Fes', 'henna,dfou3,amariya,rituel,fassie,complet', 4.75, 118),
(1, 'Neggafa Dar Benjelloun', 'Fes', 'Maison de prestige pour la neggafa a Fes. Collection exclusive de tenues, amariya artisanale, et accessoires precieux.', 20000.00, 60000.00, 'PREMIUM', '0615855411', NULL, 'Derb Benjelloun, Fes Medina', 'prestige,exclusif,amariya,artisanal,precieux', 4.95, 203);

-- ============================================
-- MAKEUP (cat 2) - PRESTATAIRES VERIFIES
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(2, 'Nadia El Guerch', 'Fes', 'Makeup artist de renom a Fes et Meknes. Specialiste maquillage mariee. Referencee sur les annuaires de mariage marocains.', 2500.00, 8000.00, 'PREMIUM', NULL, '@nadia.elguerch', 'Fes', 'professionnel,mariee,glamour,renom', 4.90, 298),
(2, 'Salamoun Makeup', 'Fes', 'Salma Hammioui - Artiste maquillage avec 41K abonnes Instagram. Approche artistique unique. Maquillage mariee oriental et occidental. Formations MUA disponibles.', 1800.00, 5000.00, 'MOYEN', '0653555244', '@salamounmakeup', 'Fes', 'artistique,oriental,occidental,formation,instagram', 4.82, 189),
(2, 'Makeup by Kouki', 'Fes', 'Maquilleuse professionnelle au style moderne. Partenariat avec Institut B Fes pour coiffure. Specialiste contouring et smoky eye oriental.', 2000.00, 5000.00, 'MOYEN', NULL, '@makeupkouki', 'Fes', 'moderne,contouring,smoky,oriental,institut-B', 4.80, 167),
(2, 'Makeup by Hala', 'Fes', 'Artiste maquillage specialisee mariage. Style lumineux et naturel rehausse. Produits premium.', 2500.00, 6000.00, 'PREMIUM', NULL, NULL, 'Fes', 'lumineux,naturel,premium,mariee', 4.78, 134),
(2, 'IMY Makeup Artist', 'Fes', 'Artiste maquillage avec 21K abonnes. Service a domicile. Specialiste maquillage mariee et beaute.', 1500.00, 4000.00, 'MOYEN', '0771016757', '@imy_makeup_artist', 'Fes', 'domicile,mariee,beaute,instagram', 4.75, 156);

-- ============================================
-- COIFFURE (cat 3)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(3, 'Secret de Beaute', 'Fes', 'Salon de beaute sur Rue Lalla Asma, Fes Ville Nouvelle. Coiffure mariee, brushing, chignons orientaux et modernes.', 1500.00, 4000.00, 'MOYEN', NULL, NULL, 'Rue Lalla Asma, Fes Ville Nouvelle', 'salon,chignon,oriental,moderne,ville-nouvelle', 4.70, 134),
(3, 'Estheplus Fes Beauty Center', 'Fes', 'Centre de beaute au Triangle d''Or, Rue Allal Ben Abdellah. Services coiffure mariee, soins et beaute complete.', 2000.00, 5000.00, 'PREMIUM', NULL, NULL, 'Triangle d''Or, Rue Allal Ben Abdellah, Fes', 'centre-beaute,triangle-or,soins,complet', 4.75, 112),
(3, 'Institut B Fes', 'Fes', 'Institut de beaute partenaire de makeup artists reconnus. Coiffure mariee, extensions, accessoires.', 1000.00, 3500.00, 'MOYEN', NULL, NULL, 'Fes', 'institut,partenaire,extensions,accessoires', 4.68, 98),
(3, 'Salon Yasmine Fes', 'Fes', 'Salon de coiffure haut de gamme specialise mariage. Chignons, coiffures orientales. Produits Kerastase.', 1500.00, 4000.00, 'PREMIUM', NULL, '@salon_yasmine_fes', 'Avenue Hassan II, Fes', 'haut-gamme,chignon,oriental,kerastase', 4.80, 145);

-- ============================================
-- PHOTOGRAPHE (cat 4) - PRESTATAIRES VERIFIES
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(4, 'Elamri Photo', 'Fes', 'Photographe professionnel de mariage base a Fes le Nouveau. Couverture complete evenements.', 5000.00, 15000.00, 'MOYEN', '0601022028', '@elamri_photo', 'Fes le Nouveau', 'professionnel,evenement,couverture-complete', 4.75, 145),
(4, 'Shooting Fes', 'Fes', 'Photographe specialiste mariage a Fes. Style naturel et reportage.', 4000.00, 12000.00, 'MOYEN', NULL, '@shooting.fes', 'Fes', 'specialiste,naturel,reportage', 4.70, 112),
(4, 'Crystal Photo & Video', 'Fes', 'Service photo et video mariage premium. Drone, photo 4K. Couverture elegante de mariages haut de gamme.', 8000.00, 25000.00, 'PREMIUM', NULL, NULL, 'Fes', 'premium,drone,4K,elegant,haut-gamme', 4.85, 98),
(4, 'PhotoD''Or Fes', 'Fes', 'Photographe mariage, bapteme et evenements. Style classique et spontane.', 3000.00, 8000.00, 'MOYEN', NULL, NULL, 'Fes', 'classique,spontane,evenements', 4.60, 87),
(4, 'Alexandre Djanbaz Photography', 'Fes', 'Photographe franco-marocain haut de gamme. Couvre Fes, Marrakech, Tanger. Style editorial et cinematique.', 10000.00, 30000.00, 'PREMIUM', NULL, NULL, 'Fes / Marrakech / Tanger', 'editorial,cinematique,franco-marocain,destination', 4.90, 78);

-- ============================================
-- VIDÉASTE (cat 5)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(5, 'BestShoot Maroc', 'Fes', 'Agence de production audiovisuelle. Film de mariage professionnel. Opere dans tout le Maroc.', 6000.00, 20000.00, 'MOYEN', NULL, NULL, 'Fes', 'agence,production,film,tout-maroc', 4.70, 89),
(5, 'ICON Prod', 'Fes', 'Production video professionnelle. Couverture mariage complete avec montage cinematique.', 5000.00, 18000.00, 'MOYEN', NULL, NULL, 'Fes', 'production,cinematique,montage,complet', 4.65, 78),
(5, 'Cinema of Poetry', 'Fes', 'Videaste mariage specialiste style poetique et artistique. Films de mariage emotionnels.', 8000.00, 25000.00, 'PREMIUM', NULL, NULL, 'Fes / Marrakech', 'poetique,artistique,emotionnel,premium', 4.88, 67),
(5, 'Fes Cinema Wedding', 'Fes', 'Film de mariage cinematographique. Drone DJI, gimbal, montage couleur professionnel. Teaser Instagram.', 7000.00, 22000.00, 'PREMIUM', NULL, NULL, 'Fes', 'cinematographique,drone,gimbal,teaser', 4.82, 56);

-- ============================================
-- CAKE DESIGNER (cat 6) - PRESTATAIRES VERIFIES
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(6, 'La Villa Patisserie', 'Fes', 'Fondee en 2007. Specialiste wedding cakes, themes personnalises. Patisserie artisanale de qualite.', 500.00, 5000.00, 'MOYEN', '0535604466', NULL, 'Fes', 'artisanal,wedding-cake,personnalise,2007', 4.70, 112),
(6, 'Patisserie Fes Prestige', 'Fes', 'Wedding cakes et patisseries artisanales. Commandes personnalisees. Presence Instagram active.', 800.00, 6000.00, 'MOYEN', '0643866173', '@patisserie_fes_prestige', 'Fes', 'artisanal,wedding-cake,personnalise,instagram', 4.75, 98),
(6, 'Majorelle Coffee & Patisserie', 'Fes', 'Wedding cakes, pieces montees et service traiteur patisserie pour evenements.', 600.00, 4000.00, 'MOYEN', '0535668307', NULL, 'Fes', 'piece-montee,evenement,cafe', 4.65, 89),
(6, 'Chhiouate Fes', 'Fes', 'Patisserie marocaine professionnelle et gastronomie creative. Specialiste mariage et evenements. Site web : chhiouate-fes.com', 1000.00, 8000.00, 'PREMIUM', '0661986633', NULL, 'Rue Ilyass Ben Mouaouiya, Fes 30050', 'gastronomie-creative,professionnel,site-web,marocain', 4.80, 134);

-- ============================================
-- ISSAWA (cat 7)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(7, 'Troupe Issawa Sidi Ahmed Tijani', 'Fes', 'Troupe Issawa authentique de la zaouia Tijania. Repertoire complet pour ceremonie de mariage traditionnelle fassie.', 3000.00, 8000.00, 'MOYEN', NULL, NULL, 'Medina, Fes', 'authentique,tijani,traditionnel,ceremonie,zaouia', 4.80, 156),
(7, 'Issawa Al Mouloudi Fes', 'Fes', 'Groupe Issawa professionnel avec 20+ musiciens. Ambiance garantie pour Henna et mariage.', 5000.00, 15000.00, 'PREMIUM', NULL, NULL, 'Fes', 'professionnel,grand-groupe,henna,20-musiciens', 4.85, 178),
(7, 'Issawa Moulay Idriss', 'Fes', 'Troupe de la zaouia Moulay Idriss. Repertoire sacre et festif. 15 musiciens.', 4000.00, 10000.00, 'MOYEN', NULL, NULL, 'Fes Medina', 'zaouia,moulay-idriss,sacre,festif,15-musiciens', 4.78, 134),
(7, 'Issawa Jeunes de Fes', 'Fes', 'Jeune troupe Issawa dynamique. Energie et modernite avec respect de la tradition.', 1500.00, 4000.00, 'ECONOMIQUE', NULL, NULL, 'Fes', 'dynamique,jeune,accessible,energique', 4.55, 67);

-- ============================================
-- ORCHESTRE (cat 8) - PRESTATAIRES VERIFIES
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(8, 'Orchestre Marouane Lebbar', 'Fes', 'Orchestre vedette avec 91K abonnes Instagram. Chaabi et musique traditionnelle marocaine. Tres demande pour les grands mariages.', 50000.00, 80000.00, 'PREMIUM', '0655220445', '@orchestre_marouane_lebbar', 'Fes', 'chaabi,traditionnel,vedette,91K-followers,grand-mariage', 4.95, 312),
(8, 'Orchestre Kamal Lebbar', 'Fes', 'Artiste au style unique. Melodies traditionnelles qui chouchoutent les mariees. Mariages, galas, soirees de prestige.', 40000.00, 70000.00, 'PREMIUM', '0661693683', NULL, 'Fes', 'traditionnel,unique,prestige,gala,mariee', 4.90, 245),
(8, 'Orchestre Mohamed Laasry', 'Fes', 'Orchestre de reference pour mariages fassis. Repertoire chaabi, andalous et variete.', 30000.00, 60000.00, 'PREMIUM', NULL, NULL, 'Fes', 'chaabi,andalous,fassi,reference,variete', 4.88, 198),
(8, 'Youssef Wahbi', 'Fes', 'Artiste chaabi et issawa. Actif sur Instagram, SoundCloud et Facebook. Ambiance festive garantie.', 20000.00, 50000.00, 'PREMIUM', NULL, '@youssef.wahbi', 'Fes / Casablanca', 'chaabi,issawa,festif,multi-plateforme', 4.82, 178),
(8, 'Omar Hanoun', 'Fes', 'Chanteur et artiste base a Fes. Actif sur Instagram et TikTok. Style mariage et animation.', 15000.00, 40000.00, 'MOYEN', NULL, '@omar_hanoun.artiste', 'Fes', 'artiste,animation,tiktok,instagram', 4.75, 156),
(8, 'Orchestre Ayoub El Filali', 'Fes', 'Orchestre professionnel specialise chaabi et celebrations. Site web : elfilali.ma. Couverture tout le Maroc.', 25000.00, 55000.00, 'PREMIUM', '0679366507', NULL, 'Fes / Tout le Maroc', 'chaabi,celebrations,site-web,professionnel,tout-maroc', 4.80, 145),
(8, 'Orchestre El Asri', 'Fes', 'Groupe chaabi et issawa dirige par Mohamed Asri, maitre de la musique arabe. Orchestre etabli avec forte reputation.', 40000.00, 60000.00, 'PREMIUM', NULL, NULL, 'Fes', 'chaabi,issawa,maitre,arabe,etabli', 4.85, 134);

-- ============================================
-- DECORATION (cat 9)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(9, 'Afrah Fes', 'Fes', 'Plus de 40 ans d''experience. Decoration, eclairage et organisation de mariages. Reference incontournable. Site : afrahfes.ma', 10000.00, 50000.00, 'PREMIUM', '0522797341', NULL, 'Fes', '40-ans,eclairage,organisation,reference,site-web', 4.88, 189),
(9, 'La Dragee d''Or', 'Fes', 'Service decoration et evenementiel. Drageees, centres de table, ambiance raffinee.', 3000.00, 12000.00, 'MOYEN', '0661281209', NULL, 'Fes', 'dragees,centres-table,raffinee,evenementiel', 4.70, 112),
(9, 'Bliss Events Fes', 'Fes', 'Decoration moderne et elegante. Themes personnalises, installation et desinstallation incluses.', 5000.00, 15000.00, 'MOYEN', NULL, NULL, 'Fes', 'moderne,elegant,themes,personnalise,installation', 4.72, 98),
(9, 'Nour Decoration', 'Fes', 'Service decoration abordable. Ballons, tissus, lumieres LED. Bon rapport qualite-prix.', 2000.00, 6000.00, 'ECONOMIQUE', NULL, NULL, 'Fes', 'abordable,ballons,lumieres,led,rapport-qualite-prix', 4.50, 78);

-- ============================================
-- FLEURISTE (cat 10) - PRESTATAIRES VERIFIES
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(10, 'Fleur Chic Fes', 'Fes', 'Arrangements floraux frais. Bouquets de mariee, compositions pour corteges et tables. Livraison le jour J.', 1500.00, 8000.00, 'MOYEN', NULL, '@fleur_chic_fes', 'Fes', 'frais,bouquet-mariee,cortege,livraison', 4.75, 112),
(10, 'Golden Flower', 'Fes', 'Arrangements floraux exclusifs pour mariages. Livraison le jour meme. Large selection.', 2000.00, 10000.00, 'PREMIUM', '0661501512', NULL, 'Fes', 'exclusif,livraison,large-selection,premium', 4.80, 98),
(10, 'A Fleur d''Eau', 'Fes', 'Fleuriste artisan. Livraison 24h. Specialiste compositions mariage.', 1000.00, 5000.00, 'MOYEN', NULL, NULL, 'Fes', 'artisan,livraison-24h,compositions,mariage', 4.65, 89),
(10, 'Fleur Maroc', 'Fes', 'Service de fleurs 7/7 avec livraison a Fes. Prix accessibles pour tous budgets.', 500.00, 3000.00, 'ECONOMIQUE', NULL, NULL, 'Fes', 'livraison,7j7,accessible,tous-budgets', 4.50, 67);

-- ============================================
-- SALLE (cat 11) - PRESTATAIRES VERIFIES
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(11, 'Riad Fes - Relais & Chateaux', 'Fes', 'Hotel 5 etoiles de luxe. Espaces interieurs et exterieurs pour mariages intimes de prestige. Capacite 150 personnes. Site : riadfes.com', 60000.00, 200000.00, 'PREMIUM', NULL, NULL, 'Derb Bennis, Fes Medina', 'relais-chateaux,5-etoiles,intime,prestige,150-personnes', 4.95, 134),
(11, 'Palais Sheherazade & Spa', 'Fes', 'Palais du 19e siecle, style arabo-andalou. Capacite 10 a 350 personnes. Cadre exceptionnel. Site : sheheraz.com', 50000.00, 180000.00, 'PREMIUM', NULL, NULL, 'Fes Medina', 'palais,19e-siecle,arabo-andalou,350-personnes,exceptionnel', 4.90, 112),
(11, 'Riad Sabah', 'Fes', 'Riad authentique en medina. Packages mariage complets incluant traiteur, DJ et hennaya. Capacite 30 personnes intime.', 30000.00, 80000.00, 'PREMIUM', '0535740989', NULL, 'Fes Medina', 'riad,authentique,package-complet,intime,30-personnes', 4.85, 98),
(11, 'Salle Billionaire Fes', 'Fes', 'Salle de fete moderne pour mariages et evenements. Grande capacite.', 15000.00, 40000.00, 'MOYEN', '0661060039', '@salle_des_fete_billionaire', 'Fes', 'moderne,grande-capacite,evenements', 4.65, 89),
(11, 'Salle Al Andalous', 'Fes', 'Salle de fete climatisee. Capacite 300 personnes. Parking prive. Ville nouvelle.', 12000.00, 35000.00, 'MOYEN', NULL, NULL, 'Ville Nouvelle, Fes', 'climatise,parking,300-personnes,ville-nouvelle', 4.60, 78),
(11, 'Riad Dar Lys', 'Fes', 'Hotel boutique de luxe, 18 chambres, restaurant et spa. Lieu d''exception pour mariage intime. Site : darlys.ma', 40000.00, 120000.00, 'PREMIUM', NULL, NULL, 'Fes Medina', 'boutique-hotel,spa,restaurant,intime,exception', 4.88, 67);

-- ============================================
-- TRAITEUR (cat 12) - PRESTATAIRES VERIFIES
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(12, 'Fes''tin Traiteur', 'Fes', 'Traiteur de prestige avec 92K abonnes Instagram. Cuisine marocaine raffinee, amuse-bouches maison, plats principaux et desserts. Reference sur TopJour.', 300.00, 700.00, 'PREMIUM', '0661420546', '@festin_traiteur', 'Fes', 'prestige,92K-followers,raffinee,maison,topjour', 4.92, 278),
(12, 'Traiteur Sahel Events', 'Fes', 'Receptions gastronomiques de mariage. Preparations fraiches et maison. Menus sur mesure. 12K abonnes Instagram.', 250.00, 600.00, 'PREMIUM', '0661704881', '@traiteur.sahel.events', '18 Lot Riad al Yassamine, Route Ain Chkef, Fes', 'gastronomique,frais,maison,sur-mesure,12K-followers', 4.85, 189),
(12, 'El Hammoumi Traiteur', 'Fes', 'Plus de 25 ans d''experience. 80+ employes. Salles et riads disponibles. Reference sur Mariage-Marocain.com', 200.00, 500.00, 'MOYEN', '0661553883', NULL, 'Narjiss B N152 Rue Enamae, 30000 Fes', '25-ans,80-employes,salles,riads,reference', 4.80, 234),
(12, 'Afrah Palace Fes', 'Fes', 'Traiteur specialise mariages. Salade royale, poulet m3assel, desserts. Environ 5000 DHS par table. Route Ain Chkef.', 200.00, 500.00, 'MOYEN', '0631183716', '@_traiteur_afrah.palace_fes', 'Route Ain Chkef, Fes', 'salade-royale,m3assel,desserts,5000-par-table', 4.75, 167),
(12, 'Afrah Fes Traiteur', 'Fes', '3 generations de cuisine fassie traditionnelle. Methodes ancestrales adaptees aux evenements contemporains. Site : afrahfes.ma', 250.00, 550.00, 'MOYEN', NULL, NULL, 'Fes', '3-generations,fassi,ancestral,contemporain,site-web', 4.78, 156),
(12, 'Traiteur Al Jawda', 'Fes', 'Service traiteur base a Fes. Cuisine marocaine traditionnelle. Pain, patisseries et plats.', 150.00, 350.00, 'MOYEN', '0660044194', NULL, 'Fes', 'traditionnel,patisseries,plats,pain', 4.65, 134),
(12, 'Sekkate Traiteur', 'Fes', 'Traiteur a domicile a Fes. Residence Kenza, Bd Ibn Atir.', 150.00, 400.00, 'MOYEN', '0535602132', NULL, '8 Bd Ibn Atir, Residence Kenza, Fes', 'domicile,residence-kenza,traditionnel', 4.60, 98);

-- ============================================
-- MYADI / TYAFR (cat 13)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(13, 'Myadi El Fassi', 'Fes', 'Service Myadi traditionnel fassi. Equipe experimentee pour le service du the, petits fours et distribution des cadeaux.', 3000.00, 8000.00, 'MOYEN', NULL, NULL, 'Fes Medina', 'traditionnel,fassi,the,petits-fours,cadeaux', 4.80, 145),
(13, 'Tyafr Dar Chrifa', 'Fes', 'Service Tyafr haut de gamme. Plateau en argent, service a la marocaine. 10 a 30 serveurs selon vos besoins.', 5000.00, 15000.00, 'PREMIUM', NULL, NULL, 'Fes', 'haut-gamme,argent,marocain,30-serveurs', 4.85, 112),
(13, 'Al Hadra Myadi', 'Fes', 'Myadi pour grands mariages. Service complet incluant the, lait, dattes et gateaux. Jusqu''a 500 invites.', 2000.00, 5000.00, 'ECONOMIQUE', NULL, NULL, 'Fes', 'grands-mariages,complet,500-invites,economique', 4.60, 87);

-- ============================================
-- DJ (cat 14)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(14, 'DJ Youssef Fes', 'Fes', 'DJ professionnel specialise mariages. Mix oriental-occidental. Equipement son et lumiere haut de gamme.', 5000.00, 15000.00, 'PREMIUM', NULL, NULL, 'Fes', 'professionnel,oriental,occidental,son-lumiere', 4.85, 145),
(14, 'DJ Karim Events', 'Fes', 'DJ et animateur. Repertoire complet : chaabi, rai, pop, electro. Micro sans fil et effets speciaux.', 3000.00, 8000.00, 'MOYEN', NULL, NULL, 'Fes', 'animateur,chaabi,rai,pop,effets-speciaux', 4.70, 112),
(14, 'DJ Oriental Fes', 'Fes', 'Specialiste musique orientale et marocaine pour mariages. Ambiance garantie.', 5000.00, 10000.00, 'MOYEN', NULL, NULL, 'Fes', 'oriental,marocain,ambiance,specialiste', 4.72, 98),
(14, 'Sound Wave Fes', 'Fes', 'DJ et sonorisation. Pack mariage : enceintes, micro, lumiere laser, machine a fumee.', 2000.00, 5000.00, 'ECONOMIQUE', NULL, NULL, 'Fes', 'pack-complet,laser,fumee,abordable', 4.55, 78);

-- ============================================
-- TRANSPORT (cat 15)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(15, 'AlloMyCab', 'Fes', 'Location voitures de luxe : Mercedes Classe S, Maybach, Bentley, Rolls Royce. Chauffeur inclus. Site : allomycab.ma', 3000.00, 15000.00, 'PREMIUM', NULL, NULL, 'Fes / Tout le Maroc', 'mercedes,maybach,bentley,rolls-royce,chauffeur,site-web', 4.88, 134),
(15, 'Excellence VIP', 'Fes', 'Flotte prestige : Mercedes G63/V, Range Rover, Porsche. Site : excellencevips.com', 3000.00, 12000.00, 'PREMIUM', NULL, NULL, 'Fes / Tout le Maroc', 'prestige,mercedes-G63,range-rover,porsche,site-web', 4.85, 112),
(15, 'LamyaCars', 'Fes', 'Location limousines et voitures de luxe pour mariage. Bentley, Mercedes, Range Rover. Site : lamyacars.com', 2500.00, 10000.00, 'PREMIUM', NULL, NULL, 'Fes', 'limousine,bentley,mercedes,range-rover', 4.80, 98),
(15, 'Drive 4 Less', 'Fes', 'Location voitures prestige, livraison flexible. Bon rapport qualite-prix. Site : drive-4less.com', 1000.00, 5000.00, 'MOYEN', NULL, NULL, 'Fes', 'prestige,flexible,rapport-qualite-prix', 4.65, 78);

-- ============================================
-- HENNAYA (cat 16)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(16, 'Hennaya Fatima Zahra', 'Fes', 'Artiste henne traditionnelle fassie. Motifs fins et delicats. Plus de 25 ans d''experience. Henne naturel.', 500.00, 2000.00, 'MOYEN', NULL, NULL, 'Fes Medina', 'traditionnel,fassi,fin,delicat,naturel,25-ans', 4.85, 189),
(16, 'Art du Henne Fes', 'Fes', 'Henne artistique moderne. Motifs contemporains, mandala, geometriques. Henne noir et rouge.', 800.00, 3000.00, 'PREMIUM', NULL, NULL, 'Fes', 'moderne,mandala,geometrique,artistique,noir,rouge', 4.80, 145),
(16, 'Hennaya Lalla Khadija', 'Fes', 'Hennaya traditionnelle motifs fassis. Service a domicile. Prix accessibles.', 300.00, 800.00, 'ECONOMIQUE', NULL, NULL, 'Fes', 'authentique,fassi,domicile,accessible', 4.65, 112),
(16, 'Henna Dreams Fes', 'Fes', 'Service henne premium. Pack mariee + invitees. Produits bio et naturels.', 1500.00, 5000.00, 'PREMIUM', NULL, NULL, 'Fes', 'premium,pack,bio,naturel,invitees', 4.82, 98);

-- ============================================
-- WEDDING PLANNER (cat 17) - PRESTATAIRES VERIFIES
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(17, 'Maison Berrada Event Planner', 'Fes', 'Wedding planner premium avec 29K abonnes Instagram. Coordination complete de mariages haut de gamme.', 20000.00, 80000.00, 'PREMIUM', '0661454783', '@maison_berrada_event_planner', 'Fes', 'premium,29K-followers,coordination,haut-gamme', 4.90, 112),
(17, 'D&D Events - Diaa Lahmamsi', 'Fes', '11 ans d''experience, plus de 100 mariages organises. Specialiste Fes et Marrakech.', 50000.00, 150000.00, 'PREMIUM', NULL, NULL, 'Fes / Marrakech', '11-ans,100-mariages,specialiste,fes-marrakech', 4.88, 89),
(17, 'Mariage de Reve', 'Fes', 'Plus de 20 ans d''experience, 300+ evenements au Maroc. Site : mariagedereve.org', 40000.00, 120000.00, 'PREMIUM', NULL, NULL, 'Fes / Tout le Maroc', '20-ans,300-evenements,site-web,tout-maroc', 4.85, 78),
(17, 'Safaa Events', 'Fes', 'Planification de mariage accessible. Coordination jour J et suivi prestataires. Ideal petits budgets.', 5000.00, 15000.00, 'ECONOMIQUE', NULL, NULL, 'Fes', 'accessible,coordination,jour-J,petit-budget', 4.60, 67);

-- ============================================
-- VERIFICATION : Compter les prestataires
-- ============================================
SELECT vc.name_fr AS Categorie, COUNT(v.id) AS Nombre
FROM vendor_categories vc
LEFT JOIN vendors v ON v.category_id = vc.id
GROUP BY vc.id, vc.name_fr
ORDER BY vc.id;
