.PHONY: build up down logs help

version ?= dev
name := yuiyeong/ticketing:$(version)

build:
	@docker build . -f docker/Dockerfile.local -t ${name} || { echo "Build failed"; exit 1; }

up:
	@docker compose -f docker/docker-compose.yml --compatibility up --build -d

down:
	@docker compose -f docker/docker-compose.yml down

logs:
	@docker compose -f docker/docker-compose.yml logs -f app

help:
	@echo "Available commands:"
	@echo "  build  : Build Docker image"
	@echo "  up     : Start containers"
	@echo "  down   : Stop and remove containers"
	@echo "  logs   : View container logs"
