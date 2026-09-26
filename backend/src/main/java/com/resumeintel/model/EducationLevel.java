package com.resumeintel.model;

public enum EducationLevel {
    NONE(0), DIPLOMA(1), BACHELORS(2), MASTERS(3), PHD(4);

    public final int rank;
    EducationLevel(int rank) { this.rank = rank; }
}
