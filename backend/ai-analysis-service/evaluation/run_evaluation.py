import json
from pathlib import Path

from app.services.rag_service import answer_sec_risk_question
from evaluation.completeness_evaluator import (
    evaluate_completeness,
    calculate_coverage,
)

QUESTIONS_FILE = Path(__file__).parent / "questions.json"
RESULTS_FILE = Path(__file__).parent / "results.json"


def load_questions():
    with open(QUESTIONS_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


def run_evaluation():
    questions = load_questions()

    results = []

    total_initial_claims = 0
    total_initial_supported = 0
    total_initial_partial = 0
    total_initial_unsupported = 0

    total_final_claims = 0
    total_final_supported = 0

    repair_triggered = 0
    abstained_questions = 0
    total_latency_ms = 0

    retrieval_evaluated_questions = 0
    retrieval_hits = []
    retrieval_recalls = []
    retrieval_reciprocal_ranks = []

    completeness_evaluated_questions = 0
    completeness_coverages = []

    for index, case in enumerate(questions, start=1):
        print(
            f"[{index}/{len(questions)}] "
            f"{case['ticker']} - {case['question']}"
        )

        try:
            response = answer_sec_risk_question(
                ticker=case["ticker"],
                question=case["question"],
                fiscal_year=case.get("fiscal_year"),
                top_k=4
            )

            trace = response["evaluation_trace"]
            latency_ms = trace["timings"]["total_ms"]

            retrieved_chunk_ids = [
                source["chunk_index"]
                for source in response["sources"]
            ]

            retrieval_metrics = None

            if "expected_chunk_ids" in case:
                expected_ids = case["expected_chunk_ids"]

                hit = hit_at_k(
                    retrieved_chunk_ids,
                    expected_ids
                )

                recall = recall_at_k(
                    retrieved_chunk_ids,
                    expected_ids
                )

                rr = reciprocal_rank(
                    retrieved_chunk_ids,
                    expected_ids
                )

                retrieval_evaluated_questions += 1
                retrieval_hits.append(hit)
                retrieval_recalls.append(recall)
                retrieval_reciprocal_ranks.append(rr)

                retrieval_metrics = {
                    "retrieved_chunk_ids": retrieved_chunk_ids,
                    "expected_chunk_ids": expected_ids,
                    "hit_at_4": hit,
                    "recall_at_4": round(recall, 4),
                    "reciprocal_rank": round(rr, 4)
                }

            completeness_metrics = None

            if "expected_points" in case:
                final_claims = [
                    claim["claim"]
                    for claim in response["answer"]["claims"]
                ]

                completeness_result = evaluate_completeness(
                    final_claims=final_claims,
                    expected_points=case["expected_points"]
                )

                coverage = calculate_coverage(
                    completeness_result
                )

                completeness_evaluated_questions += 1
                completeness_coverages.append(coverage)

                completeness_metrics = {
                    "expected_points": case["expected_points"],
                    "coverage_items": [
                        item.model_dump()
                        for item in completeness_result.items
                    ],
                    "coverage": round(coverage, 4)
                }


            if trace["initial_claim_count"] == 0:
                abstained_questions += 1

            total_initial_claims += trace["initial_claim_count"]
            total_initial_supported += trace["initial_supported"]
            total_initial_partial += trace["initial_partial"]
            total_initial_unsupported += trace["initial_unsupported"]

            total_final_claims += trace["final_claim_count"]
            total_final_supported += trace["final_supported"]

            if trace["repair_performed"]:
                repair_triggered += 1

            total_latency_ms += latency_ms

            results.append({
                "id": case["id"],
                "ticker": case["ticker"],
                "category": case["category"],
                "question": case["question"],
                "latency_ms": round(latency_ms, 2),
                "evaluation_trace": trace,
                "retrieval_metrics": retrieval_metrics,
                "completeness_metrics": completeness_metrics,
                "success": True
            })

            print(
                f"    initial: "
                f"{trace['initial_supported']}/"
                f"{trace['initial_claim_count']} supported"
            )

            print(
                f"    repair: {trace['repair_performed']}"
            )

            print(
                f"    final: "
                f"{trace['final_supported']}/"
                f"{trace['final_claim_count']} supported"
            )

            print(
                f"    latency: {latency_ms / 1000:.2f}s"
            )

        except Exception as e:
            print(f"    FAILED: {e}")

            results.append({
                "id": case["id"],
                "ticker": case["ticker"],
                "category": case["category"],
                "question": case["question"],
                "success": False,
                "error": str(e)
            })

    successful_questions = sum(
        1 for result in results
        if result["success"]
    )

    initial_support_rate = (
        total_initial_supported / total_initial_claims * 100
        if total_initial_claims > 0
        else 0
    )

    final_support_rate = (
        total_final_supported / total_final_claims * 100
        if total_final_claims > 0
        else 0
    )

    repair_trigger_rate = (
        repair_triggered / successful_questions * 100
        if successful_questions > 0
        else 0
    )

    abstain_rate = (
        abstained_questions / successful_questions * 100
        if successful_questions > 0
        else 0
    )

    average_latency_ms = (
        total_latency_ms / successful_questions
        if successful_questions > 0
        else 0
    )

    if retrieval_evaluated_questions > 0:
        hit_at_4 = (
                sum(retrieval_hits)
                / retrieval_evaluated_questions
                * 100
        )

        mean_recall_at_4 = (
                sum(retrieval_recalls)
                / retrieval_evaluated_questions
                * 100
        )

        mrr = (
                sum(retrieval_reciprocal_ranks)
                / retrieval_evaluated_questions
        )
    else:
        hit_at_4 = 0
        mean_recall_at_4 = 0
        mrr = 0

    mean_expected_point_coverage = (
        sum(completeness_coverages)
        / completeness_evaluated_questions
        * 100
        if completeness_evaluated_questions > 0
        else 0
    )

    summary = {
        "total_questions": len(questions),
        "successful_questions": successful_questions,
        "abstained_questions": abstained_questions,
        "abstain_rate": round(abstain_rate, 2),

        "total_initial_claims": total_initial_claims,
        "initial_supported": total_initial_supported,
        "initial_partial": total_initial_partial,
        "initial_unsupported": total_initial_unsupported,
        "initial_support_rate": round(initial_support_rate, 2),

        "repair_triggered_questions": repair_triggered,
        "repair_trigger_rate": round(repair_trigger_rate, 2),

        "total_final_claims": total_final_claims,
        "final_supported": total_final_supported,
        "final_support_rate": round(final_support_rate, 2),

        "average_latency_ms": round(average_latency_ms, 2),

        "retrieval_evaluated_questions": retrieval_evaluated_questions,
        "hit_at_4": round(hit_at_4, 2),
        "mean_recall_at_4": round(mean_recall_at_4, 2),
        "mrr": round(mrr, 4),
        "completeness_evaluated_questions": completeness_evaluated_questions,
        "mean_expected_point_coverage": round(
            mean_expected_point_coverage,
            2
        )
    }

    output = {
        "summary": summary,
        "results": results
    }

    with open(RESULTS_FILE, "w", encoding="utf-8") as f:
        json.dump(
            output,
            f,
            indent=2,
            ensure_ascii=False
        )

    print("\n==============================")
    print("EVALUATION COMPLETE")
    print("==============================")

    for key, value in summary.items():
        print(f"{key}: {value}")

    print(f"\nResults saved to: {RESULTS_FILE}")


def hit_at_k(retrieved_ids, expected_ids):
    """
    Returns 1 if at least one expected relevant chunk
    appears in the retrieved top-k results.
    """
    return int(
        any(chunk_id in expected_ids for chunk_id in retrieved_ids)
    )


def recall_at_k(retrieved_ids, expected_ids):
    """
    Fraction of expected relevant chunks retrieved.
    """
    if not expected_ids:
        return None

    retrieved_set = set(retrieved_ids)
    expected_set = set(expected_ids)

    return len(retrieved_set & expected_set) / len(expected_set)


def reciprocal_rank(retrieved_ids, expected_ids):
    """
    Reciprocal rank of the first relevant retrieved chunk.
    """
    expected_set = set(expected_ids)

    for rank, chunk_id in enumerate(retrieved_ids, start=1):
        if chunk_id in expected_set:
            return 1 / rank

    return 0.0


if __name__ == "__main__":
    run_evaluation()
