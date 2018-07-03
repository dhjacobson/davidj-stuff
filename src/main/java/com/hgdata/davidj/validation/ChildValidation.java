package com.hgdata.davidj.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChildValidation extends Validation {

    private String subQueryAlias;
    private List<String> childJoinFields = new ArrayList<>();
    private List<String> parentJoinFields = new ArrayList<>();

    public String getSubQueryAlias() {
        return subQueryAlias;
    }

    public void setSubQueryAlias(String subQueryAlias) {
        this.subQueryAlias = subQueryAlias;
    }

    public List<String> getChildJoinFields() {
        return childJoinFields;
    }

    public void setChildJoinFields(List<String> childJoinFields) {
        this.childJoinFields = childJoinFields;
    }

    public List<String> getParentJoinFields() {
        return parentJoinFields;
    }

    public void setParentJoinFields(List<String> parentJoinFields) {
        this.parentJoinFields = parentJoinFields;
    }

    @Override
    public List<String> fieldsToSelect() {
        ArrayList<String> fields = new ArrayList(super.fieldsToSelect());
        fields.addAll(childJoinFields);
        return fields;
    }

    public String joinOnStatement(String outerQueryAlias) {
        if (childJoinFields.size() != parentJoinFields.size()) {
            throw new IllegalArgumentException("Need an equal number of parent and child fields");
        }
        if (childJoinFields.size() == 0) {
            throw new IllegalArgumentException("Need to know what fields to join on.");
        }
        return IntStream.range(0, childJoinFields.size())
                .mapToObj(i -> new String[]{parentJoinFields.get(i), childJoinFields.get(i)})
                .map(x -> String.format("%s.%s = %s.%s", this.getSubQueryAlias(), x[0], outerQueryAlias, x[1]))
                .collect(Collectors.joining(" and "));
    }

}
