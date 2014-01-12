package com.pmease.gitop.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.base.Objects;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;

@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"project", "name"})
})
public class Branch extends AbstractEntity {
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Project project;

	@Column(nullable=false)
	private String name;
	
	@ManyToOne
	private User creator;
	
    @OneToMany(mappedBy="target")
    private Collection<PullRequest> ingoingRequests = new ArrayList<PullRequest>();

    @OneToMany(mappedBy="source")
    private Collection<PullRequest> outgoingRequests = new ArrayList<PullRequest>();
    
    private transient String headCommit;

    public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get creator of the branch. 
	 * 
	 * @return
	 * 			<tt>null</tt> if creator is unknown
	 */
	@Nullable
	public User getCreator() {
		return creator;
	}

	public void setCreator(@Nullable User creator) {
		this.creator = creator;
	}

	public Collection<PullRequest> getIngoingRequests() {
        return ingoingRequests;
    }

    public void setIngoingRequests(Collection<PullRequest> ingoingRequests) {
        this.ingoingRequests = ingoingRequests;
    }

    public Collection<PullRequest> getOutgoingRequests() {
        return outgoingRequests;
    }

    public void setOutgoingRequests(Collection<PullRequest> outgoingRequests) {
        this.outgoingRequests = outgoingRequests;
    }
    
    public String getHeadCommit() {
    	if (headCommit == null) {
	    	Git git = getProject().code();
	    	headCommit = git.parseRevision(getHeadRef(), true);
    	} 
    	return headCommit;
    }
    
    public String getHeadRef() {
    	return Git.REFS_HEADS + name; 
    }
    
    /**
     * Convert a git reference name to branch name.
     * 
     * @param refName
     *			name of the git reference 	
     * @return
     * 			name of the branch, or <tt>null</tt> if specified ref
     * 			does not represent a branch
     */ 
    public static String getName(String refName) {
		if (refName.startsWith(Git.REFS_HEADS)) 
			return refName.substring(Git.REFS_HEADS.length());
		else
			return null;
    }
    
    @Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("name", getName())
				.add("project", getProject())
				.toString();
	}
}
