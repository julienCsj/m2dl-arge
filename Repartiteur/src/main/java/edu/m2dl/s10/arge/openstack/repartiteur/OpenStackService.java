package edu.m2dl.s10.arge.openstack.repartiteur;

import org.openstack4j.api.OSClient;
import org.openstack4j.core.transport.Config;
import org.openstack4j.core.transport.ProxyHost;
import org.openstack4j.openstack.OSFactory;

/**
 * Created by julien on 11/05/16.
 */
public class OpenStackService {

    private OSClient os;

    public void auth() {


        os = OSFactory.builder()
                .endpoint("http://cloudmip.univ-tlse3.fr:5000/v2.0")
                .credentials("ens19","7J2U7F")
                .tenantName("service")
                .withConfig(Config.newConfig().withProxy(ProxyHost.of("http://127.0.0.1", 5000)))
                .authenticate();


    }


}
