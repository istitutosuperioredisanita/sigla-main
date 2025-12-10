package it.cnr.contab.util;

import it.cnr.jada.UserContext;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;

public class TestUserContext implements UserContext {
    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public String getUser() {
        return "test";
    }

    @Override
    public boolean isTransactional() {
        return false;
    }

    @Override
    public void setTransactional(boolean flag) {

    }

    @Override
    public void writeTo(PrintWriter printwriter) {

    }

    @Override
    public Dictionary getHiddenColumns() {
        return null;
    }

    @Override
    public Hashtable<String, Serializable> getAttributes() {
        return null;
    }
}