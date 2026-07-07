package com.ittools.ragassistant.seed;

import com.ittools.ragassistant.dto.DocumentRequest;
import com.ittools.ragassistant.repository.DocumentRepository;
import com.ittools.ragassistant.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DocumentRepository documentRepository;
    private final RagService ragService;

    public DataSeeder(DocumentRepository documentRepository, RagService ragService) {
        this.documentRepository = documentRepository;
        this.ragService = ragService;
    }

    @Override
    public void run(String... args) {
        if (documentRepository.count() > 0) {
            return;
        }
        try {
            DocumentRequest vpn = new DocumentRequest();
            vpn.setTitle("VPN Access Policy");
            vpn.setSourceType("policy");
            vpn.setContent("All employees connecting to internal systems from outside the office network must " +
                    "use the company VPN client. VPN access is granted automatically on your first day and uses " +
                    "your single sign-on credentials plus a hardware security key or the Okta Verify app for " +
                    "two-factor authentication. VPN sessions time out after 10 hours of inactivity. If you lose " +
                    "your security key, contact IT Helpdesk immediately to have your VPN access temporarily " +
                    "suspended and a new key issued. Contractors and interns receive VPN access only after their " +
                    "manager submits an access request, and it expires automatically at the end of the contract " +
                    "period.");
            ragService.ingestDocument(vpn);

            DocumentRequest onboarding = new DocumentRequest();
            onboarding.setTitle("New Hire Onboarding Guide");
            onboarding.setSourceType("guide");
            onboarding.setContent("New hires receive a laptop, monitor, and accessories on their first day, " +
                    "shipped to their home address for remote employees or waiting at their desk for office-based " +
                    "staff. Corporate email and Slack access are provisioned automatically the night before your " +
                    "start date. Your manager is responsible for requesting any additional software licenses " +
                    "(Figma, Salesforce, etc.) through the internal Access Request portal, which typically takes " +
                    "1-2 business days to approve. A corporate card, if needed for your role, must be requested " +
                    "separately by your manager and requires Finance approval; it usually arrives within 5-7 " +
                    "business days. All new hires must complete security awareness training within their first " +
                    "two weeks.");
            ragService.ingestDocument(onboarding);

            DocumentRequest expense = new DocumentRequest();
            expense.setTitle("Expense & Corporate Card Policy");
            expense.setSourceType("policy");
            expense.setContent("Employees with a corporate card must submit itemized receipts for any expense " +
                    "over $25 within 14 days of purchase. Expenses without a receipt over this threshold will not " +
                    "be reimbursed. Software subscriptions must be purchased through the IT team rather than " +
                    "personal cards whenever possible, so they can be tracked centrally and included in vendor " +
                    "renewal planning. Travel expenses require pre-approval from your manager for any trip " +
                    "exceeding $1,500 in estimated cost. Lost corporate cards should be reported to Finance and " +
                    "IT within 24 hours so the card can be frozen.");
            ragService.ingestDocument(expense);

            log.info("Seeded 3 sample documents into the knowledge base.");
        } catch (Exception e) {
            log.warn("Skipped seeding sample documents (this is expected if GEMINI_API_KEY isn't set yet): {}",
                    e.getMessage());
        }
    }
}
