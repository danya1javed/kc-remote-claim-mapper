//package mapper;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import jdk.nashorn.internal.ir.annotations.Ignore;
//import netscape.javascript.JSObject;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import twitter4j.JSONObject;
//
//import javax.json.Json;
//import javax.json.JsonObject;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class RemoteDataMapperTest {
//
//  @Test
//  void buildFromString() {
//    String testParams = "user=tim&age=23&style=4.232/23";
//    Map<String, String> map = new HashMap<>();
//    map.put("style", "4.232/23");
//    map.put("user", "tim");
//    map.put("age", "23");
//    assertEquals(RemoteDataMapper.buildFromString(testParams), map);
//  }
//
//  @Disabled
//  @Test
//  void getRemoteClaims() throws IOException {
//    JSONObject testObj = new JSONObject();
//    testObj.put("usertype", "civilian");
//    testObj.put("nic", "4324239430");
//    testObj.put("subscription", true);
//
//    OkHttpClient client = new OkHttpClient();
//    Request request = new Request.Builder()
//            .url("https://run.mocky.io/v3/cedd2d40-b9c0-42c2-bc3e-af70dc322dac")
//            .build();
//    Response response = client.newCall(request).execute();
//    JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
//    assertEquals(jsonObject.toString(), testObj.toString());
//  }
//
//}