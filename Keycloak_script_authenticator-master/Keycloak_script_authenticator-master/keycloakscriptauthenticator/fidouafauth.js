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
     
	  await axios.get(`http://localhost:8080/fido/v1/isauth/${req.user.attributes[this.fidoAuthenticationIdAttribute]}`).then((resp) => {
    
	var response=JSON.stringify(resp.data);
	var obj=JSON.parse(response)
	 console.log(obj.authenticated);
       
		this.latestFidoAuthenticationId = obj.authenticated;
		this.usersuname=obj.authenticated=obj.username;
       
      });

    
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
