from pydantic import BaseModel

class SearchRequest(BaseModel):
    ticker: str
    query: str
    top_k: int=3

class SearchResult(BaseModel):
    document: str
    score: float

class SearchResponse(BaseModel):
    results: list[SearchResult]