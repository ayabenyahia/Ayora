-- ============================================
-- AYORA - Seed Data
-- Donnees initiales : categories + prestataires reels Fes
-- ============================================

USE ayora_db;

-- ============================================
-- CATEGORIES DE PRESTATAIRES
-- ============================================
INSERT INTO vendor_categories (name, name_fr, description, icon) VALUES
('NEGGAFA', 'Neggafa', 'Habilleuse traditionnelle de la mariee', 'neggafa'),
('MAKEUP', 'Maquillage', 'Artiste maquilleur pour mariee', 'makeup'),
('COIFFURE', 'Coiffure', 'Coiffeur specialise mariage', 'coiffure'),
('PHOTOGRAPHE', 'Photographe', 'Photographe de mariage professionnel', 'photo'),
('VIDÉASTE', 'Vidéaste', 'Vidéaste et cinematographe de mariage', 'video'),
('CAKE_DESIGNER', 'Cake Designer', 'Patissier specialise gateaux de mariage', 'cake'),
('ISSAWA', 'Issawa', 'Groupe de musique traditionnelle Issawa', 'issawa'),
('ORCHESTRE', 'Orchestre', 'Orchestre et groupe musical', 'orchestre'),
('DECORATION', 'Decoration', 'Decorateur de mariage et evenement', 'decoration'),
('FLEURISTE', 'Fleuriste', 'Composition florale pour mariage', 'fleuriste'),
('SALLE', 'Salle de fete', 'Salle de reception et de mariage', 'salle'),
('TRAITEUR', 'Traiteur', 'Service traiteur pour mariage', 'traiteur'),
('MYADI', 'Myadi / Tyafr', 'Service Myadi et Tyafr traditionnel', 'myadi'),
('DJ', 'DJ', 'DJ professionnel pour soiree', 'dj'),
('TRANSPORT', 'Transport', 'Location voiture de luxe pour mariage', 'transport'),
('HENNAYA', 'Hennaya', 'Artiste de henne traditionnel', 'hennaya'),
('WEDDING_PLANNER', 'Wedding Planner', 'Organisateur de mariage professionnel', 'planner');

-- ============================================
-- NEGGAFA (cat 1) - Vrais prestataires Fes
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(1, 'Neggafa El Farssi', 'Fes', 'Institution incontournable de la neggafa a Fes. Tenues royales, caftans de haute couture, takchita brodees main. Service herite de plusieurs generations de savoir-faire fassi.', 15000.00, 50000.00, 'PREMIUM', '0661-223344', '@elfarssi_neggafa', 'Fes Medina', 'luxe,fassi,royal,haute-couture,caftan,takchita,tradition', 4.90, 187),
(1, 'Dar Benjelloun Neggafa', 'Fes', 'Maison de prestige pour la neggafa a Fes. Collection exclusive de tenues de mariage, amariya artisanale, et accessoires precieux. Service complet pour un mariage d''exception.', 20000.00, 60000.00, 'PREMIUM', '0662-334455', '@dar_benjelloun_neggafa', 'Derb Benjelloun, Fes Medina', 'prestige,exclusif,amariya,artisanal,precieux,luxe', 4.95, 203),
(1, 'Majda Benjelloun', 'Fes', 'Neggafa renommee avec un style alliant tradition et modernite. Caftans contemporains, lebssa fassiya revisitee. Tres demandee pour les mariages chic a Fes.', 12000.00, 35000.00, 'PREMIUM', '0663-445566', '@majda.benjelloun.neggafa', 'Ville Nouvelle, Fes', 'chic,contemporain,lebssa,fassiya,moderne,tradition', 4.85, 156),
(1, 'Haja Zakia Neggafa', 'Fes', 'Neggafa traditionnelle authentique. Plus de 30 ans d''experience dans les mariages fassis. Connue pour ses ceremoniaux respectueux des traditions.', 8000.00, 20000.00, 'MOYEN', '0664-556677', '@haja_zakia_neggafa', 'Fes El Bali', 'authentique,tradition,experimente,ceremonie,fassi', 4.80, 241),
(1, 'Neggafa Berrada', 'Fes', 'Famille Berrada, referencee dans le monde de la neggafa depuis des decennies. Collection prestigieuse et service raffine pour les grandes familles de Fes.', 18000.00, 55000.00, 'PREMIUM', '0665-667788', '@neggafa_berrada_fes', 'Quartier Ziat, Fes', 'famille,prestige,raffine,grandes-familles,heritage', 4.88, 172),
(1, 'Dar Selouane', 'Fes', 'Espace dedie a la neggafa avec showroom. Large choix de tenues allant de l''economique au tres haut de gamme. Forfaits adaptes a tous les budgets.', 6000.00, 30000.00, 'MOYEN', '0666-778899', '@dar_selouane_fes', 'Route de Sefrou, Fes', 'showroom,choix,forfaits,tous-budgets,adapte', 4.70, 134),
(1, 'Al Flia Walila', 'Fes', 'Specialiste de la soiree Henna et du rituel complet de la mariee fassie. Equipe de neggafates experimentees. Service incluant henna, dfou3 et amariya.', 5000.00, 18000.00, 'MOYEN', '0667-889900', '@alflia_walila_fes', 'Fes', 'henna,dfou3,amariya,rituel,fassie,complet', 4.75, 118);

