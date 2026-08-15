from fastapi import FastAPI
from app.routers.weather_router import router

app = FastAPI()

app.include_router(router,prefix = "/weather", tags = ["Weather"])

@app.get("/")
def home():
    return {"message" : "WeatherSphere Backend Running"}