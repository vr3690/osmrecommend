package com.osmrecommend.data.event.edit;

import com.osmrecommend.data.event.Edit;
import com.osmrecommend.persistence.domain.Way;

public class WayEdit implements Edit {

	private Way way;
	
	/**
	 * @return the way
	 */
	public Way getWay() {
		return way;
	}

	/**
	 * @param way the way to set
	 */
	public void setWay(Way way) {
		this.way = way;
	}

	@Override
	public long getUserId() {
		return way.getUser().getId();
	}

	@Override
	public long getItemId() {
		return way.getId();
	}

	@Override
	public long getTimestamp() {
		return way.getTimestamp().getTime();
	}
	
	public WayEdit(Way way) {
		this.way = way;
	}

}
