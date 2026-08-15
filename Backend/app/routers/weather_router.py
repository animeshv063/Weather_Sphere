from fastapi import APIRouter

from app.services.weather_service import (
    get_current_weather,
    get_hourly_forecast,
    get_weekly_forecast,
    get_weather_by_coordinates,
    search_city_names
)


router = APIRouter()


@router.get("/current")
def current_weather(city: str):
    return get_current_weather(city)

@router.get("/hourly/{city}")
def hourly_forecast(city: str):
    return get_hourly_forecast(city)

@router.get("/weekly/{city}")
def weekly_forecast(city: str):
    return get_weekly_forecast(city)

@router.get("/location")
def weather_by_location(latitude: float, longitude: float):
    return get_weather_by_coordinates(latitude, longitude)

@router.get("/search")
def search_city(query: str):
    return search_city_names(query)

@router.get("/search")
def search_city(query: str):
    return search_city_names(query)