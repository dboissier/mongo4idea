package org.codinjutsu.tools.mongo.view.model;

public enum NbPerPage {
    ALL("All", 0), TEN("10", 10), TWENTY("20", 20), FIFTY("50", 50);

    public final String label;
    public final int nb;

    NbPerPage(String label, int nb) {
        this.label = label;
        this.nb = nb;
    }
}
