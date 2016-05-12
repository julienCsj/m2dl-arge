package edu.m2dl.s10.arge.openstack.repartiteur;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by julien on 25/03/16.
 */
public class Repartiteur {

    private String port;
    private static List<CalculateurLocal> lesCalculateurs = new ArrayList();

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Il faut fournir le numéro du port");
        }

        String port = args[0];

        // Boucle de reception des requetes

        Repartiteur r = new Repartiteur(port);
        // Lancement du serveur
        WebServer webServer = new WebServer(new Integer(port));
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
        PropertyHandlerMapping phm = new PropertyHandlerMapping();
        try {
            phm.addHandler("Repartiteur", Repartiteur.class);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        xmlRpcServer.setHandlerMapping(phm);
        XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
        serverConfig.setEnabledForExtensions(true);
        serverConfig.setContentLengthOptional(false);

        try {
            System.out.println("REPARTITEUR -> LANCEMENT DU SERVEUR");
            webServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OpenStackService ops = new OpenStackService();
        CalculateurLocal cal = ops.addVM();

        // AJOUT D'UN PREMIER CALCULATEUR
        lesCalculateurs.add(cal);
        System.out.println(cal);

    }


    public Repartiteur(String port) {
        System.out.println("CONSTRUCTEUR = "+ lesCalculateurs.size());
    }

    public Repartiteur() {
        System.out.println("CONSTRUCTEUR = "+lesCalculateurs.size());
    }

    public Boolean add(String ip, String port) {

        OpenStackService ops = new OpenStackService();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CalculateurLocal calculateurLocal = ops.addVM();
        lesCalculateurs.add(calculateurLocal);
        System.out.println("ADD ["+calculateurLocal.ip+", "+lesCalculateurs.size()+" calculateurs]");
        return true;
    }

    public Boolean del(String ip, String port) {
        OpenStackService ops = new OpenStackService();
        for (Iterator<CalculateurLocal> iterator = lesCalculateurs.iterator(); iterator.hasNext();) {
            CalculateurLocal calculateurLocal = iterator.next();
            if (calculateurLocal.port.equals(port) && calculateurLocal.ip.equals(ip)) {
                ops.deleteVM(calculateurLocal);
                iterator.remove();
            }
        }
        System.out.println("DEL [ "+lesCalculateurs.size()+" calculateurs]");
        return true;
    }

    public Integer request(Integer request) {
        // Choisir un calculateur
        System.out.println("RAND(1, "+lesCalculateurs.size()+")");
        int random = randInt(1, lesCalculateurs.size())-1;
        CalculateurLocal calculateurLocal = lesCalculateurs.get(random);
        System.out.println("REQUEST ["+request+", "+lesCalculateurs.size()+" calculateurs, "+random+" prend en charge la requete]");

        // CONFIGURATION DU CLIENT POUR APPELER LE CALCULATEUR DISTANT
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        Integer result = null;
        try {
            config.setServerURL(new URL("http://"+calculateurLocal.ip+":"+calculateurLocal.port+"/xmlrpc"));
            config.setEnabledForExtensions(true);
            config.setConnectionTimeout(60 * 1000);
            config.setReplyTimeout(60 * 1000);

            XmlRpcClient client = new XmlRpcClient();

            // use Commons HttpClient as transport
            client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
            // set configuration
            client.setConfig(config);

            Object[] params = new Object[] {new Integer(request)};

            result = (Integer) client.execute("Calculateur.calcul", params);
            System.out.println("RESULT REPARTITEUR = "+result);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return result;
    }


    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
