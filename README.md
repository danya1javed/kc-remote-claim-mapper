
## Explanation

---

1. ### Extending the right classes:
   
   YOUR_CUSTOM_MAPPER_NAME extends **AbstractOIDCProtocolMapper** implements
     **OIDCAccessTokenMapper**, **OIDCIDTokenMapper**, **UserInfoTokenMapper**

   These classes deal with the access token, IDToken or userInfo token so if
   you want to extend the functionality override some of its method.
   
2. ### Key Variables

   1. Your mapper identifier in keycloak: `public final static String PROVIDER_ID = "YOUR_MAPPER_NAME";`
   2. Config list for your mapper: `List<ProviderConfigProperty> configProperties = new ArrayList<>();`
    
3. ### Sample Config Property
        
    Create One: 
        
       property = new ProviderConfigProperty();
       property.setName("PARAM_NAME");
       property.setLabel("LABEL");
       property.setType(ProviderConfigProperty.TYPE);
       property.setHelpText("Description of the parameter");
       property.setDefaultValue("false");
       configProperties.add(property); // add to the config list
    
    Use One: 

        There are accessable through model protcol class named ProtocolMapperModel
        String paramsFromConfig = protocolMapperModel.getConfig().get(PARAM_NAME);

4. ### Setting Claim

   `SimpleResponsePOJO claims = clientSessionCtx.getAttribute(REMOTE_AUTHORIZATION_ATTR, SimpleResponsePOJO.class);`
    
    Above line tries to retrieve claims from existing client session context use name 'REMOTE_AUTHORIZATION_ATTR' which
    is an identifier to get it from the cache. claims can be of any object type JsonNode, POJOs anything whose
    properties are accessible publicly by means of getters.
   
   ```
   if (claims == null) {
   claims = getRemoteClaims(mappingModel, userSession);
   System.out.println("Claims - " + claims.toString());
   clientSessionCtx.setAttribute(REMOTE_AUTHORIZATION_ATTR, claims);
   }

   OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claims);
   ```
   After checking claims in the session context depending on they are null or not. set the new claims retrieved using
   mapClaim(token, mapperModel, YOUR_CLAIMS). and also set it in session context for caching and retrieving later.

---

Useful Links
1. https://github.com/tholst/keycloak-json-graphql-remote-claim
2. https://medium.com/@pavithbuddhima/how-to-add-custom-claims-to-jwt-tokens-from-an-external-source-in-keycloak-52bd1ff596d3

---

_For Deploying the Mapper a jar with all its dependencies was created using maven-shade.
This doesn't explain the complete code only important parts._
