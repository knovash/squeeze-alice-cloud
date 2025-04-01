package org.knovash.alicebroker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.ZoneId;
import java.util.ResourceBundle;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {


    public int port;
    public String lmsIp;
    public String lmsPort;
    public String lmsUrl;
    public String silence;
    public String domain;
    public ZoneId zoneId;

    public void readProperties() {
        log.info("READ CONFIG FROM config.properties");
        ResourceBundle bundle = ResourceBundle.getBundle("config");
//        this.inCloud = Boolean.valueOf(bundle.getString("inCloud"));
        this.port = Integer.parseInt(bundle.getString("port"));
        this.lmsIp = bundle.getString("lmsIp");
        this.lmsPort = bundle.getString("lmsPort");
        this.silence = bundle.getString("silence");
        this.domain = bundle.getString("domain");
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
    }

    @Override
    public String toString() {
        return "org.knovash.alicebroker.Config {" + "\n" +
//                " inCloud = " + inCloud + "\n" +
                " port = " + port + "\n" +
                " lmsIp = " + lmsIp + "\n" +
                " lmsPort = " + lmsPort + "\n" +
                " lmsUrl = " + lmsUrl + "\n" +
                " silence = " + silence + "\n" +
                " domain = " + domain + "\n" +
                " zoneId = " + zoneId + "\n" +
                '}';
    }
}