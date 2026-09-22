import time

from app.clients.gemini_client import generate_content_with_retry
from app.services.semantic_search_service import (semantic_search, search_sec_risk_factors)
from app.schemas.sec_rag import SecRiskAnswer
from app.services.citation_validator import validate_citations
from app.services.answer_repair_service import repair_answer
from app.services.evidence_verifier import (
    verify_evidence,
    has_unsupported_claims,
    filter_supported_claims
)
from app.services.final_summary_service import (generate_final_summary)

def answer_question(ticker: str, question: str, top_k: int = 3):
    search_results = semantic_search(ticker=ticker, query=question, top_k=top_k)

    sources = [
        result["document"]
        for result in search_results
    ]

    context = "\n\n".join(sources)

    prompt = f"""
    You are a financial analysis assistant.

    Your task is to answer the user's question based strictly on the provided context.

    Rules:
    1. Use only information explicitly supported by the context.
    2. Do not invent financial figures or facts.
    3. Pay close attention to fiscal years.
    4. When using financial figures, clearly state the relevant fiscal year.
    5. If the answer requires comparing multiple years, use the values in the context to explain the change.
    6. Only say that information is unavailable if the context truly does not contain it.
    7. Do not use outside knowledge.

    Context:
    {context}

    Question:
    {question}
    
    Answer:
    """

    response = generate_content_with_retry(
        model="gemini-3.6-flash",
        contents=prompt
    )

    return {
        "answer": response.text.strip(),
        "sources": sources
    }


def answer_sec_risk_question(
        ticker: str,
        question: str,
        fiscal_year: int | None = None,
        top_k: int = 4
):
    total_start = time.perf_counter()

    timings = {
        "retrieval_ms": 0,
        "generation_ms": 0,
        "verification_ms": 0,
        "repair_ms": 0,
        "reverification_ms": 0,
        "final_summary_ms": 0
    }

    start = time.perf_counter()

    search_results = search_sec_risk_factors(
        ticker=ticker,
        query=question,
        fiscal_year=fiscal_year,
        top_k=top_k
    )

    timings["retrieval_ms"] = round(
        (time.perf_counter() - start) * 1000,
        2
    )

    context = "\n\n---\n\n".join(
        f"[CHUNK {result['chunk_index']}]\n{result['document']}"
        for result in search_results
    )

    prompt = f"""
    You are a financial research assistant.

    Answer the user's question using only the SEC filing
    context provided below.

    Each piece of context is labeled with a chunk ID,
    for example [CHUNK 13].

    For every claim you make:
    - Use only information supported by the provided context.
    - Include the chunk IDs that support that claim.
    - Return source_chunk_ids as integer IDs only.
    - For example: [6, 11, 13].
    - Never cite a chunk that does not support the claim.
    - Never invent a chunk ID.
    - If the provided context does not contain sufficient evidence
      to answer the question, do not create an unsupported claim.
    - Return an empty claims list instead.
    - In that case, briefly state in the summary that the retrieved
      SEC evidence was insufficient to answer the question.

    Company: {ticker.upper()}
    Fiscal year: {fiscal_year}

    SEC filing context:

    {context}

    Question:
    {question}
    """.strip()

    start = time.perf_counter()

    response = generate_content_with_retry(
        model="gemini-3.6-flash",
        contents=prompt,
        config={
            "response_mime_type": "application/json",
            "response_schema": SecRiskAnswer,
        }
    )

    timings["generation_ms"] = round(
        (time.perf_counter() - start) * 1000,
        2
    )

    analysis = SecRiskAnswer.model_validate_json(
        response.text
    )

    # TEMP TEST ONLY
    # analysis.claims.append(
    #     SecRiskClaim(
    #         claim=(
    #             "Apple's dependence on Asian suppliers will cause "
    #             "the company to go bankrupt."
    #         ),
    #         source_chunk_ids=[11]
    #     )
    # )

    # Step 1: Validate citations
    validate_citations(
        answer=analysis,
        search_results=search_results
    )

    # Step 2: Verify whether the evidence supports each claim
    start = time.perf_counter()

    verification = verify_evidence(
        answer=analysis,
        search_results=search_results
    )

    timings["verification_ms"] = round(
        (time.perf_counter() - start) * 1000,
        2
    )

    # Save the first-pass result for evaluation
    initial_verification = verification
    initial_claim_count = len(analysis.claims)

    repair_performed = False

    # Step 3: Try one repair if any claim is not fully supported
    if has_unsupported_claims(verification):
        start = time.perf_counter()

        analysis = repair_answer(
            answer=analysis,
            verification=verification,
            search_results=search_results
        )

        timings["repair_ms"] = round(
            (time.perf_counter() - start) * 1000,
            2
        )

        repair_performed = True

        # Validate repaired citations
        validate_citations(
            answer=analysis,
            search_results=search_results
        )

        # Verify repaired claims again
        start = time.perf_counter()

        verification = verify_evidence(
            answer=analysis,
            search_results=search_results
        )

        timings["reverification_ms"] = round(
            (time.perf_counter() - start) * 1000,
            2
        )

    # Step 4: Keep only fully supported claims
    supported_claims = filter_supported_claims(
        answer=analysis,
        verification=verification
    )

    # Step 5: Generate the final summary using only
    # fully supported claims
    start = time.perf_counter()

    final_summary = generate_final_summary(
        claims=supported_claims
    )

    timings["final_summary_ms"] = round(
        (time.perf_counter() - start) * 1000,
        2
    )

    # Step 6: Build the final answer
    final_answer = SecRiskAnswer(
        summary=final_summary,
        claims=supported_claims
    )

    initial_supported = sum(
        1
        for item in initial_verification.status
        if item.status == "SUPPORTED"
    )

    initial_partial = sum(
        1
        for item in initial_verification.status
        if item.status == "PARTIALLY_SUPPORTED"
    )

    initial_unsupported = sum(
        1
        for item in initial_verification.status
        if item.status == "UNSUPPORTED"
    )

    timings["total_ms"] = round(
        (time.perf_counter() - total_start) * 1000,
        2
    )

    evaluation_trace = {
        "initial_claim_count": initial_claim_count,
        "initial_supported": initial_supported,
        "initial_partial": initial_partial,
        "initial_unsupported": initial_unsupported,
        "repair_performed": repair_performed,
        "final_claim_count": len(final_answer.claims),
        "final_supported": len(final_answer.claims),
        "timings": timings,
    }



    return {
        "ticker": ticker.upper(),
        "fiscal_year": fiscal_year,
        "question": question,

        "answer": final_answer.model_dump(),

        "verification": verification.model_dump(),

        "repair_performed": repair_performed,

        "evaluation_trace": evaluation_trace,

        "sources": [
            {
                "chunk_index": result["chunk_index"],
                "score": result["score"],
                "form_type": result["form_type"],
                "section": result["section"],
                "excerpt": result["document"][:500]
            }
            for result in search_results
        ]
    }
