package com.hgdata.davidj.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public enum ValidationEvent {

    //Core Match Results
    GAINED_DOC(1, "Gained Doc", "o._id is null"),
    GAINED_URL(2, "Gained URL", "o._id is not null and o.c_u is null and n.c_u is not null"),
    CHANGED_URL(3, "Changed URL", "o.c_u <> n.c_u"),
    GAINED_TAWG(4, "Gained Tawg", "o._id is not null and not contains(o.product_ids, n.product_id)"),
    LOST_OUT_BUS(5, "Removed Out-of-Business", "o.is_out_of_business is not null and n.is_out_of_business is null"),

    //Global Hits
    GAINED_HIT(1, "Gained Hit", "o.mrf_id is null", Arrays.asList(GAINED_DOC, GAINED_URL, CHANGED_URL, GAINED_TAWG))
    ;

    public final int id;
    public final String displayText;
    public final String whereClause;
    public final List<ValidationEvent> causedBy;

    ValidationEvent(int id, String displayText, String whereClause, List<ValidationEvent> causedBy) {
        this.id = id;
        this.displayText = displayText;
        this.whereClause = whereClause;
        this.causedBy = causedBy;
    }

    ValidationEvent(int id, String displayText, String whereClause) {
        this.id = id;
        this.displayText = displayText;
        this.whereClause = whereClause;
        this.causedBy = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getWhereClause() {
        return whereClause;
    }

}
