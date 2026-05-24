package com.ayora.config;

import com.ayora.dao.AdminStatsDao;
import com.ayora.dao.DevisDao;
import com.ayora.dao.GuestDao;
import com.ayora.dao.InvitationDao;
import com.ayora.dao.QuestionnaireDao;
import com.ayora.dao.RecommendationDao;
import com.ayora.dao.RendezVousDao;
import com.ayora.dao.SubscriptionDao;
import com.ayora.dao.UserDao;
import com.ayora.dao.UserPickDao;
import com.ayora.dao.VendorDao;
import com.ayora.assistant.AssistantContextBuilder;
import com.ayora.assistant.AssistantConversationStore;
import com.ayora.assistant.AssistantOrchestrator;
import com.ayora.assistant.AssistantPromptBuilder;
import com.ayora.assistant.AssistantSafetyGuard;
import com.ayora.assistant.LlmProviderFactory;
import com.ayora.metier.AssistantService;
import com.ayora.metier.AyoraMetier;
import com.ayora.metier.IAyoraMetier;
import com.ayora.util.Database;
import com.ayora.util.MySQLDataSource;

/**
 * Wiring centralise de l'application Ayora.
 *
 * Une seule instance est creee au demarrage pour :
 *   - la DataSource
 *   - la Database
 *   - chacun des 11 DAO
 *   - le metier (AyoraMetier)
 *
 * Les Servlets recuperent la meme instance metier via getMetier().
 */
public final class AppWiring {

	// === 1. Configuration de la base de donnees ===
	private static final String DB_NAME     = "ayora_db";
	private static final String DB_USER     = "root";
	private static final String DB_PASSWORD = "";

	// === 2. DataSource + Database (instances uniques) ===
	private static final MySQLDataSource DATA_SOURCE =
		new MySQLDataSource(DB_NAME, DB_USER, DB_PASSWORD);
	private static final Database DATABASE = new Database(DATA_SOURCE);

	// === 3. DAOs (instances uniques) ===
	private static final UserDao           USER_DAO          = new UserDao(DATABASE);
	private static final VendorDao         VENDOR_DAO        = new VendorDao(DATABASE);
	private static final GuestDao          GUEST_DAO         = new GuestDao(DATABASE);
	private static final InvitationDao     INVITATION_DAO    = new InvitationDao(DATABASE);
	private static final QuestionnaireDao  QUESTIONNAIRE_DAO = new QuestionnaireDao(DATABASE);
	private static final RecommendationDao RECOMMENDATION_DAO= new RecommendationDao(DATABASE);
	private static final SubscriptionDao   SUBSCRIPTION_DAO  = new SubscriptionDao(DATABASE);
	private static final UserPickDao       USER_PICK_DAO     = new UserPickDao(DATABASE);
	private static final DevisDao          DEVIS_DAO         = new DevisDao(DATABASE);
	private static final RendezVousDao     RDV_DAO           = new RendezVousDao(DATABASE);
	private static final AdminStatsDao     ADMIN_STATS_DAO   = new AdminStatsDao(DATABASE);

	// === 4. Metier (instance unique) ===
	private static final IAyoraMetier METIER = new AyoraMetier(
		USER_DAO, VENDOR_DAO, GUEST_DAO, INVITATION_DAO,
		QUESTIONNAIRE_DAO, RECOMMENDATION_DAO, SUBSCRIPTION_DAO, USER_PICK_DAO,
		DEVIS_DAO, RDV_DAO, ADMIN_STATS_DAO);

	// === 5. AYORA Assistant pipeline ============================================
	// Real-LLM-backed assistant. The provider used at runtime is picked from
	// AYORA_AI_PROVIDER / AYORA_MODEL_ENDPOINT / AYORA_CLOUD_API_KEY env vars
	// (see com.ayora.assistant.LlmProviderFactory). When no provider is
	// configured the pipeline degrades to com.ayora.assistant.FallbackProvider,
	// which clearly tells the user it cannot give personalised advice.
	private static final LlmProviderFactory       LLM_FACTORY      = new LlmProviderFactory();
	private static final AssistantContextBuilder  CONTEXT_BUILDER  = new AssistantContextBuilder(METIER);
	private static final AssistantConversationStore CONV_STORE     = new AssistantConversationStore();
	private static final AssistantSafetyGuard     SAFETY_GUARD     = new AssistantSafetyGuard();
	private static final AssistantPromptBuilder   PROMPT_BUILDER   = new AssistantPromptBuilder();
	private static final AssistantOrchestrator    ORCHESTRATOR     = new AssistantOrchestrator(
		LLM_FACTORY, CONTEXT_BUILDER, CONV_STORE, SAFETY_GUARD, PROMPT_BUILDER);
	private static final AssistantService         ASSISTANT        = new AssistantService(ORCHESTRATOR);

	private AppWiring() {
		// utility class : pas d'instance
	}

	// === API publique ===

	/** Point d'entree principal : utilise par tous les Servlets. */
	public static IAyoraMetier getMetier() { return METIER; }

	/** AYORA conversational assistant facade. */
	public static AssistantService getAssistant() { return ASSISTANT; }

	/** Orchestrator handle (servlet uses this for chat + reset endpoints). */
	public static AssistantOrchestrator getAssistantOrchestrator() { return ORCHESTRATOR; }

	/** Provider chain inspector — surfaced by /api/assistant/health. */
	public static LlmProviderFactory getLlmProviderFactory() { return LLM_FACTORY; }

	/** Conversation store handle for reset / inspection. */
	public static AssistantConversationStore getConversationStore() { return CONV_STORE; }

	/** Database brut (utile pour Examples.java en CLI). */
	public static Database getDatabase() { return DATABASE; }
}
