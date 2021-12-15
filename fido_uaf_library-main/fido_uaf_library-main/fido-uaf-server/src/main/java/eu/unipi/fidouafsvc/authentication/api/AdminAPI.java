package eu.unipi.fidouafsvc.authentication.api;

import eu.unipi.fidouafsvc.authentication.config.APIConfiguration;
import eu.unipi.fidouafsvc.authentication.model.AccessToken;
import eu.unipi.fidouafsvc.authentication.model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by ioana on 11/12/2020.
 *Updated by Ioannis Makropodis 23/8/2021
 */
public interface AdminAPI {

    @FormUrlEncoded
    @POST("realms/" + APIConfiguration.REALM + "/protocol/openid-connect/token")
    Call<AccessToken> getAdminAccessToken(@Field("grant_type") String grant_type,
                                          @Field("client_id") String client_id,
                                          @Field("username") String username,
                                          @Field("password") String password) throws IOException;
    @GET("admin/realms/" + APIConfiguration.REALM + "/users")
    Call<List<User>> getListOfUsers(@Header("Authorization") String token);

    @PUT("admin/realms/" + APIConfiguration.REALM + "/users/{id}")
    Call<ResponseBody> updateUser(@Path("id") String id,
                                  @Header("Authorization") String token,
                                  @Header("Content_Type") String content_type,
                                  @Body User user);
}
