import json
from pathlib import Path

from app.db.vector_db import get_connection


OUTPUT_FILE = Path(__file__).parent / "sec_chunks_aapl_2025.json"


def export_chunks():
    conn = get_connection()

    try:
        with conn.cursor() as cursor:
            cursor.execute(
                """
                SELECT
                    chunk_index,
                    document
                FROM sec_filing_embeddings
                WHERE ticker = %s
                  AND fiscal_year = %s
                  AND form_type = %s
                  AND section = %s
                ORDER BY chunk_index
                """,
                (
                    "AAPL",
                    2025,
                    "10-K",
                    "RISK_FACTORS"
                )
            )

            rows = cursor.fetchall()

        chunks = [
            {
                "chunk_index": row[0],
                "document": row[1]
            }
            for row in rows
        ]

        with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
            json.dump(
                chunks,
                f,
                indent=2,
                ensure_ascii=False
            )

        print(f"Exported {len(chunks)} chunks.")
        print(f"Saved to: {OUTPUT_FILE}")

    finally:
        conn.close()


if __name__ == "__main__":
    export_chunks()