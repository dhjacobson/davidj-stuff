package com.hgdata.davidj.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Validation {

    private static String OLD_TABLE_ALIAS = "o";
    private static String NEW_TABLE_ALIAS = "n";

    private String oldTable;
    private String newTable;
    private String primaryKey;
    private List<ValidationEvent> events = new ArrayList<>();
    private List<ChildValidation> childValidations = new ArrayList<>();

    public Validation() {
    }

    public String getOldTable() {
        return oldTable;
    }

    public void setOldTable(String oldTable) {
        this.oldTable = oldTable;
    }

    public String getNewTable() {
        return newTable;
    }

    public void setNewTable(String newTable) {
        this.newTable = newTable;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<ValidationEvent> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<ValidationEvent> events) {
        this.events = events;
    }

    public void addEvent(ValidationEvent event) {
        this.events.add(event);
    }

    public List<ChildValidation> getChildValidations() {
        return childValidations;
    }

    public void setChildValidations(List<ChildValidation> childValidations) {
        this.childValidations = childValidations;
    }

    public void addChildValidation(ChildValidation validation) {
        this.childValidations.add(validation);
    }

    public List<String> fieldsToSelect() {
        return Arrays.asList(primaryKey);
    }

    public String buildQuery(String lineSeparator, int numberOfTabs) {

        List<String> lines = new ArrayList<>();

        //SELECT Statement
        lines.add("SELECT");
        lines.addAll(fieldsToSelect().stream()
                        .map(x -> x.equals(primaryKey) ? String.format("\tn.%s,", x) : String.format("\tARBITRARY(n.%1$s) as %1$s,", x))
                        .collect(Collectors.toList()));
        lines.add("\tFILTER(ARRAY[");
        lines.addAll(events.stream()
                .map(e -> String.format("\t\tIF(%s, '%s')", e.getWhereClause(), e.name()))
                .collect(Collectors.toList())
        );
        lines.add("\t], x -> x IS NOT NULL) AS events");

        //FROM Statement
        lines.add(String.format("FROM %s n", newTable));

        //JOIN to "Old" Table
        lines.add(String.format("LEFT JOIN %1$s o ON n.%2$s = o.%2$s", oldTable, primaryKey));

        //JOIN to Child Validations
        for (ChildValidation child : childValidations) {
            lines.add("LEFT JOIN (");
            lines.add(child.buildQuery("\n", numberOfTabs+1));
            lines.add(String.format(") %s ON %s", child.getSubQueryAlias(), child.joinOnStatement(child.getSubQueryAlias())));
        }

        //GROUP BY the Primary Key
        lines.add(String.format("GROUP BY %s", primaryKey));

        //Build string and return
        String tabs = new String(new char[numberOfTabs]).replace("\0", "\t");
        return tabs + lines.stream().collect(Collectors.joining(lineSeparator + tabs));
    }

    public String buildQuery() {
        return buildQuery("\n", 0);
    }

    public static void main(String[] args) {
        Validation hitsValidation = new Validation();
        hitsValidation.setOldTable("mrd20180520delivery.url_mrf_combined_global_hits");
        hitsValidation.setNewTable("mrd20180620delivery.url_mrf_combined_global_hits");
        hitsValidation.setPrimaryKey("mrf_id");
        hitsValidation.addEvent(ValidationEvent.GAINED_HIT);

        ChildValidation matchResultsValidation = new ChildValidation();
        matchResultsValidation.setOldTable("(select * from data_release_2018_05_002.match_results_core_products limit 10000)");
        matchResultsValidation.setNewTable("(select * from (select * from data_release_2018_06_001.match_results_core_products limit 10000) cross join unnest(product_ids) as x (product_id))");
        matchResultsValidation.setPrimaryKey("_id");
        matchResultsValidation.addEvent(ValidationEvent.GAINED_DOC);
        matchResultsValidation.addEvent(ValidationEvent.GAINED_URL);
        matchResultsValidation.addEvent(ValidationEvent.CHANGED_URL);
        matchResultsValidation.addEvent(ValidationEvent.GAINED_TAWG);
        matchResultsValidation.addEvent(ValidationEvent.LOST_OUT_BUS);
        matchResultsValidation.setSubQueryAlias("mr");
        matchResultsValidation.setChildJoinFields(Arrays.asList("product_id", "c_u"));
        matchResultsValidation.setParentJoinFields(Arrays.asList("product_id", "url"));
        hitsValidation.addChildValidation(matchResultsValidation);

        ChildValidation digsigValidation = new ChildValidation();
        digsigValidation.setOldTable("data_release_2018_05_002.digsig");

        System.out.println(hitsValidation.buildQuery());

    }

//    SELECT
//    n.mrf_id,
//    arbitrary(n.product_id),
//    arbitrary(n.url),
//    FILTER(ARRAY[
//            IF(arbitrary(o.mrf_id) is null, ROW('GAINED_HIT', histogram(filter(mr.events, x -> x in ('GAINED_DOC')))))
//            ], x -> x IS NOT NULL) AS events
//    FROM (select * from mrd20180520delivery.farnsworth_url_mrf_combined_global_hits limit 1000) n
//    LEFT JOIN (select * from mrd20180620delivery.farnsworth_url_mrf_combined_global_hits limit 1000) o ON n.mrf_id = o.mrf_id
//    LEFT JOIN (
//            SELECT
//                    n.*,
//            FILTER(ARRAY[
//                    IF(o._id is not null, 'GAINED_DOC'),
//    IF(o._id is not null and o.c_u is null and n.c_u is not null, 'GAINED_URL'),
//    IF(o.c_u <> n.c_u, 'CHANGED_URL'),
//    IF(o._id is not null and not contains(o.product_ids, n.product_id), 'GAINED_TAWG'),
//    IF(o.is_out_of_business is not null and n.is_out_of_business is null, 'LOST_OUT_BUS')
//		], x -> x IS NOT NULL) AS events
//    FROM (select * from (select * from data_release_2018_06_001.match_results_core_products limit 100000) cross join unnest(product_ids) as x (product_id)) n
//    LEFT JOIN (select * from data_release_2018_05_002.match_results_core_products limit 100000) o ON n._id = o._id
//) mr ON n.product_id = mr.product_id AND n.url = mr.c_u
//    group by n.mrf_id;




}
