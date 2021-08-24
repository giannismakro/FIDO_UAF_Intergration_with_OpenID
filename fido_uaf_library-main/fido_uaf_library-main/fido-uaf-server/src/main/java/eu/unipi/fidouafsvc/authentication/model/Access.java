
package eu.unipi.fidouafsvc.authentication.model;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Access implements Serializable
{

    @SerializedName("manageGroupMembership")
    @Expose
    private Boolean manageGroupMembership;
    @SerializedName("view")
    @Expose
    private Boolean view;
    @SerializedName("mapRoles")
    @Expose
    private Boolean mapRoles;
    @SerializedName("impersonate")
    @Expose
    private Boolean impersonate;
    @SerializedName("manage")
    @Expose
    private Boolean manage;
    private final static long serialVersionUID = 3123520260799267647L;

    public Boolean getManageGroupMembership() {
        return manageGroupMembership;
    }

    public void setManageGroupMembership(Boolean manageGroupMembership) {
        this.manageGroupMembership = manageGroupMembership;
    }

    public Boolean getView() {
        return view;
    }

    public void setView(Boolean view) {
        this.view = view;
    }

    public Boolean getMapRoles() {
        return mapRoles;
    }

    public void setMapRoles(Boolean mapRoles) {
        this.mapRoles = mapRoles;
    }

    public Boolean getImpersonate() {
        return impersonate;
    }

    public void setImpersonate(Boolean impersonate) {
        this.impersonate = impersonate;
    }

    public Boolean getManage() {
        return manage;
    }

    public void setManage(Boolean manage) {
        this.manage = manage;
    }

}