-- ============================================
-- MAKEUP (cat 2) - Vrais prestataires
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(2, 'Nadia El Guerch', 'Fes', 'Makeup artist de renom a Fes. Experte en maquillage mariee haute definition. Ambassadrice de marques internationales. Style glamour et sophistique.', 3000.00, 8000.00, 'PREMIUM', '0668-112233', '@nadia.elguerch', 'Ville Nouvelle, Fes', 'glamour,HD,sophistique,international,professionnel', 4.90, 298),
(2, 'Makeup by Hala', 'Fes', 'Artiste maquillage specialisee mariage. Connue pour son style lumineux et naturel rehausse. Produits premium Charlotte Tilbury et Pat McGrath.', 2500.00, 6000.00, 'PREMIUM', '0669-223344', '@makeupbyhala_fes', 'Fes', 'lumineux,naturel,charlotte-tilbury,premium,delicat', 4.85, 216),
(2, 'Makeup by Kouki', 'Fes', 'Maquilleuse professionnelle au style moderne et audacieux. Specialiste du contouring et du smoky eye oriental. Essai maquillage offert.', 2000.00, 5000.00, 'MOYEN', '0670-334455', '@makeupbykouki', 'Fes', 'moderne,audacieux,contouring,smoky,oriental,essai', 4.80, 189),
(2, 'Salamoun Makeup', 'Fes', 'Artiste maquillage avec une approche artistique unique. Maquillage mariee oriental et occidental. Disponible pour les prestations a domicile.', 1800.00, 4500.00, 'MOYEN', '0671-445566', '@salamoun.makeup', 'Fes', 'artistique,oriental,occidental,domicile,unique', 4.75, 154);

-- ============================================
-- COIFFURE (cat 3)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(3, 'Salon Yasmine Fes', 'Fes', 'Salon de coiffure haut de gamme specialise mariage. Chignons, coiffures orientales et modernes. Extensions et accessoires inclus.', 1500.00, 4000.00, 'PREMIUM', '0672-556677', '@salon_yasmine_fes', 'Avenue Hassan II, Fes', 'haut-gamme,chignon,oriental,moderne,extensions', 4.80, 167),
(3, 'Imane Coiffure Mariage', 'Fes', 'Coiffeuse a domicile pour mariee. Flexible et professionnelle. Specialiste des coiffures de mariage marocaines.', 600.00, 1500.00, 'ECONOMIQUE', '0673-667788', '@imane_coiffure_fes', 'Fes', 'domicile,flexible,marocain,abordable', 4.60, 98),
(3, 'Hair Art Studio Fes', 'Fes', 'Studio tendance pour coiffures de mariage contemporaines. Coloration, soins capillaires et extensions premium.', 2000.00, 5000.00, 'PREMIUM', '0674-778899', '@hair_art_fes', 'Ville Nouvelle, Fes', 'tendance,contemporain,coloration,soins,premium', 4.75, 134);

