/*
 * Copyright (C) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "ATTACHMENT")
public class Attachment extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "attc_seq")
    @SequenceGenerator(name = "attc_seq", sequenceName = "attc_seq")
    private Long id;

    @Column
    private String attachment;

    @Column
    private String name;

    @Column
    private String accessType;

    @Column
    private String contentType;

    @Column
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date attachedAt;

    @Column
    private long userId;

    @ManyToOne
    @JoinColumn(name = "TASK_ID")
    private Task task;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getAccessType() {
        return this.accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getAttachedAt() {
        return (this.attachedAt == null) ? null : (Date) this.attachedAt.clone();
    }

    public void setAttachedAt(Date attachedAt) {
        this.attachedAt = (attachedAt == null) ? null : (Date) attachedAt.clone();
    }

    public long getUserId() {
        return this.userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAttachment() {
        return this.attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Returns the attachment hashcode.
     * @return attachment hash code
     */
    @Override
    public int hashCode() {
        return this.id == null ? 0 : this.id.hashCode();
    }

    /**
     * Checks whether the attachment is equal to another object.
     * @param obj object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Attachment == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Attachment rhs = (Attachment) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", this.id).append("name", this.getName()).toString();
    }

}
