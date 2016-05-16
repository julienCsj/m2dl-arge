package edu.m2dl.s10.arge.openstack.calculateur;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import oshi.SystemInfo;

import java.io.IOException;

public class Calculateur {
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Il faut fournir le numéro du port");
        }

        String port = args[0];


        Calculateur r = new Calculateur(port);
        r.run();

    }

    private String port;

    public void run() {
        // Lancement du serveur
        WebServer webServer = new WebServer(new Integer(port));
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
        PropertyHandlerMapping phm = new PropertyHandlerMapping();
        try {
            phm.addHandler("Calculateur", Calculateur.class);
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

    }

    public Calculateur(String port) {
        this.port = port;
    }

    public Calculateur() {

    }

    // Calcule le nombre de diviseurs de 2^n – 1
    public Long calcul(Integer n) {
        System.out.println("Calcul pour : 2^"+n+"-1");
        Double calcul = Math.pow(2, n) - 1;
        long result = 0;
        for(int i = 1 ; i <= calcul; i++) {
            if (calcul % i == 0) {
                result++;
            }
        }
        System.out.println("Résultat : "+result);
        return result;
    }

    public Double getLoad() {
        SystemInfo systemInfo = new SystemInfo();
        return systemInfo.getHardware().getProcessor().getSystemCpuLoad();
    }
}