
PREFIX=sudo
DOCKER_COMPOSE=docker compose

all_in_dir = $(shell find $(1) -type d) $(shell find $(1) -type f -name '*')

all: spring sveltekit
	$(PREFIX) $(DOCKER_COMPOSE) up

spring:
	@ echo "Rebuilding Spring Executable"
	cd backend && gradle assemble
	@ echo "Rebuilding Spring Container"
	$(PREFIX) $(DOCKER_COMPOSE) build --no-cache spring

sveltekit:
	@ echo "Rebuilding SvelteKit Container"
	$(PREFIX )

.PHONY: clean
clean:
	$(PREFIX) $(DOCKER_COMPOSE) down -v
