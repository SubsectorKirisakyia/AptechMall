package com.aptech.aptechMall.entity.enums;

public enum AddressType {
    HOME, OFFICE, OTHER;
    public static AddressType fromString(String value) {
        return AddressType.valueOf(value.trim().toUpperCase());
    }
}
