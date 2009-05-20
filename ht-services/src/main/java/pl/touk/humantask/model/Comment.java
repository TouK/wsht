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
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Task content.
 *
 * @author Witek Wołejszo
 * @author Mateusz Lipczyński
 */
@Entity
@Table(name = "TASK_COMMENT")
public class Comment extends Base {
    
    @Transient
    private final Log log = LogFactory.getLog(Comment.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

	@Column(name = "COMMENT_DATE")
	@Temporal(TemporalType.TIME)
    private Date date;

    @Column(length = 4096)
    private String content;

    @ManyToOne
    @JoinColumn(name = "TASK_ID")
    private Task task;
    
    /***************************************************************
     * Constructor                                                 *
     ***************************************************************/

    /**
     * Creates {@link Comment}.
     */    
    public Comment() {
        super();
    }
    
    /**
     * Creates {@link Comment}.
     */
    public Comment(String content) {
        //TODO remove before going to production
        log.debug(content);
        this.content = content;
        this.date = new Date();
    }
    
    /***************************************************************
     * Getters & setters                                           *
     ***************************************************************/

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return this.date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    /***************************************************************
     * Infrastructure methods.                                     *
     ***************************************************************/

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Comment == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Comment rhs = (Comment) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {
        return (id == null ? 0 : id.hashCode());
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", this.id).append("date", this.date).append("comment", this.content).toString();
    }
}
