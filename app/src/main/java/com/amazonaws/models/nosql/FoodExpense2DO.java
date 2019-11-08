package com.amazonaws.models.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "expensehelper-mobilehub-1182243925-FoodExpense2")

public class FoodExpense2DO {
    private String _uniqueKey;
    private String _aBN;
    private String _date;
    private Double _price;

    @DynamoDBHashKey(attributeName = "UniqueKey")
    @DynamoDBAttribute(attributeName = "UniqueKey")
    public String getUniqueKey() {
        return _uniqueKey;
    }

    public void setUniqueKey(final String _uniqueKey) {
        this._uniqueKey = _uniqueKey;
    }
    @DynamoDBAttribute(attributeName = "ABN")
    public String getABN() {
        return _aBN;
    }

    public void setABN(final String _aBN) {
        this._aBN = _aBN;
    }
    @DynamoDBAttribute(attributeName = "Date")
    public String getDate() {
        return _date;
    }

    public void setDate(final String _date) {
        this._date = _date;
    }
    @DynamoDBAttribute(attributeName = "Price")
    public Double getPrice() {
        return _price;
    }

    public void setPrice(final Double _price) {
        this._price = _price;
    }

}
