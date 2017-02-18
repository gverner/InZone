package com.codeworks.pai.db.model;

// Simple or Exponential
public enum MaType {
	S(2), E(1), D(3);

    MaType(int profilioId) {
        this.portfolioId = profilioId;
    }
	int portfolioId;
    public int getPortfolioId() {
        return portfolioId;
    }

    public static MaType byProtfolioId(int value) {
        switch (value) {
            case 1: return E;
            case 2: return S;
            case 3: return D;
        }
        return E;
    }

	public static MaType parse(String value) {
		if (E.name().equals(value)) {
			return E;
		} else if (S.name().equals(value)) {
			return S;
		} else if (D.name().equals(value)) {
			return D;
		} else {
			return E; // default to E
		}
	}
}
