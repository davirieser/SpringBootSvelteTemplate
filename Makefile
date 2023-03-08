
PREFIX=sudo
DOCKER_COMPOSE=docker compose
PROFILE ?= PROD

INSTALL_SCRIPT=install.sh

SPRING_ENV=SPRING_PROFILES_ACTIVE=$(PROFILE)

all: install spring sveltekit
	@- $(PREFIX) $(DOCKER_COMPOSE) up

debug: PROFILE=DEBUG
debug: all

push:
	@ git push --recurse-submodules=on-demand

pull:
	@- git pull
	@- git submodule update --remote

install:
	@- git submodule update --init --recursive
	@- ./$(INSTALL_SCRIPT)

spring:
	@ echo "Rebuilding Spring Executable"
	cd backend && $(SPRING_ENV) gradle assemble
	@ echo "Rebuilding Spring Container"
	@ $(PREFIX) $(DOCKER_COMPOSE) build --no-cache spring

sveltekit:
	@ $(PREFIX) $(DOCKER_COMPOSE) build sveltekit 
	@ echo "Rebuilding SvelteKit Container"

.PHONY: clean
clean:
	@ $(PREFIX) $(DOCKER_COMPOSE) down -v
	@ gradle clean
