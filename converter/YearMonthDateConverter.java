package com.aurionpro.app.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;
import java.time.YearMonth;

@Converter(autoApply = true) // autoApply = true means it will be used for all YearMonth fields
public class YearMonthDateConverter implements AttributeConverter<YearMonth, LocalDate> {

    @Override
    public LocalDate convertToDatabaseColumn(YearMonth attribute) {
        // Converts YearMonth (e.g., 2025-09) to the first day of that month for DB storage
        if (attribute != null) {
            return attribute.atDay(1);
        }
        return null;
    }

    @Override
    public YearMonth convertToEntityAttribute(LocalDate dbData) {
        if (dbData != null) {
            return YearMonth.from(dbData);
        }
        return null;
    }
}