
PREFIX=sudo
DOCKER_COMPOSE=docker compose

INSTALL_SCRIPT=install.sh

all: install spring sveltekit
	@- $(PREFIX) $(DOCKER_COMPOSE) up

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
	@- cd backend && gradle assemble
	@ echo "Rebuilding Spring Container"
	@ $(PREFIX) $(DOCKER_COMPOSE) build --no-cache spring

sveltekit:
	@ $(PREFIX) $(DOCKER_COMPOSE) build sveltekit --no-cache
	@ echo "Rebuilding SvelteKit Container"

.PHONY: clean
clean:
	@ $(PREFIX) $(DOCKER_COMPOSE) down -v
