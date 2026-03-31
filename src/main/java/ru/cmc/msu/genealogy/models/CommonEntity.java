package ru.cmc.msu.genealogy.models;

public interface CommonEntity<ID> {
    ID getId();
    void setId(ID id);
}
