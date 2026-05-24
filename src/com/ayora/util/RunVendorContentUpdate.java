package com.ayora.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runner : met a jour UNIQUEMENT description + tags de 58 prestataires.
 *
 * Idempotent. Utilise PreparedStatement (zero risque d'injection / d'echappement
 * casse meme avec des apostrophes francaises dans les descriptions).
 *
 * Aucune autre colonne touchee : ni nom, ni prix, ni ville, ni gamme, ni
 * venue_type, ni photos, ni rating.
 *
 * Lance via : java -cp build/classes;WebContent/WEB-INF/lib/* com.ayora.util.RunVendorContentUpdate
 */
public class RunVendorContentUpdate {

	// Format : { nom_exact, description, tags_csv }
	private static final String[][] DATA = {
		{"Chocolat de Joie",
			"Chocolat de Joie propose des presentations raffinees pour les moments d'offrande et de reception, avec une attention particuliere aux details, aux emballages et a l'harmonie visuelle. Une belle option pour accompagner les tyafer, les cadeaux invites ou les tables d'accueil avec une touche douce et soignee.",
			"chocolat,presentation,cadeaux-invites,dragees,table-daccueil,offrandes,mariage,raffine,emballage,ceremonie"},

		{"Crystal Salle des Fetes",
			"Crystal Salle des Fetes offre un cadre elegant pour organiser une reception structuree, confortable et adaptee aux grands rassemblements familiaux. Son espace convient aux mariages qui recherchent une salle claire, bien organisee et facile a amenager selon le theme choisi.",
			"salle-reception,espace-evenement,grande-capacite,decoration,ceremonie,diner,mariage-familial,organisation,ambiance"},

		{"Dar Ba Sidi",
			"Dar Ba Sidi met en avant une atmosphere marocaine chaleureuse, parfaite pour une reception traditionnelle avec du caractere. Le lieu convient aux mariages qui veulent une ambiance conviviale, authentique et proche de l'esprit des grandes maisons marocaines.",
			"maison-traditionnelle,reception,ambiance-marocaine,patrimoine,ceremonie,diner-familial,authentique,decoration,mariage"},

		{"Dar Benjelloun Neggafa",
			"Dar Benjelloun Neggafa accompagne la mariee avec une presence raffinee, des tenues travaillees et une mise en scene pensee pour les grands moments de la soiree. La prestation convient aux mariages qui veulent une allure royale, organisee et fidele aux codes marocains.",
			"neggafa,tenues,caftan,amariya,mise-en-scene,mariee,tradition,ceremonie,accessoires,defile"},

		{"DJ Kacem - Kacem Mghabbar",
			"DJ Kacem apporte une animation dynamique pour garder la piste vivante tout au long de la soiree. Son style convient aux mariages qui veulent alterner musique moderne, ambiance familiale et moments festifs sans alourdir le deroulement.",
			"dj,animation-musicale,playlist,danse,soiree,ambiance,sonorisation,mariage,rythme,piste"},

		{"DJ Mehdi Bouchal",
			"DJ Mehdi Bouchal propose une ambiance musicale simple, efficace et adaptee aux soirees de mariage ou l'energie doit rester constante. Il accompagne les transitions, les entrees et les moments dansants avec une selection pensee pour faire participer les invites.",
			"dj,musique,animation,soiree-dansante,son,playlist,ambiance-festive,entree,danse,mariage"},

		{"Doreve Traiteur - Karima Riffi",
			"Doreve Traiteur signe une cuisine de reception soignee, pensee pour les mariages ou le service, la presentation et le gout doivent rester equilibres. Une adresse adaptee aux couples qui veulent un repas genereux, elegant et bien organise.",
			"traiteur,repas-mariage,cuisine-marocaine,buffet,service,plats,menu,reception,diner,presentation"},

		{"Emeraude by Malika Alaoui",
			"Emeraude by Malika Alaoui met en valeur les cadeaux et presentations de mariage avec une touche delicate et feminine. Ses creations conviennent aux ceremonies ou les tyafer, les douceurs et les details visuels doivent avoir une vraie presence.",
			"tyafer,myadi,presentation-cadeaux,offrandes,douceurs,ceremonie,detail,raffinement,mariage,tradition"},

		{"Faly Events",
			"Faly Events propose des presentations de mariage elegantes autour des cadeaux, douceurs et mises en scene de reception. Le style convient aux familles qui veulent un rendu propre, harmonieux et valorisant pour les moments d'entree et d'offrande.",
			"myadi,tyafer,evenementiel,presentation,offrandes,cadeaux,ceremonie,organisation,decoration,mariage"},

		{"Festin Traiteur",
			"Festin Traiteur accompagne les receptions avec une cuisine genereuse et un service pense pour les grands mariages. La prestation convient aux couples qui cherchent un repas bien presente, un deroulement fluide et une table a la hauteur de l'evenement.",
			"traiteur,service-table,repas,menu-marocain,buffet,diner,vaisselle,reception,cuisine,evenement"},

		{"Filali Tyafer",
			"Filali Tyafer valorise les moments de presentation avec des tyafer elegants, adaptes aux ceremonies de mariage marocain. Le rendu met l'accent sur l'ordre, la brillance et la mise en scene des cadeaux pour creer un moment fort devant les invites.",
			"tyafer,myadi,dfoua,presentation-cadeaux,ceremonie,tradition,offrandes,mariage,plateaux,detail"},

		{"Glamsmakeup by Ghita",
			"Glamsmakeup by Ghita propose une mise en beaute douce et travaillee pour les mariees qui veulent un resultat lumineux sans surcharge. Le style convient aux caftans, aux photos et aux changements de tenues avec une finition feminine et moderne.",
			"maquillage,coiffure,beauty,bride,makeup,hairstyle,glamour,caftan,photos,preparation"},

		{"Haj Said Berrada",
			"Haj Said Berrada apporte une prestation Issawa traditionnelle, marquee par la puissance du chant collectif et la profondeur des rythmes spirituels. Ideal pour les moments de benediction, d'entree ou de celebration familiale authentique.",
			"issawa,chant-traditionnel,rythmes,spirituel,benediction,henna,entree-mariee,ceremonie,folklore,ambiance"},

		{"Haja Zakia Neggafa",
			"Haja Zakia Neggafa accompagne la mariee avec une approche traditionnelle et rassurante, centree sur les tenues, les accessoires et le bon deroulement des passages. Une prestation adaptee aux familles qui veulent garder une ceremonie marocaine classique et bien maitrisee.",
			"neggafa,tenues-marocaines,caftan,takchita,amariya,mariee,accessoires,tradition,ceremonie,habillage"},

		{"Hnaya Fati Fes",
			"Hnaya Fati Fes propose une ceremonie du henne intime et soignee, avec un travail qui valorise la mariee et les details symboliques de ce moment. Une belle option pour une soiree de henne douce, traditionnelle et centree sur la famille.",
			"henna,hennaya,ceremonie-henne,dessin-henne,tradition,mariee,famille,benediction,soiree-henne,rituel"},

		{"Issawa Moustafa Meseyah - Bola Bola de Luxe",
			"Issawa Moustafa Meseyah propose une animation forte et populaire, pensee pour donner de l'energie aux moments traditionnels du mariage. Le style Bola Bola apporte une ambiance rythmee, expressive et tres presente pendant la soiree.",
			"issawa,bola-bola,animation-traditionnelle,rythme,ambiance-populaire,chant,percussions,ceremonie,entree,mariage"},

		{"Issawa Salim - Ahmed Salim",
			"Issawa Salim accompagne les mariages avec une prestation traditionnelle structuree, ideale pour les familles qui veulent une ambiance spirituelle et festive a la fois. La presence du groupe donne du relief aux entrees, au henne et aux moments de celebration collective.",
			"issawa,tradition,chant,percussions,ambiance,henna,entree,ceremonie,benediction,mariage"},

		{"Issawa Zakaria Faida",
			"Issawa Zakaria Faida propose une ambiance marocaine intense, portee par les chants, les rythmes et la participation des invites. Une prestation adaptee aux mariages qui veulent un moment traditionnel vivant et memorable.",
			"issawa,animation,chant-collectif,rythme,tradition-marocaine,percussions,soiree,ceremonie,ambiance,mariage"},

		{"Jad maison des fleurs",
			"Jad Maison des Fleurs cree des compositions florales pensees pour donner de la douceur et de l'elegance aux espaces de mariage. Bouquets, centres de table et decorations florales apportent une touche romantique, fraiche et harmonieuse a la celebration.",
			"fleurs,decoration-florale,bouquet,centre-de-table,arche,romantique,composition,ceremonie,reception,mariage"},

		{"Jeff de Bruges Fes",
			"Jeff de Bruges Fes apporte une touche gourmande et elegante aux cadeaux d'invites, aux tyafer et aux tables de reception. Les chocolats et presentations conviennent aux mariages qui veulent offrir un detail raffine et facilement appreciable par les invites.",
			"chocolat,confiserie,cadeaux-invites,dragees,tyafer,gourmandise,presentation,table-sucree,offrandes,mariage"},

		{"Khalid El Achouri Photographe",
			"Khalid El Achouri Photographe capture les moments du mariage avec une approche orientee emotion, details et souvenirs durables. Sa prestation convient aux couples qui veulent garder des images naturelles, elegantes et representatives de chaque etape de la journee.",
			"photographe,video,shooting,couple,reportage-mariage,emotion,souvenirs,preparatifs,ceremonie,album"},

		{"La Dragee d'Or Fes",
			"La Dragee d'Or propose des douceurs et presentations adaptees aux cadeaux invites, aux tyafer et aux tables de mariage. Son univers convient aux mariages qui veulent une touche sucree classique, bien presentee et facile a integrer dans la decoration.",
			"dragees,chocolat,cadeaux-invites,douceurs,presentation,tyafer,table-sucree,emballage,ceremonie,mariage"},

		{"Le Cacaochi",
			"Le Cacaouchi propose des creations gourmandes et presentations sucrees adaptees aux evenements familiaux et aux mariages. Une option pratique pour enrichir les tables de reception, les cadeaux invites ou les moments d'accueil avec une touche artisanale.",
			"gateaux,douceurs,table-sucree,patisserie,confiserie,cadeaux-invites,reception,gourmandise,tradition,evenement"},

		{"Le Pavillon Dor",
			"Le Pavillon Dor offre un espace de reception adapte aux mariages structures, avec une atmosphere elegante et une capacite pensee pour accueillir familles et invites dans de bonnes conditions. Le lieu convient aux soirees organisees avec diner, musique et ceremonies.",
			"salle-reception,espace-mariage,diner,ceremonie,grande-capacite,decoration,organisation,ambiance,soiree,reception"},

		{"Maison Mariee Soltana",
			"Maison Mariee Soltana accompagne la mariee dans ses changements de tenues avec une selection de caftans, accessoires et mises en scene traditionnelles. La prestation convient aux mariages qui veulent une presence feminine, royale et soigneusement orchestree.",
			"neggafa,caftan,takchita,tenues,mariee,amariya,accessoires,defile,habillage,ceremonie"},

		{"Make Me Fab",
			"Make Me Fab propose une mise en beaute complete pour les mariees qui veulent un rendu professionnel, lumineux et photo-friendly. Le style s'adapte aux ceremonies modernes comme traditionnelles, avec une attention portee au teint, aux details et a la tenue du maquillage.",
			"maquillage,coiffure,makeup,hairstyle,bride,beauty,glow,preparation,shooting,caftan"},

		{"Makeup by Ghita (GhB)",
			"Makeup by Ghita offre un style de maquillage doux, propre et adapte aux mariees qui veulent rester elegantes sans exces. La prestation met l'accent sur l'harmonie du visage, la coiffure et la coherence avec les tenues de la soiree.",
			"maquillage,coiffure,makeup,beauty,naturel,elegant,mariee,hairstyle,preparation,caftan"},

		{"Makeup by Hala",
			"Makeup by Hala accompagne les mariees avec une mise en beaute raffinee, pensee pour les photos, la lumiere et les longues soirees. Le resultat vise un equilibre entre glamour, douceur et tenue impeccable jusqu'a la fin de l'evenement.",
			"maquillage,coiffure,glamour,bride,beauty,makeup-longue-tenue,photos,hairstyle,preparation,evenement"},

		{"Marouane Hajji",
			"Marouane Hajji propose une animation orchestrale forte pour les mariages qui veulent une grande presence musicale. Sa prestation convient aux soirees ou la musique live doit porter les entrees, les danses et les moments festifs importants.",
			"orchestre,musique-live,animation,chant,soiree,danse,entree,ambiance,scene,mariage"},

		{"Mehdi Artiste Orchestre",
			"Mehdi Artiste Orchestre accompagne les mariages avec une animation musicale vivante, adaptee aux familles et aux moments de fete. Son style permet de creer une soiree progressive, entre morceaux traditionnels, ambiance dansante et participation des invites.",
			"orchestre,artiste,chanteur,musique-live,animation,danse,soiree,ambiance,entree,mariage"},

		{"Moktaka Alossar",
			"Moktaka Alossar propose un espace de reception simple et fonctionnel pour les mariages familiaux. Le lieu convient aux couples qui cherchent une salle accessible, adaptable et capable d'accueillir les principaux moments de la ceremonie.",
			"salle-reception,mariage-familial,espace-evenement,diner,ceremonie,organisation,decoration,ambiance,reception,invites"},

		{"Mounia Ramsis Tounsi - Neggafa",
			"Mounia Ramsis Tounsi propose une prestation de negafa elegante, axee sur le raffinement des tenues, la presence de la mariee et l'enchainement fluide des passages. Son style convient aux mariages qui veulent une allure sophistiquee et bien encadree.",
			"neggafa,tenues,caftan,takchita,mariee,amariya,accessoires,raffinement,defile,ceremonie"},

		{"My kiko Cake",
			"My Kiko Cake realise des creations sucrees personnalisees pour les mariages et evenements familiaux. Ses gateaux conviennent aux couples qui veulent une piece visuelle agreable, adaptee a la decoupe, aux photos et a l'ambiance de la soiree.",
			"cake-design,gateau,table-sucree,patisserie,dessert,personnalise,decoration,douceurs,mariage,evenement"},

		{"Nadia El Guerch",
			"Nadia El Guerch propose une mise en beaute soignee pour les mariees, avec un travail oriente elegance, teint lumineux et coiffure harmonieuse. Une prestation adaptee aux differentes tenues de la soiree et aux exigences des photos de mariage.",
			"maquillage,coiffure,makeup,hairstyle,bride,teint,glamour,preparation,caftan,photos"},

		{"Negafa Majda Benjelloun - Tenguif de Luxe",
			"Negafa Majda Benjelloun met l'accent sur le ceremonial, la richesse des tenues et la mise en valeur de la mariee pendant chaque passage. Sa prestation convient aux mariages qui veulent un rendu traditionnel fort, travaille et tres present visuellement.",
			"neggafa,tenguif,caftan,takchita,amariya,mariee,accessoires,ceremonie,tradition,defile"},

		{"Neggafa El Farssi",
			"Neggafa El Farssi accompagne la mariee avec une approche classique et elegante, centree sur les tenues marocaines, les accessoires et la coordination des passages. Une option adaptee aux familles qui veulent une ceremonie structuree et respectueuse des traditions.",
			"neggafa,caftan,takchita,mariee,amariya,accessoires,tradition,habillage,ceremonie,defile"},

		{"Nisrine Benkirane",
			"Nisrine Benkirane propose des gateaux et douceurs de mariage avec un style delicat, adapte aux tables sucrees et aux moments de decoupe. Ses creations conviennent aux evenements qui recherchent une presentation propre, feminine et gourmande.",
			"cake-design,gateau-mariage,douceurs,patisserie,table-sucree,dessert,decoration,personnalise,ceremonie,mariage"},

		{"Noujoum Fes Bola Bola",
			"Noujoum Fes Bola Bola apporte une animation Issawa tres rythmee, ideale pour donner une energie populaire et traditionnelle a la soiree. La prestation convient aux mariages qui veulent un moment fort, participatif et marque par les percussions.",
			"issawa,bola-bola,animation,percussions,chant,tradition,ambiance-populaire,entree,ceremonie,mariage"},

		{"Omar Hanoun",
			"Omar Hanoun propose une animation orchestrale adaptee aux soirees de mariage ou la musique doit accompagner les moments forts sans perdre l'ambiance familiale. Son style convient aux entrees, aux danses et aux sequences festives.",
			"orchestre,musique-live,animation,chant,soiree,danses,ambiance,entree,mariage,scene"},

		{"Orchestre Marouane Lebbar",
			"Orchestre Marouane Lebbar offre une prestation musicale complete pour les mariages qui veulent une ambiance live maitrisee. L'orchestre accompagne la soiree avec une presence scenique, des transitions fluides et une energie adaptee aux invites.",
			"orchestre,musique-live,animation,scene,chant,soiree,danse,ambiance,entree,mariage"},

		{"Orchestre Mohamed Laasry",
			"Orchestre Mohamed Laasry apporte une animation musicale hautement presente, pensee pour les grandes soirees de mariage. La prestation convient aux couples qui veulent un orchestre capable de porter l'ambiance, les entrees et les moments dansants.",
			"orchestre,animation-live,chant,scene,soiree,danses,ambiance-festive,entree,mariage,musique"},

		{"Orchestre Tahour",
			"Orchestre Tahour propose une prestation musicale genereuse pour les mariages qui veulent une soiree vivante, festive et marquee par la musique live. L'orchestre convient aux grandes receptions avec un public familial et varie.",
			"orchestre,musique-live,animation,chant,percussion,soiree,ambiance,danse,scene,mariage"},

		{"Riad Salam Fes",
			"Riad Salam Fes propose un cadre marocain elegant pour une reception de mariage au style traditionnel et intimiste. Le lieu convient aux couples qui veulent une atmosphere de riad, une architecture de caractere et un decor naturellement photogenique.",
			"riad,reception,architecture-marocaine,patio,ceremonie,intimiste,decoration,photos,diner,mariage"},

		{"Sahel Traiteur Events",
			"Sahel Traiteur Events accompagne les mariages avec une cuisine de reception pratique, conviviale et adaptee aux grands services. La prestation convient aux familles qui veulent un repas bien organise, des plats genereux et un deroulement simple.",
			"traiteur,repas,reception,buffet,cuisine-marocaine,service,menu,diner,evenement,mariage"},

		{"Salamoun Makeup Artist - Salma Hammioui",
			"Salamoun Makeup Artist propose une mise en beaute elegante pour les mariees qui veulent un resultat travaille, moderne et lumineux. La prestation valorise le visage, les coiffures et l'harmonie avec les tenues de mariage.",
			"maquillage,coiffure,makeup,beauty,bride,glamour,hairstyle,preparation,photos,caftan"},

		{"Salle Billionaire Fes",
			"Salle Billionaire Fes offre un espace de reception adapte aux mariages elegants, avec une organisation pensee pour les grandes soirees. La salle convient aux couples qui veulent un cadre moderne, une ambiance structuree et une reception facile a mettre en scene.",
			"salle-reception,espace-evenement,decoration,ceremonie,diner,grande-capacite,ambiance,organisation,soiree,mariage"},

		{"Salle de fete El Ouazzani",
			"Salle de Fete El Ouazzani propose un cadre accessible et fonctionnel pour organiser une reception familiale. Le lieu convient aux mariages qui veulent une salle simple, pratique et adaptable aux besoins de la ceremonie.",
			"salle-reception,mariage-familial,diner,ceremonie,espace-evenement,organisation,decoration,invites,ambiance,reception"},

		{"Salle des Fetes Dar Hajji",
			"Salle des Fetes Dar Hajji met a disposition un espace pense pour accueillir les mariages avec confort et organisation. La salle convient aux familles qui veulent une reception claire, structuree et adaptee aux etapes classiques de la soiree.",
			"salle-reception,evenement,ceremonie,diner,mariage-familial,organisation,decoration,ambiance,service,invites"},

		{"Salle The Queen",
			"Salle The Queen propose un espace de reception elegant pour les mariages qui recherchent une atmosphere soignee et une presentation valorisante. Le lieu convient aux soirees avec diner, animation et mise en scene des moments importants.",
			"salle-reception,espace-mariage,decoration,diner,ceremonie,ambiance,organisation,soiree,reception,invites"},

		{"Sekkate Traiteur",
			"Sekkate Traiteur propose une cuisine de mariage genereuse, pensee pour les grands services et les repas familiaux. La prestation convient aux couples qui veulent un menu efficace, une organisation claire et une table bien servie.",
			"traiteur,cuisine-marocaine,repas,buffet,service,diner,menu,reception,plats,mariage"},

		{"Souma Makeup",
			"Souma Makeup accompagne la mariee avec une mise en beaute sophistiquee, pensee pour les photos, la lumiere et les changements de tenues. Le style met l'accent sur un rendu elegant, feminin et durable tout au long de la soiree.",
			"maquillage,coiffure,makeup,beauty,bride,glamour,teint,hairstyle,photos,preparation"},

		{"The Cake House by Chaym",
			"The Cake House by Chaym realise des gateaux de mariage et douceurs personnalisees avec une attention portee a la presentation. La prestation convient aux couples qui veulent un dessert esthetique, agreable a photographier et adapte au theme de la soiree.",
			"cake-design,gateau,dessert,patisserie,table-sucree,decoration,douceurs,personnalise,mariage,ceremonie"},

		{"Traiteur Al Jawda",
			"Traiteur Al Jawda propose un service de restauration pense pour les receptions marocaines, avec des plats genereux et une organisation adaptee aux familles. Une option solide pour les mariages qui veulent un repas simple, efficace et bien servi.",
			"traiteur,repas,cuisine-marocaine,buffet,service,diner,menu,plats,reception,mariage"},

		{"Traiteur El Amane",
			"Traiteur El Amane accompagne les mariages avec une cuisine conviviale et un service oriente reception familiale. La prestation convient aux couples qui cherchent un traiteur accessible, organise et capable d'assurer les moments essentiels du repas.",
			"traiteur,service,repas,cuisine,menu,diner,buffet,plats,reception,mariage"},

		{"Traiteur El Kortbi",
			"Traiteur El Kortbi propose des prestations culinaires adaptees aux evenements de mariage, avec une attention portee au service et a la presentation des plats. Une solution pratique pour organiser un diner fluide et genereux.",
			"traiteur,cuisine-marocaine,repas,plats,buffet,service,menu,diner,reception,evenement"},

		{"Yassine Makeup & Hair",
			"Yassine Makeup & Hair propose une mise en beaute complete, avec maquillage et coiffure adaptes aux mariees qui veulent un rendu structure, net et photographique. La prestation convient aux ceremonies modernes comme aux soirees traditionnelles.",
			"maquillage,coiffure,makeup,hair,bride,beauty,shooting,preparation,glamour,caftan"},

		{"Younes Rbati",
			"Younes Rbati apporte une animation orchestrale forte, adaptee aux grandes receptions et aux soirees de mariage tres vivantes. Sa prestation convient aux couples qui veulent une ambiance musicale marquee, festive et capable de porter toute la soiree.",
			"orchestre,musique-live,animation,chant,scene,soiree,danses,ambiance-festive,mariage,entree"},

		{"Youssef Wahbi",
			"Youssef Wahbi propose une animation orchestrale elegante et rythmee, pensee pour accompagner les moments cles du mariage. Son style convient aux entrees, aux danses familiales et aux soirees ou la musique live occupe une place centrale.",
			"orchestre,animation,musique-live,chant,soiree,scene,danse,ambiance,entree,mariage"},
	};

