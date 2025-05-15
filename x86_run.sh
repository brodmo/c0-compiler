#!/bin/bash
docker run --platform linux/amd64 --rm -v "$(pwd)":/work -w /work gcc:latest "$@"
