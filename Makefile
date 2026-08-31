# ============================================================
# PayFlow Developer Automation Makefile
# ============================================================

.PHONY: help build test clean up down ps logs k8s-deploy load-test demo

help:
	@echo "PayFlow Automation Commands:"
	@echo "  make build       - Compile Maven reactor"
	@echo "  make test        - Run all 98 unit and integration tests"
	@echo "  make clean       - Clean all Maven target directories"
	@echo "  make up          - Start full Docker Compose stack in background"
	@echo "  make down        - Stop Docker Compose stack and remove volumes"
	@echo "  make ps          - View running containers"
	@echo "  make logs        - Tail logs for all services"
	@echo "  make k8s-deploy  - Deploy Helm chart to Kubernetes cluster"
	@echo "  make load-test   - Run 1,000 TPS K6 stress test"
	@echo "  make demo        - Execute automated end-to-end payment demonstration"

build:
	mvn clean compile -DskipTests

test:
	mvn clean verify

clean:
	mvn clean

up:
	docker compose --env-file .env up -d --build

down:
	docker compose down -v

ps:
	docker compose ps

logs:
	docker compose logs -f

k8s-deploy:
	helm upgrade --install payflow ./k8s/helm/payflow --namespace payflow --create-namespace

load-test:
	k6 run performance/k6/concurrent_transfers.js

demo:
	pwsh scripts/e2e-demo.ps1
