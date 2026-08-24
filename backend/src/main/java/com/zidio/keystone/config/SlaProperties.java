package com.zidio.keystone.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Centralized SLA policy so response-time hours are never scattered through the codebase.
 * Backed by keystone.sla.* in application.yml (environment-overridable).
 */
@Component
@ConfigurationProperties(prefix = "keystone.sla")
public class SlaProperties {

    private int lowHours = 72;
    private int mediumHours = 48;
    private int highHours = 24;
    private int urgentHours = 4;
    private int atRiskThresholdPercent = 80;

    public int getLowHours() { return lowHours; }
    public void setLowHours(int lowHours) { this.lowHours = lowHours; }

    public int getMediumHours() { return mediumHours; }
    public void setMediumHours(int mediumHours) { this.mediumHours = mediumHours; }

    public int getHighHours() { return highHours; }
    public void setHighHours(int highHours) { this.highHours = highHours; }

    public int getUrgentHours() { return urgentHours; }
    public void setUrgentHours(int urgentHours) { this.urgentHours = urgentHours; }

    public int getAtRiskThresholdPercent() { return atRiskThresholdPercent; }
    public void setAtRiskThresholdPercent(int atRiskThresholdPercent) { this.atRiskThresholdPercent = atRiskThresholdPercent; }
}
