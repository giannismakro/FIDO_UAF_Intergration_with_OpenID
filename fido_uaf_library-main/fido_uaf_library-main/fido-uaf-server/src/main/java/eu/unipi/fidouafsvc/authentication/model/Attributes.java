
package eu.unipi.fidouafsvc.authentication.model;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import eu.unipi.fidouafsvc.authentication.config.APIConfiguration;

public class Attributes implements Serializable
{

    @SerializedName(APIConfiguration.FIDO_AUTHENTICATION_ID)
    @Expose
    private List<String> fidoAuthenticationId = null;
    private final static long serialVersionUID = 7698841098409851980L;

    public List<String> getFidoAuthenticationId() {
        return fidoAuthenticationId;
    }

    public void setFidoAuthenticationId(List<String> fidoAuthenticationId) {
        this.fidoAuthenticationId = fidoAuthenticationId;
    }

}
