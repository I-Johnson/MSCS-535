#!/bin/sh
# Generates the local demo certificates into ./certs
# (trusted CA, genuine localhost server cert, and an attacker CA + rogue cert)
set -e
mkdir -p certs && cd certs
openssl req -x509 -newkey rsa:2048 -keyout ca-key.pem -out ca-cert.pem -days 30 -nodes -subj "/CN=PayDemo Local CA"
openssl req -newkey rsa:2048 -keyout server-key.pem -out server.csr -nodes -subj "/CN=localhost"
printf "subjectAltName=DNS:localhost,DNS:127.0.0.1" > san.cnf
openssl x509 -req -in server.csr -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out server-cert.pem -days 30 -extfile san.cnf
openssl req -x509 -newkey rsa:2048 -keyout rogue-ca-key.pem -out rogue-ca-cert.pem -days 30 -nodes -subj "/CN=Attacker CA"
openssl req -newkey rsa:2048 -keyout rogue-key.pem -out rogue.csr -nodes -subj "/CN=localhost"
openssl x509 -req -in rogue.csr -CA rogue-ca-cert.pem -CAkey rogue-ca-key.pem -CAcreateserial -out rogue-cert.pem -days 30 -extfile san.cnf
