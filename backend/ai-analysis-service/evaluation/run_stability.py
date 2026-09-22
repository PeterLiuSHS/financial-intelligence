import json
import statistics
from pathlib import Path

from app.services.rag_service import answer_sec_risk_question


QUESTIONS_FILE = Path(__file__).parent / "questions.json"
RESULTS_FILE = Path(__file__).parent / "stability_results.json"

RUNS = 3


def load_questions():
    with open(QUESTIONS_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


def safe_mean(values):
    if not values:
        return 0.0
    return statistics.mean(values)


def safe_stdev(values):
    if len(values) < 2:
        return 0.0
    return statistics.stdev(values)


def run_stability():
    questions = load_questions()

    all_runs = []

    support_rates = []
    claim_counts = []
    repair_rates = []
    abstain_rates = []
    latencies = []

    for run_number in range(1, RUNS + 1):
        print("\n======================================")
        print(f"STABILITY RUN {run_number}/{RUNS}")
        print("======================================")

        run_results = []

        total_initial_claims = 0
        total_initial_supported = 0

        total_final_claims = 0

        repair_count = 0
        abstain_count = 0
        successful_questions = 0

        run_latencies = []

        for index, case in enumerate(questions, start=1):
            print(
                f"[Run {run_number}] "
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

                initial_claim_count = trace["initial_claim_count"]
                initial_supported = trace["initial_supported"]
                final_claim_count = trace["final_claim_count"]

                repair_performed = trace["repair_performed"]

                latency_ms = trace["timings"]["total_ms"]

                abstained = initial_claim_count == 0

                successful_questions += 1

                total_initial_claims += initial_claim_count
                total_initial_supported += initial_supported
                total_final_claims += final_claim_count

                if repair_performed:
                    repair_count += 1

                if abstained:
                    abstain_count += 1

                run_latencies.append(latency_ms)

                run_results.append({
                    "id": case["id"],
                    "ticker": case["ticker"],
                    "category": case["category"],
                    "question": case["question"],
                    "initial_claim_count": initial_claim_count,
                    "initial_supported": initial_supported,
                    "initial_partial": trace["initial_partial"],
                    "initial_unsupported": trace["initial_unsupported"],
                    "final_claim_count": final_claim_count,
                    "final_supported": trace["final_supported"],
                    "repair_performed": repair_performed,
                    "abstained": abstained,
                    "latency_ms": latency_ms,
                    "success": True
                })

                print(
                    f"    initial: "
                    f"{initial_supported}/"
                    f"{initial_claim_count} supported"
                )

                print(
                    f"    repair: {repair_performed}"
                )

                print(
                    f"    final claims: {final_claim_count}"
                )

                print(
                    f"    latency: {latency_ms / 1000:.2f}s"
                )

            except Exception as e:
                print(f"    FAILED: {e}")

                run_results.append({
                    "id": case["id"],
                    "ticker": case["ticker"],
                    "category": case["category"],
                    "question": case["question"],
                    "success": False,
                    "error": str(e)
                })

        initial_support_rate = (
            total_initial_supported
            / total_initial_claims
            * 100
            if total_initial_claims > 0
            else 0
        )

        repair_rate = (
            repair_count
            / successful_questions
            * 100
            if successful_questions > 0
            else 0
        )

        abstain_rate = (
            abstain_count
            / successful_questions
            * 100
            if successful_questions > 0
            else 0
        )

        average_claim_count = (
            total_initial_claims
            / successful_questions
            if successful_questions > 0
            else 0
        )

        average_latency_ms = safe_mean(run_latencies)

        run_summary = {
            "run": run_number,
            "total_questions": len(questions),
            "successful_questions": successful_questions,

            "total_initial_claims": total_initial_claims,
            "initial_supported": total_initial_supported,
            "initial_support_rate": round(
                initial_support_rate,
                2
            ),

            "total_final_claims": total_final_claims,

            "repair_count": repair_count,
            "repair_rate": round(
                repair_rate,
                2
            ),

            "abstain_count": abstain_count,
            "abstain_rate": round(
                abstain_rate,
                2
            ),

            "average_initial_claim_count": round(
                average_claim_count,
                2
            ),

            "average_latency_ms": round(
                average_latency_ms,
                2
            )
        }

        all_runs.append({
            "summary": run_summary,
            "results": run_results
        })

        support_rates.append(initial_support_rate)
        claim_counts.append(average_claim_count)
        repair_rates.append(repair_rate)
        abstain_rates.append(abstain_rate)
        latencies.append(average_latency_ms)

        print("\nRUN SUMMARY")
        print(
            f"Initial support rate: "
            f"{initial_support_rate:.2f}%"
        )
        print(
            f"Average claim count: "
            f"{average_claim_count:.2f}"
        )
        print(
            f"Repair rate: "
            f"{repair_rate:.2f}%"
        )
        print(
            f"Abstain rate: "
            f"{abstain_rate:.2f}%"
        )
        print(
            f"Average latency: "
            f"{average_latency_ms / 1000:.2f}s"
        )

    stability_summary = {
        "runs": RUNS,
        "questions_per_run": len(questions),

        "initial_support_rate": {
            "mean": round(
                safe_mean(support_rates),
                2
            ),
            "std": round(
                safe_stdev(support_rates),
                2
            ),
            "values": [
                round(value, 2)
                for value in support_rates
            ]
        },

        "average_initial_claim_count": {
            "mean": round(
                safe_mean(claim_counts),
                2
            ),
            "std": round(
                safe_stdev(claim_counts),
                2
            ),
            "values": [
                round(value, 2)
                for value in claim_counts
            ]
        },

        "repair_rate": {
            "mean": round(
                safe_mean(repair_rates),
                2
            ),
            "std": round(
                safe_stdev(repair_rates),
                2
            ),
            "values": [
                round(value, 2)
                for value in repair_rates
            ]
        },

        "abstain_rate": {
            "mean": round(
                safe_mean(abstain_rates),
                2
            ),
            "std": round(
                safe_stdev(abstain_rates),
                2
            ),
            "values": [
                round(value, 2)
                for value in abstain_rates
            ]
        },

        "average_latency_ms": {
            "mean": round(
                safe_mean(latencies),
                2
            ),
            "std": round(
                safe_stdev(latencies),
                2
            ),
            "values": [
                round(value, 2)
                for value in latencies
            ]
        }
    }

    output = {
        "stability_summary": stability_summary,
        "runs": all_runs
    }

    with open(
        RESULTS_FILE,
        "w",
        encoding="utf-8"
    ) as f:
        json.dump(
            output,
            f,
            indent=2,
            ensure_ascii=False
        )

    print("\n======================================")
    print("STABILITY EVALUATION COMPLETE")
    print("======================================")

    print(
        "Initial support rate: "
        f"{stability_summary['initial_support_rate']['mean']}% "
        f"± "
        f"{stability_summary['initial_support_rate']['std']}%"
    )

    print(
        "Average initial claim count: "
        f"{stability_summary['average_initial_claim_count']['mean']} "
        f"± "
        f"{stability_summary['average_initial_claim_count']['std']}"
    )

    print(
        "Repair rate: "
        f"{stability_summary['repair_rate']['mean']}% "
        f"± "
        f"{stability_summary['repair_rate']['std']}%"
    )

    print(
        "Abstain rate: "
        f"{stability_summary['abstain_rate']['mean']}% "
        f"± "
        f"{stability_summary['abstain_rate']['std']}%"
    )

    print(
        "Average latency: "
        f"{stability_summary['average_latency_ms']['mean'] / 1000:.2f}s "
        f"± "
        f"{stability_summary['average_latency_ms']['std'] / 1000:.2f}s"
    )

    print(
        f"\nResults saved to: {RESULTS_FILE}"
    )


if __name__ == "__main__":
    run_stability()