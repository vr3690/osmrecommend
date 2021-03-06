package com.osmrecommend.persistence.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Geometry;

@Entity
@Table(name = "nodes")
@Cacheable
public class Node implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2973199082591265051L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	
	@Column(name="node_id")
	private Long nodeId;

	@Column(name = "version")
	private Integer version;
	
	@OneToOne
	@Fetch(FetchMode.JOIN)
	@JoinColumn(name = "user_id")
	private User user;
	
	@Column(name = "tstamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date tstamp;
	
	@Column(name = "changeset_id")
	private Long changesetId;
	
	@Column(name = "geom")
	@Type(type = "org.hibernate.spatial.GeometryType")
	private Geometry geom;
	
	@Column(name = "tags")
	private String tags;
	

	/**
	 * @return the nodeId
	 */
	public Long getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId the nodeId to set
	 */
	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return the tags
	 */
	public String getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(String tags) {
		this.tags = tags;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getTstamp() {
		return tstamp;
	}

	public void setTstamp(Date tstamp) {
		this.tstamp = tstamp;
	}

	public Long getChangesetId() {
		return changesetId;
	}

	public void setChangesetId(Long changesetId) {
		this.changesetId = changesetId;
	}

	public Geometry getGeom() {
		return geom;
	}

	public void setGeom(Geometry geom) {
		this.geom = geom;
	}
	
	
}
