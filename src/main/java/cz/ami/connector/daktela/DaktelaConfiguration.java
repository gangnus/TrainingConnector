
package cz.ami.connector.daktela;

import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;


public class DaktelaConfiguration extends AbstractConfiguration {
	
    private String serviceAddress = null;


    private Integer timeoutSeconds = 100;

    public DaktelaConfiguration() {
        //TODO default values
    }
    
    @Override
    public void validate() {
        // TODO Auto-generated method stub
    }
    
    @ConfigurationProperty(order = 10, displayMessageKey = "serviceAddress.display",
    groupMessageKey = "basic.group", helpMessageKey = "serviceAddress.help", required = true, confidential = false)
    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.serviceAddress = serviceAddress;
    }

    @ConfigurationProperty(order = 10, displayMessageKey = "default.timeout.label",
            groupMessageKey = "basic.group", helpMessageKey = "default.timeout.hint", required = true, confidential = false)
    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