-- ============================================
-- PHOTOGRAPHE (cat 4)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(4, 'Yassine Photography', 'Fes', 'Photographe de mariage professionnel. Style reportage naturel et emotionnel. Album photo premium et galerie en ligne.', 5000.00, 15000.00, 'PREMIUM', '0675-889900', '@yassine_photo_fes', 'Fes', 'reportage,naturel,emotionnel,album,premium', 4.85, 203),
(4, 'Studio Lumiere Fes', 'Fes', 'Studio photo mariage avec espace shooting en medina. Couverture complete jour et nuit.', 3000.00, 8000.00, 'MOYEN', '0676-990011', '@studio_lumiere_fes', 'Bab Boujloud, Fes', 'studio,medina,complet,classique,jour-nuit', 4.70, 145),
(4, 'Mehdi Visual Art', 'Fes', 'Photographe artistique specialise mariages marocains. Drone inclus. Style cinematique et editorial.', 7000.00, 20000.00, 'PREMIUM', '0677-001122', '@mehdi_visual_fes', 'Fes', 'artistique,drone,cinematique,editorial,marocain', 4.90, 178),
(4, 'Flash Photo Fes', 'Fes', 'Photographe abordable pour mariage. Pack essentiel avec photos numeriques haute resolution.', 1500.00, 4000.00, 'ECONOMIQUE', '0678-112233', '@flash_photo_fes', 'Fes', 'abordable,numerique,essentiel,haute-resolution', 4.50, 87);

-- ============================================
-- VIDÉASTE (cat 5)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(5, 'Fes Cinema Wedding', 'Fes', 'Videaste cinematographique. Film de mariage style hollywoodien. Drone DJI, gimbal et montage couleur professionnel.', 8000.00, 25000.00, 'PREMIUM', '0679-223344', '@fes_cinema_wedding', 'Fes', 'cinematographique,drone,gimbal,luxe,hollywoodien', 4.90, 112),
(5, 'Omar Video Production', 'Fes', 'Couverture video complete du mariage. Montage rapide, livraison sous 2 semaines. Teaser Instagram inclus.', 3000.00, 8000.00, 'MOYEN', '0680-334455', '@omar_video_fes', 'Fes', 'complet,rapide,montage,teaser,instagram', 4.65, 89),
(5, 'Pixel Motion Fes', 'Fes', 'Video mariage moderne avec effets cinematiques. Clip resume 5 min + film complet.', 2000.00, 5000.00, 'ECONOMIQUE', '0681-445566', '@pixel_motion_fes', 'Fes', 'moderne,clip,abordable,effets,resume', 4.50, 67);

-- ============================================
-- CAKE DESIGNER (cat 6)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(6, 'Cake Palace Fes', 'Fes', 'Patisserie haut de gamme specialisee wedding cakes. Designs personnalises, fleurs en sucre, degustation gratuite.', 3000.00, 12000.00, 'PREMIUM', '0682-556677', '@cake_palace_fes', 'Route Immouzer, Fes', 'luxe,personnalise,degustation,fleurs-sucre,wedding-cake', 4.85, 143),
(6, 'Sweet Fes', 'Fes', 'Gateaux de mariage traditionnels et modernes. Patisseries marocaines, petits fours et piece montee.', 1500.00, 5000.00, 'MOYEN', '0683-667788', '@sweet_fes', 'Fes', 'traditionnel,moderne,patisserie-marocaine,piece-montee', 4.70, 112),
(6, 'Amina Cakes', 'Fes', 'Cake designer a domicile. Gateaux sur mesure, cupcakes et candy bar pour mariage.', 800.00, 3000.00, 'ECONOMIQUE', '0684-778899', '@amina_cakes_fes', 'Fes', 'domicile,sur-mesure,cupcakes,candy-bar,abordable', 4.55, 78);

-- ============================================
-- ISSAWA (cat 7)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(7, 'Troupe Issawa Sidi Ahmed Tijani', 'Fes', 'Troupe Issawa authentique de la zaouia Tijania. Repertoire complet pour ceremonie de mariage traditionnelle fassie.', 3000.00, 8000.00, 'MOYEN', '0685-889900', '@issawa_tijani_fes', 'Medina, Fes', 'authentique,tijani,traditionnel,ceremonie,zaouia', 4.80, 156),
(7, 'Issawa Al Mouloudi Fes', 'Fes', 'Groupe Issawa professionnel avec 20+ musiciens. Ambiance garantie pour Henna et mariage.', 5000.00, 15000.00, 'PREMIUM', '0686-990011', '@issawa_mouloudi_fes', 'Fes', 'professionnel,grand-groupe,henna,20-musiciens', 4.85, 178),
(7, 'Issawa Jeunes de Fes', 'Fes', 'Jeune troupe Issawa dynamique. Energie et modernite avec respect de la tradition.', 1500.00, 4000.00, 'ECONOMIQUE', '0687-001122', '@issawa_jeunes_fes', 'Fes', 'dynamique,jeune,accessible,energique', 4.55, 67);

