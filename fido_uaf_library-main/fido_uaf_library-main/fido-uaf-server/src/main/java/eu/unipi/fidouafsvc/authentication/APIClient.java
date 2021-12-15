package eu.unipi.fidouafsvc.authentication;

import eu.unipi.fidouafsvc.authentication.api.AdminAPI;
import eu.unipi.fidouafsvc.authentication.config.APIConfiguration;
import eu.unipi.fidouafsvc.authentication.model.AccessToken;
import eu.unipi.fidouafsvc.authentication.model.User;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by ioana on 11/12/2020.
  *Updated by Ioannis Makropodis 23/8/2021
 */
public class APIClient {

    public AdminAPI client;

    public APIClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIConfiguration.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        client = retrofit.create(AdminAPI.class);
    }

    public AccessToken getAccessToken() throws IOException {

        Call<AccessToken> call = client.getAdminAccessToken(
                APIConfiguration.GRANT_TYPE,
                APIConfiguration.CLIENT_ID,
                APIConfiguration.USERNAME,
                APIConfiguration.PASSWORD);

        Response<AccessToken> response = call.execute();
        if (!response.isSuccessful()) {

            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }
        return response.body();
    }

    public List<User> getUsers(String token) throws IOException {
        String access_token = "Bearer " + token;
        Call<List<User>> call = client.getListOfUsers(access_token);
        Response<List<User>> response = call.execute();
        if (!response.isSuccessful()) {

            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }
        return response.body();
    }

    public void updateUserAuthenticationId(String id, String token, User user) throws IOException {
        String access_token = "Bearer " + token;
        Call<ResponseBody> call = client.updateUser(id, access_token, APIConfiguration.JSON_CONTENT_TYPE, user);
        Response response = call.execute();
        if (!response.isSuccessful()) {

            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }
    }

}
