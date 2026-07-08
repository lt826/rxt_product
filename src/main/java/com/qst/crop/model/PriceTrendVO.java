package com.qst.crop.model;

import lombok.Data;
import java.util.List;

@Data
public class PriceTrendVO {
    private List<String> xAxis;
    private List<Double> historicalPrice;
    private List<Double> predictedPrice;
}
