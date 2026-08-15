import os
import requests
from fastapi import HTTPException
from dotenv import load_dotenv

load_dotenv()

API_KEY = os.getenv("WEATHER_API_KEY")
BASE_URL = "https://api.weatherapi.com/v1/current.json"


def get_current_weather(city):
    try:
        params = {
            "key": API_KEY,
            "q": city
        }

        response = requests.get(BASE_URL, params=params)
        response.raise_for_status()

        return response.json()

    except Exception:
        raise HTTPException(
            status_code=500,
            detail="Unable to fetch current weather."
        )


def get_hourly_forecast(city: str):
    try:
        url = "https://api.weatherapi.com/v1/forecast.json"

        params = {
            "key": API_KEY,
            "q": city,
            "days": 1,
            "aqi": "no",
            "alerts": "no"
        }

        response = requests.get(url, params=params)
        response.raise_for_status()

        return response.json()

    except Exception:
        raise HTTPException(
            status_code=500,
            detail="Unable to fetch hourly forecast."
        )


def get_weekly_forecast(city: str):
    try:
        url = "https://api.weatherapi.com/v1/forecast.json"

        params = {
            "key": API_KEY,
            "q": city,
            "days": 7,
            "aqi": "no",
            "alerts": "no"
        }

        response = requests.get(url, params=params)
        response.raise_for_status()

        return response.json()

    except Exception:
        raise HTTPException(
            status_code=500,
            detail="Unable to fetch weekly forecast."
        )


def get_weather_by_coordinates(latitude: float, longitude: float):
    try:
        url = "https://api.weatherapi.com/v1/current.json"

        params = {
            "key": API_KEY,
            "q": f"{latitude},{longitude}",
            "aqi": "no"
        }

        response = requests.get(url, params=params)
        response.raise_for_status()

        return response.json()

    except Exception:
        raise HTTPException(
            status_code=500,
            detail="Unable to fetch weather by coordinates."
        )

def search_city_names(query: str):
    try:

        url = "https://api.weatherapi.com/v1/search.json"

        params = {
            "key": API_KEY,
            "q": query
        }

        response = requests.get(url, params=params)
        response.raise_for_status()

        return response.json()

    except Exception:
        raise HTTPException(
            status_code=500,
            detail="Unable to search cities."
        )