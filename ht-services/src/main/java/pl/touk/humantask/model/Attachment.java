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
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(name = "ATTACHMENT")
public class Attachment extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    @Temporal(javax.persistence.TemporalType.TIME)
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
        return name;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getContentType() {
        return contentType;
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
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Task getTask() {
        return task;
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
        int hash = 7;
        hash = 17 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 17 * hash + (this.attachment != null ? this.attachment.hashCode() : 0);
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 17 * hash + (this.accessType != null ? this.accessType.hashCode() : 0);
        hash = 17 * hash + (this.contentType != null ? this.contentType.hashCode() : 0);
        hash = 17 * hash + (this.attachedAt != null ? this.attachedAt.hashCode() : 0);
        hash = 17 * hash + (int) (this.userId ^ (this.userId >>> 32));
        hash = 17 * hash + (this.task != null ? this.task.hashCode() : 0);
        return hash;
    }

    /**
     * Checks whether the attachment is equal to another object.
     * @param obj object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Attachment other = (Attachment) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if ((this.attachment == null) ? (other.attachment != null) : !this.attachment.equals(other.attachment)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.accessType == null) ? (other.accessType != null) : !this.accessType.equals(other.accessType)) {
            return false;
        }
        if ((this.contentType == null) ? (other.contentType != null) : !this.contentType.equals(other.contentType)) {
            return false;
        }
        if (this.attachedAt != other.attachedAt && (this.attachedAt == null || !this.attachedAt.equals(other.attachedAt))) {
            return false;
        }
        if (this.userId != other.userId) {
            return false;
        }
        if (this.task != other.task && (this.task == null || !this.task.equals(other.task))) {
            return false;
        }
        return true;
    }

}
