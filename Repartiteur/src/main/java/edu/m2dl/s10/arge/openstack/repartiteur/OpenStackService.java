package edu.m2dl.s10.arge.openstack.repartiteur;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.openstack.OSFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by julien on 11/05/16.
 */
public class OpenStackService {

    private OSClient os;

    public OpenStackService() {
        System.out.println("** Connexion à l'API CLOUDMIP");
        String url = "http://195.220.53.61:5000/v2.0";
        System.out.println(url);
        os = OSFactory.builder()
                .endpoint(url)
                .credentials("ens19", "7J2U7F")
                .tenantName("service")
                .authenticate();

        System.out.println("** SUCCESS -> OSClient OK");

    }

    public CalculateurLocal addVM() {
        System.out.println("** Ajout d'un nouveau calculateur");

        ServerCreate sc;
        List networksId = Arrays.asList("c1445469-4640-4c5a-ad86-9c0cb6650cca");
        sc = Builders.server().name("julien-elliot-cal-"+new Date().getTime())
                .flavor("2") // SMALL
                .image("e0dcc0a9-6132-40c7-9efc-034e5618daef")
                .keypairName("mykeyJulien")
                .networks(networksId)
                .userData("")
                .build();

        Server server = os.compute().servers().boot(sc);

        if (server.getStatus() == Server.Status.ERROR) {
            System.out.println("** ERREUR -> lors de la création du calculateur");
        }

        // En attente du boot de la VM
        while ((server = os.compute().servers().get(server.getId())).getStatus() != Server.Status.ACTIVE) {
            try {
                System.out.println("** En attente du boot de la VM");
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                System.out.println("** ERREUR -> Attente du boot de la VM impossible");
            }
        }

        // Récupération de l'IP de la machine
        Map<String, List<? extends Address>> adresses = server.getAddresses().getAddresses();
        if (adresses.size() <= 0) {
            System.out.println("** ERREUR -> Impossible de récupérer une IP");
        }

        Address address = adresses.values().iterator().next().get(0);

        System.out.println("** SUCCESS -> Calculateur ["+address.getAddr()+"] crée");

        CalculateurLocal calculateurLocal = new CalculateurLocal(address.getAddr(), "7070", server.getId());
        return calculateurLocal;

    }

    public boolean deleteVM(CalculateurLocal calculateur) {
        System.out.println("** Demande la destruction de la VM ["+calculateur.ip+", "+calculateur.id+"]");
        os.compute().servers().delete(calculateur.id);
        return true;
    }


}
