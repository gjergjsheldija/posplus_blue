/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.panels;

import java.util.*;
import com.openbravo.pos.util.StringUtils;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppView;
import java.text.SimpleDateFormat;

/**
 *
 * @author gjergj
 */
public class SupplyModel {

    private Integer m_iSupplies;
    private Double m_dSuppliesTotal;
    private java.util.List<SupplyLine> m_lsupply;
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd";
    public static final String DATE_FORMAT_PRINT = "dd-MM-yyyy";

    private SupplyModel() {
    }

    public static SupplyModel emptyInstance() {

        SupplyModel p = new SupplyModel();

        p.m_iSupplies = new Integer(0);
        p.m_dSuppliesTotal = new Double(0.0);
        p.m_lsupply = new ArrayList<SupplyLine>();
        
        return p;
    }

    public static SupplyModel loadInstance(AppView app) throws BasicException {

        SupplyModel p = new SupplyModel();

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        Date dNow = new Date();

        Object[] valtickets = (Object []) new StaticSentence(app.getSession()
            , "SELECT COUNT( * ) , SUM( STOCKDIARY.UNITS * PRODUCTS.PRICESELL ) " +
              "FROM STOCKDIARY INNER JOIN PRODUCTS ON STOCKDIARY.PRODUCT = PRODUCTS.ID " +
              "WHERE STOCKDIARY.REASON = '1' AND STOCKDIARY.DATENEW >= ? "
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
            .find(sdf.format(dNow));

        if (valtickets == null) {
            p.m_iSupplies = new Integer(0);
            p.m_dSuppliesTotal = new Double(0.0);
        } else {
            p.m_iSupplies = (Integer) valtickets[0];
            p.m_dSuppliesTotal = (Double) valtickets[1];
        }

        List<SupplyLine> l = new StaticSentence(app.getSession(),
                "SELECT PRODUCTS.NAME, STOCKDIARY.UNITS, PRODUCTS.PRICESELL " +
                "FROM STOCKDIARY INNER JOIN PRODUCTS ON STOCKDIARY.PRODUCT = PRODUCTS.ID " +
                "WHERE STOCKDIARY.REASON = '1' AND STOCKDIARY.DATENEW >= ? ",
                SerializerWriteString.INSTANCE,
                new SerializerReadClass(SupplyModel.SupplyLine.class))
                .list(sdf.format(dNow));

        if (l == null) {
            p.m_lsupply = new ArrayList<SupplyLine>();
        } else {
            p.m_lsupply = l;
        }

        return p;
    }
    public double getTotal() {
        return m_dSuppliesTotal.doubleValue();
    }
    public String printSuppliesTotal() {
        return Formats.CURRENCY.formatValue(m_dSuppliesTotal);
    }
    public List<SupplyLine> getSupplyLines() {
        return m_lsupply;
    }

    public Integer getTicketType() {
        return 0;
    }
    public String printDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PRINT);
        Date dNow = new Date();
        return sdf.format(dNow);
    }

    public static class SupplyLine implements SerializableRead {

        private String m_ArticleName;
        private Double m_ArticleUnits;
        private Double m_ArticlePrice;

        public void readValues(DataRead dr) throws BasicException {
            m_ArticleName = dr.getString(1);
            m_ArticleUnits = dr.getDouble(2);
            m_ArticlePrice = dr.getDouble(3);
        }

        public String printArticleName() {
            return StringUtils.encodeXML(m_ArticleName);
        }

        public String getArticleName() {
            return StringUtils.encodeXML(m_ArticleName);
        }

        public String printUnits() {
            return Formats.DOUBLE.formatValue(m_ArticleUnits);
        }

        public Double getUnits() {
            return m_ArticleUnits;
        }

        public String printPrice() {
            return Formats.CURRENCY.formatValue(m_ArticlePrice);
        }

        public Double getPrice() {
            return m_ArticlePrice;
        }

    }
}
