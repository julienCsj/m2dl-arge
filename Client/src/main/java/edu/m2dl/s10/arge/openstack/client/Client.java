package edu.m2dl.s10.arge.openstack.client;


import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Client {
    private static int nbReq;
    private static String ipRepartiteur;
    private static String portRepartiteur;

    public Client(int nbReq, String ipRepartiteur, String port) {
        this.nbReq = nbReq;
        this.ipRepartiteur = ipRepartiteur;
        this.portRepartiteur = port;
    }

    public Client() {

    }

    public boolean update(int nbReq, String ipRepartiteur, String port) {
        System.out.println("******");
        System.out.println("APPEL DE UPDATE");
        this.nbReq = nbReq;
        this.ipRepartiteur = ipRepartiteur;
        this.portRepartiteur = port;
        return true;
    }

    public static void main(String args[]) throws Exception {

        if (args.length != 3) {
            throw new Exception("Erreur nb arg");
        }

        nbReq = new Integer(args[0]);
        ipRepartiteur = args[1];
        portRepartiteur = args[2];

        // LANCEMENT DU SERVEUR POUR RECEVOIR LES REQUETES DE UPDATECLIENT
        WebServer webServer = new WebServer(6060);
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
        PropertyHandlerMapping phm = new PropertyHandlerMapping();
        try {
            phm.addHandler("Client", Client.class);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        xmlRpcServer.setHandlerMapping(phm);
        XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);

        try {
            webServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // CONFIGURATION DU CLIENT POUR APPELER LE REPARTITEUR
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

        System.out.println("NB REQ = "+Client.nbReq);

        while (true) {

            try {
                Integer random = randInt(24, 27);
                Object[] params = new Object[] {random};
                Long result = null;
                result = (Long) client.execute("Repartiteur.request", params);
                System.out.println("diviseurs(2^"+random+"-1) = "+result);
            } catch (XmlRpcException e) {
                e.printStackTrace();
            }

            sleep(1000/nbReq);
        }
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
