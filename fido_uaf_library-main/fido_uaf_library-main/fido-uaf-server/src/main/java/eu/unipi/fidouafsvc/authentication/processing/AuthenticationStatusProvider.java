package eu.unipi.fidouafsvc.authentication.processing;

import eu.unipi.fidouafsvc.authentication.APIClient;
import eu.unipi.fidouafsvc.authentication.model.AccessToken;
import eu.unipi.fidouafsvc.authentication.model.User;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ioana on 11/13/2020.
 */
public class AuthenticationStatusProvider {

    public String username;
    public String fidoAuthenticationId;

    public AuthenticationStatusProvider(String username, String fidoAuthenticationId) {
        this.username = username;
        this.fidoAuthenticationId = fidoAuthenticationId;
    }

    public void authenticateUser() throws IOException {
        try {
            APIClient client = new APIClient();
            AccessToken accessToken = client.getAccessToken();
            List<User> userList = client.getUsers(accessToken.getAccessToken());
            User user = findUserByUsername(userList);
            String userId = setAuthenticationId(user);
            client.updateUserAuthenticationId(userId, accessToken.getAccessToken(), user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User findUserByUsername(List<User> userList) {
        User user = new User();
        for(int i = 0; i < userList.size(); i++) {
            if ((userList.get(i).getUsername()).equals(username)) {
                user = userList.get(i);
            }
        }
        return user;
    }
    public String setAuthenticationId(User user) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(fidoAuthenticationId);
        user.getAttributes().setFidoAuthenticationId(arrayList);
        return user.getId();
    }
}
