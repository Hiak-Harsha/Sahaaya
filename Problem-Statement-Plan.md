# H5 ASHA Companion Research Brief

Oct 18, 2026 · @Yogesh

H5 is a safety-and-efficiency problem, not a chatbot problem: cited offline answers, hard-coded danger-sign escalation, a prioritised visit list and a measured footprint.

## What the problem statement means

The user is an ASHA (village-level) or ANM (sub-centre nurse-midwife): a trained paramedical worker, not a doctor. India has over 10.29 lakh ASHAs as of June 2024, with a norm of about one per 1,000 people ([ASHA overview, Vikaspedia](<https://en.vikaspedia.in/viewcontent/health/nrhm/national-health-mission/initiatives-for-community-participation-under-nhm/accredited-social-health-activist-(asha)?lgn=en>)).

The setting is a village with weak or no signal, a low-end phone, and heavy maternal, child and chronic-disease follow-up work.

The task has five parts:

- Answer questions only from the official protocols, with a citation.
- Work offline.
- Escalate danger signs before any AI reasoning.
- Refuse out-of-scope questions (such as antibiotic doses) and redirect to a doctor.
- Generate a prioritised daily visit list.

This research brief comes from public sources. We have not seen the provided corpus or evaluate.py, so clinical thresholds must come from the provided protocols, not from this document.

## What the judges are expecting

Each required output has a clear way to score well and a clear way to lose points. The rules add that the jury may ask for a demo, architecture and code walkthrough, and AI-generated code you cannot explain can hurt evaluation.

| Expected output | What it means for you | Easy way to lose points |
| --- | --- | --- |
| Protocol-grounded Q&A | Every answer shows the protocol section ID | Answers without citations, or from model memory |
| Offline mode and footprint | Model size, RAM and CPU response time, all measured | Claiming "lightweight" without numbers |
| Hard-coded danger-sign layer | Runs before the model; 100% on the escalation set | One missed phrasing fails the criterion |
| Daily visit list | Who is due for what, prioritised | A plain list with no priority logic |
| Refusal on out-of-scope | Clear redirect to a doctor | Answering a dosage question |
| Stretch goals | Voice input in one language; sync-when-connected queue | Spending time here before the core works |

## The real-life problems (before any solution)

Nine problems recur across studies and field reports. Each is listed with the evidence.

**A. Knowledge is hard to reach in the field**

- ASHAs rely on bulky flipbooks, complex newborn checklists and multiple reporting formats, which is hard for low-literate workers ([mSakhi project description](https://www.qualcomm.com/media/documents/files/india-msakhi.pdf)).
- In a Karnataka study, only 43% of ASHAs knew convulsions are a danger sign during labour ([Belagavi study](https://doaj.org/article/b9caf86cda734e8087acec4da8166883)).
- In Punjab, about 90% of ASHAs could not readily name several of their job responsibilities ([Punjab appraisal](https://demodspace.chitkara.edu.in/handle/123456789/74)).

**B. Danger signs kill when missed or referred late**

- Obstetric haemorrhage is the leading cause of maternal death in India (47% of maternal deaths in one nationally representative analysis; infection 12%, hypertensive disorders 7%) ([PMC analysis](https://pmc.ncbi.nlm.nih.gov/articles/PMC9292773)).
- National danger-sign lists include vaginal bleeding, convulsions, severe headache with blurred vision, high fever and high blood pressure ([Vikaspedia](https://en.vikaspedia.in/health/women-health/pregnancy-health-1/danger-signs-during-the-course-of-a-pregnancy)). The NHSRC training deck treats bleeding and convulsions as emergencies needing referral ([NHSRC](https://www.nhsrcindia.org/sites/default/files/2022-11/Emergency%20Care%20for%20FLW%20-%20Obstetric%20and%20Neonatal%20Emergencies.pdf)).

**C. Connectivity and devices**

- About 1,700 AP villages are reported to have no mobile network (around 1,000 in Alluri Sitharama Raju district), and about 3,160 lack 4G ([Telecom Talk](https://telecomtalk.info/?p=654511)).
- Workers report poor connectivity, glitches and poor-quality phones that make digital tools unreliable ([Ideas for India](https://www.ideasforindia.in/topics/productivity-innovation/is-digitalisation-a-double-edged-sword-for-workers-in-indias-public-healthcare-system)), and some buy their own data packs ([Wadhwani AI](https://www.wadhwaniai.org/?p=8694)).
- The Suryapet (Telangana) study lists limited smartphone and internet access, insufficient training, usability issues, language and literacy barriers, and data security concerns ([AJOL](https://www.ajol.info/index.php/jcmphc/article/download/303807/285582)).

**D. App overload and double work**

- One ASHA described juggling seven apps plus WhatsApp groups and sheets, and doing everything twice, on paper and online ([New Lines](https://newlinesmag.com/reportage/indias-digital-health-push-is-overworking-its-front-line-women/)).
- A 2024 paper reports AP ANMs maintain at least 28 registers and 23 mobile apps ([IJCMPH](https://www.ijcmph.com/index.php/ijcmph/article/download/16413/9807/85078)). Verify the exact figure before quoting it.
- Apps with layered menus and English terminology are hard to use even for workers comfortable with WhatsApp. ASHA collectives have also pushed back on a mandatory app, which was later recalled ([Ideas for India](https://www.ideasforindia.in/topics/productivity-innovation/is-digitalisation-a-double-edged-sword-for-workers-in-indias-public-healthcare-system)).

**E. Language and code-mixed questions**

- On real Indian patient queries, factual correctness is generally lower for Indic-language queries, and code-mixed queries are especially hard ([Health-Pariksha](https://arxiv.org/abs/2410.13671v1)).

**F. Follow-up planning is a constant burden**

- Newborn home visits follow a fixed schedule: day 3, 7, 14, 21, 28 and 42 for SNCU-discharged babies, with a first visit within 24 hours of discharge ([HBNC portal](https://hbnc-hbyc.mohfw.gov.in/AboutUs/aboutHBNC)).
- Immunisation has many windows (BCG, OPV-0 and Hep B at birth; boosters at 16-24 months; DPT booster at 5-6 years; Td at 10 and 16 years), and a missed schedule is continued, not restarted ([Odisha NHM UIP slides](https://nhmodisha.gov.in/wp-content/uploads/2024/02/UIP.pdf)).
- NCD care adds screening and 3-month follow-ups for people aged 30 and above. Nearly half of people with diabetes or hypertension are estimated to be undetected ([MoHFW screening manual](https://npcbvi.mohfw.gov.in/Douments/Training_Manuals/Expanded_services_HWC_non_Eye/Prevention_Control_PBS_of_Hypertension.pdf)).

**G. Scope limits**

- ASHAs have a drug kit and a first-contact care role, but not a doctor's prescribing authority ([ASHA Module 6](https://hbnc-hbyc.mohfw.gov.in/assets/document/ASHA_Training_Module_6.pdf)). The tool must not become a prescribing assistant.

**H. AI-specific risks**

- WHO warns that large AI models can give plausible but wrong health answers ([WHO guidance summary](https://www.downtoearth.org.in/science-technology/who-releases-guidelines-for-multi-modal-generative-ai-in-healthcare-resonates-with-recommendations-for-other-sectors-93991)).
- A 2026 systematic review concludes the most credible path is hybrid control: retrieval grounding, verification, calibrated generation and human oversight ([ITM Web Conf.](https://www.itm-conferences.org/10.1051/itmconf/20268403005)).

**I. Tool-specific pitfall**

- India uses MUAC below 115 mm to flag severe acute malnutrition ([PMC analysis](https://pmc.ncbi.nlm.nih.gov/articles/PMC10271291)), but one Madhya Pradesh survey found low sensitivity at that cut-off (17.5%) ([Indian Pediatrics](https://indianpediatrics.net/jan2013/jan-154-155.htm)). Use only the thresholds in the provided protocol, and add a "refer if unsure" path.

## From problems to your solution

Each design decision answers a documented problem and covers a stated hackathon expectation.

| Problem | Design decision | Expectation covered |
| --- | --- | --- |
| No signal, low-end phone | All core logic on-device: quantised model plus small embeddings, with a measured footprint report | Offline mode and footprint |
| Missed danger signs | Deterministic rule layer (keywords, normalised multilingual phrases, vitals thresholds) that runs first and returns "refer now" plus the protocol section | 100% escalation detection |
| LLM hallucination | Extractive-first: return protocol text with its section ID, use the model only to rephrase, refuse if retrieval confidence is low | Grounded and cited answers |
| Code-mixed language | Normalise transliteration and spelling; multilingual embeddings plus keyword (BM25) retrieval | Q&A on the 200 code-mixed questions |
| Out-of-scope questions | Intent gate for dosage, diagnosis and prescribing, with a fixed refusal and doctor redirect | Refusal behaviour |
| Follow-up burden | Visit-list engine from the register: danger flags first, then overdue items, then upcoming due dates | Daily visit list |
| App fatigue | One screen, short answers, big buttons, minimal typing | Usability in the demo |
| Unsynced data | Local queue that syncs when connectivity returns | Stretch goal |
| Voice | Voice input in one language, only after the core works | Stretch goal |

Proposed pipeline, in order:

1. Emergency rules scan the input first and can end the flow with a referral.
2. An intent gate refuses dosage, diagnosis and prescribing questions.
3. Retrieval finds the matching protocol sections.
4. The answer returns the protocol text with its section ID, or declines if confidence is low.

**Research behind the design**

- Hybrid retrieval, dense embeddings plus BM25, often improves precision and recall ([Healthier LLMs paper](https://arxiv.org/pdf/2607.06641)).
- A maternal-health chatbot paper evaluates safety routing separately from retrieval quality and keeps retrieval on the original-language query for code-mixed text ([arXiv 2603.13168](https://arxiv.org/pdf/2603.13168)). This supports running rules before the model.
- SMARThealth GPT built a RAG system on Indian pregnancy guidelines for community health workers ([medRxiv](https://medrxiv.org/node/741058)). Its team stresses improving the existing ASHA workflow, not replacing it ([George Institute](https://www.georgeinstitute.org/news-and-media/news/building-ai-solutions-based-on-essential-needs-not-just-hype-part-2)).
- On-device RAG with small models is feasible: a 2026 Android study reports 94.5% accuracy on first aid guidance ([Pocket RAG](https://www.arxiv.org/pdf/2602.13229)).

**Device realities**

- A 7B model at 4-bit needs roughly 4-6 GB of RAM, and mid-range Android phones with 4 GB often cannot run it reliably ([OMR](https://omr.it.com/blog/shipping-on-device-llms-mobile-apps-architecture-tradeoffs/)).
- Target models of roughly 0.5-1B parameters. A 0.5B model at 8-bit is around 600 MB ([RunAnywhere llama.cpp notes](https://pub.dev/packages/runanywhere_llamacpp/versions/0.20.10)). Use whichever model the organisers pre-cached.

## What can make you stand out

1. **A safety-first architecture slide:** emergency rules, then intent gate, then retrieval, then a cited answer or refusal.
2. **Published numbers:** rule-layer recall on your own test set (aim for 100%), refusal accuracy, citation rate, model size in MB, peak RAM, and median and p95 CPU latency.
3. **Your own escalation test set:** 150+ phrasings in English, Telugu, Hinglish and transliterated forms with typos, so you can show tested coverage.
4. **An explainable visit-list rule:** danger flags first, then overdue items, then nearest due date.
5. **Fewer apps, not one more app:** position the tool as reducing the burden, which matches the field evidence on app overload.
6. **Honest limitations:** small model, MUAC pitfalls and a clear "clinician decides" message.

## Caveats

- Take clinical thresholds from the provided corpus, not from this brief or from memory. The public sources here are for context and justification.
- Some figures come from secondary sites (Vikaspedia, current-affairs pages). Check primary sources before quoting.
- Maternal mortality estimates differ by source (SRS 2021-23: 88 per lakh; a UN-based 2023 estimate: 116 per lakh, per [Vajiram & Ravi](https://vajiramandravi.com/current-affairs/maternal-mortality-ratio/)). Name the source when you cite one.
- The BharatGPT RAG claims are from a vendor blog. Treat them as a lead, not proof.
- No Talent AP past-winner data was found; this is likely a first edition.

## How to cite sources in the PPT

Add short tags like \[1\] on the problem slide and a References slide, using 5-8 strong sources rather than all of them. A good set:

- MoHFW HBNC/HBYC pages
- The UIP schedule
- The NHSRC emergency care deck
- The maternal mortality causes paper
- New Lines or Ideas for India for app burden
- The Telangana digital-challenges study
- George Institute / SMARThealth for RAG
- WHO guidance on large multimodal models

## Sources

Pages listed come from search results gathered on 18 Oct 2026.

**Field reality and digital burden**

- [Digital health push overworking front-line women (New Lines)](https://newlinesmag.com/reportage/indias-digital-health-push-is-overworking-its-front-line-women/)
- [Is digitalisation a double-edged sword? (Ideas for India)](https://www.ideasforindia.in/topics/productivity-innovation/is-digitalisation-a-double-edged-sword-for-workers-in-indias-public-healthcare-system)
- [Digital challenges of CHWs in Telangana (AJOL)](https://www.ajol.info/index.php/jcmphc/article/download/303807/285582)
- [Over-reliance on frontline health workers, AP 2024 (IJCMPH)](https://www.ijcmph.com/index.php/ijcmph/article/download/16413/9807/85078)
- [Wadhwani AI ASHA user research](https://www.wadhwaniai.org/?p=8694)
- [ASHA programme overview (Vikaspedia)](<https://en.vikaspedia.in/viewcontent/health/nrhm/national-health-mission/initiatives-for-community-participation-under-nhm/accredited-social-health-activist-(asha)?lgn=en>)

**Connectivity**

- [1,700 AP villages with no mobile network (Telecom Talk)](https://telecomtalk.info/?p=654511)

**Protocols and clinical context**

- [Danger signs in pregnancy (Vikaspedia)](https://en.vikaspedia.in/health/women-health/pregnancy-health-1/danger-signs-during-the-course-of-a-pregnancy)
- [NHSRC emergency care for frontline workers](https://www.nhsrcindia.org/sites/default/files/2022-11/Emergency%20Care%20for%20FLW%20-%20Obstetric%20and%20Neonatal%20Emergencies.pdf)
- [HBNC/HBYC portal (MoHFW)](https://hbnc-hbyc.mohfw.gov.in/AboutUs/aboutHBNC)
- [UIP presentation (Odisha NHM)](https://nhmodisha.gov.in/wp-content/uploads/2024/02/UIP.pdf)
- [Maternal mortality causes (PMC)](https://pmc.ncbi.nlm.nih.gov/articles/PMC9292773)
- [Intranatal and postnatal knowledge of ASHAs (DOAJ)](https://doaj.org/article/b9caf86cda734e8087acec4da8166883)
- [Population-based NCD screening manual (MoHFW)](https://npcbvi.mohfw.gov.in/Douments/Training_Manuals/Expanded_services_HWC_non_Eye/Prevention_Control_PBS_of_Hypertension.pdf)
- [MUAC cut-off validation (Indian Pediatrics)](https://indianpediatrics.net/jan2013/jan-154-155.htm)
- [STOP Diarrhoea guidance (NHM HP)](https://nhm.hp.gov.in/storage/app/media/uploaded-files/Guidance.pdf)

**Existing tools**

- [ANMOL, RCH portal, Kilkari, Mobile Academy (MoHFW)](https://main.mohfw.gov.in/sites/default/files/MMPC%20Brief.pdf)
- [mSakhi (Qualcomm/IntraHealth)](https://www.qualcomm.com/media/documents/files/india-msakhi.pdf)
- [PIERS on the Move evaluation (DOAJ)](https://doaj.org/article/158f5c65cced43ff9300ad77928aba67)

**AI evidence**

- [SMARThealth GPT (medRxiv)](https://medrxiv.org/node/741058)
- [George Institute blog on AI for ASHAs](https://www.georgeinstitute.org/news-and-media/news/building-ai-solutions-based-on-essential-needs-not-just-hype-part-2)
- [Maternal health chatbot paper (arXiv)](https://arxiv.org/pdf/2603.13168)
- [Health-Pariksha (arXiv)](https://arxiv.org/abs/2410.13671v1)
- [Healthier LLMs, RAG for public health QA (arXiv)](https://arxiv.org/pdf/2607.06641)
- [Pocket RAG on Android (arXiv)](https://www.arxiv.org/pdf/2602.13229)
- [Medical hallucination review (ITM Web Conf.)](https://www.itm-conferences.org/10.1051/itmconf/20268403005)
- [WHO guidance on large multimodal models (summary)](https://www.downtoearth.org.in/science-technology/who-releases-guidelines-for-multi-modal-generative-ai-in-healthcare-resonates-with-recommendations-for-other-sectors-93991)
- [On-device LLM tradeoffs (OMR)](https://omr.it.com/blog/shipping-on-device-llms-mobile-apps-architecture-tradeoffs/)
