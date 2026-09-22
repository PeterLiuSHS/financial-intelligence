from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.analysis import router as analysis_router
from app.api.embedding import router as embedding_router
from app.api.search import router as search_router
from app.api.rag import router as rag_router
from app.api.company import router as company_router
from app.api.index import router as index_router
from app.db.vector_db import initialize_vector_db
from app.api.chunking import router as chunking_router
from app.api.sec_index import router as sec_index_router
from app.exceptions.ai_exceptions import AIServiceException
from app.core.exception_handlers import ai_service_exception_handler

@asynccontextmanager
async def lifespan(app: FastAPI):
    initialize_vector_db()
    yield
app = FastAPI(
    title="FinIntel AI Analysis Service",
    version="1.0",
    lifespan=lifespan
)

app.include_router(analysis_router)
app.include_router(embedding_router)
app.include_router(search_router)
app.include_router(rag_router)
app.include_router(company_router)
app.include_router(index_router)
app.include_router(chunking_router)
app.include_router(sec_index_router)
app.add_exception_handler(
    AIServiceException,
    ai_service_exception_handler
)
@app.get("/health")
async def health():
    return {"status": "UP"}

