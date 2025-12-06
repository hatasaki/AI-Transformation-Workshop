import asyncio
import logging
import os
import threading
from datetime import date
from typing import Any, Dict, Optional
from urllib.parse import urlparse

import httpx
from flask import Flask, jsonify, request

from agent_framework import ChatAgent
from agent_framework.openai import OpenAIChatClient
from dotenv import load_dotenv


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

load_dotenv()

app = Flask(__name__, static_folder="static", static_url_path="")

# Single shared event loop to avoid closing between Flask requests.
_background_loop = asyncio.new_event_loop()
_loop_thread = threading.Thread(target=_background_loop.run_forever, daemon=True)
_loop_thread.start()


def _run_in_loop(coro: asyncio.Future) -> Any:
    return asyncio.run_coroutine_threadsafe(coro, _background_loop).result()


def _get_required_setting(name: str) -> str:
    value = os.environ.get(name)
    if not value:
        raise RuntimeError(
            f"Required environment variable '{name}' is not set. "
            "Set the value before calling the chat API."
        )
    return value

PROJECT_ENDPOINT_ENV = "FOUNDRY_PROJECT_ENDPOINT"
MODEL_DEPLOYMENT_ENV = "FOUNDRY_MODEL_DEPLOYMENT_NAME"
PROJECT_KEY_ENV = "FOUNDRY_PROJECT_KEY"
AGENT_INSTRUCTIONS = os.environ.get("WORKSHOP_AGENT_INSTRUCTIONS")
AGENT_NAME = os.environ.get("WORKSHOP_AGENT_NAME", "Travel Assistant")
TRAVEL_REQUEST_API_ENV = "TRAVEL_REQUEST_API_ENDPOINT"
HOTEL_SEARCH_API_ENV = "HOTEL_SEARCH_API_ENDPOINT"


def _build_instructions() -> str:
    today = date.today().isoformat()
    parts = []
    if AGENT_INSTRUCTIONS:
        parts.append(AGENT_INSTRUCTIONS)
    parts.append(f"Today's date: {today}")
    return "\n".join(parts)

def _foundry_openai_base_url(project_endpoint: str) -> str:
    parsed = urlparse(project_endpoint)
    if not parsed.scheme or not parsed.netloc:
        raise RuntimeError(
            "FOUNDRY_PROJECT_ENDPOINT must include scheme and host, e.g. https://<resource>.services.ai.azure.com/api/projects/<project>."
        )
    host = f"{parsed.scheme}://{parsed.netloc}"
    return f"{host}/openai/v1"


def _post_json_sync(endpoint: str, payload: Dict[str, Any]) -> Any:
    # Sync client to avoid background async cleanup after the event loop closes.
    with httpx.Client(timeout=30.0) as client:
        response = client.post(endpoint, json=payload)
        response.raise_for_status()
        if response.headers.get("content-type", "").startswith("application/json"):
            return response.json()
        return response.text


def submit_travel_request(
    title: str,
    startDate: str,
    endDate: str,
    purpose: str,
    city: str,
    estimatedCost: int,
) -> Dict[str, Any]:
    """Register a business travel request with title, dates, purpose, city, and estimated cost."""

    endpoint = _get_required_setting(TRAVEL_REQUEST_API_ENV)
    payload = {
        "title": title,
        "startDate": startDate,
        "endDate": endDate,
        "purpose": purpose,
        "city": city,
        "estimatedCost": estimatedCost,
    }
    try:
        result = _post_json_sync(endpoint, payload)
    except httpx.HTTPStatusError as exc:
        return {
            "error": "Travel request API returned an error",
            "status_code": exc.response.status_code,
            "body": exc.response.text,
        }
    except Exception as exc:  # pylint: disable=broad-exception-caught
        return {"error": f"Travel request API call failed: {exc}"}

    return {"message": "Travel request submitted", "data": result}


def search_hotels(
    city: str,
    maxPrice: int,
    feature: Optional[str] = None,
) -> Dict[str, Any]:
    """Search hotels by city with an optional feature keyword and maxPrice ceiling."""

    endpoint = _get_required_setting(HOTEL_SEARCH_API_ENV)
    payload: Dict[str, Any] = {
        "city": city,
        "maxPrice": maxPrice,
    }
    if feature:
        payload["feature"] = feature

    try:
        result = _post_json_sync(endpoint, payload)
    except httpx.HTTPStatusError as exc:
        return {
            "error": "Hotel search API returned an error",
            "status_code": exc.response.status_code,
            "body": exc.response.text,
        }
    except Exception as exc:  # pylint: disable=broad-exception-caught
        return {"error": f"Hotel search API call failed: {exc}"}

    return {"results": result}


async def run_agent_interaction(
    message: str,
    *,
    incoming_agent_id: Optional[str] = None,
    serialized_thread: Optional[Dict[str, Any]] = None,
) -> Dict[str, Any]:
    project_endpoint = _get_required_setting(PROJECT_ENDPOINT_ENV)
    model_deployment_name = _get_required_setting(MODEL_DEPLOYMENT_ENV)
    project_key = _get_required_setting(PROJECT_KEY_ENV)

    chat_client = OpenAIChatClient(
        api_key=project_key,
        model_id=model_deployment_name,
        base_url=_foundry_openai_base_url(project_endpoint),
    )

    chat_agent_manager: ChatAgent = ChatAgent(
        chat_client=chat_client,
        name=AGENT_NAME,
        instructions=_build_instructions(),
        tools=[search_hotels, submit_travel_request],
    )

    async with chat_agent_manager as agent:
        if serialized_thread:
            thread = await agent.deserialize_thread(serialized_thread)
        else:
            thread = agent.get_new_thread()

        response = await agent.run(message, thread=thread, store=True)
        serialized = await thread.serialize()

        return {
            "reply": response.text,
            "agentId": incoming_agent_id or "local-foundry-agent",
            "thread": serialized,
        }


@app.route("/")
def index():
    return app.send_static_file("index.html")


@app.route("/api/chat", methods=["POST"])
def chat():
    payload = request.get_json(silent=True) or {}
    message = (payload.get("message") or "").strip()
    if not message:
        return jsonify({"error": "message is required"}), 400

    agent_id = payload.get("agentId")
    thread_state = payload.get("thread")

    try:
        result = _run_in_loop(
            run_agent_interaction(
                message,
                incoming_agent_id=agent_id,
                serialized_thread=thread_state,
            )
        )
    except RuntimeError as exc:
        logger.exception("Configuration error: %s", exc)
        return jsonify({"error": str(exc)}), 500
    except Exception as exc:  # pylint: disable=broad-exception-caught
        logger.exception("Agent call failed")
        return jsonify({"error": "Failed to contact Azure AI Agent", "details": str(exc)}), 502

    return jsonify(result)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", "5000")))