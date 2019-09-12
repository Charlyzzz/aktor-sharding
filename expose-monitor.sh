#!/usr/bin/env bash
export NODE_IP=$(minikube ip)
export NODE_PORT=$(kubectl get services/akka-cluster-demo --namespace akka-cluster-1 -o go-template='{{(index .spec.ports 0).nodePort}}')
echo http://$NODE_IP:$NODE_PORT
kubectl proxy