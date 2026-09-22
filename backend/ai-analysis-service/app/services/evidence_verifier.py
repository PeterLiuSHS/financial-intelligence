from app.clients.gemini_client import generate_content_with_retry
from app.schemas.sec_rag import SecRiskAnswer
from app.schemas.evidence_verification import EvidenceVerificationResult

def verify_evidence(
        answer: SecRiskAnswer,
        search_results: list[dict]
) -> EvidenceVerificationResult:

    chunk_map = {
        result["chunk_index"]: result["document"]
        for result in search_results
    }

    verification_items = []

    for claim in answer.claims:

        evidence_parts = []

        for chunk_id in claim.source_chunk_ids:
            document = chunk_map[chunk_id]

            evidence_parts.append(
                f"[CHUNK {chunk_id}]\n{document}"
            )

        evidence = "\n\n---\n\n".join(evidence_parts)

        verification_items.append(
            f"""
CLAIM: {claim.claim}

CITED EVIDENCE: {evidence}
""".strip()
        )

    verification_context = "\n\n=======================\n\n".join(verification_items)

    prompt = f"""
You are an evidence verification system for financial research.

Your job is NOT to answer the user's original question.

Your only job is to determine whether each claim is supported
by its cited SEC filing evidence.

For each claim, classify it as exactly one of:

SUPPORTED:
The cited evidence directly supports the claim.

PARTIALLY_SUPPORTED:
Only part of the claim is supported, or the claim is broader
or stronger than the cited evidence.

UNSUPPORTED:
The cited evidence does not support the claim.

Be strict.
Do not use outside knowledge.
Do not assume facts that are not explicitly supported by
the cited evidence.

Claims and evidence:

{verification_context}
""".strip()

    response = generate_content_with_retry(
        model="gemini-3.6-flash",
        contents=prompt,
        config={
            "response_mime_type": "application/json",
            "response_schema": EvidenceVerificationResult,
        }
    )

    return EvidenceVerificationResult.model_validate_json(
        response.text
    )

def has_unsupported_claims(
        verification: EvidenceVerificationResult
) -> bool:

    return any(
        item.status != "SUPPORTED"
        for item in verification.status
    )

def filter_supported_claims(
        answer: SecRiskAnswer,
        verification: EvidenceVerificationResult
) -> list:

    supported_claim_texts = {
        item.claim
        for item in verification.status
        if item.status == "SUPPORTED"
    }

    return [
        claim
        for claim in answer.claims
        if claim.claim in supported_claim_texts
    ]