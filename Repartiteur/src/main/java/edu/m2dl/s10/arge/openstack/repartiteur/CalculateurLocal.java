package edu.m2dl.s10.arge.openstack.repartiteur;

import java.util.Date;

/**
 * Created by julien on 11/05/16.
 */
public class CalculateurLocal {

    public String ip;
    public String port;
    public Date lastUsage;
    public String id;

    public CalculateurLocal(String ip, String port, String id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }

    @Override
    public String toString() {
        return "CalculateurLocal{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", lastUsage=" + lastUsage +
                ", id='" + id + '\'' +
                '}';
    }
}
