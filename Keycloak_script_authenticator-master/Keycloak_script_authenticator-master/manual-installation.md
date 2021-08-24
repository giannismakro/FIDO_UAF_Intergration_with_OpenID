# FIDO UAF Javascript Authenticator

## Configure and Deploy instructions for FIDO UAF Javascript Authenticator

### These instructions have been tested on Ubuntu 20.04.02

#### Installing Node.js service

Update the local package index to reflect the latest upstream changes :

```bash
sudo apt-get update
```
Then install Node.js:

```bash
sudo apt install nodejs
```

Check that the install was successful by querying node for its version number:

```bash
nodejs -v
```
Install npm :

```bash
sudo apt install npm
```
Install Node.js required packages for Keycloak FIDO2 JS Authenticator:

```bash
    npm install --save express
   
    npm install --save axios
   
    npm install --save keycloak-rest-authenticator
```
In the **index.js** file change the ** keycloakDeploymentLocation**.

Run the Keycloak FIDO UAF JS Authentication REST service:
 ```bash   
    cd  keycloakscriptauthenticator && node index.js
 ```
 In the background the FIDO UAF JS Authenticator will be built as a jar file and will be deployed in Keycloak server deployments directory. 
   You should be able to see it here: keycloak13.0.0/standalone/deployments
 
 
 nsure that the FIDO2 JS Authenticator has been successfully deployed and can be used in Keycloak by going here:
 http://localhost:8180/auth/admin/master/console/#/server-info/providers and serching for "script-/uaf/" in the authenticators section.
 
 #### Configure Keycloak Server to use the FIDO UAF JS Authenticator
1. On the browser open the Keycloak Admin console by visiting http://localhost:8180/auth/admin and authenticate

2. From the Keycloak Admin console go to Configure > Authentication

3. From the Authentication/Flows dropdown choose the Direct Grant and using the "Copy" 
button on the right create a copy of the Direct Grant Flow and name it "Copy of Browser".

4. Select the newly created "Copy of Browser" authentication flow and delete all its authenticator.

6. Click the "Add execution" button and from the provider dropdown try to find the username form select it and save it.

7. Click the "Add execution" button and from the provider dropdown try to find the FIDO UAF JS Authenticator provider (e.g., "Http://localhost:10000/uaf") select it and save it.

8. Set the newly added  FIDO UAF JS Authenticator Auth Type as ALTERNATIVE 

9. Edit the Keycloak Client that you want to enable FIDO UAF authentication (e.g., the "oidc-client" client) as follows:
    
    - Go to Clients > oidc-client
    
    - Scroll down and open the "Authentication Flow Overrides" and for the Direct Grant Flow select "Copy of Browser"
 