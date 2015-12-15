package com.code.aon.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.code.aon.common.ITransferObject;
import com.code.aon.config.enumeration.WorkGroupStatus;

@Entity
@Table(name="workgroup")
public class WorkGroup implements ITransferObject {

	private Integer id;
	
	private String description;
	
	private WorkGroupStatus status;

	@Id
	@GeneratedValue
	@Column(nullable=false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	@Column(length=64, nullable=false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public WorkGroupStatus getStatus() {
		return status;
	}

	public void setStatus(WorkGroupStatus status) {
		this.status = status;
	}
}
