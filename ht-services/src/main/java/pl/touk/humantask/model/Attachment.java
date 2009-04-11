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

@Entity
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

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