	public static void main(String[] args) throws Exception {
		MySQLDataSource ds = new MySQLDataSource("ayora_db", "root", "");
		List<String> updated = new ArrayList<String>();
		List<String> missing = new ArrayList<String>();
		Map<String, List<String>> duplicateDescriptions = new LinkedHashMap<String, List<String>>();

		try (Connection c = ds.getConnection()) {
			c.setAutoCommit(true);

			try (PreparedStatement check = c.prepareStatement(
					"SELECT id FROM vendors WHERE name = ?");
				 PreparedStatement update = c.prepareStatement(
					"UPDATE vendors SET description = ?, tags = ? WHERE name = ?")) {

				for (String[] row : DATA) {
					String name = row[0];
					String desc = row[1];
					String tags = row[2];

					check.setString(1, name);
					try (ResultSet rs = check.executeQuery()) {
						if (!rs.next()) { missing.add(name); continue; }
					}

					update.setString(1, desc);
					update.setString(2, tags);
					update.setString(3, name);
					int n = update.executeUpdate();
					if (n > 0) updated.add(name);
					else missing.add(name + " (existe mais UPDATE=0)");
				}
			}

			// === Verification 1 : descriptions uniques ===
			Map<String, List<String>> descToNames = new HashMap<String, List<String>>();
			try (PreparedStatement ps = c.prepareStatement(
					"SELECT name, description FROM vendors WHERE description IS NOT NULL");
				 ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String n = rs.getString("name");
					String d = rs.getString("description");
					if (d == null) continue;
					List<String> list = descToNames.get(d);
					if (list == null) { list = new ArrayList<String>(); descToNames.put(d, list); }
					list.add(n);
				}
			}
			for (Map.Entry<String, List<String>> e : descToNames.entrySet()) {
				if (e.getValue().size() > 1) duplicateDescriptions.put(e.getKey(), e.getValue());
			}

			// === Verification 2 : tags propres (pas de nom, pas de ville, pas de gamme) ===
			List<String> tagWarnings = new ArrayList<String>();
			String[] forbiddenTagWords = {"premium", "moyen", "economique", "luxe", "fes", "fez", "casablanca", "rabat", "marrakech"};
			try (PreparedStatement ps = c.prepareStatement(
					"SELECT name, tags FROM vendors WHERE id IN (SELECT id FROM vendors WHERE name IN (" + placeholders(DATA.length) + "))")) {
				for (int i = 0; i < DATA.length; i++) ps.setString(i + 1, DATA[i][0]);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String n = rs.getString("name");
						String t = rs.getString("tags");
						if (t == null) continue;
						String low = t.toLowerCase();
						for (String w : forbiddenTagWords) {
							if (low.matches(".*\\b" + w + "\\b.*")) {
								tagWarnings.add(n + " contient le mot interdit '" + w + "'");
							}
						}
						String[] firstWord = n.toLowerCase().split("\\s+");
						if (firstWord.length > 0 && firstWord[0].length() >= 4 && low.contains(firstWord[0])) {
							tagWarnings.add(n + " : tag contient le nom '" + firstWord[0] + "'");
						}
					}
				}
			}

			// === Rapport ===
			System.out.println("\n========================================================");
			System.out.println(" RAPPORT MISE A JOUR PRESTATAIRES");
			System.out.println("========================================================");
			System.out.println("Demandes : " + DATA.length);
			System.out.println("MAJ OK   : " + updated.size());
			System.out.println("Manquants: " + missing.size());
			if (!missing.isEmpty()) {
				System.out.println("\nPRESTATAIRES NON TROUVES EN BASE :");
				for (String m : missing) System.out.println("  - " + m);
			}
			if (!duplicateDescriptions.isEmpty()) {
				System.out.println("\nDESCRIPTIONS DUPLIQUEES (toutes vendors confondus) :");
				for (Map.Entry<String, List<String>> e : duplicateDescriptions.entrySet()) {
					System.out.println("  Description partagee par " + e.getValue());
				}
			} else {
				System.out.println("\nOK : toutes les descriptions sont uniques.");
			}
			if (!tagWarnings.isEmpty()) {
				System.out.println("\nWARNING TAGS :");
				for (String w : tagWarnings) System.out.println("  - " + w);
			} else {
				System.out.println("OK : aucun tag interdit detecte.");
			}
			System.out.println("========================================================\n");
		}
	}

	private static String placeholders(int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) { if (i > 0) sb.append(","); sb.append("?"); }
		return sb.toString();
	}
}
