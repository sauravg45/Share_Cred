package com.example.credpass.DTO;

import android.util.ArrayMap;
import android.view.autofill.AutofillId;

import java.util.List;

public class AutofillParserDTO {
    List<UIDataDTO> dbData;
    ArrayMap<String, AutofillId> fields;

    public AutofillParserDTO(List<UIDataDTO> dbData, ArrayMap<String, AutofillId> fields) {
        this.dbData = dbData;
        this.fields = fields;
    }

    public List<UIDataDTO> getDbData() {
        return dbData;
    }

    public void setDbData(List<UIDataDTO> dbData) {
        this.dbData = dbData;
    }

    public ArrayMap<String, AutofillId> getFields() {
        return fields;
    }

    public void setFields(ArrayMap<String, AutofillId> fields) {
        this.fields = fields;
    }
}