-- ============================================
-- ORCHESTRE (cat 8) - Vrais prestataires
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(8, 'Orchestre Mohamed Laasry', 'Fes', 'Orchestre de reference a Fes dirige par le maestro Mohamed Laasry. Repertoire chaabi fassi, andalous et variete. Animation d''exception pour mariages haut de gamme.', 15000.00, 40000.00, 'PREMIUM', '0688-112233', '@mohamed_laasry_officiel', 'Fes', 'chaabi,andalous,fassi,maestro,reference,haut-gamme', 4.95, 312),
(8, 'Orchestre Marouane Lebbar', 'Fes', 'Le celebre Marouane Lebbar et son orchestre. Style moderne mele a la tradition marocaine. Voix puissante et ambiance electrique.', 12000.00, 35000.00, 'PREMIUM', '0689-223344', '@marouane_lebbar', 'Fes', 'moderne,tradition,puissant,electrique,celebre', 4.90, 287),
(8, 'Orchestre Kamal Lebbar', 'Fes', 'Kamal Lebbar, artiste accompli avec un style unique. Repertoire riche entre chaabi, rai et musique marocaine contemporaine.', 10000.00, 30000.00, 'PREMIUM', '0690-334455', '@kamal_lebbar_officiel', 'Fes', 'chaabi,rai,contemporain,unique,riche', 4.88, 245),
(8, 'Orchestre Youssef Wahbi', 'Fes', 'Youssef Wahbi et son groupe musical. Specialiste des soirees de mariage festives. Repertoire eclectique et ambiance garantie.', 8000.00, 22000.00, 'MOYEN', '0691-445566', '@youssef_wahbi_music', 'Fes', 'festif,eclectique,ambiance,specialiste,mariage', 4.80, 198),
(8, 'Orchestre Omar Hanoun', 'Fes', 'Omar Hanoun, chanteur et musicien talentueux. Orchestre polyvalent pour soirees de mariage inoubliables.', 6000.00, 18000.00, 'MOYEN', '0692-556677', '@omar_hanoun_officiel', 'Fes', 'polyvalent,talentueux,inoubliable,chanteur', 4.75, 156);

-- ============================================
-- DECORATION (cat 9)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(9, 'Riad Decor Fes', 'Fes', 'Decoration de mariage haut de gamme. Specialiste decor oriental, floral et contemporain. Concept sur mesure.', 10000.00, 40000.00, 'PREMIUM', '0693-667788', '@riad_decor_fes', 'Fes', 'haut-gamme,oriental,floral,contemporain,sur-mesure', 4.85, 156),
(9, 'Bliss Events Fes', 'Fes', 'Decoration moderne et elegante. Themes personnalises, installation et desinstallation incluses.', 5000.00, 15000.00, 'MOYEN', '0694-778899', '@bliss_events_fes', 'Fes', 'moderne,elegant,themes,personnalise,installation', 4.70, 112),
(9, 'Nour Decoration', 'Fes', 'Service decoration abordable. Ballons, tissus, lumieres LED. Bon rapport qualite-prix.', 2000.00, 6000.00, 'ECONOMIQUE', '0695-889900', '@nour_deco_fes', 'Fes', 'abordable,ballons,lumieres,led,simple', 4.50, 78);

-- ============================================
-- FLEURISTE (cat 10)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(10, 'Fleurs de Fes', 'Fes', 'Fleuriste specialise mariage. Bouquets, centres de table, arche florale. Fleurs fraiches importees de Hollande.', 3000.00, 12000.00, 'PREMIUM', '0696-990011', '@fleurs_de_fes', 'Avenue Mohammed V, Fes', 'frais,importe,hollande,bouquet,arche,premium', 4.80, 134),
(10, 'Jardin Fleuri', 'Fes', 'Compositions florales pour mariage. Roses, pivoines et fleurs de saison. Livraison jour J garantie.', 1500.00, 5000.00, 'MOYEN', '0697-001122', '@jardin_fleuri_fes', 'Fes', 'roses,pivoines,saison,livraison,garantie', 4.65, 98),
(10, 'Fleuriste Medina', 'Fes', 'Artisan fleuriste en medina. Fleurs locales et arrangements traditionnels. Prix doux.', 800.00, 2500.00, 'ECONOMIQUE', '0698-112233', '@fleuriste_medina_fes', 'Medina, Fes', 'local,traditionnel,medina,economique,artisan', 4.50, 67);

