from app.clients.gemini_client import generate_content_with_retry
from app.schemas.sec_rag import SecRiskAnswer
from app.schemas.evidence_verification import EvidenceVerificationResult


def repair_answer(
        answer: SecRiskAnswer,
        verification: EvidenceVerificationResult,
        search_results: list[dict]
) -> SecRiskAnswer:

    chunk_map = {
        result["chunk_index"]: result["document"]
        for result in search_results
    }

    evidence_context = "\n\n---\n\n".join(
        f"[CHUNK {chunk_id}]\n{document}"
        for chunk_id, document in chunk_map.items()
    )

    failed_verifications = [
        item
        for item in verification.status
        if item.status != "SUPPORTED"
    ]

    failed_context = "\n\n".join(
        (
            f"CLAIM:\n{item.claim}\n\n"
            f"VERIFICATION STATUS:\n{item.status}\n\n"
            f"VERIFIER FEEDBACK:\n{item.reasoning}"
        )
        for item in failed_verifications
    )

    prompt = f"""
You are repairing a financial research answer.

The previous answer contains one or more claims that were not
fully supported by the cited SEC filing evidence.

Your task is to produce a corrected answer.

Rules:
- Use only the SEC evidence provided below.
- Preserve claims that are fully supported.
- Rewrite or remove claims that are partially supported or unsupported.
- Do not add new facts.
- Every claim must include at least one supporting chunk ID.
- source_chunk_ids must contain integer IDs only.
- Never cite a chunk that does not support the claim.
- Never invent a chunk ID.

PREVIOUS ANSWER:

{answer.model_dump_json(indent=2)}

VERIFIER FEEDBACK:

{failed_context}

AVAILABLE SEC EVIDENCE:

{evidence_context}
""".strip()

    response = generate_content_with_retry(
        model="gemini-3.6-flash",
        contents=prompt,
        config={
            "response_mime_type": "application/json",
            "response_schema": SecRiskAnswer,
        }
    )

    return SecRiskAnswer.model_validate_json(
        response.text
    )