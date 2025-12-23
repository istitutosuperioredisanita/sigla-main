package it.cnr.contab.web.rest.resource.security;

import jakarta.ws.rs.FormParam;

public class LoginForm {
    @FormParam("j_username")
    private String username;
    @FormParam("j_password")
    private String password;

    public LoginForm() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
