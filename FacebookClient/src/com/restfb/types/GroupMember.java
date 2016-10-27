package com.restfb.types;

import lombok.Getter;
import lombok.Setter;

import com.restfb.Facebook;

/**
 * 
 * @author Chris Petsos (chpetsos@seerc.org)
 *
 * Data class that extends User and adds a isAdmin field. 
 */
public class GroupMember extends User {

	@Getter
	@Setter
	@Facebook("administrator")
	private Boolean isAdmin;

}
