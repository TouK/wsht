/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

/**
 * Enum type [spec 3.2]
 * @author wcr
 */
public enum GenericHumanRole {
    
    TASK_INITIATOR("taskInitiator"),
    TASK_STAKEHOLDERS("taskStakeholders"),
    POTENTIAL_OWNERS("potentialOwners"),
    //TODO: ?? spec ?? 
    ACTUAL_OWNER("actualOwner"),
    BUSINESS_ADMINISTRATORS("businessAdministrators"),
    NOTIFICATION_RECIPIENTS("notificationRecipients"),
    RECIPIENTS("recipients");

    GenericHumanRole(String value) {
        this.value = value;
    }
    private final String value;

    public static GenericHumanRole fromValue(String value){
        for (GenericHumanRole ghr : GenericHumanRole.values())
            if (null!=value && value.equals(ghr.toString()))
                return ghr;
        
        return null;
    }
    @Override
    public String toString() {
        return value;
    }
}