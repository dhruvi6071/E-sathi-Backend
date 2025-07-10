package com.example.ESathi.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;

import java.time.YearMonth;

@Converter(autoApply = true)
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth attribute) {
        return attribute != null ? attribute.toString() : null; // stored as "2025-07"
    }

    @Override
    public YearMonth convertToEntityAttribute(String dbData) {
        return dbData != null ? YearMonth.parse(dbData) : null;
    }
}