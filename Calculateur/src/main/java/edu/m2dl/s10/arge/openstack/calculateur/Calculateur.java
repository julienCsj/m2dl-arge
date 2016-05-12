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

    // Calcul du Nieme element de la suite de Fibonacci avec une boucle
    public Long calcul(Integer number) {
        if(number == 1 || number == 2){
            return 1L;
        }
        long fibo1=1, fibo2=1, fibonacci=1;
        for(int i= 3; i<= number; i++){
            fibonacci = fibo1 + fibo2;
            fibonacci = fibo1 + fibo2;
            fibo1 = fibo2;
            fibo2 = fibonacci;

        }
        return fibonacci;
    }

    public Double getLoad() {
        SystemInfo systemInfo = new SystemInfo();
        return systemInfo.getHardware().getProcessor().getSystemCpuLoad();
    }
}