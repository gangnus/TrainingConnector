package cz.ami.connector.daktela.model;

import lombok.Data;

@Data
public class Profile {
    private String name;
    private String title;
    private String description;
    private Integer maxActivities;
    private Integer maxOutRecords;
    private Boolean deleteMissedActivity;
    private Boolean noQueueCallsAllowed;
    private String canTransferCall;
//    private String options;
//    private String customViews;

}
