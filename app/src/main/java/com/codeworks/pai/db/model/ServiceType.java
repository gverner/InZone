package com.codeworks.pai.db.model;

public enum ServiceType {
	
	DEFAULT(0), PRICE(1), FULL(2), START(3), SETUP(4), BUSY(5), DONE(6), ERROR(7), INFO(8);
	
	int index;
	ServiceType(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	public static ServiceType fromIndex(int index) {
		for (ServiceType serviceType : values()) {
			if (serviceType.getIndex() == index) {
				return serviceType;
			}
		}
		return DEFAULT;
	}
}
