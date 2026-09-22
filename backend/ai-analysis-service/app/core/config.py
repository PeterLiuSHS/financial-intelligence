from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    gemini_api_key: str

    financial_data_service_url: str = "http://localhost:8081"

    vector_db_host: str = "localhost"
    vector_db_port: int = 5433
    vector_db_name: str = "finintel_vector_db"
    vector_db_user: str = "finintel"
    vector_db_password: str = "finintel"

    model_config = SettingsConfigDict(
        env_file='.env',
        env_file_encoding='utf-8'
    )

settings = Settings()