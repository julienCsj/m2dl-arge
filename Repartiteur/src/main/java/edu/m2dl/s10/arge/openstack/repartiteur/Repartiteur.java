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

    //public static String mode = "LOCAL"; // Utilise les calculateurs locaux
    public static String mode = "PROD"; // Utilise les calculateurs sur cloudMIP

    private String port;
    private static List<CalculateurLocal> lesCalculateurs = new ArrayList();

    private static List<Double> cpuLoadHistory = new ArrayList();
    private static int currentPositionHistory = 0;
    private static final int HISTORY_SIZE = 10;

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Il faut fournir le numéro du port");
        }

        String port = args[0];

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
            System.out.println("REPARTITEUR -> LANCEMENT DU SERVEUR ["+mode+"] sur le port ["+port+"]");
            webServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(mode.equals("PROD")) {
            System.out.println("REPARTITEUR -> LANCEMENT DU CALCULATEUR INITIAL");
            OpenStackService ops = new OpenStackService();
            CalculateurLocal cal = ops.addVM();

            // AJOUT D'UN PREMIER CALCULATEUR
            lesCalculateurs.add(cal);
            System.out.println(cal);
        }
    }


    public Repartiteur(String port) {
        System.out.println("CONSTRUCTEUR = "+ lesCalculateurs.size());
    }

    public Repartiteur() {
        System.out.println("CONSTRUCTEUR AUTO = "+lesCalculateurs.size());
    }

    public Boolean add(String ip, String port) {


        CalculateurLocal calculateurLocal = null;

        if(mode.equals("PROD")) {
            OpenStackService ops = new OpenStackService();
            calculateurLocal = ops.addVM();
        }

        if(mode.equals("LOCAL")) {
            calculateurLocal = new CalculateurLocal("localhost", port, null);
        }

        lesCalculateurs.add(calculateurLocal);
        System.out.println("ADD ["+calculateurLocal.ip+", "+lesCalculateurs.size()+" calculateurs]");
        return true;
    }

    public Boolean del(String ip, String port) {
        OpenStackService ops = null;
        if(mode.equals("PROD")) {
            ops = new OpenStackService();
        }

        if(ip == null && port == null) {
            CalculateurLocal cal = lesCalculateurs.get(lesCalculateurs.size()-1);
            ops.deleteVM(cal);
        }

        for (Iterator<CalculateurLocal> iterator = lesCalculateurs.iterator(); iterator.hasNext();) {
            CalculateurLocal calculateurLocal = iterator.next();
            if (calculateurLocal.port.equals(port) && calculateurLocal.ip.equals(ip)) {
                if(mode.equals("PROD")) {
                    ops.deleteVM(calculateurLocal);
                }
                iterator.remove();
            }
        }
        System.out.println("DEL [ "+lesCalculateurs.size()+" calculateurs]");
        return true;
    }

    public Long request(Integer request) {
        // Choisir un calculateur
        int random = randInt(1, lesCalculateurs.size())-1;
        CalculateurLocal calculateurLocal = lesCalculateurs.get(random);

        // CONFIGURATION DU CLIENT POUR APPELER LE CALCULATEUR DISTANT
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        Long result = null;
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

            System.out.println("** ENVOI D'UN CALCUL AU CALCULATEUR");
            Object[] params = new Object[] {new Integer(request)};
            result = (Long) client.execute("Calculateur.calcul", params);

            params = new Object[] {};
            calculateurLocal.load = (Double) client.execute("Calculateur.getLoad", params);
            System.out.println("REPARTITEUR -> LA CHARGE DE ["+calculateurLocal.ip+"] est de "+calculateurLocal.load);

            // On effectue le load balancing
            loadBalancing();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void loadBalancing() {

        // Calcul de la moyenne de consommation courante
        Double moyenneLoad = 0D;
        for (CalculateurLocal calculateur : lesCalculateurs) {
            moyenneLoad += calculateur.load;
        }
        moyenneLoad = moyenneLoad / lesCalculateurs.size();

        // Ajout de la moyenne à l'historique
        cpuLoadHistory.add(currentPositionHistory, moyenneLoad);

        if (currentPositionHistory == HISTORY_SIZE) {
            Double averageConsumption = averageConsumption();

            System.out.println("Charge moyenne : "+averageConsumption()*100+"%");

            // Pas plus de 3 VM de calcul
            if(averageConsumption > 0.80 && lesCalculateurs.size() < 3) {
                System.out.println("AJOUT VM");
                add(null, null);
            }

            if(averageConsumption < 0.20 && lesCalculateurs.size() > 1) {
                System.out.println("DELETE VM");
                del(null, null);
            }
        }
        currentPositionHistory = (currentPositionHistory + 1) % HISTORY_SIZE;
    }

    private Double averageConsumption() {
        Double res = 0D;
        for (Double d : cpuLoadHistory) {
            res += d;
        }
        return res / HISTORY_SIZE;
    }


    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
