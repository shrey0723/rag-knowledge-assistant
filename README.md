# AI Internal Knowledge Assistant (RAG)

An internal IT/HR knowledge assistant: employees ask plain-English questions
("how long does a corporate card take to arrive?"), and the system retrieves
the most relevant excerpts from ingested internal documents (policies,
guides, runbooks) and generates a grounded answer **with citations back to
the source document** — a real Retrieval-Augmented Generation (RAG)
pipeline, not a chatbot wrapper.

**Stack:** Java 21 / Spring Boot 3 (backend) · React (frontend, CDN, no
build step) · H2 file database · Google Gemini API (embeddings + generation)

---

## 1. Before you start: get a free Gemini API key

1. Go to **[aistudio.google.com](https://aistudio.google.com)** and sign in
   with a Google account.
2. Sidebar → **Get API key** → **Create API key**.
3. Copy the key (starts with `AIza...`). Free tier, no credit card needed —
   generous enough for building and demoing this project.
4. **Never commit this key to git.** Set it as an environment variable
   instead (shown below).

---

## 2. Run it locally

### Backend
Requires **Java 21+** and **Maven** installed.

```bash
cd backend

# macOS/Linux
export GEMINI_API_KEY=your_key_here

# Windows (PowerShell)
$env:GEMINI_API_KEY="your_key_here"

mvn spring-boot:run
```
Starts the API on **http://localhost:4001**. On first run it auto-creates an
H2 database file at `backend/data/ragdb.mv.db` and seeds 3 sample internal
documents (VPN policy, onboarding guide, expense policy).

### Frontend
```bash
cd frontend
python -m http.server 5500
```
Open **http://localhost:5500**.

---

## 3. What it demonstrates

| Skill | Where |
|---|---|
| Java / Spring Boot REST API design | `backend/.../controller/*.java` |
| Real LLM integration (not mocked) | `GeminiClient.java` — calls Gemini's `embedContent` and `generateContent` endpoints directly |
| RAG pipeline | `RagService.java` — chunk → embed → cosine-similarity retrieval → grounded generation → citations |
| Data modeling / JPA | `model/`, `repository/` — Document → Chunk (1:many), auditable QueryLog |
| Frontend (React) | Document ingestion UI, live Q&A with expandable citations, query history |

**How the pipeline works:**
1. A document is submitted → split into ~800-character overlapping chunks (`ChunkingService`)
2. Each chunk is embedded via Gemini's embedding model and stored
3. A question is embedded the same way, then compared against every stored
   chunk using cosine similarity (done in-memory in Java — genuinely
   correct at this scale; a production system with a much larger corpus
   would swap this for an indexed vector store like Postgres + pgvector)
4. The top-K most similar chunks are passed to Gemini as grounding context
5. The model is instructed to answer **only** from that context, and the
   answer is returned alongside the specific chunks it was grounded in

---

## 4. Deploying it live

**Backend → Render:**
1. Push to GitHub, create a Render **Web Service** pointed at this repo —
   `render.yaml` is already configured (root dir `backend`, Maven build,
   runs the packaged jar).
2. In the Render dashboard, add an environment variable: `GEMINI_API_KEY`
   with your key. **Do not put it in render.yaml or in git.**

**Frontend → Vercel/Netlify:**
1. Update `const API = "http://localhost:4001/api"` in `frontend/index.html`
   to your deployed backend URL.
2. Deploy `frontend/` as a static site (drag-and-drop works on both Netlify
   and Vercel for a single `index.html`).

**Note on persistence:** Render's free tier has an ephemeral disk, so the H2
database resets on redeploy — fine for a demo, since it auto-reseeds. For
real persistence, swap H2 for a managed Postgres instance.

---

## 5. Suggested resume bullet

> Built a full-stack Retrieval-Augmented Generation (RAG) system (Java/Spring
> Boot, React) that ingests internal documents, generates embeddings via the
> Gemini API, and answers employee questions with citation-grounded
> responses — including the retrieval, chunking, and grounding pipeline
> end-to-end.

---

## 6. Easy extensions

- Swap H2 + brute-force cosine similarity for Postgres + pgvector (indexed ANN search) at scale
- Add semantic/paragraph-aware chunking instead of fixed-size chunking
- Add authentication (JWT) so different departments only see their own documents
- Add a feedback loop: let users mark answers as helpful/unhelpful and log it for future tuning
- Support file uploads (PDF/Word) instead of pasted text, using a text-extraction step before chunking
