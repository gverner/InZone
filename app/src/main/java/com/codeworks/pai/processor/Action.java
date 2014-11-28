package com.codeworks.pai.processor;

import java.util.Locale;

public enum Action {
	SCHEDULE(0), REPEATING(1), ONE_TIME(2), MANUAL(3), MANUAL_MENU(4), PRICE_UPDATE(5), SET_PROGRESS_BAR(6);
	
	public static int _SCHEDULE=0;
	
	int		index;
	String	actionName;

	Action(int index) {
		this.index = index;
		this.actionName = "action_" + this.name().toLowerCase(Locale.US);
	}

	public int getIndex() {
		return index;
	}

	public String getActionName() {
		return actionName;
	}

	public static Action fromIndex(int index) {
		for (Action action : values()) {
			if (index == action.getIndex()) {
				return action;
			}
		}
		return null;
	}

	public static Action actionNameValueOf(String actionName) {
		for (Action action : values()) {
			if (action.getActionName().equals(actionName)) {
				return action;
			}
		}
		return null;
	}

}
