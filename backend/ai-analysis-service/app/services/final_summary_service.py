from app.clients.gemini_client import generate_content_with_retry
from app.schemas.sec_rag import SecRiskClaim


def generate_final_summary(
        claims: list[SecRiskClaim]
) -> str:

    if not claims:
        return (
            "The retrieved SEC filing evidence was insufficient "
            "to produce a fully supported answer."
        )

    claims_text = "\n".join(
        f"- {claim.claim}"
        for claim in claims
    )

    prompt = f"""
You are writing the final summary of a financial research answer.

The claims below have already been verified against SEC filing evidence.

Write one concise paragraph summarizing ONLY these verified claims.

Rules:
- Use only the verified claims provided below.
- Do not introduce new facts.
- Do not add explanations, assumptions, or outside knowledge.
- Do not strengthen the claims.
- Preserve important qualifications from the claims.
- Return only the summary paragraph.

VERIFIED CLAIMS:

{claims_text}
""".strip()

    response = generate_content_with_retry(
        model="gemini-3.6-flash",
        contents=prompt
    )

    return response.text.strip()