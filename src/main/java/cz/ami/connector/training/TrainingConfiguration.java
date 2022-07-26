package cz.ami.connector.training;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

public class TrainingConfiguration extends AbstractConfiguration {
    private String serviceAddress = null;
    private GuardedString accessToken = null;
    private Boolean trustAllCertificates = false;

    public TrainingConfiguration() {
        // default values
    }
    
    @Override
    public void validate() {
        // Auto-generated method stub
    }

    @ConfigurationProperty(order = 10, displayMessageKey = "serviceAddress.display",
            groupMessageKey = "basic.group", helpMessageKey = "serviceAddress.help", required = false, confidential = false)
    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }


    @ConfigurationProperty(order = 20, displayMessageKey = "accessToken.display",
            groupMessageKey = "basic.group", required = false, confidential = false)
    public GuardedString getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(GuardedString accessToken) {
        this.accessToken = accessToken;
    }

    @ConfigurationProperty(order = 30, displayMessageKey = "trustAllCertificates.display",
            groupMessageKey = "basic.group", helpMessageKey = "trustAllCertificates.help", required = true, confidential = false)
    public boolean getTrustAllCertificates() {
        return trustAllCertificates;
    }

    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }
}
