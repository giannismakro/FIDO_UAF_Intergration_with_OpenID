'use strict'

const express = require("express");
const {
  KeycloakScriptPackager,
  RestAuthenticator,
} = require("keycloak-rest-authenticator");
const app = express();

app.use(express.json());

app.get("/", (req, res)=>{ res.json({hello: "it works"}) })
const packager = new KeycloakScriptPackager({
  // Dockerfile deployment location
  keycloakDeploymentLocation: "/home/fido-server/Downloads/keycloak-13.0.0/standalone/deployments",
  // keycloakDeploymentLocation: "../keycloak11.0.3/standalone/deployments",
  keycloakAccessibleBaseUrl: "http://localhost:10000",
});

const FidoAuthenticator = require("./fidouafauth.js");
app.use(
  RestAuthenticator.declare("/uaf", new FidoAuthenticator(), packager)
);

app.listen(10000, "0.0.0.0", ()=>{
  console.log("Keycloak-FIDO UAF REST Authenticator running on http://localhost:10000");
  packager.make();
});
