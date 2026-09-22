from pydantic import BaseModel

class AnalysisRequest(BaseModel):
    prompt: str

class AnalysisResponse(BaseModel):
    result: str