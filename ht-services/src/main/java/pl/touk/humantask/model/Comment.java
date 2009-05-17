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

/**
 * Task content.
 *
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
@Entity
@Table(name = "TASK_COMMENT")
public class Comment extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

	@Column(name = "COMMENT_DATE")
    private Date date;

    @Column(length = 4096)
    private String content;

    @ManyToOne
    @JoinColumn(name = "TASK_ID")
    private Task task;

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setComment(String comment) {
        this.content = comment;
    }

    public String getComment() {
        return content;
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
