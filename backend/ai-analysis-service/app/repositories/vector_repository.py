from pgvector.psycopg import register_vector

from app.db.vector_db import get_connection



def save_embedding(
        ticker: str,
        fiscal_year: int,
        document: str,
        embedding: list[float]
):
    with get_connection() as connection:
        register_vector(connection)

        with connection.cursor() as cursor:
            cursor.execute(
                """
                INSERT INTO financial_document_embeddings
                    (ticker, fiscal_year, document, embedding)
                VALUES (%s, %s, %s, %s)
                ON CONFLICT (ticker, fiscal_year)
                DO UPDATE SET
                    document = EXCLUDED.document,
                    embedding = EXCLUDED.embedding,
                    created_at = CURRENT_TIMESTAMP;
                """,
                (
                    ticker.upper(),
                    fiscal_year,
                    document,
                    embedding
                )
            )

        connection.commit()

def search_similar(
        ticker: str,
        query_embedding: list[float],
        top_k = 3
):
    with get_connection() as connection:
        register_vector(connection)

        with connection.cursor() as cursor:
            cursor.execute(
                """
                SELECT
                    document,
                    1-(embedding <=> %s::vector) AS score
                FROM financial_document_embeddings
                WHERE ticker = %s
                ORDER BY embedding <=> %s::vector
                LIMIT %s;
                """,
                (
                    query_embedding,
                    ticker.upper(),
                    query_embedding,
                    top_k
                )
            )

            rows = cursor.fetchall()

    return [
        {
            "document": row[0],
            "score": float(row[1])
        }
        for row in rows
    ]

def save_sec_filing_chunk(
        ticker: str,
        fiscal_year: int,
        form_type: str,
        section: str,
        chunk_index: int,
        document: str,
        embedding: list[float]
):
    with get_connection() as connection:
        register_vector(connection)

        with connection.cursor() as cursor:
            cursor.execute("""
                INSERT INTO sec_filing_embeddings (
                    ticker,
                    fiscal_year,
                    form_type,
                    section,
                    chunk_index,
                    document,
                    embedding
                )
                VALUES (
                    %s,
                    %s,
                    %s,
                    %s,
                    %s,
                    %s,
                    %s::vector
                )
                ON CONFLICT (
                    ticker,
                    fiscal_year,
                    form_type,
                    section,
                    chunk_index
                )
                DO UPDATE SET
                    document = EXCLUDED.document,
                    embedding = EXCLUDED.embedding,
                    created_at = CURRENT_TIMESTAMP
            """, (
                ticker.upper(),
                fiscal_year,
                form_type,
                section,
                chunk_index,
                document,
                embedding
            ))

        connection.commit()

def search_sec_filing_chunks(
        ticker: str,
        query_embedding: list[float],
        top_k: int = 5,
        fiscal_year: int | None = None,
        form_type: str = "10-K",
        section: str = "RISK_FACTORS"
):
    with get_connection() as connection:
        register_vector(connection)

        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT
                    fiscal_year,
                    form_type,
                    section,
                    chunk_index,
                    document,
                    1 - (embedding <=> %s::vector) AS score
                FROM sec_filing_embeddings
                WHERE ticker = %s
                  AND form_type = %s
                  AND section = %s
                  AND (%s IS NULL OR fiscal_year = %s)
                ORDER BY embedding <=> %s::vector
                LIMIT %s
            """, (
                query_embedding,
                ticker.upper(),
                form_type,
                section,
                fiscal_year,
                fiscal_year,
                query_embedding,
                top_k
            ))

            rows = cursor.fetchall()

    return [
        {
            "fiscal_year": row[0],
            "form_type": row[1],
            "section": row[2],
            "chunk_index": row[3],
            "document": row[4],
            "score": float(row[5])
        }
        for row in rows
    ]