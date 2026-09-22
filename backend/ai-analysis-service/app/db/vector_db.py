import psycopg

from app.core.config import settings

def get_connection():
    return psycopg.connect(
        host=settings.vector_db_host,
        port=settings.vector_db_port,
        dbname=settings.vector_db_name,
        user=settings.vector_db_user,
        password=settings.vector_db_password
    )

def initialize_vector_db():
    connection = get_connection()
    with get_connection() as connection:
        with connection.cursor() as cursor:

            cursor.execute("""
                CREATE EXTENSION IF NOT EXISTS vector;
            """)

            cursor.execute("""
                CREATE TABLE IF NOT EXISTS financial_document_embeddings (
                    id BIGSERIAL PRIMARY KEY,
                    ticker VARCHAR(20) NOT NULL,
                    fiscal_year INTEGER NOT NULL,
                    document TEXT NOT NULL,
                    embedding VECTOR(3072) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                    UNIQUE(ticker, fiscal_year)
                );
            """)

            cursor.execute("""
                CREATE TABLE IF NOT EXISTS sec_filing_embeddings (
                    id BIGSERIAL PRIMARY KEY,
                    ticker VARCHAR(20) NOT NULL,
                    fiscal_year INTEGER NOT NULL,
                    form_type VARCHAR(20) NOT NULL,
                    section VARCHAR(100) NOT NULL,
                    chunk_index INTEGER NOT NULL,

                    document TEXT NOT NULL,
                    embedding VECTOR(3072) NOT NULL,

                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(
                              ticker,
                              fiscal_year,
                              form_type,
                              section,
                              chunk_index
                          )
                    );
                """)

        connection.commit()