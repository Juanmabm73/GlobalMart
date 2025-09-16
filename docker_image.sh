#!/bin/bash

# Variables
IMAGE_NAME="alvaro3517/webapp6"

echo "🔨 Building image: $IMAGE_NAME"
docker build -t $IMAGE_NAME .

echo "🚀 Pushing image to Docker Hub"
docker push $IMAGE_NAME
