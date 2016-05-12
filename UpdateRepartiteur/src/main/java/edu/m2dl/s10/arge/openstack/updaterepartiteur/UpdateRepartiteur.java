package edu.m2dl.s10.arge.openstack.updaterepartiteur;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Leo on 25/03/16.
 */
public class UpdateRepartiteur {


    public static void main(String[] args) throws MalformedURLException, XmlRpcException {
        if (args.length != 5) {
            throw new RuntimeException("update_repartiteur IPRepartiteur PortRepartiteur add|del Argument1 Argument2");
        }

        String ipRepartiteur = args[0];
        String portRepartiteur = args[1];
        String action = args[2];
        String ipCalculateur = args[3];
        String portCalculateur = args[4];

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://"+ipRepartiteur+":"+portRepartiteur+"/xmlrpc"));
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(60 * 1000);
        config.setReplyTimeout(60 * 1000);

        XmlRpcClient client = new XmlRpcClient();

        // use Commons HttpClient as transport
        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        // set configuration
        client.setConfig(config);

        if(action.equals("add")) {
            Object[] params = new Object[]{ipCalculateur, portCalculateur};
            client.execute("Repartiteur.add", params);
        }

        if(action.equals("del")) {
            Object[] params = new Object[]{ipCalculateur, portCalculateur};
            client.execute("Repartiteur.del", params);
        }

        System.out.println("C'est fait !");

    }
}

