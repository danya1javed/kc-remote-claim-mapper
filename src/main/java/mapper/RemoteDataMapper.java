package mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RemoteDataMapper extends AbstractOIDCProtocolMapper implements
        OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

  // list of configurable properties
  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  private final static String REMOTE_URL = "remote.url";
  private final static String REMOTE_HEADERS = "remote.headers";
  private final static String REMOTE_PARAMETERS = "remote.parameters";
  private final static String REMOTE_PARAMETERS_USERNAME = "remote.parameters.username";
  private final static String REMOTE_PARAMETERS_CLIENTID = "remote.parameters.clientid";

  // used for cache reason. means if the result is already present we can use it.
  private final static String REMOTE_AUTHORIZATION_ATTR = "remote-authorizations";

  // identifier for mapper
  public final static String PROVIDER_ID = "Remote-data-mapper";

  static {
    OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, RemoteDataMapper.class);
    OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);

    ProviderConfigProperty property;

    // username from the userSessionModel

    // client id
    property = new ProviderConfigProperty();
    property.setName("REMOTE_PARAMETERS_CLIENTID");
    property.setLabel("set client id");
    property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
    property.setHelpText("send client id in request.");
    property.setDefaultValue("false");
    configProperties.add(property);

    // username
    property = new ProviderConfigProperty();
    property.setName("REMOTE_PARAMETERS_USERNAME");
    property.setLabel("set username");
    property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
    property.setHelpText("send username in request.");
    property.setDefaultValue("false");
    configProperties.add(property);

    // url
    property = new ProviderConfigProperty();
    property.setName(REMOTE_URL);
    property.setLabel("Set remote URL");
    property.setType(ProviderConfigProperty.STRING_TYPE);
    property.setHelpText("Enter URL to retrieve data from.");
    configProperties.add(property);

    // Parameters
    property = new ProviderConfigProperty();
    property.setName(REMOTE_PARAMETERS);
    property.setLabel("Parameters");
    property.setType(ProviderConfigProperty.STRING_TYPE);
    property.setHelpText("List of query parameters to send separated by '&'. Separate parameter name and value by an equals sign '=', the value can contain equals signs (ex: scope=all&full=true).");
    configProperties.add(property);

    // Headers
    property = new ProviderConfigProperty();
    property.setName(REMOTE_HEADERS);
    property.setLabel("Headers");
    property.setType(ProviderConfigProperty.STRING_TYPE);
    property.setHelpText("List of headers to send separated by '&'. Separate header name and value by an equals sign '=', the value can contain equals signs (ex: Authorization=az89d).");
    configProperties.add(property);
  }

  @Override
  public String getDisplayCategory(){
    return "Token mapper";
  }

  @Override
  public String getDisplayType() {
    return "JSON Remote URL claim";
  }

  @Override
  public String getHelpText() {
    return "Retrieve JSON data to include from a remote HTTP endpoint.";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  protected void setClaim(
          IDToken token,
          ProtocolMapperModel mappingModel,
          UserSessionModel userSession,
          KeycloakSession keycloakSession,
          ClientSessionContext clientSessionCtx
  ) {
    SimpleResponsePOJO claims = clientSessionCtx.getAttribute(REMOTE_AUTHORIZATION_ATTR, SimpleResponsePOJO.class);
    System.out.println("Claims - " + claims);
    if (claims == null) {
      claims = getRemoteClaims(mappingModel, userSession);
      System.out.println("Claims - " + claims.toString());
      clientSessionCtx.setAttribute(REMOTE_AUTHORIZATION_ATTR, claims);
    }

    OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claims);
  }

  private SimpleResponsePOJO getRemoteClaims(ProtocolMapperModel protocolMapperModel, UserSessionModel userSessionModel) {
    OkHttpClient client = new OkHttpClient();
    String URL = protocolMapperModel.getConfig().get(REMOTE_URL);
    Map<String, String> headers = getheaders(protocolMapperModel, userSessionModel);
    Map<String, String> params = getQueryParams(protocolMapperModel, userSessionModel);
    SimpleResponsePOJO responsePOJO = null;

    Headers.Builder headerBuilder = new Headers.Builder();
    headers.forEach(headerBuilder::add);

    HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(URL)).newBuilder();
    if (params != null) {
      params.forEach(urlBuilder::addQueryParameter);
    }

    Request request = new Request.Builder()
            .url(urlBuilder.build())
            .headers(headerBuilder.build())
            .build();

    System.out.println("REQUEST - " + request.toString());
    try {
      Response response = client.newCall(request).execute();
      ObjectMapper objectMapper = new ObjectMapper();
      responsePOJO = objectMapper.readValue(
              Objects.requireNonNull(response.body()).string(),
              SimpleResponsePOJO.class
      );
    } catch (IOException e) {
      e.printStackTrace();
    }
    return responsePOJO;
  }

  private Map<String, String> getQueryParams(ProtocolMapperModel protocolMapperModel, UserSessionModel userSessionModel){
    String paramsFromConfig = protocolMapperModel.getConfig().get(REMOTE_PARAMETERS);
    boolean setUserName = "true".equals(protocolMapperModel.getConfig().get(REMOTE_PARAMETERS_USERNAME));
    boolean setClientId = "true".equals(protocolMapperModel.getConfig().get(REMOTE_PARAMETERS_CLIENTID));

    //set other params here
    Map<String, String> formattedParams = buildFromString(paramsFromConfig);

    if(setUserName){
      formattedParams.put("username", userSessionModel.getLoginUsername());
    }

    if(setClientId){
      String clientID = userSessionModel.getAuthenticatedClientSessions().values().stream()
              .map(AuthenticatedClientSessionModel::getClient)
              .map(ClientModel::getClientId)
              .distinct()
              .collect(Collectors.joining(","));
      formattedParams.put("clientId", clientID);
    }
    return formattedParams;
  }

  private Map<String, String> getheaders(ProtocolMapperModel mappingModel, UserSessionModel userSession) {
    final String configuredHeaders = mappingModel.getConfig().get(REMOTE_HEADERS);

    // Get headers
    return buildFromString(configuredHeaders);
  }

  // auth=1&username=tim&age=60
  public static Map<String, String> buildFromString(String str) {
    Map<String,String> map = new HashMap<>();
    if(str != null && !"".equals(str.trim())){
      Arrays.stream(str.split("&")).forEach(el -> {
        String[] elSplit = el.split("=");
        map.put(elSplit[0], elSplit[1]);
      });
    }
    return map;
  }

}
