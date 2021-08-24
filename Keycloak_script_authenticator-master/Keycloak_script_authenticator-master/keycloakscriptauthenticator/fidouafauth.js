const { createAuthenticator } = require("keycloak-rest-authenticator");
const axios = require('axios');

const FidoAuthenticator = createAuthenticator(
  function () {
    this.fidoAuthenticationIdAttribute = "fidoAuthenticationId";
  },
  {
    processNew: async function (req) {
      console.info(`\n[${new Date()}] New request from Keycloak received for User: ${req.user.username}`);
	 console.info(`\n[${req.user.attributes[this.fidoAuthenticationIdAttribute]}`);
      /*
        Send an HTTP GET Request to the FIDO2 Server to get the last fidoAuthenticationId 
        of the user and compare it with the fidoAuthenticationId attribute value of the user
       */
      // await axios.get(`http://localhost:8090/fido2/lastAuthId/${req.user.username}`).then((resp) => {
	  await axios.get(`http://localhost:8080/fido/v1/isauth/${req.user.attributes[this.fidoAuthenticationIdAttribute]}`).then((resp) => {
     // await axios.get("https://api.github.com/users/janbodnar").then((resp) => {
	var response=JSON.stringify(resp.data);
	var obj=JSON.parse(response)
	 console.log(obj.authenticated);
        // this.latestFidoAuthenticationId = resp.data.lastAuthId;
		this.latestFidoAuthenticationId = obj.authenticated;
		this.usersuname=obj.authenticated=obj.username;
        //this.latestFidoAuthenticationId = "test1234";
      });

      // Check if the FIDO AuthenticationId attribute of the user matches the last
      // FIDO AuthenticationId returned from the FIDO2 Server for that user
      if (
        //req.user &&
        //req.user.attributes[this.fidoAuthenticationIdAttribute] ==
	 req.user.username==this.usersuname &&
          this.latestFidoAuthenticationId==true
      ) {
        return {} // meaning successful verification
      } else {
        // const createUserAttributes = {};
        // createUserAttributes[this.fidoAuthenticationIdAttribute] =
        //   "Invalid FIDO2 AuthenticationId";
        return {
          // username: req.user.username,
          // userAttributes: createUserAttributes,
          failure: "invalidCredentials",
        };
      }
    },
    processInteraction: async function (req) {

    },
    processInterruption: async function (req) {

    }
  }
)

module.exports = FidoAuthenticator;