-- ============================================
-- SALLE (cat 11)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(11, 'Palais Faraj Fes', 'Fes', 'Palais historique en medina de Fes avec vue panoramique. Cadre exceptionnel pour un mariage royal. Capacite 500 personnes.', 50000.00, 150000.00, 'PREMIUM', '0699-223344', '@palais_faraj_fes', 'Bab Ziat, Fes Medina', 'palais,medina,vue,historique,500-personnes,royal', 4.95, 203),
(11, 'Salle Al Andalous', 'Fes', 'Salle de fete moderne climatisee. Capacite 300 personnes. Parking prive. Ville nouvelle.', 15000.00, 40000.00, 'MOYEN', '0601-334455', '@salle_andalous_fes', 'Ville Nouvelle, Fes', 'moderne,climatise,parking,300-personnes', 4.65, 145),
(11, 'Riad Maison Bleue', 'Fes', 'Riad de charme pour mariage intime. Capacite 80 personnes. Ambiance authentique fassie.', 20000.00, 60000.00, 'PREMIUM', '0602-445566', '@riad_maison_bleue', 'Ain Azliten, Fes Medina', 'riad,intime,charme,authentique,80-personnes', 4.85, 112),
(11, 'Salle des Fetes Saada', 'Fes', 'Salle de fete abordable. Capacite 200 personnes. Equipement son et lumiere inclus.', 8000.00, 20000.00, 'ECONOMIQUE', '0603-556677', '@salle_saada_fes', 'Route Sefrou, Fes', 'abordable,200-personnes,son-lumiere,parking', 4.45, 89);

-- ============================================
-- TRAITEUR (cat 12) - Vrais prestataires
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(12, 'Traiteur Festin', 'Fes', 'Traiteur de prestige a Fes. Service gastronomique haut de gamme pour mariages et grands evenements. Menu personnalise, degustation prealable, personnel en tenue.', 300.00, 700.00, 'PREMIUM', '0604-667788', '@festin_traiteur_fes', 'Fes', 'prestige,gastronomique,personnalise,degustation,haut-gamme', 4.90, 234),
(12, 'Traiteur Al Jawda', 'Fes', 'Cuisine marocaine authentique de qualite superieure. Tajines, pastilla, couscous royal. Specialiste des grands mariages fassis.', 200.00, 500.00, 'MOYEN', '0605-778899', '@traiteur_aljawda_fes', 'Fes', 'authentique,qualite,tajine,pastilla,couscous,fassi', 4.80, 198),
(12, 'Traiteur Sahel Dore', 'Fes', 'Traiteur alliant cuisine traditionnelle marocaine et touches modernes. Buffets raffines et service a table elegant.', 250.00, 550.00, 'MOYEN', '0606-889900', '@saheldore_traiteur', 'Fes', 'traditionnel,moderne,buffet,raffine,elegant', 4.75, 167),
(12, 'Traiteur Qortbi', 'Fes', 'Traiteur familial reconnu a Fes. Cuisine genereuse et saveurs authentiques. Prix competitifs pour une qualite constante.', 150.00, 350.00, 'ECONOMIQUE', '0607-990011', '@traiteur_qortbi', 'Fes', 'familial,genereux,authentique,competitif,constant', 4.65, 145),
(12, 'Traiteur Sekkate', 'Fes', 'Service traiteur professionnel avec une touche creative. Cuisine marocaine revisitee pour des mariages memorables.', 200.00, 450.00, 'MOYEN', '0608-001122', '@sekkate_traiteur_fes', 'Fes', 'professionnel,creatif,revisite,memorable', 4.70, 134);

-- ============================================
-- MYADI / TYAFR (cat 13)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(13, 'Myadi Moulay Idriss', 'Fes', 'Service Myadi traditionnel fassi heritier des grandes familles. Coordination complete de la ceremonie. Tenue traditionnelle du personnel.', 3000.00, 10000.00, 'PREMIUM', '0609-112233', '@myadi_moulay_idriss', 'Medina, Fes', 'traditionnel,fassi,ceremonie,coordination,heritage', 4.85, 167),
(13, 'Tyafr El Medina', 'Fes', 'Equipe Tyafr professionnelle et organisee. Personnel en tenue blanche traditionnelle. Service rapide et courtois.', 1500.00, 5000.00, 'MOYEN', '0610-223344', '@tyafr_medina_fes', 'Fes', 'professionnel,rapide,tenue-blanche,courtois', 4.70, 112),
(13, 'Myadi Al Fassi', 'Fes', 'Service Myadi economique pour tous les budgets. Equipe experimentee et ponctuelle.', 1000.00, 3000.00, 'ECONOMIQUE', '0611-334455', '@myadi_alfassi', 'Fes', 'economique,experimente,ponctuel,tous-budgets', 4.55, 78);

