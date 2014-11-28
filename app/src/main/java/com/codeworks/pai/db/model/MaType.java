package com.codeworks.pai.db.model;

// Simple or Exponential
public enum MaType {
	S, E;

	public static MaType parse(String value) {
		if (E.name().equals(value)) {
			return E;
		} else if (S.name().equals(value)) {
			return S;
		} else {
			return E; // defautl to E
		}
	}
}
