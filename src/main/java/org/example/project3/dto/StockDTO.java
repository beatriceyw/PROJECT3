package org.example.project3.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class StockDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String stockCode;
    private String stockName;
    private Double pbr;
    private Double per;
    private LocalDateTime createDate;

    public StockDTO() {}

    public StockDTO(Long id, String stockCode, String stockName, Double pbr, Double per, LocalDateTime createDate) {
        this.id = id;
        setStockCode(stockCode);
        setStockName(stockName);
        this.pbr = pbr;
        this.per = per;
        this.createDate = createDate;
    }

    /* ===== Getters ===== */
    public Long getId() { return id; }
    public String getStockCode() { return stockCode; }
    public String getStockName() { return stockName; }
    public Double getPbr() { return pbr; }
    public Double getPer() { return per; }
    public LocalDateTime getCreateDate() { return createDate; }

    /* JSP에서 문자열 바로 출력 */
    public String getCreateDateText() {
        if (createDate == null) return "";
        return createDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /* ===== Setters ===== */
    public void setId(Long id) { this.id = id; }

    public void setStockCode(String stockCode) {
        String v = (stockCode == null) ? null : stockCode.trim();
        this.stockCode = (v == null || v.isEmpty()) ? null : v;
    }

    public void setStockName(String stockName) {
        String v = (stockName == null) ? null : stockName.trim();
        this.stockName = (v == null || v.isEmpty()) ? null : v;
    }

    public void setPbr(Double pbr) { this.pbr = pbr; }
    public void setPer(Double per) { this.per = per; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }

    /* ===== equals/hashCode: 고유키 stockCode 기준 ===== */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockDTO)) return false;
        StockDTO that = (StockDTO) o;
        return Objects.equals(stockCode, that.stockCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockCode);
    }

    /* ===== toString ===== */
    @Override
    public String toString() {
        String dt = (createDate == null) ? "N/A" : getCreateDateText();
        String pbrStr = (pbr == null) ? "-" : String.valueOf(pbr);
        String perStr = (per == null) ? "-" : String.valueOf(per);
        return String.format("[%s] %s (code=%s) PBR=%s, PER=%s, created=%s",
                id, stockName, stockCode, pbrStr, perStr, dt);
    }
}
