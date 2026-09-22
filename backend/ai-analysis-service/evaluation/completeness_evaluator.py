from pydantic import BaseModel

from app.clients.gemini_client import generate_content_with_retry


class CoverageItem(BaseModel):
    point: str
    covered: bool


class CompletenessResult(BaseModel):
    items: list[CoverageItem]


def evaluate_completeness(
        final_claims: list[str],
        expected_points: list[str]
) -> CompletenessResult:
    if not expected_points:
        return CompletenessResult(items=[])

    claims_text = "\n".join(
        f"- {claim}"
        for claim in final_claims
    )

    expected_points_text = "\n".join(
        f"- {point}"
        for point in expected_points
    )

    prompt = f"""
You are evaluating the completeness of an answer produced by a
financial SEC RAG system.

Your task is to determine whether EACH expected point is substantively
covered by the final answer claims.

IMPORTANT RULES:

1. Use ONLY the final answer claims provided below.
2. Do NOT use outside knowledge.
3. Do NOT infer facts that are not clearly communicated by the claims.
4. A point is COVERED when the claims clearly communicate the same
   substantive meaning, even if different wording is used.
5. A point is NOT COVERED if it is only vaguely related, implied,
   or missing.
6. Evaluate every expected point exactly once.
7. Preserve the exact text of each expected point in the output.

FINAL ANSWER CLAIMS:

{claims_text}

EXPECTED POINTS:

{expected_points_text}
""".strip()

    response = generate_content_with_retry(
        model="gemini-3.6-flash",
        contents=prompt,
        config={
            "response_mime_type": "application/json",
            "response_schema": CompletenessResult,
        }
    )

    return CompletenessResult.model_validate_json(
        response.text
    )


def calculate_coverage(
        result: CompletenessResult
) -> float:
    if not result.items:
        return 0.0

    covered_count = sum(
        1
        for item in result.items
        if item.covered
    )

    return covered_count / len(result.items)