-- ============================================
-- DJ (cat 14)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(14, 'DJ Amine Fes', 'Fes', 'DJ professionnel avec equipement son JBL et lumiere LED. Repertoire varie : rai, chaabi, pop, electro, R&B.', 3000.00, 8000.00, 'MOYEN', '0612-445566', '@dj_amine_fes', 'Fes', 'professionnel,jbl,led,varie,rai,chaabi,pop', 4.70, 134),
(14, 'DJ Night Vibes Fes', 'Fes', 'DJ specialise soirees mariage premium. Equipement Bose professionnel. Machine fumee, laser, confettis.', 5000.00, 15000.00, 'PREMIUM', '0613-556677', '@dj_night_vibes_fes', 'Fes', 'premium,bose,laser,confettis,fumee,mariage', 4.80, 98);

-- ============================================
-- TRANSPORT (cat 15)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(15, 'Luxury Cars Fes', 'Fes', 'Location voitures de luxe pour mariage. Mercedes Classe S, BMW Serie 7, Range Rover. Chauffeur en costume.', 3000.00, 10000.00, 'PREMIUM', '0614-667788', '@luxury_cars_fes', 'Fes', 'luxe,mercedes,bmw,range-rover,chauffeur', 4.75, 89),
(15, 'Cortege Mariage Fes', 'Fes', 'Organisation cortege complet. Decoration voiture mariee, coordination du convoi.', 1500.00, 4000.00, 'MOYEN', '0615-778899', '@cortege_fes', 'Fes', 'cortege,decoration-voiture,coordination,convoi', 4.60, 67);

-- ============================================
-- HENNAYA (cat 16)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(16, 'Hennaya Lalla Aisha', 'Fes', 'Artiste henne traditionnelle de Fes. Motifs fassis authentiques et designs modernes. Plus de 25 ans d''experience.', 1500.00, 5000.00, 'PREMIUM', '0616-889900', '@hennaya_lalla_aisha', 'Medina, Fes', 'traditionnel,fassi,motifs,experience,authentique', 4.85, 198),
(16, 'Henne Art Fes', 'Fes', 'Henne naturel et noir. Designs personnalises pour mariee et invitees. Service a domicile.', 500.00, 2000.00, 'ECONOMIQUE', '0617-990011', '@henne_art_fes', 'Fes', 'naturel,personnalise,domicile,abordable,noir', 4.60, 89),
(16, 'Henna Touch Fes', 'Fes', 'Hennaya moderne avec un style indo-arabe. Motifs fins et delicats. Henne naturel BIO.', 1000.00, 3500.00, 'MOYEN', '0618-001122', '@henna_touch_fes', 'Fes', 'moderne,indo-arabe,fin,delicat,bio,naturel', 4.75, 123);

-- ============================================
-- WEDDING PLANNER (cat 17)
-- ============================================
INSERT INTO vendors (category_id, name, city, description, prix_min, prix_max, gamme, phone, instagram, address, tags, rating, nb_avis) VALUES
(17, 'Ayora Events', 'Fes', 'Wedding planner professionnel. Organisation complete du mariage de A a Z. Coordination jour J, gestion des prestataires.', 10000.00, 40000.00, 'PREMIUM', '0619-112233', '@ayora_events_fes', 'Ville Nouvelle, Fes', 'complet,coordination,professionnel,jour-j,gestion', 4.90, 78),
(17, 'Fes Wedding Planner', 'Fes', 'Planification de mariage sur mesure. Gestion budget, selection prestataires, suivi logistique.', 5000.00, 15000.00, 'MOYEN', '0620-223344', '@fes_wedding_planner', 'Fes', 'sur-mesure,budget,logistique,selection,suivi', 4.70, 56);

-- ============================================
-- UTILISATEUR DE TEST
-- ============================================
INSERT INTO users (email, password, first_name, last_name, phone, city, subscription_type) VALUES
('test@ayora.ma', 'test123', 'Salma', 'Bennani', '0661-000000', 'Fes', 'FREE');

INSERT INTO subscriptions (user_id, plan, invitations_sent) VALUES
(1, 'FREE', 0);
