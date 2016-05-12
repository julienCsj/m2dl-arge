package edu.m2dl.s10.arge.openstack.updateclient;

import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

import java.net.URL;
import java.util.Vector;


public class UpdateClient {


    public static void main(String[] args) throws Exception {

        System.out.println("ARGS COUNT = "+args.length);

        if(args.length != 3) {
            throw new RuntimeException("Syntaxe: update_client param addresse port");
        }

        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://127.0.0.1:7070/xmlrpc"));
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(60 * 1000);
        config.setReplyTimeout(60 * 1000);

        XmlRpcClient client = new XmlRpcClient();

        // use Commons HttpClient as transport
        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        // set configuration
        client.setConfig(config);

        // make the a regular call
        Object[] params = new Object[]{ new Integer(args[0]), new String(args[1]), new String(args[2]) };
        client.execute("Client.update", params);
    }
}
